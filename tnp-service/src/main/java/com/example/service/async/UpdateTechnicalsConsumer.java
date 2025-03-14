package com.example.service.async;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.jms.Message;
import javax.jms.Session;

import com.example.dto.OHLCV;
import com.example.service.*;
import com.example.service.calc.AverageDirectionalIndexCalculatorService;
import com.example.service.calc.ExponentialMovingAverageCalculatorService;
import com.example.service.calc.MovingAverageConvergenceDivergenceService;
import com.example.service.calc.RelativeStrengthIndexCalculatorService;
import com.example.storage.model.*;
import com.example.storage.model.assembler.StockPriceOHLCVAssembler;
import com.example.storage.model.result.HighLowResult;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.repo.TechnicalsTemplate;
import com.example.util.FormulaService;
import com.example.util.MiscUtil;
import com.example.util.SupportAndResistanceUtil;
import com.example.util.io.model.ResearchIO;
import com.example.util.io.model.StockPriceIO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.model.master.Stock;
import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.repo.stocks.StockTechnicalsRepository;
import com.example.util.io.model.StockTechnicalsIO;

@Component
@Slf4j
public class UpdateTechnicalsConsumer {
//	public class UpdateTechnicalsConsumer extends AbstractEventConsumer<StockPriceIO> {

	private static final long MIN_TRADING_DAYS = 32;

	@Autowired
	private StockService stockService;
	@Autowired
	private StockTechnicalsRepository stockTechnicalsRepository;

	@Autowired
	private QueueService queueService;

	@Autowired
	private ResearchLedgerFundamentalService researchLedgerFundamentalService;

	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private TechnicalsTemplate technicalsTemplate;

	@Autowired
	private FormulaService formulaService;

	@Autowired
	private PriceTemplate priceTemplate;

	@Autowired private ObjectMapper mapper;

	@Autowired
	private RelativeStrengthIndexCalculatorService relativeStrengthIndexService;

	@Autowired
	private ExponentialMovingAverageCalculatorService exponentialMovingAverageService;

	@Autowired
	private MovingAverageConvergenceDivergenceService movingAverageConvergenceDivergenceService;

	@Autowired
	private AverageDirectionalIndexCalculatorService averageDirectionalIndexService;

	@Autowired
	private StockPriceOHLCVAssembler stockPriceOHLCVAssembler;

	@Autowired
	private MiscUtil miscUtil;

	@Autowired
	private UpdateTechnicalsService updateTechnicalsService;

	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_TECHNICALS_TXN_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers,
							   Message message, Session session) throws InterruptedException {

		log.info("{} Starting technicals update.", stockPriceIO.getNseSymbol());
			try {
				this.processTechnicalsUpdate(stockPriceIO);

				if(stockPriceIO.getSeries().equalsIgnoreCase("EQ")) {
					ResearchIO researchIO = new ResearchIO(stockPriceIO.getNseSymbol(), ResearchIO.ResearchType.TECHNICAL, ResearchIO.ResearchTrigger.BUY_SELL);

					queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);
				}

			}catch(Exception e){
				log.error("An error occured while processing technicals update {}", stockPriceIO.getNseSymbol(), e);
			}

		log.info("{} Completed technicals update.", stockPriceIO.getNseSymbol());
	}

