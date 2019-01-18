package com.example.storage.repo;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.example.storage.model.Stock;
import com.example.storage.model.StockPrice;
import com.example.storage.model.TradingSession;

@Repository
public class StorageTemplate {

	@Autowired
	private MongoTemplate mongoTemplate;

	final String COLLECTION_STK = "stocks";
	
	final String COLLECTION_TS = "tading_sessions";

	public void create(Stock stock) {
		mongoTemplate.insert(stock);
	}

	public void createTS(TradingSession tadingSession) {
		mongoTemplate.insert(tadingSession);
	}
	
	public void update(Stock stock) {
		mongoTemplate.save(stock);
	}

	public Stock find(Stock stock) {
		Query query = new Query(Criteria.where("_id").is(stock.getId()));
		return mongoTemplate.findOne(query, Stock.class, COLLECTION_STK);
	}

	public List<TradingSession> getTradingSessions(int days){
		Query query = new Query();
		
		query.with(new Sort(Sort.Direction.DESC,"tradingDate"));
		query.limit(days);
		
		return (List<TradingSession>) mongoTemplate.find(query, TradingSession.class);
	}
	
	public TradingSession getTradingSession(int days){
		Query query = new Query();
		
		query.with(new Sort(Sort.Direction.DESC,"tradingDate"));
		query.limit(days);
		
		List<TradingSession> tsList =  mongoTemplate.find(query, TradingSession.class);
		
		return tsList.get(tsList.size() - 1);
	}
	
	public Stock findByNseSymbol(String nseSymbol) {
		Query query = new Query(Criteria.where("nseSymbol").is(nseSymbol));
		return mongoTemplate.findOne(query, Stock.class, COLLECTION_STK);
	}

	public List<StockPrice> findPriceByNseSymbol(String nseSymbol) {
		Stock stock = this.findByNseSymbol(nseSymbol);
		
		return stock.getStockPrices();
	}
	public Stock findBySMAPrices(String nseSymbol, Instant instant) {
		Query query = new Query(Criteria.where("nseSymbol").is(nseSymbol)
				.and("stockPrices.bhavDate").gte(instant));
		return mongoTemplate.findOne(query, Stock.class, COLLECTION_STK);
	}
	
	
	public double getSMA(String nseSymbol, int days) {
		
		TradingSession ts = this.getTradingSession(days);
		
		Stock stock = this.findBySMAPrices(nseSymbol, ts.getTradingDate());
		
		List<StockPrice> stkPriceList= stock.getStockPrices();
		
		double sum = stkPriceList.stream().mapToDouble( p -> p.getClose()).sum();
		
		return sum;
	}
	
	public double getRSI(String nseSymbol, int days) {
		TradingSession ts = this.getTradingSession(days);
		
		Stock stock = this.findBySMAPrices(nseSymbol, ts.getTradingDate());
		
		List<StockPrice> stkPriceList= stock.getStockPrices();
		
		double averageGain = stkPriceList.stream().filter(sp -> sp.getChange() >= 0.0).mapToDouble( p -> p.getClose()).average().orElse(0.0);
		
		double averageLoss = stkPriceList.stream().filter(sp -> sp.getChange() < 0.0).mapToDouble( p -> p.getClose()).average().orElse(0.0);
		
		double rs= averageGain / averageLoss;
		
		double rsi = (100 - (100 / (1+rs)));
		
		return rsi;
	}
	
	public List<Stock> findAll() {
		return (List<Stock>) mongoTemplate.findAll(Stock.class);
	}

	public void addPrice(String nseSymbol, StockPrice stockPrice) {

		Stock stock = this.findByNseSymbol(nseSymbol);

		if (stock != null) {

			mongoTemplate.updateFirst(Query.query(Criteria.where("nseSymbol").is(nseSymbol)),
					new Update().push("stockPrices", stockPrice), "stocks");
		}else {
			stock = new Stock(nseSymbol);
			this.create(stock);
			mongoTemplate.updateFirst(Query.query(Criteria.where("nseSymbol").is(nseSymbol)),
					new Update().push("stockPrices", stockPrice), "stocks");
			
		}
	}

}
