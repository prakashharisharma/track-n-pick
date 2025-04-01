package com.config;


import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Component;

@Component
@EnableJpaRepositories(basePackages = {"com.example.data.transactional.repo"})
@EntityScan(basePackages = {"com.example.data.transactional.entities"})
public class JpaConfig {
}
