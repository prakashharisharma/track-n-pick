package com.example.storage.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.example.storage.model.StockResearch;

@Repository
public class ResearchTemplate {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	final String COLLECTION_PH = "research_history";
	
	public void create(StockResearch stockResearch) {
		mongoTemplate.insert(stockResearch);
	}
}
