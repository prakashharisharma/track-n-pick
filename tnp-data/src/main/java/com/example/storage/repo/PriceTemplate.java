package com.example.storage.repo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.data.common.type.Timeframe;
import com.example.storage.model.*;
import com.example.storage.model.result.*;
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

	public List<StockPrice> get(String nseSymbol, int days){

		Query query = new Query();
		query.addCriteria(
				Criteria.where("nseSymbol").is(nseSymbol)
		);

		query.with(Sort.by(Sort.Direction.DESC, "bhavDate")).limit(days);

		return mongoTemplate.find(query, StockPrice.class);
	}


}
