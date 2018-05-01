package com.kobe.dubbok.registry.api.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LoadBalanceAlgo {

	private static Map<String, Integer> serverWeightMap = new HashMap<String, Integer>();
	
	public static void add(String address, int weight){
		serverWeightMap.put(address, weight);
	}
	
	public static void delete(String address){
		serverWeightMap.remove(address);
	}
	
	public static String randomLoadBalance(List<String> serverList){
		int length = serverList.size();
		int totalWeight = 0;
		boolean sameWeight = true;
		Random random = new Random();
		for(int i=0; i<length; ++i){
			int weight = serverWeightMap.get(serverList.get(i));
			totalWeight += weight;
			if(sameWeight && i>0 && weight!=serverWeightMap.get(serverList.get(i-1))){
				sameWeight = false;
			}
		}
		
		if(totalWeight > 0 && sameWeight){
			int offset = random.nextInt(totalWeight);
			for(int i=0; i<length; i++){
				offset -= serverWeightMap.get(serverList.get(i));
				if(offset < 0){
					return serverList.get(i);
				}
			}
		}
		
	   return serverList.get(random.nextInt(length));
	}
	
}
