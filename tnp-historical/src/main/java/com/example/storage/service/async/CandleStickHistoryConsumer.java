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
import com.example.storage.model.CandleStick;
import com.example.storage.model.CandleStick.Color;
import com.example.storage.model.CandleStick.Gap;
import com.example.storage.model.CandleStick.Pattern;
import com.example.storage.repo.CandleStickTemplate;
import com.example.util.io.model.StockPriceIO;

@Component
public class CandleStickHistoryConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CandleStickHistoryConsumer.class);

	@Autowired
	private CandleStickTemplate candleStickTemplate;

	@JmsListener(destination = QueueConstants.HistoricalQueue.UPDATE_CANDLESTICK_QUEUE)
	public void receiveMessage(@Payload StockPriceIO stockPriceIO, @Headers MessageHeaders headers, Message message,
			Session session) throws InterruptedException {

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_CANDLESTICK_QUEUE.toUpperCase() + " : "
				+ stockPriceIO.getNseSymbol() + " : START");

		LOGGER.trace(QueueConstants.HistoricalQueue.UPDATE_CANDLESTICK_QUEUE.toUpperCase() + " : "
				+ stockPriceIO.getNseSymbol() + " : updating Bhav History..");

		double open = stockPriceIO.getOpen();

		double high = stockPriceIO.getHigh();

		double low = stockPriceIO.getLow();

		double close = stockPriceIO.getClose();

		double prevClose = stockPriceIO.getPrevClose();

		CandleStick CandleStick = new com.example.storage.model.CandleStick(stockPriceIO.getNseSymbol(), open, high,
				low, close, prevClose, stockPriceIO.getBhavDate());

		CandleStick.setColor(this.calculateColor(open, close));

		CandleStick.setGap(this.calculateGap(open, close, prevClose));

		CandleStick.setHead(this.calculateHead(open, high, low, close));

		CandleStick.setBody(this.calculateBody(open, high, low, close));

		CandleStick.setTail(this.calculateTail(open, high, low, close));
		
		CandleStick.setPattern(this.calculatePattern(open, high, low, close));

		candleStickTemplate.create(CandleStick);

		LOGGER.debug(QueueConstants.HistoricalQueue.UPDATE_CANDLESTICK_QUEUE.toUpperCase() + " : "
				+ stockPriceIO.getNseSymbol() + " : END");

	}

	private Color calculateColor(double open, double close) {
		if (open < close) {
			return Color.GREEN;
		} else {
			return Color.RED;
		}
	}

	private Gap calculateGap(double open, double close, double prevClose) {

		if (open > prevClose && close > prevClose) {
			return Gap.UP;
		} else if (open < prevClose && close < prevClose) {
			return Gap.DOWN;
		} else {
			return Gap.NONE;
		}
	}

	private double calculateHead(double open, double high, double low, double close) {

		double head = 0.0;

		Color color = this.calculateColor(open, close);

		if (color == Color.GREEN) {
			head = high - close;

		} else {
			head = high - open;

		}

		return head;
	}

	private double calculateBody(double open, double high, double low, double close) {

		double body = 0.0;
		Color color = this.calculateColor(open, close);

		if (color == Color.GREEN) {

			body = close - open;

		} else {

			body = open - close;

		}
		return body;
	}

	private double calculateTail(double open, double high, double low, double close) {

		double tail = 0.0;
		Color color = this.calculateColor(open, close);

		if (color == Color.GREEN) {

			tail = open - low;
		} else if (color == Color.RED) {

			tail = close - low;
		}

		return tail;
	}

	private Pattern calculatePattern(double open, double high, double low, double close) {

		Pattern pattern = Pattern.NONE;

		if (this.isHammer(open, high, low, close)) {
			pattern = Pattern.HAMMER;
		} else if (this.isShootingStar(open, high, low, close)) {
			pattern = Pattern.SHOOTING_STAR;
		} else if (this.isDoji(open, high, low, close)) {
			pattern = Pattern.DOJI;
		} else if (this.isBullishMarubozu(open, high, low, close)) {
			pattern = Pattern.BULLISH_MARUBOZU;
		}else if(this.isBearishMarubozu(open, high, low, close)) {
			pattern = Pattern.BEARISH_MARUBOZU;
		}else {
			pattern = Pattern.NONE;
		}

		return pattern;
	}

	private boolean isHammer(double open, double high, double low, double close) {

		boolean isHammer = false;

		double tail = this.calculateTail(open, high, low, close);

		double body = this.calculateBody(open, high, low, close);

		double head = this.calculateHead(open, high, low, close);
		if(body > 0.0 && tail > 0.00) {
		if (tail >= (body * 2) && head == 0.0) {

			isHammer = true;

		}
		}
		return isHammer;
	}

	private boolean isShootingStar(double open, double high, double low, double close) {

		boolean isShootingStar = false;

		double tail = this.calculateTail(open, high, low, close);

		double body = this.calculateBody(open, high, low, close);

		double head = this.calculateHead(open, high, low, close);
		if(body > 0.0 && head > 0.0 ) {
		if (head >= (body * 2) && tail == 0.0) {
			isShootingStar = true;

		}
		}
		return isShootingStar;
	}

	private boolean isDoji(double open, double high, double low, double close) {

		boolean isDoji = false;

		double tail = this.calculateTail(open, high, low, close);

		double body = this.calculateBody(open, high, low, close);

		double head = this.calculateHead(open, high, low, close);

		if ((int) open == (int) close) {
			if(body > 0.0 && head > 0.0 && tail > 0.00) {
			if (head > (body * 2) && tail > (body * 2)) {
				isDoji = true;

			}
			}
		}

		return isDoji;
	}

	private boolean isBullishMarubozu(double open, double high, double low, double close) {

		boolean isBullishMarubozu = false;

		if (open == low && high == close) {
			isBullishMarubozu = true;

		}

		return isBullishMarubozu;
	}
	private boolean isBearishMarubozu(double open, double high, double low, double close) {

		boolean isBearishMarubozu = false;

		if (open == high && low == close) {
			isBearishMarubozu = true;

		}

		return isBearishMarubozu;
	}
}
