package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@Configuration
public class HazelcastConfig {

	@Bean
	public HazelcastInstance configProd() {

		Config config = new Config();

		HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
		
		return hazelcastInstance;
	}

}
