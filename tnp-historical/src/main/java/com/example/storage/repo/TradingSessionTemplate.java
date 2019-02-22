package com.example.storage.repo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.example.storage.model.StockTechnicals;
import com.example.storage.model.TradingSession;
import com.example.storage.model.deprecated.Stock;
import com.example.storage.model.deprecated.StockPriceD;
import com.example.storage.model.deprecated.StockPriceUnwind;
import com.example.storage.model.result.StockPriceResult;

@Repository
public class TradingSessionTemplate {

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

	public List<TradingSession> getTradingSessions(int days) {
		Query query = new Query();

		query.with(new Sort(Sort.Direction.DESC, "tradingDate"));
		query.limit(days);

		return (List<TradingSession>) mongoTemplate.find(query, TradingSession.class);
	}

	public TradingSession currentTradingSession() {
		Query query = new Query();

		query.with(new Sort(Sort.Direction.DESC, "tradingDate"));

		query.limit(1);

		List<TradingSession> tsList = mongoTemplate.find(query, TradingSession.class);

		return tsList.get(tsList.size() - 1);
	}

	public TradingSession getTradingSessionBeforeDays(int days) {
		Query query = new Query();

		query.with(new Sort(Sort.Direction.DESC, "tradingDate"));
		query.limit(days);

		List<TradingSession> tsList = mongoTemplate.find(query, TradingSession.class);

		return tsList.get(tsList.size() - 1);
	}

	public Stock findByNseSymbol(String nseSymbol) {
		Query query = new Query(Criteria.where("nseSymbol").is(nseSymbol));
		return mongoTemplate.findOne(query, Stock.class, COLLECTION_STK);
	}

	public double getAveragePrice(String nseSymbol, int days) {
		TradingSession ts = this.getTradingSessionBeforeDays(days);

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		UnwindOperation unwindPrices = Aggregation.unwind("stockPrices");

		MatchOperation matchPrices = Aggregation
				.match(new Criteria("stockPrices.bhavDate").gte(Date.from(ts.getTradingDate())));

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").avg("stockPrices.close").as("avgPrice");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("avgPrice").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, unwindPrices, matchPrices, yearHighGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, "stocks",
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double averagePrice= 0.00;
		if(stockPriceResult != null) {
			averagePrice = stockPriceResult.getResultPrice();
		}
		
		//System.out.println(result.getRawResults());

		return averagePrice;
	}
	
	public void test(String nseSymbol, int days) {
		TradingSession ts = this.getTradingSessionBeforeDays(days);

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		UnwindOperation unwindPrices = Aggregation.unwind("stockPrices");

		
		Criteria criteriaTotalGain = new Criteria().andOperator(
				new Criteria("stockPrices.bhavDate").gte(Date.from(ts.getTradingDate())),
				new Criteria("stockPrices.change").lt(0.00)
		    );
		
		MatchOperation matchPrices = Aggregation
				.match(criteriaTotalGain);

	//	GroupOperation yearHighGroup = Aggregation.group("nseSymbol").sum("stockPrices.change").as("totalGain");

	/*	ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("totalGain").as("resultPrice");*/

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, unwindPrices, matchPrices);

		AggregationResults<String> result = mongoTemplate.aggregate(aggregation, "stocks",
				String.class);

		//StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		//System.out.println(result.getRawResults());

