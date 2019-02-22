package com.example.storage.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.example.storage.model.StockFactor;

@Repository
public class FactorTemplate {


	@Autowired
	private MongoTemplate mongoTemplate;
	
	final String COLLECTION_PH = "factor_history";
	
	public void create(StockFactor stockFactor) {
		mongoTemplate.insert(stockFactor);
	}
}
