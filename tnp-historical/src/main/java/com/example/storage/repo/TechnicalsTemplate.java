package com.example.storage.repo;

import java.util.Date;

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
import org.springframework.stereotype.Repository;

import com.example.storage.model.RsiCountResult;
import com.example.storage.model.StockPrice;
import com.example.storage.model.result.StockPriceResult;
import com.example.storage.model.StockTechnicals;
import com.example.storage.model.TradingSession;

@Repository
public class TechnicalsTemplate {
	@Autowired
	private MongoTemplate mongoTemplate;

	final String COLLECTION_PH = "price_history";

	final String COLLECTION_TH = "technicals_history";

	public void create(StockTechnicals stockTechnicals) {
		mongoTemplate.insert(stockTechnicals);
	}

	public double getPrevTotalGain(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("avgGain").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double prevTotalGain = 0.00;

		if (stockPriceResult != null) {
			prevTotalGain = stockPriceResult.getResultPrice();
		}

		return prevTotalGain;
	}

	public double getPrevTotalLoss(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("avgLoss").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double prevTotalLoss = 0.00;

		if (stockPriceResult != null) {
			prevTotalLoss = stockPriceResult.getResultPrice();
		}

		return prevTotalLoss;
	}

	public double getCurrentRSI(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("indicator.rsi.rsi").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double currentRSI = 0.00;

		if (stockPriceResult != null) {
			currentRSI = stockPriceResult.getResultPrice();
		}

		return currentRSI;
	}

	public double getCurrentSmoothedRSI(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("indicator.rsi.smoothedRsi").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double currentRSI = 0.00;

		if (stockPriceResult != null) {
			currentRSI = stockPriceResult.getResultPrice();
		}

		return currentRSI;
	}

	public double getPriorDaysSma50Average(String nseSymbol, int days) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation avgSma50Group = Aggregation.group("nseSymbol").avg("movingAverage.sma50").as("avgsma50");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("avgsma50").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				avgSma50Group, projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double currentRSI = 0.00;

		if (stockPriceResult != null) {
			currentRSI = stockPriceResult.getResultPrice();
		}

		return currentRSI;
	}

	public int getrsiCountAbove(double rsi, String nseSymbol, int days) {

		Criteria criteriaAbove = new Criteria().andOperator(new Criteria("nseSymbol").is(nseSymbol),
				new Criteria("indicator.rsi.smoothedRsi").gt(rsi));

		MatchOperation matchSymbol = Aggregation.match(criteriaAbove);

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation avgSma50Group = Aggregation.group("nseSymbol").count().as("aboveCount");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("aboveCount").as("count");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				avgSma50Group, projectToMatchModel);

		AggregationResults<RsiCountResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				RsiCountResult.class);

		RsiCountResult rsiCountResult = result.getUniqueMappedResult();

		int count = 0;

		if (rsiCountResult != null) {
			count = rsiCountResult.getCount();
		}

		return count;
	}

	public int getrsiCountBelow(double rsi, String nseSymbol, int days) {

		Criteria criteriaBelow = new Criteria().andOperator(new Criteria("nseSymbol").is(nseSymbol),
				new Criteria("indicator.rsi.smoothedRsi").lte(rsi));

		MatchOperation matchSymbol = Aggregation.match(criteriaBelow);

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation avgSma50Group = Aggregation.group("nseSymbol").count().as("belowCount");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("belowCount").as("count");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				avgSma50Group, projectToMatchModel);

		AggregationResults<RsiCountResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				RsiCountResult.class);

		RsiCountResult rsiCountResult = result.getUniqueMappedResult();

		int count = 0;

		if (rsiCountResult != null) {
			count = rsiCountResult.getCount();
		}

		return count;
	}

	public double getPrevSessionSma50(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToTwoDoc = Aggregation.limit(2);

		GroupOperation prevSma50Group = Aggregation.group("nseSymbol").last("movingAverage.sma50").as("prevSma50");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("prevSma50").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToTwoDoc,
				prevSma50Group, projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double prevSessionSma50 = 0.00;

		if (stockPriceResult != null) {
			prevSessionSma50 = stockPriceResult.getResultPrice();
		}

		return prevSessionSma50;
	}

	public double getPrevSessionSma200(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(new Sort(Direction.DESC, "bhavDate"));

		LimitOperation limitToTwoDoc = Aggregation.limit(2);

		GroupOperation prevSma200Group = Aggregation.group("nseSymbol").last("movingAverage.sma200").as("prevSma200");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("prevSma200").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToTwoDoc,
				prevSma200Group, projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double prevSessionSma50 = 0.00;

		if (stockPriceResult != null) {
			prevSessionSma50 = stockPriceResult.getResultPrice();
		}

		return prevSessionSma50;
	}
}
