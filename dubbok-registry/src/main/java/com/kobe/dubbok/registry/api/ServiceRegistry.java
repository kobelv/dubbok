package com.kobe.dubbok.registry.api;

public interface ServiceRegistry {
    /**
     * key: service name (function name + version)
     * value: server address
     */
    void register(String serviceName, String serviceAddress);
}