package com.example.storage.repo;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import com.example.storage.model.StockPrice;
import com.example.storage.model.result.*;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.example.storage.model.StockTechnicals;

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

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("momentum.rsi.avgGain").as("resultPrice");

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

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("momentum.rsi.avgLoss").as("resultPrice");

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

	
	public long getOBV(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("volume.obv").as("resultOBV");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				projectToMatchModel);

		AggregationResults<StockOBVResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				StockOBVResult.class);

		StockOBVResult stockOBVResult = result.getUniqueMappedResult();

		long prevOBV = 0;

		if (stockOBVResult != null) {
			prevOBV = stockOBVResult.getResultOBV();
		}

		return prevOBV;
	}
	
	
	public double getCurrentRSI(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("momentum.rsi.rsi").as("resultPrice");

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

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("momentum.rsi.smoothedRsi").as("resultPrice");

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

	public Long getAverageVolume(String nseSymbol, int days) {
		
		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));
		
		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").avg("volume.volume").as("avgVolume");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("avgVolume").as("volume");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc, yearHighGroup,
				projectToMatchModel);

		AggregationResults<VolumeResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				VolumeResult.class);

		VolumeResult volumeResult = result.getUniqueMappedResult();

		Long averageVolume = 0l;
		if(volumeResult != null) {
			averageVolume = volumeResult.getVolume();
		}
		
		return averageVolume;
	}

	public Double getAverageMacd(String nseSymbol, int days) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").avg("momentum.macd.macd").as("avgMacd");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("avgMacd").as("value");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc, yearHighGroup,
				projectToMatchModel);

		AggregationResults<DoubleResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				DoubleResult.class);

		DoubleResult macdResult = result.getUniqueMappedResult();

		Double averageMacd = 0.00;

		if(macdResult != null) {
			averageMacd = macdResult.getValue();
		}

		return averageMacd;
	}

	public Double getAverageRsi(String nseSymbol, int days) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").avg("momentum.rsi.smoothedRsi").as("avgRsi");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("avgRsi").as("value");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc, yearHighGroup,
				projectToMatchModel);

		AggregationResults<DoubleResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				DoubleResult.class);

		DoubleResult rsiResult = result.getUniqueMappedResult();

		Double averageRsi = 0.00;

		if(rsiResult != null) {
			averageRsi = rsiResult.getValue();
		}

		return averageRsi;
	}
	
	public double getPriorDaysSma50Average(String nseSymbol, int days) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation avgSma50Group = Aggregation.group("nseSymbol").avg("trend.movingAverage.simple.avg50").as("avgsma50");

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

	@Deprecated
	public int getrsiCountAbove(double rsi, String nseSymbol, int days) {

		Criteria criteriaAbove = new Criteria().andOperator(new Criteria("nseSymbol").is(nseSymbol),
				new Criteria("indicator.rsi.smoothedRsi").gt(rsi));

		MatchOperation matchSymbol = Aggregation.match(criteriaAbove);

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

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

	@Deprecated
	public int getrsiCountBelow(double rsi, String nseSymbol, int days) {

		Criteria criteriaBelow = new Criteria().andOperator(new Criteria("nseSymbol").is(nseSymbol),
				new Criteria("indicator.rsi.smoothedRsi").lte(rsi));

		MatchOperation matchSymbol = Aggregation.match(criteriaBelow);

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

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

	@Deprecated
	public double getPrevSessionSma21(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToTwoDoc = Aggregation.limit(2);

		GroupOperation prevSma50Group = Aggregation.group("nseSymbol").last("movingAverage.sma21").as("prevSma21");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("prevSma21").as("resultPrice");

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
	
	
	public double getPrevSessionSma50(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToTwoDoc = Aggregation.limit(2);

		GroupOperation prevSma50Group = Aggregation.group("nseSymbol").last("trend.movingAverage.simple.avg50").as("prevSma50");

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

	public double getPrevSessionSma100(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToTwoDoc = Aggregation.limit(2);

		GroupOperation prevSma50Group = Aggregation.group("nseSymbol").last("trend.movingAverage.simple.avg100").as("prevSma100");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("prevSma100").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToTwoDoc,
				prevSma50Group, projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double prevSessionSma100 = 0.00;

		if (stockPriceResult != null) {
			prevSessionSma100 = stockPriceResult.getResultPrice();
		}

		return prevSessionSma100;
	}
	
	
	public double getPrevSessionSma200(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToTwoDoc = Aggregation.limit(2);

		GroupOperation prevSma200Group = Aggregation.group("nseSymbol").last("trend.movingAverage.simple.avg200").as("prevSma200");

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
	
	public double getAverageStochasticOscillatorK(String nseSymbol, int days) {
		//TradingSession ts = this.getTradingSessionBeforeDays(days);

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));
		
		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").avg("momentum.stochasticOscillator.k").as("avgD");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("avgD").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc, yearHighGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				StockPriceResult.class);

		
		
		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double averagePrice= 0.00;
		if(stockPriceResult != null) {
			averagePrice = stockPriceResult.getResultPrice();
		}
		

		return averagePrice;
	}
	
	
	public StockTechnicals getPrevTechnicals(String nseSymbol, int days) {

		Query query = new Query(new Criteria("nseSymbol").is(nseSymbol));
		
		query.with(Sort.by(Sort.Direction.DESC,"bhavDate")).limit(days);
		
		List<StockTechnicals> stockTechnicalsList = mongoTemplate.find(query, StockTechnicals.class);
		
		StockTechnicals stockTechnicals = null;
		
		if(!stockTechnicalsList.isEmpty() && stockTechnicalsList.size() == days) {
			stockTechnicals = stockTechnicalsList.get(days-1);
		}
		
		return stockTechnicals;
		
	}

	public StockTechnicals getPreviousSessionTechnicals(String nseSymbol, LocalDate from, LocalDate to){
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("bhavDate").gte(from.atStartOfDay().toInstant(ZoneOffset.UTC)).lte(to.atStartOfDay().toInstant(ZoneOffset.UTC)),
						Criteria.where("nseSymbol").is(nseSymbol)
				)
		);

		query.with(Sort.by(Direction.ASC, "bhavDate"));

		List<StockTechnicals> stockTechnicalsList = mongoTemplate.find(query, StockTechnicals.class);

		StockTechnicals stockTechnicals = new StockTechnicals();

		if(stockTechnicals !=null && !stockTechnicalsList.isEmpty()  ) {
			stockTechnicals = stockTechnicalsList.get(stockTechnicalsList.size()-1);
		}

		return stockTechnicals;

	}

	public StockTechnicals getForDate(String nseSymbol, LocalDate date){
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("bhavDate").is(date.atStartOfDay().toInstant(ZoneOffset.UTC)),
						Criteria.where("nseSymbol").is(nseSymbol)
				)
		);

		List<StockTechnicals> stockTechnicalsList = mongoTemplate.find(query, StockTechnicals.class);

		StockTechnicals stockTechnicals = null;

		if(!stockTechnicalsList.isEmpty()) {
			stockTechnicals = stockTechnicalsList.get(0);
		}

		return stockTechnicals;

	}



	public List<StockTechnicals> get(String nseSymbol, LocalDate from, LocalDate to){
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("bhavDate").gte(from.atStartOfDay().toInstant(ZoneOffset.UTC)).lte(to.atStartOfDay().toInstant(ZoneOffset.UTC)),
						Criteria.where("nseSymbol").is(nseSymbol)
				)
		);

		query.with(Sort.by(Direction.ASC, "bhavDate"));

		return mongoTemplate.find(query, StockTechnicals.class);

	}

	public long getTrCount(String nseSymbol){

		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(

						Criteria.where("nseSymbol").is(nseSymbol),
						Criteria.where("trend.adx.tr").exists(Boolean.TRUE)
				)
		);

		return mongoTemplate.count(query, StockTechnicals.class);
	}

	public double getAdxAverage(String nseSymbol, String type, int days) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").avg("trend.adx."+type).as("avg");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("avg").as("avg");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc, yearHighGroup,
				projectToMatchModel);

		AggregationResults<AverageResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_TH,
				AverageResult.class);

		AverageResult averageResult = result.getUniqueMappedResult();

		double average = 0;

		if(averageResult != null) {
			average = averageResult.getAvg();
		}

		return average;
	}

	public void upsert(StockTechnicals stockTechnicals){

		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("bhavDate").is(stockTechnicals.getBhavDate()),
						Criteria.where("nseSymbol").is(stockTechnicals.getNseSymbol())
				)
		);

		Document doc = new Document(); // org.bson.Document
		mongoTemplate.getConverter().write(stockTechnicals, doc);
		Update update = Update.fromDocument(doc);

		UpdateResult updateResult = mongoTemplate.upsert(query, update, COLLECTION_TH);
	}

}
