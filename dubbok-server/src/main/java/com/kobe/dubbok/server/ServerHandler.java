package com.kobe.dubbok.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobe.dubbok.payload.Request;
import com.kobe.dubbok.payload.Response;
import com.kobe.dubbok.util.StringUtil;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/*
 * 1. the incoming requst is de-seriliazed and is ready to response
 * 2. 
 * 
 * */
public class ServerHandler extends SimpleChannelInboundHandler<Request>{
	private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
	    
	private Map<String, Object> classToObjectMap;

	public ServerHandler(Map<String, Object> classToObjectMap) {
		this.classToObjectMap = classToObjectMap;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Request request) throws Exception {
		Server.submit(new Runnable(){
			@Override
			public void run() {
		        Response response = new Response();
		        response.setRequestId(request.getRequestId());
		        try {
		            Object result = handle(request);
		            response.setResult(result);
		        } catch (Exception e) {
		            LOGGER.error("failed to handle request", e);
		            response.setException(e);
		        }
		        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);				
			}
        	
        });
	}

	private Object handle(Request request) throws Exception {
        String serviceName = request.getInterfaceName();
        String serviceVersion = request.getServiceVersion();
        if (StringUtil.isNotEmpty(serviceVersion)) {
            serviceName += "-" + serviceVersion;
        }
        Object serviceBean = classToObjectMap.get(serviceName);
        if (serviceBean == null) {
            throw new RuntimeException(String.format("service is not existing: %s", serviceName));
        }
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }
}
