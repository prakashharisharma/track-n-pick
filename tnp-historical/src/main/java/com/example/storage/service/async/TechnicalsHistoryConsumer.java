package com.example.storage.service.async;

import javax.jms.Session;

import org.apache.activemq.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.example.mq.constants.QueueConstants;
import com.example.mq.producer.QueueService;
import com.example.storage.model.Exponential;
import com.example.storage.model.Momentum;
import com.example.storage.model.MovingAverage;
import com.example.storage.model.Volume;
import com.example.storage.model.RSI;
import com.example.storage.model.Simple;
import com.example.storage.model.StochasticOscillator;
import com.example.storage.model.StockTechnicals;
import com.example.storage.model.Trend;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.repo.TechnicalsTemplate;
import com.example.storage.service.StorageService;
import com.example.util.FormulaService;
import com.example.util.io.model.StockPriceIO;
import com.example.util.io.model.StockTechnicalsIO;

@Component
public class TechnicalsHistoryConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TechnicalsHistoryConsumer.class);

	//@Autowired
	//private StorageService storageService;

	@Autowired
	private TechnicalsTemplate technicalsTemplate;

	@Autowired
	private FormulaService formulaService;

	@Autowired
	private QueueService queueService;

	@Autowired
	private PriceTemplate priceTemplate;

	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,Session session) throws InterruptedException {

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : " + stockPriceIO.getNseSymbol() + " : START");
		StockTechnicals prevStockTechnicals = null;
		
		// Get Technicals
		try {
			prevStockTechnicals = technicalsTemplate.getPrevTechnicals(stockPriceIO.getNseSymbol(), 1);
		}catch(Exception e) {
			LOGGER.error(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : Error in Getting PREV Technicals "
					+ stockPriceIO.getNseSymbol());
		}
		
		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : PREV TECHNICALS "+ prevStockTechnicals);

		Volume volume = this.getVolume(stockPriceIO, prevStockTechnicals);

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : NEW PRICEVOLUME " + volume);

		Trend trend = this.getTrend(stockPriceIO, prevStockTechnicals);

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : NEW MA " + trend);
		
		Momentum momentum = this.getMomentum(stockPriceIO, prevStockTechnicals);

		StockTechnicals stockTechnicals = new StockTechnicals(stockPriceIO.getNseSymbol(), stockPriceIO.getBhavDate(),volume, trend, momentum);

		technicalsTemplate.create(stockTechnicals);

		Thread.sleep(70);

		//
		StockTechnicalsIO stockTechnicalsIO = this.createStockTechnicalsIO(stockPriceIO, prevStockTechnicals, volume,trend,momentum);

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : "+ stockPriceIO.getNseSymbol() + " : Queuing to update Transactional Technicals.. ");
		// Save
		queueService.send(stockTechnicalsIO, QueueConstants.MTQueue.UPDATE_TECHNICALS_TXN_QUEUE);

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : "+ stockPriceIO.getNseSymbol() + " : START");
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

		double currentAverageGain = priceTemplate.getAverageGain(stockPriceIO.getNseSymbol(), 14);

		double currentAverageLoss = priceTemplate.getAverageLoss(stockPriceIO.getNseSymbol(), 14);

		double rs = formulaService.calculateRs(currentAverageGain, currentAverageLoss);

		double rsi = formulaService.calculateRsi(rs);

		double currentGain = this.getCurrentGain(stockPriceIO);

		double currentLoss = this.getCurrentLoss(stockPriceIO);

		double prevAverageGain = 0.00;
		double prevAverageLoss = 0.00;

		// TO be changes to get from Prev RSI
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

		RSI rsiObj = new RSI(rs, rsi, smoothedRs, smoothedRsi,currentAverageGain,currentAverageLoss);

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

		double sma50 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(), 50);

		double sma100 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(), 100);

		double sma200 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(), 200);

		Simple simple = new Simple(sma50, sma100, sma200);

		double close = stockPriceIO.getClose();

		double prevEma50 = close;
		double prevEma100 = close;
		double prevEma200 = close;

		if (prevStockTechnicals != null) {

			if (prevStockTechnicals.getTrend() != null) {
				if (prevStockTechnicals.getTrend().getMovingAverage() != null) {
					if (prevStockTechnicals.getTrend().getMovingAverage().getExponential() != null) {

						Double avg50 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg50();
						Double avg100 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg100();
						Double avg200 = prevStockTechnicals.getTrend().getMovingAverage().getExponential().getAvg200();

						prevEma50 = avg50 != null ? avg50 : close;
						prevEma100 = avg100 != null ? avg100 : close;
						prevEma200 = avg200 != null ? avg200 : close;
					}
				}
			}
		}

		double ema50 = formulaService.calculateEMA(close, prevEma50, 50);

		double ema100 = formulaService.calculateEMA(close, prevEma100, 100);

		double ema200 = formulaService.calculateEMA(close, prevEma200, 200);

		Exponential exponential = new Exponential(ema50, ema100, ema200);

		MovingAverage movingAverage = new MovingAverage(simple, exponential);

		return movingAverage;
	}

	private Volume getVolume(StockPriceIO stockPriceIO, StockTechnicals prevStockTechnicals) {
		long prevOBV = 1;

		if (prevStockTechnicals != null) {

			if (prevStockTechnicals.getVolume() != null) {

				if (prevStockTechnicals.getVolume().getObv() != null) {
					prevOBV = prevStockTechnicals.getVolume().getObv();
				}
			}
		}

		long OBV = formulaService.calculateOBV(prevOBV, stockPriceIO.getPrevClose(), stockPriceIO.getClose(),
				stockPriceIO.getTottrdqty());

		double roc = formulaService.calculateRateOfChange(OBV, prevOBV);

		Long volume = stockPriceIO.getTottrdqty();

		double voumeChange = 0.00;

		Long avgVolume10 = technicalsTemplate.getAverageVolume(stockPriceIO.getNseSymbol(), 10);
		
		Long avgVolume30 = technicalsTemplate.getAverageVolume(stockPriceIO.getNseSymbol(), 30);

		Volume priceVolume = new Volume(OBV, roc, volume, voumeChange, avgVolume10,avgVolume30);

		return priceVolume;
	}

	private Trend getTrend(StockPriceIO stockPriceIO, StockTechnicals prevStockTechnicals) {

		MovingAverage movingAverage = this.getMovingAverage(stockPriceIO, prevStockTechnicals);

		Trend trend = new Trend(movingAverage);

		return trend;
	}

	private Momentum getMomentum(StockPriceIO stockPriceIO, StockTechnicals prevStockTechnicals) {
		RSI rsiObj = this.getRSI(stockPriceIO, prevStockTechnicals);

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : NEW RS " + rsiObj);

		StochasticOscillator stochasticOscillator = this.getStochasticOscillator(stockPriceIO, prevStockTechnicals);

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase()
				+ " : NEW StochasticOscillator " + stochasticOscillator);
		
		Momentum momentum = new Momentum(rsiObj, stochasticOscillator);
		
		return momentum;
	}
	
	private StockTechnicalsIO createStockTechnicalsIO(StockPriceIO stockPriceIO, StockTechnicals prevStockTechnicals, Volume volume,
			Trend trend, Momentum momentum) {
		StockTechnicalsIO stockTechnicalsIO = new StockTechnicalsIO();
		stockTechnicalsIO.setNseSymbol(stockPriceIO.getNseSymbol());


		stockTechnicalsIO.setSma50(trend.getMovingAverage().getSimple().getAvg50());
		stockTechnicalsIO.setSma100(trend.getMovingAverage().getSimple().getAvg100());
		stockTechnicalsIO.setSma200(trend.getMovingAverage().getSimple().getAvg200());

		stockTechnicalsIO.setPrevSma50(prevStockTechnicals.getTrend().getMovingAverage().getSimple().getAvg50());
		stockTechnicalsIO.setPrevSma100(prevStockTechnicals.getTrend().getMovingAverage().getSimple().getAvg100());
		stockTechnicalsIO.setPrevSma200(prevStockTechnicals.getTrend().getMovingAverage().getSimple().getAvg200());

		stockTechnicalsIO.setRsi(momentum.getRsi().getSmoothedRsi());
		//stockTechnicalsIO.setRsi(0.00);

		stockTechnicalsIO.setSok(momentum.getStochasticOscillator().getK());
		stockTechnicalsIO.setSod(momentum.getStochasticOscillator().getD());

		stockTechnicalsIO.setObv(volume.getObv());

		stockTechnicalsIO.setRocv(volume.getRoc());

		stockTechnicalsIO.setVolume(volume.getVolume());
		stockTechnicalsIO.setAvgVolume(volume.getAvgVolume10());
		
		return stockTechnicalsIO;
	}

}