/*
	@KafkaListener(
			topics = "${kafka.topic.prefix:}" + QueueConstants.MTQueue.UPDATE_TECHNICALS_TXN_QUEUE,
			clientIdPrefix = "json",
			containerFactory = "kafkaListenerContainerFactory",
			concurrency = "${spring.kafka.listener.concurrency:1}")
	@Override
	public void consume(
			ConsumerRecord<String, com.example.dto.kafka.Message<StockPriceIO>> consumerRecord, com.example.dto.kafka.Message<StockPriceIO> messageWrapper)
			throws Exception {
		this.process(consumerRecord, messageWrapper);
	}

	@Override
	public void process(com.example.dto.kafka.Message<StockPriceIO> messageWrapper) throws Exception {

		StockPriceIO stockPriceIO = this.map(messageWrapper);

		log.info("{} Starting technicals update.", stockPriceIO.getNseSymbol());

		this.processTechnicalsUpdate(stockPriceIO);

		log.info("{} Completed technicals update.", stockPriceIO.getNseSymbol());
	}

	private StockPriceIO map(Message<StockPriceIO> messageWrapper) throws IOException {

		ObjectWriter objectWriter = mapper.writer().withDefaultPrettyPrinter();

		String jsonString = objectWriter.writeValueAsString(messageWrapper.getPayload());

		return mapper.readValue(jsonString, new TypeReference<StockPriceIO>() {});
	}
 */

	public void processTechnicalsUpdate(StockPriceIO stockPriceIO){

		updateTechnicalsService.updateTechnicals(stockPriceIO);

	}

	public void updateTechnicalsHistory(StockPriceIO stockPriceIO){

		log.info("{} Updating technicals history.", stockPriceIO.getNseSymbol());

		try {

			StockTechnicals stockTechnicals = this.build(stockPriceIO);

			this.setYearHighLow(stockPriceIO, stockTechnicals);

			technicalsTemplate.upsert(stockTechnicals);

			this.updateTechnicalsTxn(stockTechnicals, stockPriceIO);

		} catch (Exception e) {

			log.error(" {} Error while updating echnicals ", stockPriceIO.getNseSymbol(), e);

		}

		log.info("{} Updated technicals history.", stockPriceIO.getNseSymbol());
	}


	private StockTechnicals build(StockPriceIO stockPriceIO){

		String nseSymbol = stockPriceIO.getNseSymbol();

		long tradingDays = priceTemplate.count(nseSymbol);

		StockTechnicals prevStockTechnicals=this.init(stockPriceIO);

		if(tradingDays <= 1){
			return prevStockTechnicals;
		}

		if(tradingDays > MIN_TRADING_DAYS){
			prevStockTechnicals = technicalsTemplate.getPrevTechnicals(nseSymbol,  1);
		}

		//To handle if there is corrupt data
		if(prevStockTechnicals.getEma() == null){
			tradingDays = 2;
		}

		List<OHLCV> ohlcvList = this.fetch(nseSymbol, tradingDays);

		Volume volume = this.build(nseSymbol, ohlcvList, prevStockTechnicals.getVolume(), tradingDays);

		//OnBalanceVolume onBalanceVolume = this.build(nseSymbol, ohlcvList, prevStockTechnicals.getObv(), tradingDays);

		SimpleMovingAverage sma = this.build(nseSymbol, ohlcvList, prevStockTechnicals.getSma(), tradingDays);

		ExponentialMovingAverage ema = this.build(nseSymbol, ohlcvList,prevStockTechnicals.getEma(), tradingDays);

		AverageDirectionalIndex adx = this.build(nseSymbol, ohlcvList, prevStockTechnicals.getAdx(), tradingDays);

		RelativeStrengthIndex rsi = this.build(nseSymbol, ohlcvList, prevStockTechnicals.getRsi(), tradingDays);

		MovingAverageConvergenceDivergence macd = this.build(nseSymbol, ohlcvList, prevStockTechnicals.getMacd(), tradingDays);

		return new StockTechnicals(nseSymbol, stockPriceIO.getBhavDate(), volume, null,  sma, ema, adx, rsi, macd);
		//return new StockTechnicals(nseSymbol, stockPriceIO.getBhavDate(), volume,  sma, ema);
	}

	private StockTechnicals init(StockPriceIO stockPriceIO){

		StockTechnicals stockTechnicals = new StockTechnicals();

		stockTechnicals.setBhavDate(stockPriceIO.getBhavDate());
		stockTechnicals.setNseSymbol(stockPriceIO.getNseSymbol());

		Volume volume = new Volume(stockPriceIO.getTottrdqty(),stockPriceIO.getTottrdqty(), stockPriceIO.getTottrdqty(), stockPriceIO.getTottrdqty());
		stockTechnicals.setVolume(volume);


		SimpleMovingAverage sma = new SimpleMovingAverage(0.00,0.00,0.00,0.00,0.00,0.00);
		stockTechnicals.setSma(sma);



		ExponentialMovingAverage ema = new ExponentialMovingAverage(0.00,0.00,0.00,0.00,0.00,0.00);
		stockTechnicals.setEma(ema);


		AverageDirectionalIndex adx = new AverageDirectionalIndex(0.00, 0.00,0.00, 0.00, 0.00, 0.00, 0.00);
		stockTechnicals.setAdx(adx);



		RelativeStrengthIndex rsi = new RelativeStrengthIndex(0.00, 0.00, 0.00, 0.00);
		stockTechnicals.setRsi(rsi);



		MovingAverageConvergenceDivergence macd = new MovingAverageConvergenceDivergence(0.00, 0.00, 0.00, 0.00);
		stockTechnicals.setMacd(macd);


		return stockTechnicals;
	}

	private List<OHLCV> fetch(String nseSymbol, long tradingDays){

		int limit = tradingDays > MIN_TRADING_DAYS ? 2 : 700;

		List<StockPrice> stockPriceList =  priceTemplate.get(nseSymbol, limit);

		Collections.reverse(stockPriceList);

		return stockPriceOHLCVAssembler.toModel(stockPriceList);
	}

	private Volume build(String nseSymbol, List<OHLCV> ohlcvList, Volume prevVolume, long tradingDays){

		OHLCV ohlcv = ohlcvList.get(ohlcvList.size() -1);

		Long avgVolume5 = technicalsTemplate.getAverageVolume(nseSymbol, 5);

		Long avgVolume20 = technicalsTemplate.getAverageVolume(nseSymbol, 20);
		Long avgVolume30 = technicalsTemplate.getAverageVolume(nseSymbol, 30);

		//avgVolume5 = formulaService.calculateSmoothedMovingAverage(avgVolume5, ohlcv.getVolume(), 5);
		return new Volume(ohlcv.getVolume(), avgVolume5,avgVolume20, avgVolume30);
	}

