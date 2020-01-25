package com.example.mq.constants;

public interface QueueConstants {

	public interface HistoricalQueue{

		public static String UPDATE_TRADING_SESSION_QUEUE = "queue.historical.tradingsession.update";
		
		public static String UPDATE_PRICE_QUEUE = "queue.historical.price.update";
		
		public static String UPDATE_BHAV_QUEUE = "queue.historical.bhav.update";
		
		public static String UPDATE_CANDLESTICK_QUEUE = "queue.historical.candlestick.update";
		
		public static String UPDATE_TECHNICALS_QUEUE = "queue.historical.technicals.update";
		
		public static String UPDATE_FACTORS_QUEUE = "queue.historical.factors.update";
		
		public static String UPDATE_RESEARCH_QUEUE = "queue.historical.research.update";
		
	}
	public interface IntegrationQueue{
		public static String PROCESS_BHAV_QUEUE = "queue.integration.stocks.bhav";
		public static String UPDATE_SECTORS_QUEUE = "queue.integration.update.sectors";
		
		public static String UPDATE_NIFTY1000_QUEUE = "queue.integration.update.nifty1000";
		public static String UPDATE_NIFTY750_QUEUE = "queue.integration.update.nifty750";
		public static String UPDATE_NIFTY500_QUEUE = "queue.integration.update.nifty500";
		public static String UPDATE_NIFTY250_QUEUE = "queue.integration.update.nifty250";
		public static String UPDATE_NIFTY100_QUEUE = "queue.integration.update.nifty100";
		public static String UPDATE_NIFTY50_QUEUE = "queue.integration.update.nifty50";
	}
	public interface TransactionQueue{
		
	}
	
	public interface MTQueue{
		public static String DOWNLOAD_TRIGGER_QUEUE = "queue.mt.download.trigger";
		
		public static String UPDATE_TRIGGER_QUEUE = "queue.mt.update.trigger";
		
		public static String UPDATE_PRICE_TXN_QUEUE = "queue.mt.price.update";
		
		public static String UPDATE_TECHNICALS_TXN_QUEUE = "queue.mt.technicals.update";
		
		public static String UPDATE_FACTOR_TXN_QUEUE = "queue.mt.factor.update";
/*		
		public static String RESEARCH_UNDERVALUE_QUEUE = "queue.mt.research.fundamental";
		
		public static String RESEARCH_TECHNICALS_QUEUE = "queue.mt.research.technical";*/
		
		public static String RESEARCH_QUEUE = "queue.mt.research.gateway";
		
		public static String RESEARCH_FUNDAMENTAL_QUEUE = "queue.mt.research.fundamental";
		
		public static String RESEARCH_TECHNICAL_QUEUE = "queue.mt.research.technical";
		
		public static String RESEARCH_BREAKOUT_QUEUE = "queue.mt.research.breakout";
		
		//public static String PORTFOLIO_NOTIFICATION_TRIGGER = "queue.mt.notification.portfolio";
		
		public static String RESEARCH_NOTIFICATION_TRIGGER = "queue.mt.notification.research";
		
		public static String NOTIFICATION_SEND_MAIL_TRIGGER = "queue.mt.notification.send";
		
		public static String UPDATE_MASTER_SECTOR_QUEUE = "queue.mt.master.update.sector";
		
		public static String UPDATE_MASTER_STOCK_QUEUE = "queue.mt.master.update.stock";
		
		public static String UPDATE_MASTER_FACTOR_QUEUE = "queue.mt.master.update.factor";
		
		
	}
	public interface ExternalQueue{
		public static String SEND_EMAIL_QUEUE = "queue.external.email.send";
	}
}
