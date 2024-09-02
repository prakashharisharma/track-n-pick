package com.example.mt.service.async;

import java.time.LocalDate;

import javax.jms.Session;

import com.example.storage.model.*;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.repo.TechnicalsTemplate;
import com.example.util.FormulaService;
import com.example.util.io.model.StockPriceIO;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.Message;
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
import com.example.service.PortfolioService;
import com.example.service.ResearchLedgerFundamentalService;
import com.example.service.StockService;
import com.example.util.io.model.StockTechnicalsIO;

@Component
@Slf4j
public class TechnicalsUpdateConsumer {

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


	@JmsListener(destination = QueueConstants.MTQueue.UPDATE_TECHNICALS_TXN_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers,
							   Message message, Session session) throws InterruptedException {

		log.info("{} Starting technicals update.", stockPriceIO.getNseSymbol());

		this.processTechnicalsUpdate(stockPriceIO);

		log.info("{} Completed technicals update.", stockPriceIO.getNseSymbol());
	}

	public void processTechnicalsUpdate(StockPriceIO stockPriceIO){

		this.updateTechnicalsHistory(stockPriceIO);

		queueService.send(stockPriceIO, QueueConstants.MTQueue.UPDATE_FACTOR_TXN_QUEUE);

	}

	public void updateTechnicalsHistory(StockPriceIO stockPriceIO){

		log.info("{} Updating technicals history.", stockPriceIO.getNseSymbol());

		StockTechnicals prevStockTechnicals = null;

		try {

			StockTechnicals existingStockTechnicals = technicalsTemplate.getForDate(stockPriceIO.getNseSymbol(), stockPriceIO.getTimestamp());

			if(existingStockTechnicals!=null)
			{
				log.info("{} Technicals already exist for the bhav date {}", stockPriceIO.getNseSymbol(), stockPriceIO.getBhavDate());
			}else {

				prevStockTechnicals = technicalsTemplate.getPrevTechnicals(stockPriceIO.getNseSymbol(), 1);

				Volume volume = this.getVolume(stockPriceIO, prevStockTechnicals);

				Trend trend = this.getTrend(stockPriceIO, prevStockTechnicals);

				Momentum momentum = this.getMomentum(stockPriceIO, prevStockTechnicals, trend.getMovingAverage().getExponential());

				StockTechnicals stockTechnicals = new StockTechnicals(stockPriceIO.getNseSymbol(), stockPriceIO.getBhavDate(),
						volume, trend, momentum);

				technicalsTemplate.create(stockTechnicals);

				StockTechnicalsIO stockTechnicalsIO = this.createStockTechnicalsIO(stockPriceIO, prevStockTechnicals, volume,
						trend, momentum);

				this.updateTechnicalsTxn(stockTechnicalsIO, stockPriceIO);


			}
		} catch (Exception e) {

			log.error(" {} Error while updating echnicals ", stockPriceIO.getNseSymbol(), e);

		}



		log.info("{} Updated technicals history.", stockPriceIO.getNseSymbol());
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

	private RSI getRSI(StockPriceIO stockPriceIO, StockTechnicals prevStockTechnicals) {

		log.info("{} Calculating RSI.", stockPriceIO.getNseSymbol());

		double currentAverageGain = priceTemplate.getAverageGain(stockPriceIO.getNseSymbol(), 14);

		double currentAverageLoss = priceTemplate.getAverageLoss(stockPriceIO.getNseSymbol(), 14);

		double rs = formulaService.calculateRs(currentAverageGain, currentAverageLoss);

		double rsi = formulaService.calculateRsi(rs);

		double currentGain = this.getCurrentGain(stockPriceIO);

		double currentLoss = this.getCurrentLoss(stockPriceIO);

		double prevAverageGain = 0.00;
		double prevAverageLoss = 0.00;

		if (prevStockTechnicals != null) {
			if (prevStockTechnicals.getMomentum() != null) {
				if (prevStockTechnicals.getMomentum().getRsi() != null) {

					prevAverageGain = prevStockTechnicals.getMomentum().getRsi().getAvgGain() != null
							? prevStockTechnicals.getMomentum().getRsi().getAvgGain()
							: 0.00;
					prevAverageLoss = prevStockTechnicals.getMomentum().getRsi().getAvgLoss() != null
							? prevStockTechnicals.getMomentum().getRsi().getAvgLoss()
							: 0.00;
				}
			}
		}

		double smoothedRs = formulaService.calculateSmoothedRs(prevAverageGain, prevAverageLoss, currentGain,
				currentLoss);

		double smoothedRsi = formulaService.calculateSmoothedRsi(smoothedRs);

		RSI rsiObj = new RSI(rs, rsi, smoothedRs, smoothedRsi, currentAverageGain, currentAverageLoss);

		log.info("{} Calculated RSI {} ", stockPriceIO.getNseSymbol(), rsi);

		return rsiObj;
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

	private MovingAverage getMovingAverage(StockPriceIO stockPriceIO, StockTechnicals prevStockTechnicals) {

		log.info("{} Calculating moving average.", stockPriceIO.getNseSymbol());

		double close = stockPriceIO.getClose();

		double sma5 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(), close, 5);

		double sma10 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(), close, 10);

		double sma20 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(), close, 20);

