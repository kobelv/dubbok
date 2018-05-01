package com.kobe.dubbok.registry.api;

public interface ServiceDiscovery {
    /**
     * input: service name (function name + version)
     * output: server address
     */
    String discover(String serviceName);
}