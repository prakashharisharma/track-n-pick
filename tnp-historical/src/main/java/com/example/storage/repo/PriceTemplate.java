package com.example.storage.repo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;

import com.example.storage.model.StockTechnicals;
import com.example.storage.model.result.*;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.internal.bulk.UpdateRequest;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.BulkOperationException;
import org.springframework.data.mongodb.core.BulkOperations;
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

import com.example.storage.model.StockPrice;

@Repository
public class PriceTemplate {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	final String COLLECTION_PH = "price_history";

	final String COLLECTION_TH = "technicals_history";

	public void create(StockPrice stockPrice) {
		mongoTemplate.insert(stockPrice);
	}

	public void create(List<StockPrice>  stockPriceList) {

		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, StockPrice.class);
		bulkOps.insert(stockPriceList);
		BulkWriteResult result;
		try {
			result = bulkOps.execute();
			System.out.println("Inserted " + result.getInsertedCount());
		} catch(BulkOperationException e) {
			result = e.getResult();
		}
	}

	public List<YearHighLowResult> getYearLowStocks(LocalDate localdate) {

		System.out.println("IN1");
		
		Instant bhavInstant = localdate.atStartOfDay().toInstant(ZoneOffset.UTC);
		
		
		Criteria criteriaYearLow = new Criteria("bhavDate").is(Date.from(bhavInstant));

		MatchOperation matchSymbol = Aggregation.match(criteriaYearLow);

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.ASC, "nseSymbol"));


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
	
	public List<YearHighLowResult> getYearHighStocks(LocalDate localdate) {

		System.out.println("IN1");
		
		Instant bhavInstant = localdate.atStartOfDay().toInstant(ZoneOffset.UTC);
		
		Criteria criteriaYearLow = new Criteria("bhavDate").is(Date.from(bhavInstant));

		MatchOperation matchSymbol = Aggregation.match(criteriaYearLow);

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.ASC, "nseSymbol"));


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

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

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
	
	public HighLowResult getHighLowByDays(String nseSymbol, int days) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation yearLowGroup = Aggregation.group("nseSymbol").min("low").as("daysLow").max("high").as("daysHigh");
		
		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("daysLow").as("low").andExpression("daysHigh").as("high");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,yearLowGroup,
				projectToMatchModel);

		AggregationResults<HighLowResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				HighLowResult.class);

		HighLowResult highLowResult = result.getUniqueMappedResult();

		

		if (highLowResult == null) {
			highLowResult = new HighLowResult("NO_DATA_FOUND",0.00,0.00);
		}

		return highLowResult;
	
	}
	@Deprecated
	public double getDaysLow(String nseSymbol, int days) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation yearLowGroup = Aggregation.group("nseSymbol").min("low").as("daysLow");
		
		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("daysLow").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,yearLowGroup,
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
	@Deprecated
	public double getDaysHigh(String nseSymbol, int days) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation yearLowGroup = Aggregation.group("nseSymbol").max("high").as("daysHigh");
		
		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("daysHigh").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,yearLowGroup,
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

	public HighLowResult getHighLowByDate(String nseSymbol, LocalDate fromDate, LocalDate toDate) {
		Instant fromInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant toInstant = toDate.atStartOfDay().toInstant(ZoneOffset.UTC);

		MatchOperation matchSymbolAndDate = Aggregation.match(new Criteria().andOperator(
				Criteria.where("bhavDate").gte(Date.from(fromInstant)).lte(Date.from(toInstant)),
				Criteria.where("nseSymbol").is(nseSymbol)
		));

		GroupOperation yearLowGroup = Aggregation.group("nseSymbol").min("low").as("yearLow").max("high").as("yearHigh");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearLow").as("low").andExpression("yearHigh").as("high");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbolAndDate,  yearLowGroup, projectToMatchModel);

		AggregationResults<HighLowResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				HighLowResult.class);

		HighLowResult highLowResult = result.getUniqueMappedResult();

		if(highLowResult == null) {
			highLowResult = new HighLowResult("NO_DATA_FOUND",0.00,0.00);
		}

		return highLowResult;
	}

	public HighLowResult getHighLowByDate(String nseSymbol, LocalDate fromDate) {
		Instant yearBackInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);

		//MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));
		MatchOperation matchSymbolAndDate = Aggregation.match(new Criteria().andOperator(
				Criteria.where("bhavDate").gte(Date.from(yearBackInstant)),
				Criteria.where("nseSymbol").is(nseSymbol)
		));

		//MatchOperation matchPrices = Aggregation.match(new Criteria("bhavDate").gte(Date.from(yearBackInstant)));
		
		GroupOperation yearLowGroup = Aggregation.group("nseSymbol").min("low").as("yearLow").max("high").as("yearHigh");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearLow").as("low").andExpression("yearHigh").as("high");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbolAndDate,  yearLowGroup, projectToMatchModel);

		AggregationResults<HighLowResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				HighLowResult.class);

		HighLowResult highLowResult = result.getUniqueMappedResult();

		if(highLowResult == null) {
			highLowResult = new HighLowResult("NO_DATA_FOUND",0.00,0.00);
		}
		
		return highLowResult;
	}
	
	@Deprecated
	public double getLowFromDate(String nseSymbol, LocalDate fromDate) {

		Instant yearBackInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));


		MatchOperation matchPrices = Aggregation
				.match(new Criteria("bhavDate").gte(Date.from(yearBackInstant)));