		double sma50 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(), close, 50);

		double sma100 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(), close, 100);

		double sma200 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(),close, 200);

		Simple simple = new Simple(sma5, sma10, sma20, sma50, sma100, sma200);


		double prevEma5 = close;
		double prevEma10 = close;
		double prevEma12 = close;
		double prevEma20 = close;
		double prevEma26 = close;
		double prevEma50 = close;
		double prevEma100 = close;
		double prevEma200 = close;

		if (prevStockTechnicals != null) {

			if (prevStockTechnicals.getTrend() != null) {
				if (prevStockTechnicals.getTrend().getMovingAverage() != null) {
					if (prevStockTechnicals.getTrend().getMovingAverage().getExponential() != null) {

						Double avg5 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg5();
						Double avg10 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg10();
						Double avg12 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg12();
						Double avg20 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg20();
						Double avg26 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg26();
						Double avg50 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg50();
						Double avg100 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg100();
						Double avg200 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg200();

						prevEma5 = avg5 != null ? avg5 : close;

						prevEma10 = avg10 != null ? avg10 : close;
						prevEma12 = avg12 != null ? avg12 : close;
						prevEma20 = avg20 != null ? avg20 : close;
						prevEma26 = avg26 != null ? avg26 : close;
						prevEma50 = avg50 != null ? avg50 : close;
						prevEma100 = avg100 != null ? avg100 : close;
						prevEma200 = avg200 != null ? avg200 : close;
					}
				}
			}
		}

		double ema5 = formulaService.calculateEMA(close, prevEma5, 5);

		double ema10 = formulaService.calculateEMA(close, prevEma10, 10);
		double ema12 = formulaService.calculateEMA(close, prevEma12, 12);
		double ema20 = formulaService.calculateEMA(close, prevEma20, 20);
		double ema26 = formulaService.calculateEMA(close, prevEma26, 26);
		double ema50 = formulaService.calculateEMA(close, prevEma50, 50);

		double ema100 = formulaService.calculateEMA(close, prevEma100, 100);

		double ema200 = formulaService.calculateEMA(close, prevEma200, 200);

		Exponential exponential = new Exponential(ema5, ema10, ema20, ema50, ema100, ema200);

		exponential.setAvg12(ema12);
		exponential.setAvg26(ema26);

		MovingAverage movingAverage = new MovingAverage(simple, exponential);

		log.info("{} Calculated moving average.", stockPriceIO.getNseSymbol());

		return movingAverage;
	}

	private Volume getVolume(StockPriceIO stockPriceIO, StockTechnicals prevStockTechnicals) {

		log.info("{} Calculating volume ", stockPriceIO.getNseSymbol());

		if(stockPriceIO.getTottrdqty() < 1000) {
			stockPriceIO.setTottrdqty(1);
		}else {
			long volume = stockPriceIO.getTottrdqty() / 1000;

			stockPriceIO.setTottrdqty(volume);
		}

		long prevOBV = 1;
		double volumeChange = 0.00;

		if (prevStockTechnicals != null) {

			if (prevStockTechnicals.getVolume() != null) {

				if (prevStockTechnicals.getVolume().getObv() != null) {
					prevOBV = prevStockTechnicals.getVolume().getObv();
					volumeChange = formulaService.calculateRateOfChange(stockPriceIO.getTottrdqty(), prevStockTechnicals.getVolume().getVolume());
				}
			}
		}

		long OBV = formulaService.calculateOBV(prevOBV, stockPriceIO.getPrevClose(), stockPriceIO.getClose(),
				stockPriceIO.getTottrdqty());

		double roc = formulaService.calculateRateOfChange(OBV, prevOBV);

		Long volume = stockPriceIO.getTottrdqty();



		Long avgVolume10 = technicalsTemplate.getAverageVolume(stockPriceIO.getNseSymbol(), 10);

		Long avgVolume30 = technicalsTemplate.getAverageVolume(stockPriceIO.getNseSymbol(), 30);

		Volume priceVolume = new Volume(OBV, roc, volume, volumeChange, avgVolume10, avgVolume30);

		priceVolume.setVoumeChange(volumeChange);

		log.info("{} Calculated volume ", stockPriceIO.getNseSymbol());

		return priceVolume;
	}

	private Trend getTrend(StockPriceIO stockPriceIO, StockTechnicals prevStockTechnicals) {

		MovingAverage movingAverage = this.getMovingAverage(stockPriceIO, prevStockTechnicals);

		Trend trend = new Trend(movingAverage);

		return trend;
	}

	private Momentum getMomentum(StockPriceIO stockPriceIO, StockTechnicals prevStockTechnicals, Exponential exponential) {
		RSI rsiObj = this.getRSI(stockPriceIO, prevStockTechnicals);

		StochasticOscillator stochasticOscillator = this.getStochasticOscillator(stockPriceIO, prevStockTechnicals);

		log.trace("{} New StochasticOscillator {}", stockPriceIO.getNseSymbol(),  stochasticOscillator);

		Macd macd = this.calculateMacd(exponential, prevStockTechnicals);

		Momentum momentum = new Momentum(macd, rsiObj, stochasticOscillator);

		return momentum;
	}

	private Macd calculateMacd(Exponential exponential, StockTechnicals prevStockTechnicals){

		double macd =  exponential.getAvg12() - exponential.getAvg26();

		double prevSignal = macd;

		if(prevStockTechnicals!=null && prevStockTechnicals.getMomentum()!=null && prevStockTechnicals.getMomentum().getMacd()!=null) {
			Double prevMacd = prevStockTechnicals.getMomentum().getMacd().getMacd();
			prevSignal = prevMacd != null ? prevMacd : macd;
		}

		double signal = formulaService.calculateEMA(macd, prevSignal, 9);

		return new Macd(macd, signal);
	}

	private StockTechnicalsIO createStockTechnicalsIO(StockPriceIO stockPriceIO, StockTechnicals prevStockTechnicals,
													  Volume volume, Trend trend, Momentum momentum) {
		StockTechnicalsIO stockTechnicalsIO = new StockTechnicalsIO();

		stockTechnicalsIO.setNseSymbol(stockPriceIO.getNseSymbol());

		if (trend.getMovingAverage() != null) {
			stockTechnicalsIO.setSma5(trend.getMovingAverage().getSimple().getAvg5());
			stockTechnicalsIO.setSma10(trend.getMovingAverage().getSimple().getAvg10());
			stockTechnicalsIO.setSma20(trend.getMovingAverage().getSimple().getAvg20());

			stockTechnicalsIO.setSma50(trend.getMovingAverage().getSimple().getAvg50());
			stockTechnicalsIO.setSma100(trend.getMovingAverage().getSimple().getAvg100());
			stockTechnicalsIO.setSma200(trend.getMovingAverage().getSimple().getAvg200());

			stockTechnicalsIO.setEma5(trend.getMovingAverage().getExponential().getAvg5());
			stockTechnicalsIO.setEma10(trend.getMovingAverage().getExponential().getAvg10());
			stockTechnicalsIO.setEma20(trend.getMovingAverage().getExponential().getAvg20());
			stockTechnicalsIO.setEma50(trend.getMovingAverage().getExponential().getAvg50());
			stockTechnicalsIO.setEma100(trend.getMovingAverage().getExponential().getAvg100());
			stockTechnicalsIO.setEma200(trend.getMovingAverage().getExponential().getAvg200());


			if (prevStockTechnicals != null) {
				stockTechnicalsIO
						.setPrevSma5(prevStockTechnicals.getTrend().getMovingAverage().getSimple().getAvg5());
				stockTechnicalsIO
						.setPrevSma10(prevStockTechnicals.getTrend().getMovingAverage().getSimple().getAvg10());
				stockTechnicalsIO
						.setPrevSma20(prevStockTechnicals.getTrend().getMovingAverage().getSimple().getAvg20());
				stockTechnicalsIO
						.setPrevSma50(prevStockTechnicals.getTrend().getMovingAverage().getSimple().getAvg50());
				stockTechnicalsIO
						.setPrevSma100(prevStockTechnicals.getTrend().getMovingAverage().getSimple().getAvg100());
				stockTechnicalsIO
						.setPrevSma200(prevStockTechnicals.getTrend().getMovingAverage().getSimple().getAvg200());

				stockTechnicalsIO
						.setPrevEma5(prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg5());
				stockTechnicalsIO
						.setPrevEma10(prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg10());
				stockTechnicalsIO
						.setPrevEma20(prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg20());
				stockTechnicalsIO
						.setPrevEma50(prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg50());
				stockTechnicalsIO
						.setPrevEma100(prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg100());
				stockTechnicalsIO
						.setPrevEma200(prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg200());

				if(prevStockTechnicals.getMomentum()!=null && prevStockTechnicals.getMomentum().getMacd()!=null) {
					stockTechnicalsIO.setPrevMacd(prevStockTechnicals.getMomentum().getMacd().getMacd());
					stockTechnicalsIO.setPrevSignalLine(prevStockTechnicals.getMomentum().getMacd().getSignal());
				}else {
					stockTechnicalsIO.setPrevMacd(momentum.getMacd().getMacd());
					stockTechnicalsIO.setPrevSignalLine(momentum.getMacd().getMacd());
				}

			} else {
				stockTechnicalsIO.setPrevSma5(stockPriceIO.getClose());
				stockTechnicalsIO.setPrevSma10(stockPriceIO.getClose());
				stockTechnicalsIO.setPrevSma20(stockPriceIO.getClose());
				stockTechnicalsIO.setPrevSma50(stockPriceIO.getClose());
				stockTechnicalsIO.setPrevSma100(stockPriceIO.getClose());
				stockTechnicalsIO.setPrevSma200(stockPriceIO.getClose());

				stockTechnicalsIO.setPrevEma5(stockPriceIO.getClose());
				stockTechnicalsIO.setPrevEma10(stockPriceIO.getClose());
				stockTechnicalsIO.setPrevEma20(stockPriceIO.getClose());
				stockTechnicalsIO.setPrevEma50(stockPriceIO.getClose());
				stockTechnicalsIO.setPrevEma100(stockPriceIO.getClose());
				stockTechnicalsIO.setPrevEma200(stockPriceIO.getClose());

				stockTechnicalsIO.setPrevSignalLine(stockPriceIO.getClose());
			}
		} else {
			stockTechnicalsIO.setSma5(stockPriceIO.getClose());
			stockTechnicalsIO.setSma10(stockPriceIO.getClose());
			stockTechnicalsIO.setSma20(stockPriceIO.getClose());
			stockTechnicalsIO.setSma50(stockPriceIO.getClose());
			stockTechnicalsIO.setSma100(stockPriceIO.getClose());
			stockTechnicalsIO.setSma200(stockPriceIO.getClose());

			stockTechnicalsIO.setPrevSma5(stockPriceIO.getClose());
			stockTechnicalsIO.setPrevSma10(stockPriceIO.getClose());
			stockTechnicalsIO.setPrevSma20(stockPriceIO.getClose());
			stockTechnicalsIO.setPrevSma50(stockPriceIO.getClose());
			stockTechnicalsIO.setPrevSma100(stockPriceIO.getClose());
			stockTechnicalsIO.setPrevSma200(stockPriceIO.getClose());

			stockTechnicalsIO.setEma5(stockPriceIO.getClose());
			stockTechnicalsIO.setEma10(stockPriceIO.getClose());
			stockTechnicalsIO.setEma20(stockPriceIO.getClose());
			stockTechnicalsIO.setEma50(stockPriceIO.getClose());
			stockTechnicalsIO.setEma100(stockPriceIO.getClose());
			stockTechnicalsIO.setEma200(stockPriceIO.getClose());
			stockTechnicalsIO.setSignalLine(stockPriceIO.getClose());

			stockTechnicalsIO.setPrevEma5(stockPriceIO.getClose());
			stockTechnicalsIO.setPrevEma10(stockPriceIO.getClose());
			stockTechnicalsIO.setPrevEma20(stockPriceIO.getClose());
			stockTechnicalsIO.setPrevEma50(stockPriceIO.getClose());
			stockTechnicalsIO.setPrevEma100(stockPriceIO.getClose());
			stockTechnicalsIO.setPrevEma200(stockPriceIO.getClose());

			stockTechnicalsIO.setPrevSignalLine(stockPriceIO.getClose());
		}

		if (momentum != null) {
			if(momentum.getRsi().getSmoothedRsi()!=null) {
				stockTechnicalsIO.setRsi(momentum.getRsi().getSmoothedRsi());
			}else {
				stockTechnicalsIO.setRsi(1.0);
			}
			stockTechnicalsIO.setSok(momentum.getStochasticOscillator().getK());
			stockTechnicalsIO.setSod(momentum.getStochasticOscillator().getD());
			stockTechnicalsIO.setMacd(momentum.getMacd().getMacd());
			stockTechnicalsIO.setSignalLine(momentum.getMacd().getSignal());

			if(prevStockTechnicals!=null && prevStockTechnicals.getMomentum().getRsi()!=null){
				stockTechnicalsIO.setPrevRsi(prevStockTechnicals.getMomentum().getRsi().getRsi());
			}

		}else {
			stockTechnicalsIO.setRsi(1.00);
			stockTechnicalsIO.setSok(1.00);
			stockTechnicalsIO.setSod(1.00);
		}
		if (volume != null) {
			stockTechnicalsIO.setObv(volume.getObv());
			if(volume.getRoc()!=null) {
				stockTechnicalsIO.setRocv(volume.getRoc());
			}else {
				stockTechnicalsIO.setRocv(1.0);
			}
			if(volume.getVolume()!=null) {

				stockTechnicalsIO.setVolume(volume.getVolume());

			}else {
				stockTechnicalsIO.setVolume(1);
			}
			stockTechnicalsIO.setAvgVolume(volume.getAvgVolume10());
		}else {
			stockTechnicalsIO.setObv(1);

			stockTechnicalsIO.setRocv(1);

			stockTechnicalsIO.setVolume(1);
			stockTechnicalsIO.setAvgVolume(1);
		}

		return stockTechnicalsIO;
	}


	private void updateTechnicalsTxn(StockTechnicalsIO stockTechnicalsIO, StockPriceIO stockPriceIO) {

		log.info("{} Updating transactional technicals ", stockPriceIO.getNseSymbol());

		Stock stock = stockService.getStockByNseSymbol(stockTechnicalsIO.getNseSymbol());


		com.example.model.stocks.StockTechnicals stockTechnicals = stock.getTechnicals();

		if (stockTechnicals != null && (stockTechnicals.getBhavDate().isEqual(stockPriceIO.getTimestamp())
		|| stockTechnicals.getBhavDate().isAfter(stockPriceIO.getTimestamp()))) {
			log.info("{} Transactional technicals is already up to date", stockTechnicalsIO.getNseSymbol());
		}else {

			if (stockTechnicals == null) {
				stockTechnicals = new
						com.example.model.stocks.StockTechnicals();
			}

			stockTechnicals.setStock(stock);
			stockTechnicals.setBhavDate(stockPriceIO.getTimestamp());

			stockTechnicals.setSma5(stockTechnicalsIO.getSma5());
			stockTechnicals.setSma10(stockTechnicalsIO.getSma10());

			stockTechnicals.setSma20(stockTechnicalsIO.getSma20());

			stockTechnicals.setSma50(stockTechnicalsIO.getSma50());
			stockTechnicals.setSma100(stockTechnicalsIO.getSma100());
			stockTechnicals.setSma200(stockTechnicalsIO.getSma200());

			stockTechnicals.setPrevSma5(stockTechnicalsIO.getPrevSma5());
			stockTechnicals.setPrevSma10(stockTechnicalsIO.getPrevSma10());
			stockTechnicals.setPrevSma20(stockTechnicalsIO.getPrevSma20());
			stockTechnicals.setPrevSma50(stockTechnicalsIO.getPrevSma50());
			stockTechnicals.setPrevSma100(stockTechnicalsIO.getPrevSma100());
			stockTechnicals.setPrevSma200(stockTechnicalsIO.getPrevSma200());

			stockTechnicals.setEma5(stockTechnicalsIO.getEma5());
			stockTechnicals.setEma10(stockTechnicalsIO.getEma10());
			stockTechnicals.setEma20(stockTechnicalsIO.getEma20());
			stockTechnicals.setEma50(stockTechnicalsIO.getEma50());
			stockTechnicals.setEma100(stockTechnicalsIO.getEma100());
			stockTechnicals.setEma200(stockTechnicalsIO.getEma200());

			stockTechnicals.setPrevEma5(stockTechnicalsIO.getPrevEma5());
			stockTechnicals.setPrevEma10(stockTechnicalsIO.getPrevEma10());
			stockTechnicals.setPrevEma20(stockTechnicalsIO.getPrevEma20());
			stockTechnicals.setPrevEma50(stockTechnicalsIO.getPrevEma50());
			stockTechnicals.setPrevEma100(stockTechnicalsIO.getPrevEma100());
			stockTechnicals.setPrevEma200(stockTechnicalsIO.getPrevEma200());


			stockTechnicals.setRsi(stockTechnicalsIO.getRsi());
			stockTechnicals.setPrevRsi(stockTechnicalsIO.getPrevRsi());

			stockTechnicals.setMacd(stockTechnicalsIO.getMacd());
			stockTechnicals.setPrevMacd(stockTechnicalsIO.getPrevMacd());

			stockTechnicals.setSignal(stockTechnicalsIO.getSignalLine());
			stockTechnicals.setPrevSignal(stockTechnicalsIO.getPrevSignalLine());

			stockTechnicals.setLastModified(LocalDate.now());

			stockTechnicals.setSok(stockTechnicalsIO.getSok());
			stockTechnicals.setSod(stockTechnicalsIO.getSod());
			stockTechnicals.setObv(stockTechnicalsIO.getObv());
			stockTechnicals.setRocv(stockTechnicalsIO.getRocv());

			stockTechnicals.setVolume(stockTechnicalsIO.getVolume());

			stockTechnicals.setAvgVolume(stockTechnicalsIO.getAvgVolume());

			stockTechnicalsRepository.save(stockTechnicals);



			log.info("{} Updated transactional technicals ", stockPriceIO.getNseSymbol());
		}
	}

}
