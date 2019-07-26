package com.example.storage.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.example.storage.model.CandleStick;

@Repository
public class CandleStickTemplate {
	@Autowired
	private MongoTemplate mongoTemplate;
	
	final String COLLECTION_CH = "candlesticks_history";
	
	public void create(CandleStick candleStick) {
		mongoTemplate.insert(candleStick);
	}
}
