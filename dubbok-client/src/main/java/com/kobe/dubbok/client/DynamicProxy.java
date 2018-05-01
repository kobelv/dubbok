package com.kobe.dubbok.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobe.dubbok.payload.Request;
import com.kobe.dubbok.payload.Response;
import com.kobe.dubbok.registry.api.ServiceDiscovery;
import com.kobe.dubbok.util.StringUtil;

public class DynamicProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicProxy.class);
    private String serviceAddress;
    private ServiceDiscovery serviceDiscovery;

    public DynamicProxy(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public DynamicProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    public <T> T create(final Class<?> interfaceClass) {
        return create(interfaceClass, "");
    }

    @SuppressWarnings("unchecked")
    public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Request request = new Request();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setInterfaceName(method.getDeclaringClass().getName());
                        request.setServiceVersion(serviceVersion);
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);
                        
                        if (serviceDiscovery != null) {
                            String serviceName = interfaceClass.getName();
                            if (StringUtil.isNotEmpty(serviceVersion)) {
                                serviceName += "-" + serviceVersion;
                            }
                            serviceAddress = serviceDiscovery.discover(serviceName);
                            LOGGER.debug("discovered service: {} => {}", serviceName, serviceAddress);
                        }
                        if (StringUtil.isEmpty(serviceAddress)) {
                            throw new RuntimeException("server address is empty");
                        }
                        String[] array = StringUtil.split(serviceAddress, ":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);
                        Client client = new Client(host, port);
                        long time = System.currentTimeMillis();
                        Response response = client.send(request);
                        LOGGER.debug("rpc call response time: {}ms", System.currentTimeMillis() - time);
                        if (response == null) {
                            throw new RuntimeException("response is null");
                        }
                        if (response.hasException()) {
                            throw response.getException();
                        } else {
                            return response.getResult();
                        }
                    }
                }
        );
    }
}
