package com.example.storage.repo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.storage.model.*;
import com.example.storage.model.result.*;
import com.example.util.io.model.type.Timeframe;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Repository
public class PriceTemplate {
	public static final String COLLECTION_PH = "price_history";

	public static final String COLLECTION_WPH = "weekly_price_history";

	public static final String COLLECTION_TH = "technicals_history";

	public static final String COLLECTION_WTH = "weekly_technicals_history";

	// Mapping between Timeframe and corresponding StockPrice class
	private static final Map<Timeframe, Class<? extends StockPrice>> PRICE_COLLECTIONS = Map.of(
			Timeframe.YEARLY, YearlyStockPrice.class,
			Timeframe.QUARTERLY, QuarterlyStockPrice.class,
			Timeframe.MONTHLY, MonthlyStockPrice.class,
			Timeframe.WEEKLY, WeeklyStockPrice.class,
			Timeframe.DAILY, StockPrice.class // Default case
	);

	@Autowired
	private MongoTemplate mongoTemplate;

	public void create(StockPrice stockPrice) {
		mongoTemplate.insert(stockPrice);
	}

	public long create(List<StockPrice>  stockPriceList) {

		long insertCount = 0;

		BulkWriteResult result;

		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, StockPrice.class);

		bulkOps.insert(stockPriceList);

		try {

			result = bulkOps.execute();

			insertCount = result.getInsertedCount();

			log.info("Successfully inserted {} documents", insertCount);

		} catch(BulkOperationException e) {

			log.error("An error occurred while bulk insert", e);

		}

