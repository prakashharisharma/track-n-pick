package com.example.service.async;

import java.io.IOException;
import java.time.LocalDate;

import com.example.mq.producer.EventProducerService;
import com.example.service.SectorService;
import com.example.util.SupportAndResistanceUtil;
import com.example.util.io.model.StockIO;
import com.example.util.io.model.UpdateTriggerIO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.kafka.annotation.KafkaListener;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.model.StockPrice;
import com.example.storage.model.result.HighLowResult;
import com.example.model.master.Stock;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.repo.stocks.StockPriceRepository;
import com.example.service.StockService;
import com.example.util.io.model.StockPriceIO;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import javax.jms.Message;
import javax.jms.Session;


@Slf4j
@Service
public class UpdatePriceConsumer {
//public class UpdatePriceConsumer extends AbstractEventConsumer<StockPriceIO> {
	@Autowired
	private QueueService queueService;

	@Autowired
	private StockService stockService;

	@Autowired
	private SectorService sectorService;

	@Autowired
	private StockPriceRepository stockPriceRepository;

	@Autowired
	private PriceTemplate priceTemplate;

	@Autowired private ObjectMapper mapper;

	@Autowired private EventProducerService eventProducerService;

	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,
							   Session session) throws InterruptedException {

		log.info("{} Starting price update.", stockPriceIO.getNseSymbol());

		if (stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol()) != null){

			this.processPriceUpdate(stockPriceIO);

		}else {

			this.addStockToMaster(stockPriceIO);
		}

		log.info("{} Completed price update.", stockPriceIO.getNseSymbol());
	}

