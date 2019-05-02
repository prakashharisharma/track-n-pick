
package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

@SpringBootApplication
@EnableJpaRepositories
@EnableWebSecurity
@EnableHazelcastHttpSession
@EnableCaching
@EnableJms
public class Application {

	public static void main(String[] args) throws Exception {
		
		SpringApplication.run(Application.class, args); 
		  
	}  
}
