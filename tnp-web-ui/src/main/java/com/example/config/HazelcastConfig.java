package com.example.config;

import java.util.ArrayList;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.serialization.impl.ArrayListStreamSerializer;

import info.jerrinot.subzero.SubZero;

@Configuration
public class HazelcastConfig {

	@Bean
	public HazelcastInstance configProd() {
		/*
		 * ClientConfig clientConfig = new ClientConfig();
		 * clientConfig.getGroupConfig().setName("jet").setPassword("jet-pass");
		 * clientConfig.getNetworkConfig().addAddress("127.0.0.1:5701");
		 * 
		 * 
		 * return HazelcastClient.newHazelcastClient(clientConfig);
		 */
		Config config = new Config();
	    //SubZero.useAsGlobalSerializer(config);
		// SubZero.useForClasses(config, ArrayListSerializer.class);

	    
		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
		
		return hazelcastInstance;
	}

}
