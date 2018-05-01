package com.kobe.dubbok.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.kobe.dubbok.contant.Constant;
import com.kobe.dubbok.registry.api.ServiceRegistry;
import com.kobe.dubbok.util.StringUtil;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class Server implements InitializingBean, ApplicationContextAware{
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    
    private String serviceAddress;
    private ServiceRegistry serviceRegistry;
    private Map<String, Object> classToObjectMap = new HashMap<>();
    private static ExecutorService threadPoolExecutor = Executors.newFixedThreadPool(Constant.Netty_Thread_Num);
    
    public Server(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public Server(String serviceAddress, ServiceRegistry serviceRegistry) {
        this.serviceAddress = serviceAddress;
        this.serviceRegistry = serviceRegistry;
    }
    
	@Override
	public void afterPropertiesSet() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try{
			ServerBootstrap sbt = new ServerBootstrap();
			sbt.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.option(ChannelOption.SO_BACKLOG, 1024)
			.childOption(ChannelOption.SO_KEEPALIVE, true)
			//set logger
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ServerChannelInitializer(classToObjectMap));
			
			String[] addressArray = StringUtil.split(serviceAddress, ":");
			int port = Integer.parseInt(addressArray[1]);
			ChannelFuture f = sbt.bind(addressArray[0], port).sync();
			
		    if (serviceRegistry != null) {
                for (String interfaceName : classToObjectMap.keySet()) {
                    serviceRegistry.register(interfaceName, serviceAddress);
                    LOGGER.debug("register service: {} => {}", interfaceName, serviceAddress);
                }
            }
			
			LOGGER.debug("dubbox server started on port {}", port);
			f.channel().closeFuture().sync();
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcServiceProvider.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
            	RpcServiceProvider rpcService = serviceBean.getClass().getAnnotation(RpcServiceProvider.class);
                String serviceName = rpcService.value().getName();
                String serviceVersion = rpcService.version();
                if (StringUtil.isNotEmpty(serviceVersion)) {
                    serviceName += "-" + serviceVersion;
                }
                classToObjectMap.put(serviceName, serviceBean);
            }
        }
	}
	
	 public static void submit(Runnable task){
		 threadPoolExecutor.submit(task);
	 }
	
}
