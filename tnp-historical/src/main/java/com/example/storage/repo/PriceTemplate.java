package com.example.storage.repo;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.example.storage.model.StockPrice;
import com.example.storage.model.TradingSession;
import com.example.storage.model.result.StockPriceResult;
import com.example.storage.model.result.YearHighLowResult;

@Repository
public class PriceTemplate {

	@Autowired
	private MongoTemplate mongoTemplate;
 
	@Autowired
	private TradingSessionTemplate storageTemplate;
	
	final String COLLECTION_PH = "price_history";

	final String COLLECTION_TH = "technicals_history";

	public void create(StockPrice stockPrice) {
		mongoTemplate.insert(stockPrice);
	}

	public List<YearHighLowResult> getYearLowStocks() {

		System.out.println("IN1");
		
		TradingSession ts = storageTemplate.currentTradingSession();
		
		Criteria criteriaYearLow = new Criteria("bhavDate").is(Date.from(ts.getTradingDate()));

		MatchOperation matchSymbol = Aggregation.match(criteriaYearLow);

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.ASC, "nseSymbol"));


		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("_id").as("_id").andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearLow == low").as("match").andExpression("yearLow").as("yearLow");;

		Criteria criteriaMatch = new Criteria("match").is(true);
		
		MatchOperation matchCri = Aggregation.match(criteriaMatch);
		
		ProjectionOperation projectToMatchModel1 = Aggregation.project().andExpression("_id").as("_id").andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearLow").as("resultPrice");
		
		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc,
				 projectToMatchModel,matchCri,projectToMatchModel1);

		AggregationResults<YearHighLowResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				YearHighLowResult.class);

		System.out.println(result.getRawResults());
		
		List<YearHighLowResult> yearLowResultList = result.getMappedResults();

		yearLowResultList.forEach(System.out::println);
		

		return yearLowResultList;
	}
	
	public List<YearHighLowResult> getYearHighStocks() {

		System.out.println("IN1");
		
		TradingSession ts = storageTemplate.currentTradingSession();
		
		Criteria criteriaYearLow = new Criteria("bhavDate").is(Date.from(ts.getTradingDate()));

		MatchOperation matchSymbol = Aggregation.match(criteriaYearLow);

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.ASC, "nseSymbol"));


		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("_id").as("_id").andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearHigh == high").as("match").andExpression("yearHigh").as("yearHigh");;

		Criteria criteriaMatch = new Criteria("match").is(true);
		
		MatchOperation matchCri = Aggregation.match(criteriaMatch);
		
		ProjectionOperation projectToMatchModel1 = Aggregation.project().andExpression("_id").as("_id").andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearHigh").as("resultPrice");
		
		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc,
				 projectToMatchModel,matchCri,projectToMatchModel1);

		AggregationResults<YearHighLowResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				YearHighLowResult.class);

		System.out.println(result.getRawResults());
		
		List<YearHighLowResult> yearHighResultList = result.getMappedResults();

		yearHighResultList.forEach(System.out::println);
		

		return yearHighResultList;
	}
	
	public double getyearLow(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearLow").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double yearLow = 0.00;

		if (stockPriceResult != null) {
			yearLow = stockPriceResult.getResultPrice();
		}

		return yearLow;
	}
	
	public double getyearHigh(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearHigh").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double yearHigh = 0.00;

		if (stockPriceResult != null) {
			yearHigh = stockPriceResult.getResultPrice();
		}

		return yearHigh;
	}
}
