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
import com.example.storage.model.Momentum;
import com.example.storage.model.MovingAverage;
import com.example.storage.model.PriceVolume;
import com.example.storage.model.RSI;
import com.example.storage.model.StochasticOscillator;
import com.example.storage.model.StockTechnicals;
import com.example.storage.repo.PriceTemplate;
import com.example.storage.repo.TechnicalsTemplate;
import com.example.storage.service.StorageService;
import com.example.util.FormulaService;
import com.example.util.io.model.StockPriceIO;
import com.example.util.io.model.StockTechnicalsIO;

@Component
public class TechnicalsHistoryConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TechnicalsHistoryConsumer.class);

	@Autowired
	private StorageService storageService;

	@Autowired
	private TechnicalsTemplate technicalsTemplate;

	@Autowired
	private FormulaService formulaService;

	@Autowired
	private QueueService queueService;

	@Autowired
	private PriceTemplate priceTemplate;

	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : "
				+ stockPriceIO.getNseSymbol() + " : START");

		// Get Technicals

		double currentAverageGain = priceTemplate.getAverageGain(stockPriceIO.getNseSymbol(), 14);

		double currentAverageLoss = priceTemplate.getAverageLoss(stockPriceIO.getNseSymbol(), 14);

		double rs = formulaService.calculateRs(currentAverageGain, currentAverageLoss);

		double rsi = formulaService.calculateRsi(rs);

		StockTechnicals prevStockTechnicals = technicalsTemplate.getPrevTechnicals(stockPriceIO.getNseSymbol());

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : PREV TECHNICALS "
				+ prevStockTechnicals);

		double ema20;
		double ema50;
		double ema100;
		double ema200;

		double sma20;
		double sma50;
		double sma100;
		double sma200;

		double prevSma20;
		double prevSma50;
		double prevSma100;
		double prevSma200;

		double prevAverageGain = 0.00;
		double prevAverageLoss = 0.00;
		if (prevStockTechnicals != null) {
			prevAverageGain = prevStockTechnicals.getAvgGain();

			prevAverageLoss = prevStockTechnicals.getAvgLoss();
			if (prevStockTechnicals.getMovingAverage() != null) {
				ema20 = prevStockTechnicals.getMovingAverage().getEma20();
				ema50 = prevStockTechnicals.getMovingAverage().getEma50();
				ema100 = prevStockTechnicals.getMovingAverage().getEma100();
				ema200 = prevStockTechnicals.getMovingAverage().getEma200();

				prevSma20 = prevStockTechnicals.getMovingAverage().getSma21();
				prevSma50 = prevStockTechnicals.getMovingAverage().getSma50();
				prevSma100 = prevStockTechnicals.getMovingAverage().getSma100();
				prevSma200 = prevStockTechnicals.getMovingAverage().getSma200();

			} else {
				ema20 = stockPriceIO.getClose();
				ema50 = stockPriceIO.getClose();
				ema100 = stockPriceIO.getClose();
				ema200 = stockPriceIO.getClose();

				prevSma20 = stockPriceIO.getClose();
				prevSma50 = stockPriceIO.getClose();
				prevSma100 = stockPriceIO.getClose();
				prevSma200 = stockPriceIO.getClose();

			}
		} else {
			ema20 = stockPriceIO.getClose();
			ema50 = stockPriceIO.getClose();
			ema100 = stockPriceIO.getClose();
			ema200 = stockPriceIO.getClose();

			prevSma20 = stockPriceIO.getClose();
			prevSma50 = stockPriceIO.getClose();
			prevSma100 = stockPriceIO.getClose();
			prevSma200 = stockPriceIO.getClose();
		}

		double currentGain = 0.0;

		double currentLoss = 0.0;

		double change = stockPriceIO.getClose() - stockPriceIO.getPrevClose();

		if (change > 0) {
			currentGain = change;
		} else {
			currentLoss = change;
		}

		// Start Calculate from Second Day
		double smoothedRs = formulaService.calculateSmoothedRs(prevAverageGain, prevAverageLoss, currentGain,
				currentLoss);

		double smoothedRsi = formulaService.calculateSmoothedRsi(smoothedRs);

		//

		sma20 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(), 20);

		sma50 = priceTemplate.getAveragePrice(stockPriceIO.getNseSymbol(), 50);

		sma100 = storageService.getSMA(stockPriceIO.getNseSymbol(), 100);

		sma200 = storageService.getSMA(stockPriceIO.getNseSymbol(), 200);

		double close = stockPriceIO.getClose();

		ema20 = sma20;
		ema20 = formulaService.calculateEMA(close, ema20, 20);
	
		ema100 = formulaService.calculateEMA(close, ema100, 100);
		ema200 = formulaService.calculateEMA(close, ema200, 200);

		MovingAverage movingAverage = new MovingAverage(sma20, sma50, sma100, sma200, ema20, ema50, ema100, ema200);

		LOGGER.trace(
				QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : NEW MA " + movingAverage);

		RSI rsiObj = new RSI(rs, rsi, smoothedRs, smoothedRsi);

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : NEW RS " + rsiObj);

		double stochasticOscillatorValue = formulaService.calculateStochasticOscillatorValue(stockPriceIO.getClose(),
				stockPriceIO.getLow14(), stockPriceIO.getHigh14());

		double avg2D = 0.00;

		double avg3D = 0.00;

		try {

			avg2D = technicalsTemplate.getAverageStochasticOscillatorK(stockPriceIO.getNseSymbol(), 2);

			// Thread.sleep(50);

			avg3D = formulaService.calculateAverage(avg2D, stochasticOscillatorValue);

		} catch (Exception e) {
			avg2D = 0.00;

			avg3D = 0.00;
		}

		StochasticOscillator stochasticOscillator = new StochasticOscillator(stochasticOscillatorValue, avg3D);

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase()
				+ " : NEW StochasticOscillator " + stochasticOscillator);

		long prevOBV = 1;

		if (prevStockTechnicals != null) {

			if (prevStockTechnicals.getIndicator() != null) {
				if (prevStockTechnicals.getIndicator().getPriceVolume() != null) {
					if (prevStockTechnicals.getIndicator().getPriceVolume().getObv() != null) {
						prevOBV = prevStockTechnicals.getIndicator().getPriceVolume().getObv();
					}
				}
			}
		}

		long OBV = formulaService.calculateOBV(prevOBV, stockPriceIO.getPrevClose(), stockPriceIO.getClose(),
				stockPriceIO.getTottrdqty());

		double roc = formulaService.calculateRateOfChange(OBV, prevOBV);

		PriceVolume priceVolume = new PriceVolume(OBV, roc);

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : NEW PRICEVOLUME "
				+ priceVolume);

		Momentum indicator = new Momentum(rsiObj, stochasticOscillator, priceVolume);

		StockTechnicals stockTechnicals = new StockTechnicals(stockPriceIO.getNseSymbol(), stockPriceIO.getBhavDate(),
				currentAverageGain, currentAverageLoss, movingAverage, indicator);

		technicalsTemplate.create(stockTechnicals);

		Thread.sleep(70);

		//
		StockTechnicalsIO stockTechnicalsIO = new StockTechnicalsIO();
		stockTechnicalsIO.setNseSymbol(stockPriceIO.getNseSymbol());
		stockTechnicalsIO.setSma21(sma20);
		stockTechnicalsIO.setSma50(sma50);
		stockTechnicalsIO.setSma100(sma100);
		stockTechnicalsIO.setSma200(sma200);

		stockTechnicalsIO.setPrevSma21(prevSma20);

		stockTechnicalsIO.setPrevSma50(prevSma50);
		stockTechnicalsIO.setPrevSma100(prevSma100);
		stockTechnicalsIO.setPrevSma200(prevSma200);

		stockTechnicalsIO.setRsi(smoothedRsi);


		stockTechnicalsIO.setSok(stochasticOscillatorValue);
		stockTechnicalsIO.setSod(avg3D);

		// OBV in K
		stockTechnicalsIO.setObv(OBV);

		stockTechnicalsIO.setRocv(roc);

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : "
				+ stockPriceIO.getNseSymbol() + " : Queuing to update Transactional Technicals.. ");
		// Save
		queueService.send(stockTechnicalsIO, QueueConstants.MTQueue.UPDATE_TECHNICALS_TXN_QUEUE);

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE.toUpperCase() + " : "
				+ stockPriceIO.getNseSymbol() + " : START");
	}

}
