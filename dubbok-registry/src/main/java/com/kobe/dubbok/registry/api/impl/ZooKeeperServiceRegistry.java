package com.kobe.dubbok.registry.api.impl;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobe.dubbok.contant.Constant;
import com.kobe.dubbok.registry.api.ServiceRegistry;

public class ZooKeeperServiceRegistry implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);

    private final ZkClient zkClient;

    public ZooKeeperServiceRegistry(String zkServerAddress) {
        zkClient = new ZkClient(zkServerAddress, Constant.Zk_Session_Timeout, Constant.Zk_Connection_Timeout);
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        String registryPath = Constant.Zk_Registry_Path;
        if (!zkClient.exists(registryPath)) {
            zkClient.createPersistent(registryPath);
            LOGGER.debug("create registry node: {}", registryPath);
        }

        String servicePath = registryPath + "/" + serviceName;
        if (!zkClient.exists(servicePath)) {
            zkClient.createPersistent(servicePath);
            LOGGER.debug("create service node: {}", servicePath);
        }

        String addressPath = servicePath + "/provider@";
        String addressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
       
        LOGGER.debug("create provider address node: {}", addressNode);
    }
}