		return insertCount;
	}

	public void createWeekly(List<StockPrice>  stockPriceList) {

		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, WeeklyStockPrice.class);
		bulkOps.insert(stockPriceList);
		BulkWriteResult result;
		try {
			result = bulkOps.execute();
			System.out.println("Inserted " + result.getInsertedCount());
		} catch(BulkOperationException e) {
			result = e.getResult();
		}
	}

	public void createMonthly(List<StockPrice>  stockPriceList) {

		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, MonthlyStockPrice.class);
		bulkOps.insert(stockPriceList);
		BulkWriteResult result;
		try {
			result = bulkOps.execute();
			System.out.println("Inserted " + result.getInsertedCount());
		} catch(BulkOperationException e) {
			result = e.getResult();
		}
	}

	public void createQuarterly(List<StockPrice>  stockPriceList) {

		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, QuarterlyStockPrice.class);
		bulkOps.insert(stockPriceList);
		BulkWriteResult result;
		try {
			result = bulkOps.execute();
			System.out.println("Inserted " + result.getInsertedCount());
		} catch(BulkOperationException e) {
			result = e.getResult();
		}
	}

	public void createYearly(List<StockPrice>  stockPriceList) {

		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, YearlyStockPrice.class);
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

	public long delete(String nseSymbol) {
		Query query = new Query();
		query.addCriteria(Criteria.where("nseSymbol").is(nseSymbol));

		if (mongoTemplate.exists(query, COLLECTION_PH)) {
			DeleteResult deleteResult = mongoTemplate.remove(query, COLLECTION_PH);
			return deleteResult.wasAcknowledged() ? deleteResult.getDeletedCount() : 0L;
		}
		return 0L;
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

	public StockPrice getOHLC(String nseSymbol, Timeframe timeframe, LocalDate from, LocalDate to) {
		Aggregation aggregation;

		switch (timeframe) {
			case WEEKLY:
				aggregation = Aggregation.newAggregation(
						Aggregation.match(Criteria.where("nseSymbol").is(nseSymbol)
								.and("bhavDate").gte(from.atStartOfDay().toInstant(ZoneOffset.UTC))
								.lte(to.atStartOfDay().toInstant(ZoneOffset.UTC))),
						Aggregation.project()
								.and("nseSymbol").as("nseSymbol")
								.and("bhavDate").as("bhavDate")
								.and("open").as("open")
								.and("high").as("high")
								.and("low").as("low")
								.and("close").as("close")
								.and("volume").as("volume")
								.andExpression("{$isoWeekYear: '$bhavDate'}").as("year")
								.andExpression("{$week: '$bhavDate'}").as("week"),
						Aggregation.group("nseSymbol", "year", "week")
								.last("bhavDate").as("bhavDate")
								.first("open").as("open")
								.max("high").as("high")
								.min("low").as("low")
								.last("close").as("close")
								.sum("volume").as("volume"),
						Aggregation.sort(Sort.Direction.ASC, "bhavDate")
				);
				break;

			case MONTHLY:
				aggregation = Aggregation.newAggregation(
						Aggregation.match(Criteria.where("nseSymbol").is(nseSymbol)
								.and("bhavDate").gte(from.atStartOfDay().toInstant(ZoneOffset.UTC))
								.lte(to.atStartOfDay().toInstant(ZoneOffset.UTC))),
						Aggregation.project()
								.and("nseSymbol").as("nseSymbol")
								.and("bhavDate").as("bhavDate")
								.and("open").as("open")
								.and("high").as("high")
								.and("low").as("low")
								.and("close").as("close")
								.and("volume").as("volume")
								.andExpression("{$year: '$bhavDate'}").as("year")
								.andExpression("{$month: '$bhavDate'}").as("month"),
						Aggregation.group("nseSymbol", "year", "month")
								.last("bhavDate").as("bhavDate")
								.first("open").as("open")
								.max("high").as("high")
								.min("low").as("low")
								.last("close").as("close")
								.sum("volume").as("volume"),
						Aggregation.sort(Sort.Direction.ASC, "bhavDate")
				);
				break;

			case QUARTERLY:
				aggregation = Aggregation.newAggregation(
						Aggregation.match(Criteria.where("nseSymbol").is(nseSymbol)
								.and("bhavDate").gte(from.atStartOfDay().toInstant(ZoneOffset.UTC))
								.lte(to.atStartOfDay().toInstant(ZoneOffset.UTC))),
						Aggregation.project()
								.and("nseSymbol").as("nseSymbol")
								.and("bhavDate").as("bhavDate")
								.and("open").as("open")
								.and("high").as("high")
								.and("low").as("low")
								.and("close").as("close")
								.and("volume").as("volume")
								.andExpression("{$year: '$bhavDate'}").as("year")
								.andExpression("{$ceil: {$divide: [{$month: '$bhavDate'}, 3]}}").as("quarter"),
						Aggregation.group("nseSymbol", "year", "quarter")
								.last("bhavDate").as("bhavDate")
								.first("open").as("open")
								.max("high").as("high")
								.min("low").as("low")
								.last("close").as("close")
								.sum("volume").as("volume"),
						Aggregation.sort(Sort.Direction.ASC, "bhavDate")
				);
				break;

			case YEARLY:
				aggregation = Aggregation.newAggregation(
						Aggregation.match(Criteria.where("nseSymbol").is(nseSymbol)
								.and("bhavDate").gte(from.atStartOfDay().toInstant(ZoneOffset.UTC))
								.lte(to.atStartOfDay().toInstant(ZoneOffset.UTC))),
						Aggregation.project()
								.and("nseSymbol").as("nseSymbol")
								.and("bhavDate").as("bhavDate")
								.and("open").as("open")
								.and("high").as("high")
								.and("low").as("low")
								.and("close").as("close")
								.and("volume").as("volume")
								.andExpression("{$year: '$bhavDate'}").as("year"),
						Aggregation.group("nseSymbol", "year")
								.last("bhavDate").as("bhavDate")
								.first("open").as("open")
								.max("high").as("high")
								.min("low").as("low")
								.last("close").as("close")
								.sum("volume").as("volume"),
						Aggregation.sort(Sort.Direction.ASC, "bhavDate")
				);
				break;

			default:
				throw new IllegalArgumentException("Invalid timeframe: " + timeframe);
		}

		AggregationResults<StockPrice> results = mongoTemplate.aggregate(aggregation, "price_history", StockPrice.class);
		List<StockPrice> stockPriceList = results.getMappedResults();

		return (stockPriceList != null && !stockPriceList.isEmpty()) ?
				stockPriceList.get(stockPriceList.size() - 1) :
				new StockPrice(nseSymbol, from.atStartOfDay().toInstant(ZoneOffset.UTC), 0.00, 0.00, 0.00, 0.00, 0L);
	}




	public <T extends StockPrice> List<T> get(Timeframe timeframe, String nseSymbol, LocalDate from, LocalDate to){
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("bhavDate").gte(from.atStartOfDay().toInstant(ZoneOffset.UTC)).lte(to.atStartOfDay().toInstant(ZoneOffset.UTC)),
						Criteria.where("nseSymbol").is(nseSymbol)
				)
		);

		query.with(Sort.by(Direction.ASC, "bhavDate"));

		// Fetch the correct class based on Timeframe
		Class<T> stockPriceClass = (Class<T>) PRICE_COLLECTIONS.getOrDefault(timeframe, StockPrice.class);

		return mongoTemplate.find(query, stockPriceClass);
	}


	public List<WeeklyStockPrice> getWeekly(String nseSymbol, LocalDate from, LocalDate to){
		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("bhavDate").gte(from.atStartOfDay().toInstant(ZoneOffset.UTC)).lte(to.atStartOfDay().toInstant(ZoneOffset.UTC)),
						Criteria.where("nseSymbol").is(nseSymbol)
				)
		);

		query.with(Sort.by(Direction.ASC, "bhavDate"));

		return mongoTemplate.find(query, WeeklyStockPrice.class);

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

		this.upsert(stockPrice, COLLECTION_PH);

	}

	public void upsert(StockPrice stockPrice, final String collection){

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

		UpdateResult updateResult = mongoTemplate.upsert(query, update, collection);
	}

	public void upsert(Timeframe timeframe, StockPrice stockPrice) {

		Query query = new Query();
		query.addCriteria(
				new Criteria().andOperator(
						Criteria.where("bhavDate").is(stockPrice.getBhavDate()),
						Criteria.where("nseSymbol").is(stockPrice.getNseSymbol())
				)
		);

		// Convert stockPrice object to BSON document
		Document doc = new Document();
		mongoTemplate.getConverter().write(stockPrice, doc);
		Update update = Update.fromDocument(doc);

		// Fetch the correct collection class based on Timeframe
		Class<? extends StockPrice> collectionClass = PRICE_COLLECTIONS.getOrDefault(timeframe, StockPrice.class);

		// Perform the upsert
		UpdateResult updateResult = mongoTemplate.upsert(query, update, collectionClass);

	}

	public long create(Timeframe timeframe, List<StockPrice>  stockPriceList) {

		long insertCount = 0;

		BulkWriteResult result;

		// Fetch the correct collection class based on Timeframe
		Class<? extends StockPrice> collectionClass = PRICE_COLLECTIONS.getOrDefault(timeframe, StockPrice.class);

		BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, collectionClass);

		bulkOps.insert(stockPriceList);

		try {

			result = bulkOps.execute();

			insertCount = result.getInsertedCount();

			log.info("Successfully inserted {} documents", insertCount);

		} catch(BulkOperationException e) {

			log.error("An error occurred while bulk insert", e);

		}

		return insertCount;
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