		//return stockPriceResult.getResultPrice();
	}
	
	@Deprecated
	public double getPreviousGain(String nseSymbol) {


		TradingSession ts = this.currentTradingSession();

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		UnwindOperation unwindPrices = Aggregation.unwind("stockPrices");

/*		MatchOperation matchPrices = Aggregation
				.match(new Criteria("stockPrices.bhavDate").gte(Date.from(ts.getTradingDate())));*/

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "stockPrices.bhavDate"));
		
		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(2);
		
		GroupOperation previosDayPrice = Aggregation.group("nseSymbol").sum("stockPrices.change").as("totalGain");

		
		//Aggregation aggregation = Aggregation.newAggregation(matchSymbol, unwindPrices, matchPrices, sortByAvgPopAsc, limitToOnlyFirstDoc);
		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, unwindPrices,  sortByAvgPopAsc, limitToOnlyFirstDoc);

		AggregationResults<StockPriceUnwind> result = mongoTemplate.aggregate(aggregation, "stocks",
				StockPriceUnwind.class);

		StockPriceUnwind stkU = result.getUniqueMappedResult();

		//return stkU.getStockPrices().getClose();
		
		return 0.0;
	}
	
	@Deprecated
	public double getPreviousLoss(String nseSymbol) {
		
		
		return 0.0;
	}
	
	public double getTotalGain(String nseSymbol, int days) {
		TradingSession ts = this.getTradingSessionBeforeDays(days);

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		UnwindOperation unwindPrices = Aggregation.unwind("stockPrices");

		
		Criteria criteriaTotalGain = new Criteria().andOperator(
				new Criteria("stockPrices.bhavDate").gte(Date.from(ts.getTradingDate())),
				new Criteria("stockPrices.change").gt(0.00)
		    );
		
		MatchOperation matchPrices = Aggregation
				.match(criteriaTotalGain);

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").sum("stockPrices.change").as("totalGain");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("totalGain").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, unwindPrices, matchPrices, yearHighGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, "stocks",
				StockPriceResult.class);

		double totalGain = 0.00;
		
		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		if(stockPriceResult != null) {
			totalGain = stockPriceResult.getResultPrice();
		}
		
		//System.out.println("TOTAL GAIN " + result.getRawResults());

		return totalGain;
	}
	
	public double getTotalLoss(String nseSymbol, int days) {
		TradingSession ts = this.getTradingSessionBeforeDays(days);

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		UnwindOperation unwindPrices = Aggregation.unwind("stockPrices");

		Criteria criteriaTotalLoss = new Criteria().andOperator(
				new Criteria("stockPrices.bhavDate").gte(Date.from(ts.getTradingDate())),
				new Criteria("stockPrices.change").lt(0.00)
		    );
		
		
		MatchOperation matchPrices = Aggregation
				.match(criteriaTotalLoss);

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").sum("stockPrices.change").as("totalLoss");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("totalLoss").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, unwindPrices, matchPrices, yearHighGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, "stocks",
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double totalLoss = 0.00;
		
		if(stockPriceResult != null) {
			totalLoss = stockPriceResult.getResultPrice();
		}
		
		//System.out.println("TOTAL LOSS " + result.getRawResults());

		return totalLoss;
	}
	
	public double getRSI(String nseSymbol, int days) {
		
		double averageGain = Math.abs((this.getTotalGain(nseSymbol, days) / days));

		double averageLoss = Math.abs((this.getTotalLoss(nseSymbol, days) / days));

		double rs = averageGain / averageLoss;

		double rsi = (100 - (100 / (1 + rs)));

		return rsi;
	}

	@Deprecated
	public double getyearLow(String nseSymbol) {

		// TradingSession ts = this.getTradingSessionBeforeDays(365);

		LocalDate localdate = LocalDate.now().minusYears(1);

		Instant yearBackInstant = localdate.atStartOfDay().toInstant(ZoneOffset.UTC);

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		UnwindOperation unwindPrices = Aggregation.unwind("stockPrices");

		MatchOperation matchPrices = Aggregation
				.match(new Criteria("stockPrices.bhavDate").gte(Date.from(yearBackInstant)));

		GroupOperation yearLowGroup = Aggregation.group("nseSymbol").min("stockPrices.low").as("yearLow");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearLow").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, unwindPrices, matchPrices, yearLowGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, "stocks",
				StockPriceResult.class);

		double yearLow= 0.00;
		
		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		if(stockPriceResult != null) {
			yearLow = stockPriceResult.getResultPrice();
		}
		
		return yearLow;
	}

	@Deprecated
	public double getyearHigh(String nseSymbol) {

		// TradingSession ts = this.getTradingSessionBeforeDays(365);

		LocalDate localdate = LocalDate.now().minusYears(1);

		Instant yearBackInstant = localdate.atStartOfDay().toInstant(ZoneOffset.UTC);

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		UnwindOperation unwindPrices = Aggregation.unwind("stockPrices");

		MatchOperation matchPrices = Aggregation
				.match(new Criteria("stockPrices.bhavDate").gte(Date.from(yearBackInstant)));

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").max("stockPrices.high").as("yearHigh");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearHigh").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, unwindPrices, matchPrices, yearHighGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, "stocks",
				StockPriceResult.class);

		double yearHigh = 0.00;
		
		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		if(stockPriceResult != null) {
			yearHigh = stockPriceResult.getResultPrice();;
		}
		
		//System.out.println(result.getRawResults());

		return yearHigh;
	}

	public double getCurrentPrice(String nseSymbol) {

		TradingSession ts = this.currentTradingSession();

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		UnwindOperation unwindPrices = Aggregation.unwind("stockPrices");

/*		MatchOperation matchPrices = Aggregation
				.match(new Criteria("stockPrices.bhavDate").gte(Date.from(ts.getTradingDate())));*/

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "stockPrices.bhavDate"));
		
		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);
		
		//Aggregation aggregation = Aggregation.newAggregation(matchSymbol, unwindPrices, matchPrices, sortByAvgPopAsc, limitToOnlyFirstDoc);
		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, unwindPrices,  sortByAvgPopAsc, limitToOnlyFirstDoc);

		AggregationResults<StockPriceUnwind> result = mongoTemplate.aggregate(aggregation, "stocks",
				StockPriceUnwind.class);

		double currentPrice = 0.00;
		
		StockPriceUnwind stkU = result.getUniqueMappedResult();
		
		if(stkU!=null) {
			currentPrice = stkU.getStockPrices().getClose();
		}
		

		return currentPrice;
	}

	public List<Stock> findAll() {
		return (List<Stock>) mongoTemplate.findAll(Stock.class);
	}

	public void addPrice(String nseSymbol, StockPriceD stockPrice) {

		Stock stock = this.findByNseSymbol(nseSymbol);

		if (stock != null) {

			mongoTemplate.updateFirst(Query.query(Criteria.where("nseSymbol").is(nseSymbol)),
					new Update().push("stockPrices", stockPrice), "stocks");
		} else {
			stock = new Stock(nseSymbol);
			this.create(stock);
			mongoTemplate.updateFirst(Query.query(Criteria.where("nseSymbol").is(nseSymbol)),
					new Update().push("stockPrices", stockPrice), "stocks");

		}
	}

	public void addTechnicals(String nseSymbol, StockTechnicals stockTechnicals) {

		Stock stock = this.findByNseSymbol(nseSymbol);

		if (stock != null) {

			mongoTemplate.updateFirst(Query.query(Criteria.where("nseSymbol").is(nseSymbol)),
					new Update().push("stockTechnicals", stockTechnicals), "stocks");
		} else {
			stock = new Stock(nseSymbol);
			this.create(stock);
			mongoTemplate.updateFirst(Query.query(Criteria.where("nseSymbol").is(nseSymbol)),
					new Update().push("stockTechnicals", stockTechnicals), "stocks");

		}
	}
	
}