/*
	@KafkaListener(
			topics = "${kafka.topic.prefix:}" + QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE,
			clientIdPrefix = "json",
			containerFactory = "kafkaListenerContainerFactory",
			concurrency = "${spring.kafka.listener.concurrency:1}")
	@Override
	public void consume(
			ConsumerRecord<String, Message<StockPriceIO>> consumerRecord, Message<StockPriceIO> messageWrapper)
			throws Exception {
		this.process(consumerRecord, messageWrapper);
	}

	@Override
	public void process(Message<StockPriceIO> messageWrapper) throws Exception {

		StockPriceIO stockPriceIO = this.map(messageWrapper);

		log.info("{} Starting price update.", stockPriceIO.getNseSymbol());

		if (stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol()) != null){

			this.processPriceUpdate(stockPriceIO);

		}else {

			this.addStockToMaster(stockPriceIO);
		}

		log.info("{} Completed price update.", stockPriceIO.getNseSymbol());
	}

	private StockPriceIO map(Message<StockPriceIO> messageWrapper) throws IOException {

		ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();

		String jsonString = objectWriter.writeValueAsString(messageWrapper.getPayload());

		return mapper.readValue(jsonString, new TypeReference<StockPriceIO>() {});
	}
*/

	private void processPriceUpdate(StockPriceIO stockPriceIO){

		this.updatePriceHistory(stockPriceIO);


	}

	private void updatePriceHistory(StockPriceIO stockPriceIO){

		log.info("{} Updating historical price", stockPriceIO.getNseSymbol());

		StockPrice stockPrice = new StockPrice(stockPriceIO.getNseSymbol(),stockPriceIO.getBhavDate(), stockPriceIO.getOpen(), stockPriceIO.getHigh(),
				stockPriceIO.getLow(), stockPriceIO.getClose(),  stockPriceIO.getTottrdqty());

		priceTemplate.upsert(stockPrice);
		log.info("{} Updated historical price for date {}", stockPriceIO.getNseSymbol(), stockPriceIO.getBhavDate());
		//StockPrice existingStockPriceHistory = priceTemplate.getForDate(stockPriceIO.getNseSymbol(), stockPriceIO.getTimestamp());


		/*
		this.setYearHighLow(stockPriceIO, stockPriceHistory);
		this.set14DaysHighLow(stockPriceIO, stockPriceHistory);

		if(existingStockPriceHistory == null){

			priceTemplate.create(stockPriceHistory);

			log.info("{} Updated historical price", stockPriceIO.getNseSymbol());

		}else{
			log.info("{} Already updated historical price", stockPriceIO.getNseSymbol());
		}
		*/

		this.setYearHighLow(stockPriceIO);

		this.updatePriceTxn(stockPriceIO);

	}

	private void updatePriceTxn(StockPriceIO stockPriceIO) {

		log.info("{} Updating transactional price.", stockPriceIO.getNseSymbol());

		Stock stock = stockService.getStockByNseSymbol(stockPriceIO.getNseSymbol());

		com.example.model.stocks.StockPrice stockPrice = stock.getStockPrice();

		if (stockPrice != null && ( stockPrice.getBhavDate().isEqual(stockPriceIO.getTimestamp())
				|| stockPrice.getBhavDate().isAfter(stockPriceIO.getTimestamp()))) {
			log.info("{} Transactional price is already up to date", stockPriceIO.getNseSymbol());
		}else {

			if (stockPrice == null) {
				stockPrice = new com.example.model.stocks.StockPrice();
			}

			stockPrice.setStock(stock);

			stockPrice.setBhavDate(stockPriceIO.getTimestamp());

			stockPrice.setPrevOpen(stockPrice.getOpen());
			stockPrice.setPrevHigh(stockPrice.getHigh());
			stockPrice.setPrevLow(stockPrice.getLow());
			stockPrice.setPrevClose(stockPrice.getClose());

			stockPrice.setOpen(stockPriceIO.getOpen());
			stockPrice.setHigh(stockPriceIO.getHigh());
			stockPrice.setLow(stockPriceIO.getLow());
			stockPrice.setClose(stockPriceIO.getClose());

			stockPrice.setCurrentPrice(stockPriceIO.getClose());


			stockPrice.setYearHigh(stockPriceIO.getYearHigh());
			stockPrice.setYearLow(stockPriceIO.getYearLow());

			stockPrice.setLastModified(LocalDate.now());
			stockPriceRepository.save(stockPrice);

			log.info("{} Updated transactional price", stockPriceIO.getNseSymbol());
		}

		if(stockPriceIO.isLastRecordToProcess()) {

			UpdateTriggerIO updateTriggerIO = new UpdateTriggerIO(UpdateTriggerIO.TriggerType.UPDATE_RESEARCH);

			queueService.send(updateTriggerIO, QueueConstants.MTQueue.UPDATE_TRIGGER_QUEUE);
		}

		queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_TECHNICALS_TXN_QUEUE);

		queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_FACTOR_TXN_QUEUE);

		//this.createEvent(stockPriceIO);
	}





	private void setYearHighLow(StockPriceIO stockPriceIO ){

		HighLowResult highLowResult = priceTemplate.getHighLowByDate(stockPriceIO.getNseSymbol(), LocalDate.now().minusWeeks(52));

		double high = this.calculateHigh(highLowResult, stockPriceIO);

		double low = this.calculateLow(highLowResult, stockPriceIO);

		stockPriceIO.setYearLow(low);
		stockPriceIO.setYearHigh(high);
	}
	private void set14DaysHighLow(StockPriceIO stockPriceIO, StockPrice stockPriceHistory ){

		HighLowResult highLowResult = priceTemplate.getHighLowByDays(stockPriceIO.getNseSymbol(), 14);

		double high = this.calculateHigh(highLowResult, stockPriceIO);

		double low = this.calculateLow(highLowResult, stockPriceIO);

		stockPriceIO.setLow14(low);
		stockPriceIO.setHigh14(high);

		stockPriceHistory.setLow14(low);
		stockPriceHistory.setHigh14(high);
	}

	private double calculateHigh(HighLowResult highLowResult, StockPriceIO stockPriceIO){

		double high = 0.00;

		if(highLowResult != null && !highLowResult.get_id().equalsIgnoreCase("NO_DATA_FOUND")) {

			high = highLowResult.getHigh();

		}else {

			high = stockPriceIO.getHigh();
		}

		if (high < stockPriceIO.getHigh()) {

			high = stockPriceIO.getHigh();

		}

		return high;
	}

	private double calculateLow(HighLowResult highLowResult, StockPriceIO stockPriceIO){

		double low = 0.00;

		if(highLowResult != null && !highLowResult.get_id().equalsIgnoreCase("NO_DATA_FOUND")) {

			low = highLowResult.getLow();

		}else {
			low = stockPriceIO.getLow();

		}

		if (low > stockPriceIO.getLow()) {

			low = stockPriceIO.getLow();

		}

		return low;
	}

	private void addStockToMaster(StockPriceIO stockPriceIO){

		log.info("{} Adding to master", stockPriceIO.getNseSymbol());

		StockIO stockIO = new StockIO(stockPriceIO.getCompanyName(), "NIFTY", stockPriceIO.getNseSymbol(), stockPriceIO.getSeries(), stockPriceIO.getIsin(), StockIO.IndiceType.NSE);

		stockIO.setBseCode(stockPriceIO.getBseCode());

		if(stockPriceIO.getExchange().equalsIgnoreCase("NSE")) {
			stockIO.setSector("NSE");
			stockIO.setIndice(StockIO.IndiceType.NSE);
			stockIO.setExchange(StockIO.Exchange.NSE);
		}else if(stockPriceIO.getExchange().equalsIgnoreCase("BSE") && stockPriceIO.getSeries().equalsIgnoreCase("A")){
			stockIO.setSector("BSE");
			stockIO.setIndice(StockIO.IndiceType.BSE_A);
			stockIO.setExchange(StockIO.Exchange.BSE);
		} else if (stockPriceIO.getExchange().equalsIgnoreCase("BSE") && stockPriceIO.getSeries().equalsIgnoreCase("B")) {
			stockIO.setSector("BSE");
			stockIO.setIndice(StockIO.IndiceType.BSE_B);
			stockIO.setExchange(StockIO.Exchange.BSE);
		}else  {
			stockIO.setSector("BSE");
			stockIO.setIndice(StockIO.IndiceType.BSE_M);
			stockIO.setExchange(StockIO.Exchange.BSE);
		}

		Stock stock = stockService.add(stockIO.getExchange(),stockIO.getIsin(), stockIO.getCompanyName(), stockIO.getNseSymbol(), stockIO.getBseCode(), stockIO.getIndice(), sectorService.getOrAddSectorByName(stockIO.getSector()));

		log.info("{} Added to master", stockPriceIO.getNseSymbol());

		queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_PRICE_TXN_QUEUE);
	}

/*
	private void createEvent(StockPriceIO stockPriceIO) {
		Message<StockPriceIO> message = new Message<>(stockPriceIO);
		eventProducerService.create(QueueConstants.MTQueue.UPDATE_TECHNICALS_TXN_QUEUE, message);
		log.info("send event");
	}
 */
}