/*		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.ASC, "low"));
		
		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);*/
		
		GroupOperation yearLowGroup = Aggregation.group("nseSymbol").min("low").as("yearLow");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearLow").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol,  matchPrices,  yearLowGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				StockPriceResult.class);

		double yearLow= 0.00;
		
		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		if(stockPriceResult != null) {
			yearLow = stockPriceResult.getResultPrice();
		}
		
		return yearLow;
	}

	public StockPrice getForDate(String nseSymbol, LocalDate date){
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("bhavDate").is(date.atStartOfDay().toInstant(ZoneOffset.UTC)),
						Criteria.where("nseSymbol").is(nseSymbol)
				)
		);

		List<StockPrice> stockPriceList = mongoTemplate.find(query, StockPrice.class);

		StockPrice stockPrice = null;

		if(!stockPriceList.isEmpty()) {
			stockPrice = stockPriceList.get(0);
		}

		return stockPrice;

	}

	public long delete(String nseSymbol){
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("nseSymbol").is(nseSymbol)
				)
		);

		DeleteResult deleteResult = mongoTemplate.remove(query, COLLECTION_PH);


		if(deleteResult!=null && deleteResult.wasAcknowledged()) {
			return deleteResult.getDeletedCount();
		}

		return 0l;

	}

	public List<StockPrice> get(String nseSymbol, LocalDate from, LocalDate to){
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("bhavDate").gte(from.atStartOfDay().toInstant(ZoneOffset.UTC)).lte(to.atStartOfDay().toInstant(ZoneOffset.UTC)),
						Criteria.where("nseSymbol").is(nseSymbol)
				)
		);

		query.with(Sort.by(Direction.ASC, "bhavDate"));

		return mongoTemplate.find(query, StockPrice.class);

	}

	@Deprecated
	public double getHighFromDate(String nseSymbol, LocalDate fromDate) {


		Instant yearBackInstant = fromDate.atStartOfDay().toInstant(ZoneOffset.UTC);

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		MatchOperation matchPrices = Aggregation
				.match(new Criteria("bhavDate").gte(Date.from(yearBackInstant)));

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").max("high").as("yearHigh");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("yearHigh").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol,  matchPrices, yearHighGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				StockPriceResult.class);

		double yearHigh = 0.00;
		
		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		if(stockPriceResult != null) {
			yearHigh = stockPriceResult.getResultPrice();;
		}
		
		//System.out.println(result.getRawResults());

		return yearHigh;
	}
	
	
	
	public double getyearHigh(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

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
	
	public long getTotalTradedQuantity(String nseSymbol) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));

		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(1);

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("totalTradedQuantity").as("resultOBV");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc,
				projectToMatchModel);

		AggregationResults<StockOBVResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				StockOBVResult.class);

		StockOBVResult stockOBVResult = result.getUniqueMappedResult();

		long prevOBV = 0;

		if (stockOBVResult != null) {
			prevOBV = stockOBVResult.getResultOBV();
		}

		return prevOBV;
	}
	
	public double getAveragePrice(String nseSymbol, double close, int days) {
		//TradingSession ts = this.getTradingSessionBeforeDays(days);

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));
		
		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").avg("close").as("avgPrice");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("avgPrice").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc, limitToOnlyFirstDoc, yearHighGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation, COLLECTION_PH,
				StockPriceResult.class);

		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		double averagePrice = close;

		if(stockPriceResult != null) {
			averagePrice = stockPriceResult.getResultPrice();
		}

		return averagePrice;
	}
	
	public double getTotalGain(String nseSymbol, int days) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));
		
		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);
		
		Criteria criteriaTotalGain = new Criteria("change").gt(0.00);
		
		MatchOperation matchPrices = Aggregation.match(criteriaTotalGain);

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").sum("change").as("totalGain");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("totalGain").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc,limitToOnlyFirstDoc, matchPrices, yearHighGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation,  COLLECTION_PH,
				StockPriceResult.class);

		double totalGain = 0.00;
		
		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		if(stockPriceResult != null) {
			totalGain = stockPriceResult.getResultPrice();
		}

		return totalGain;
	
	}
	
	public double getAverageGain(String nseSymbol, int days) {
		double averageGain =  this.getTotalGain(nseSymbol, days) / days;
		return averageGain;
	}
	
	public double getTotalLoss(String nseSymbol, int days) {

		MatchOperation matchSymbol = Aggregation.match(new Criteria("nseSymbol").is(nseSymbol));

		SortOperation sortByAvgPopAsc = Aggregation.sort(Sort.by(Direction.DESC, "bhavDate"));
		
		LimitOperation limitToOnlyFirstDoc = Aggregation.limit(days);
		
		Criteria criteriaTotalGain = new Criteria("change").lt(0.00);
		
		MatchOperation matchPrices = Aggregation.match(criteriaTotalGain);

		GroupOperation yearHighGroup = Aggregation.group("nseSymbol").sum("change").as("totalLoss");

		ProjectionOperation projectToMatchModel = Aggregation.project().andExpression("nseSymbol").as("nseSymbol")
				.andExpression("totalLoss").as("resultPrice");

		Aggregation aggregation = Aggregation.newAggregation(matchSymbol, sortByAvgPopAsc,limitToOnlyFirstDoc, matchPrices, yearHighGroup,
				projectToMatchModel);

		AggregationResults<StockPriceResult> result = mongoTemplate.aggregate(aggregation,  COLLECTION_PH,
				StockPriceResult.class);

		double totalLoss = 0.00;
		
		StockPriceResult stockPriceResult = result.getUniqueMappedResult();

		if(stockPriceResult != null) {
			totalLoss = stockPriceResult.getResultPrice();
		}

		return totalLoss;
	
	}
	
	public double getAverageLoss(String nseSymbol, int days) {
		double averageLoss = Math.abs( this.getTotalLoss(nseSymbol, days) / days);
		return averageLoss;
	}

	public StockPrice getPrevPrice(String nseSymbol, int days) {

		Query query = new Query(new Criteria("nseSymbol").is(nseSymbol));

		query.with(Sort.by(Sort.Direction.DESC,"bhavDate")).limit(days);

		List<StockPrice> stockPriceList = mongoTemplate.find(query, StockPrice.class);

		StockPrice stockPrice = null;

		if(!stockPriceList.isEmpty() && stockPriceList.size() == days) {
			stockPrice = stockPriceList.get(days - 1);
		}

		return stockPrice;

	}

	public void upsert(StockPrice stockPrice){

		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("bhavDate").is(stockPrice.getBhavDate()),
						Criteria.where("nseSymbol").is(stockPrice.getNseSymbol())
				)
		);

		Document doc = new Document(); // org.bson.Document
		mongoTemplate.getConverter().write(stockPrice, doc);
		Update update = Update.fromDocument(doc);

		UpdateResult updateResult = mongoTemplate.upsert(query, update, COLLECTION_PH);
	}

	public long count(String nseSymbol){
		Query query = new Query();
		query.addCriteria(
				Criteria.where("nseSymbol").is(nseSymbol)
		);

		return mongoTemplate.count(query, COLLECTION_PH);
	}

	public List<StockPrice> get(String nseSymbol, int days){

		Query query = new Query();
		query.addCriteria(
				Criteria.where("nseSymbol").is(nseSymbol)
		);

		query.with(Sort.by(Sort.Direction.DESC, "bhavDate")).limit(days);

		return mongoTemplate.find(query, StockPrice.class);
	}

	public StockPrice getByHigh(String nseSymbol, double high){

		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
					Criteria.where("nseSymbol").is(nseSymbol),
					Criteria.where("high").is(high)
				)
		);

		List<StockPrice> stockPriceList = mongoTemplate.find(query, StockPrice.class);

		StockPrice stockPrice = null;

		if(!stockPriceList.isEmpty() && !stockPriceList.isEmpty()) {
			stockPrice = stockPriceList.get(stockPriceList.size() - 1);
		}

		return stockPrice;
	}

	public StockPrice getByLow(String nseSymbol, double high){

		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("nseSymbol").is(nseSymbol),
						Criteria.where("low").is(high)
				)
		);

		List<StockPrice> stockPriceList = mongoTemplate.find(query, StockPrice.class);

		StockPrice stockPrice = null;

		if(!stockPriceList.isEmpty() && !stockPriceList.isEmpty()) {
			stockPrice = stockPriceList.get(stockPriceList.size() - 1);
		}

		return stockPrice;
	}

}