private SimpleMovingAverage build(String nseSymbol, List<OHLCV> ohlcvList, SimpleMovingAverage prevSimpleMovingAverage, long tradingDays){

	double close = 0.00;

	double sma5 = priceTemplate.getAveragePrice(nseSymbol, close, 5);

	double sma10 = priceTemplate.getAveragePrice(nseSymbol, close, 10);

	double sma20 = priceTemplate.getAveragePrice(nseSymbol, close, 20);

	double sma50 = priceTemplate.getAveragePrice(nseSymbol, close, 50);

	double sma100 = priceTemplate.getAveragePrice(nseSymbol, close, 100);

	double sma200 = priceTemplate.getAveragePrice(nseSymbol,close, 200);

	return new SimpleMovingAverage(miscUtil.formatDouble(sma5,"00"),
			miscUtil.formatDouble(sma10,"00"),
			miscUtil.formatDouble(sma20,"00"),
			miscUtil.formatDouble(sma50,"00"),
			miscUtil.formatDouble(sma100,"00"),
			miscUtil.formatDouble(sma200,"00"));
}

	private ExponentialMovingAverage build(String nseSymbol,List<OHLCV> ohlcvList, ExponentialMovingAverage prevExponentialMovingAverage, long tradingDays){

		if(tradingDays > MIN_TRADING_DAYS) {
			OHLCV ohlcv = ohlcvList.get(1);
			double ema5 = exponentialMovingAverageService.calculate(ohlcv, prevExponentialMovingAverage.getAvg5(), 5);
			double ema10 = exponentialMovingAverageService.calculate(ohlcv,prevExponentialMovingAverage.getAvg10(), 10);
			double ema20 = exponentialMovingAverageService.calculate(ohlcv, prevExponentialMovingAverage.getAvg20(), 20);
			double ema50 = exponentialMovingAverageService.calculate(ohlcv, prevExponentialMovingAverage.getAvg50(), 50);
			double ema100 = exponentialMovingAverageService.calculate(ohlcv,prevExponentialMovingAverage.getAvg100(), 100);
			double ema200 = exponentialMovingAverageService.calculate(ohlcv, prevExponentialMovingAverage.getAvg200(), 200);

			return new ExponentialMovingAverage(ema5, ema10, ema20, ema50, ema100, ema200);
		}

		int resultIndex = ohlcvList.size()-1;
		double ema5 = exponentialMovingAverageService.calculate(ohlcvList, 5).get(resultIndex);;
		double ema10 = exponentialMovingAverageService.calculate(ohlcvList, 10).get(resultIndex);
		double ema20 = exponentialMovingAverageService.calculate(ohlcvList, 20).get(resultIndex);
		double ema50 = exponentialMovingAverageService.calculate(ohlcvList, 50).get(resultIndex);
		double ema100 = exponentialMovingAverageService.calculate(ohlcvList, 100).get(resultIndex);
		double ema200 = exponentialMovingAverageService.calculate(ohlcvList, 200).get(resultIndex);

		return new ExponentialMovingAverage(ema5, ema10, ema20, ema50, ema100, ema200);
	}

	private AverageDirectionalIndex build(String nseSymbol,List<OHLCV> ohlcvList, AverageDirectionalIndex prevAverageDirectionalIndex, long tradingDays){

		if(tradingDays > MIN_TRADING_DAYS) {
			return averageDirectionalIndexService.calculate(ohlcvList, prevAverageDirectionalIndex);
		}

		return averageDirectionalIndexService.calculate(ohlcvList).get(ohlcvList.size() -1);
	}

	private RelativeStrengthIndex build(String nseSymbol,List<OHLCV> ohlcvList, RelativeStrengthIndex prevRelativeStrengthIndex, long tradingDays){
		if(tradingDays > MIN_TRADING_DAYS) {
			return relativeStrengthIndexService.calculate(ohlcvList, prevRelativeStrengthIndex);
		}

		return relativeStrengthIndexService.calculate(ohlcvList).get(ohlcvList.size() -1);
	}

	private MovingAverageConvergenceDivergence build(String nseSymbol,List<OHLCV> ohlcvList, MovingAverageConvergenceDivergence prevMovingAverageConvergenceDivergence, long tradingDays){

		if(tradingDays > MIN_TRADING_DAYS) {
			return movingAverageConvergenceDivergenceService.calculate(ohlcvList.get(ohlcvList.size()-1), prevMovingAverageConvergenceDivergence);
		}

		return movingAverageConvergenceDivergenceService.calculate(ohlcvList).get(ohlcvList.size() -1);
	}


	private double getPriceChange(StockPriceIO stockPriceIO) {

		double priceChange = stockPriceIO.getClose() - stockPriceIO.getPrevClose();

		return priceChange;
	}

	private double getCurrentGain(StockPriceIO stockPriceIO) {

		double currentGain = 0.0;

		double change = this.getPriceChange(stockPriceIO);

		if (change > 0) {
			currentGain = change;
		}

		return currentGain;
	}

	private double getCurrentLoss(StockPriceIO stockPriceIO) {

		double currentLoss = 0.0;

		double change = this.getPriceChange(stockPriceIO);

		if (change <= 0) {
			currentLoss = change;
		}

		return currentLoss;
	}

	private StochasticOscillator getStochasticOscillator(StockPriceIO stockPriceIO,
														 StockTechnicals prevStockTechnicals) {

		double stochasticOscillatorValue = formulaService.calculateStochasticOscillatorValue(stockPriceIO.getClose(),
				stockPriceIO.getLow14(), stockPriceIO.getHigh14());

		double avg2D = 0.00;

		double avg3D = 0.00;

		try {

			avg2D = technicalsTemplate.getAverageStochasticOscillatorK(stockPriceIO.getNseSymbol(), 2);

			avg3D = formulaService.calculateAverage(avg2D, stochasticOscillatorValue);

		} catch (Exception e) {

			avg2D = 0.00;

			avg3D = 0.00;
		}

		StochasticOscillator stochasticOscillator = new StochasticOscillator(stochasticOscillatorValue, avg3D);

		return stochasticOscillator;
	}



	private StockTechnicalsIO createStockTechnicalsIO(StockPriceIO stockPriceIO, StockTechnicals stockTechnicals) {

		StockTechnicalsIO stockTechnicalsIO = new StockTechnicalsIO();

		stockTechnicalsIO.setNseSymbol(stockPriceIO.getNseSymbol());

		stockTechnicalsIO.setSma5(stockTechnicals.getSma().getAvg5());
		stockTechnicalsIO.setSma10(stockTechnicals.getSma().getAvg10());
		stockTechnicalsIO.setSma20(stockTechnicals.getSma().getAvg20());
		stockTechnicalsIO.setSma50(stockTechnicals.getSma().getAvg50());
		stockTechnicalsIO.setSma100(stockTechnicals.getSma().getAvg100());
		stockTechnicalsIO.setSma200(stockTechnicals.getSma().getAvg200());

		stockTechnicalsIO.setEma5(stockTechnicals.getEma().getAvg5());
		stockTechnicalsIO.setEma10(stockTechnicals.getEma().getAvg10());
		stockTechnicalsIO.setEma20(stockTechnicals.getEma().getAvg20());
		stockTechnicalsIO.setEma50(stockTechnicals.getEma().getAvg50());
		stockTechnicalsIO.setEma100(stockTechnicals.getEma().getAvg100());
		stockTechnicalsIO.setEma200(stockTechnicals.getEma().getAvg200());

		stockTechnicalsIO.setAdx(stockTechnicals.getAdx().getAdx());
		stockTechnicalsIO.setPlusDi(stockTechnicals.getAdx().getPlusDi());
		stockTechnicalsIO.setMinusDi(stockTechnicals.getAdx().getMinusDi());

		stockTechnicalsIO.setMacd(stockTechnicals.getMacd().getMacd());
		stockTechnicalsIO.setSignal(stockTechnicals.getMacd().getSignal());

		stockTechnicalsIO.setRsi(stockTechnicals.getRsi().getRsi());

		stockTechnicalsIO.setVolume(stockTechnicals.getVolume().getVolume());
		stockTechnicalsIO.setVolumeAvg5(stockTechnicals.getVolume().getAvg5());
		stockTechnicalsIO.setVolumeAvg20(stockTechnicals.getVolume().getAvg20());
		stockTechnicalsIO.setVolumeAvg50(stockTechnicals.getVolume().getAvg50());

		return stockTechnicalsIO;
	}


	private void updateTechnicalsTxn(StockTechnicals stockTechnicals, StockPriceIO stockPriceIO) {

		log.info("{} Updating transactional technicals ", stockPriceIO.getNseSymbol());

		StockTechnicalsIO stockTechnicalsIO = this.createStockTechnicalsIO(stockPriceIO, stockTechnicals);

		Stock stock = stockService.getStockByNseSymbol(stockTechnicalsIO.getNseSymbol());

		com.example.model.stocks.StockTechnicals stockTechnicalsTxn = stock.getTechnicals();

		if (stockTechnicalsTxn != null && (stockTechnicalsTxn.getBhavDate().isEqual(stockPriceIO.getTimestamp())
		|| stockTechnicalsTxn.getBhavDate().isAfter(stockPriceIO.getTimestamp()))) {
			log.info("{} Transactional technicals is already up to date for {}", stockTechnicalsIO.getNseSymbol(), stockPriceIO.getBhavDate());
		}else {

			if (stockTechnicalsTxn == null) {
				stockTechnicalsTxn = new
						com.example.model.stocks.StockTechnicals();
			}

			stockTechnicalsTxn.setStock(stock);
			stockTechnicalsTxn.setBhavDate(stockPriceIO.getTimestamp());


			stockTechnicalsTxn.setPrevSma5(stockTechnicalsTxn.getSma5());
			stockTechnicalsTxn.setPrevSma10(stockTechnicalsTxn.getSma10());
			stockTechnicalsTxn.setPrevSma20(stockTechnicalsTxn.getSma20());
			stockTechnicalsTxn.setPrevSma50(stockTechnicalsTxn.getSma50());
			stockTechnicalsTxn.setPrevSma100(stockTechnicalsTxn.getSma100());
			stockTechnicalsTxn.setPrevSma200(stockTechnicalsTxn.getSma200());


			stockTechnicalsTxn.setSma5(stockTechnicalsIO.getSma5());
			stockTechnicalsTxn.setSma10(stockTechnicalsIO.getSma10());
			stockTechnicalsTxn.setSma20(stockTechnicalsIO.getSma20());
			stockTechnicalsTxn.setSma50(stockTechnicalsIO.getSma50());
			stockTechnicalsTxn.setSma100(stockTechnicalsIO.getSma100());
			stockTechnicalsTxn.setSma200(stockTechnicalsIO.getSma200());


			stockTechnicalsTxn.setPrevEma5(stockTechnicalsTxn.getEma5());
			stockTechnicalsTxn.setPrevEma10(stockTechnicalsTxn.getEma10());
			stockTechnicalsTxn.setPrevEma20(stockTechnicalsTxn.getEma20());
			stockTechnicalsTxn.setPrevEma50(stockTechnicalsTxn.getEma50());
			stockTechnicalsTxn.setPrevEma100(stockTechnicalsTxn.getEma100());
			stockTechnicalsTxn.setPrevEma200(stockTechnicalsTxn.getEma200());


			stockTechnicalsTxn.setEma5(stockTechnicalsIO.getEma5());
			stockTechnicalsTxn.setEma10(stockTechnicalsIO.getEma10());
			stockTechnicalsTxn.setEma20(stockTechnicalsIO.getEma20());
			stockTechnicalsTxn.setEma50(stockTechnicalsIO.getEma50());
			stockTechnicalsTxn.setEma100(stockTechnicalsIO.getEma100());
			stockTechnicalsTxn.setEma200(stockTechnicalsIO.getEma200());


			stockTechnicalsTxn.setPrevAdx(stockTechnicalsTxn.getAdx());
			stockTechnicalsTxn.setAdx(stockTechnicalsIO.getAdx());


			stockTechnicalsTxn.setPrevPlusDi(stockTechnicalsTxn.getPlusDi());
			stockTechnicalsTxn.setPlusDi(stockTechnicalsIO.getPlusDi());


			stockTechnicalsTxn.setPrevMinusDi(stockTechnicalsTxn.getMinusDi());
			stockTechnicalsTxn.setMinusDi(stockTechnicalsIO.getMinusDi());


			stockTechnicalsTxn.setPrevRsi(stockTechnicalsTxn.getRsi());
			stockTechnicalsTxn.setRsi(stockTechnicalsIO.getRsi());


			stockTechnicalsTxn.setPrevMacd(stockTechnicalsTxn.getMacd());
			stockTechnicalsTxn.setMacd(stockTechnicalsIO.getMacd());


			stockTechnicalsTxn.setPrevSignal(stockTechnicalsTxn.getSignal());
			stockTechnicalsTxn.setSignal(stockTechnicalsIO.getSignal());


			stockTechnicalsTxn.setPrevVolume(stockTechnicalsTxn.getVolume());
			stockTechnicalsTxn.setVolumeAvg5Prev(stockTechnicalsTxn.getVolumeAvg5());
			stockTechnicalsTxn.setVolumeAvg20Prev(stockTechnicalsTxn.getVolumeAvg20());
			stockTechnicalsTxn.setPrevVolumeAvg50(stockTechnicalsTxn.getVolumeAvg50());

			stockTechnicalsTxn.setVolume(stockTechnicalsIO.getVolume());
			stockTechnicalsTxn.setVolumeAvg5(stockTechnicalsIO.getVolumeAvg5());
			stockTechnicalsTxn.setVolumeAvg20(stockTechnicalsIO.getVolumeAvg20());
			stockTechnicalsTxn.setVolumeAvg50(stockTechnicalsIO.getVolumeAvg50());


			stockTechnicalsTxn.setPrevPivotPoint(stockTechnicalsTxn.getPivotPoint());
			stockTechnicalsTxn.setPrevFirstResistance(stockTechnicalsTxn.getFirstResistance());
			stockTechnicalsTxn.setPrevSecondResistance(stockTechnicalsTxn.getSecondResistance());
			stockTechnicalsTxn.setPrevThirdResistance(stockTechnicalsTxn.getThirdResistance());
			stockTechnicalsTxn.setPrevFirstSupport(stockTechnicalsTxn.getFirstSupport());
			stockTechnicalsTxn.setPrevSecondSupport(stockTechnicalsTxn.getSecondSupport());
			stockTechnicalsTxn.setPrevThirdSupport(stockTechnicalsTxn.getThirdSupport());


			stockTechnicalsTxn.setPivotPoint(SupportAndResistanceUtil.pivotPoint(stockPriceIO.getHigh(), stockPriceIO.getLow(), stockPriceIO.getClose()));
			stockTechnicalsTxn.setFirstResistance(SupportAndResistanceUtil.firstResistance(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));
			stockTechnicalsTxn.setSecondResistance(SupportAndResistanceUtil.secondResistance(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));
			stockTechnicalsTxn.setThirdResistance(SupportAndResistanceUtil.thirdResistance(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));
			stockTechnicalsTxn.setFirstSupport(SupportAndResistanceUtil.firstSupport(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));
			stockTechnicalsTxn.setSecondSupport(SupportAndResistanceUtil.secondSupport(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));
			stockTechnicalsTxn.setThirdSupport(SupportAndResistanceUtil.thirdSupport(stockTechnicalsTxn.getPivotPoint(), stockPriceIO.getHigh(), stockPriceIO.getLow()));


			stockTechnicalsTxn.setLastModified(LocalDate.now());

			stockTechnicalsRepository.save(stockTechnicalsTxn);

			ResearchIO researchIO = new ResearchIO(stockPriceIO.getNseSymbol(), ResearchIO.ResearchType.TECHNICAL, ResearchIO.ResearchTrigger.BUY_SELL);

			queueService.send(researchIO, QueueConstants.MTQueue.RESEARCH_QUEUE);

			log.info("{} Updated transactional technicals ", stockPriceIO.getNseSymbol());
		}
	}


	private void setYearHighLow(StockPriceIO stockPriceIO, StockTechnicals stockTechnicals ){
		long start = System.currentTimeMillis();
		stockTechnicals.setYearLow(stockPriceIO.getYearLow());
		stockTechnicals.setYearHigh(stockPriceIO.getYearHigh());
		long end  = System.currentTimeMillis();
		log.info("Time took to calculate year high low {} {}", (end - start), stockPriceIO.getNseSymbol());
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

}
