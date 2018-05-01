package com.kobe.dubbok.registry.api.impl;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobe.dubbok.contant.Constant;
import com.kobe.dubbok.registry.api.ServiceDiscovery;
import com.kobe.dubbok.util.CollectionUtil;

public class ZooKeeperServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);

    private String zkServerAddress;

    public ZooKeeperServiceDiscovery(String zkServerAddress) {
        this.zkServerAddress = zkServerAddress;
    }

    @Override
    public String discover(String name) {
        ZkClient zkClient = new ZkClient(zkServerAddress, Constant.Zk_Session_Timeout, 
        		Constant.Zk_Connection_Timeout);
        LOGGER.debug("zookeeper server connected");
        try {
            String servicePath = Constant.Zk_Registry_Path + "/" + name;
            if (!zkClient.exists(servicePath)) {
            	LOGGER.debug(String.format("can not find any service node on path: %s", servicePath));
                throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));
            }
            List<String> addressList = zkClient.getChildren(servicePath);
            if (CollectionUtil.isEmpty(addressList)) {
            	LOGGER.debug(String.format("can not find any address node on path: %s", servicePath));
                throw new RuntimeException(String.format("can not find any address node on path: %s", servicePath));
            }

           /* zkClient.subscribeChildChanges(servicePath, new IZkChildListener() {
				@Override
				public void handleChildChange(String arg0, List<String> arg1) throws Exception {
					addressList = arg1;
				}
			});*/
            
            String address;
            int size = addressList.size();
            if (size == 1) {
                address = addressList.get(0);
                LOGGER.debug("only discovered one address node: {}", address);
            } else {
                address = LoadBalanceAlgo.randomLoadBalance(addressList);
                LOGGER.debug("randomly discovered one address node: {}", address);
            }
            String addressPath = servicePath + "/" + address;
            return zkClient.readData(addressPath);
            
        } finally {
            zkClient.close();
        }
    }
}