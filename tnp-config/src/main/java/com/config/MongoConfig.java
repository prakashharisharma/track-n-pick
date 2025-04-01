package com.config;

import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories(basePackages = {"com.example.data.storage.repo"})
public class MongoConfig {
}
