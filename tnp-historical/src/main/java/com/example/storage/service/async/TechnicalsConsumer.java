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
import com.example.storage.model.Indicator;
import com.example.storage.model.MovingAverage;
import com.example.storage.model.RSI;
import com.example.storage.model.StockTechnicals;
import com.example.storage.model.Trend;
import com.example.storage.model.type.Direction;
import com.example.storage.repo.TechnicalsTemplate;
import com.example.storage.service.StorageService;
import com.example.storage.service.TrendServiceImpl;
import com.example.util.FormulaService;
import com.example.util.io.model.DirectionIO;
import com.example.util.io.model.StockPriceIO;
import com.example.util.io.model.StockTechnicalsIO;

@Component
public class TechnicalsConsumer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TechnicalsConsumer.class);

	@Autowired
	private StorageService storageService;

	@Autowired
	private TechnicalsTemplate technicalsTemplate;

	@Autowired
	private FormulaService formulaService;

	@Autowired
	private QueueService queueService;
	@Autowired
	private TrendServiceImpl trendService;
	
	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_TECHNICALS_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {
		
		System.out.println("TC_CONSUMER START " + stockPriceIO);
		
		// Get Technicals

				double currentAverageGain = storageService.getAverageGain(stockPriceIO.getNseSymbol(), 14);

				double currentAverageLoss = storageService.getAverageLoss(stockPriceIO.getNseSymbol(), 14);

				double rs = formulaService.calculateRs(currentAverageGain, currentAverageLoss);

				double rsi = formulaService.calculateRsi(rs);

				double prevAverageGain = technicalsTemplate.getPrevTotalGain(stockPriceIO.getNseSymbol());

				double prevAverageLoss = technicalsTemplate.getPrevTotalLoss(stockPriceIO.getNseSymbol());

				double currentGain = 0.0;
				double currentLoss = 0.0;

				double change = stockPriceIO.getClose() - stockPriceIO.getPrevClose();

				double currentPrice = stockPriceIO.getClose();

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

				double sma50 = storageService.getSMA(stockPriceIO.getNseSymbol(), 50);

				double sma100 = storageService.getSMA(stockPriceIO.getNseSymbol(), 100);

				double sma200 = storageService.getSMA(stockPriceIO.getNseSymbol(), 200);

				double avgSma50 = technicalsTemplate.getPriorDaysSma50Average(stockPriceIO.getNseSymbol(), 3);

				MovingAverage movingAverage = new MovingAverage(sma50, sma100, sma200);

				RSI rsiObj = new RSI(rs, rsi, smoothedRs, smoothedRsi);

				Indicator indicator = new Indicator(rsiObj);

				StockTechnicals stockTechnicals = new StockTechnicals(stockPriceIO.getNseSymbol(), stockPriceIO.getBhavDate(),
						currentAverageGain, currentAverageLoss, movingAverage, indicator);

				technicalsTemplate.create(stockTechnicals);
				
				Thread.sleep(70);
				
				LOGGER.info("ADDED TECHNICALs :" + stockPriceIO.getNseSymbol());
				
				//
				StockTechnicalsIO stockTechnicalsIO = new StockTechnicalsIO();
				stockTechnicalsIO.setNseSymbol(stockPriceIO.getNseSymbol());
				stockTechnicalsIO.setSma50(sma50);
				stockTechnicalsIO.setPrevSma50(sma100);
				stockTechnicalsIO.setSma100(sma200);
				stockTechnicalsIO.setSma200(technicalsTemplate.getPrevSessionSma50(stockPriceIO.getNseSymbol()));
				stockTechnicalsIO.setPrevSma200(technicalsTemplate.getPrevSessionSma200(stockPriceIO.getNseSymbol()));
				stockTechnicalsIO.setRsi(smoothedRsi);
				
				Trend trend = trendService.getMovingAverageTrends(stockPriceIO.getNseSymbol());
				
				stockTechnicalsIO.setCurrentTrend(this.directionAdapter(trend.getCurrentTrend()));
				stockTechnicalsIO.setMidTermTrend(this.directionAdapter(trend.getMidTermTrend()));
				stockTechnicalsIO.setLongTermTrend(this.directionAdapter(trend.getLongTermTrend()));
				
				
				// Save
				queueService.send(stockTechnicalsIO, QueueConstants.MTQueue.UPDATE_TECHNICALS_TXN_QUEUE);
	}
	
	private DirectionIO directionAdapter(Direction inputDirection) {
		
		if(inputDirection == Direction.UPTREND) {
			return DirectionIO.UPTREND;
		}else {
			return DirectionIO.DOWNTREND;
		}
	}
}
