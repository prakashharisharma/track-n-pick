package com.example.storage.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.example.storage.model.DailyBhav;

@Repository
public class BhavTemplate {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	final String COLLECTION_BH = "bhav_history";
	
	public void create(DailyBhav dailyBhav) {
		mongoTemplate.insert(dailyBhav);
	}
}
