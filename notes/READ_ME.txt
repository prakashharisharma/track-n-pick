http://localhost:8081/h2-console/login.do?jsessionid=d3567d9c136e33ecb44d94743a6fa960

--HOLIDAY
https://www.nseindia.com/global/content/market_timings_holidays/market_timings_holidays.htm

-- BHAV_BSE
https://www.bseindia.com/markets/equity/EQReports/BhavCopyDebt.aspx

https://www.bseindia.com/download/BhavCopy/Equity/EQ_ISINCODE_101018.zip
-- BHAV_NSE
https://www.nseindia.com/products/content/equities/equities/archieve_eq.htm

https://www.nseindia.com/content/historical/EQUITIES/2018/SEP/cm10SEP2018bhav.csv.zip

NSE_50
https://www.nseindia.com/content/indices/ind_nifty50list.csv

References - 

https://investinganswers.com/education/ratio-analysis/15-financial-ratios-every-investor-should-use-3011

https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=NSE:HDIL&outputsize=full&apikey=MCAF9B429I44328U
https://www.alphavantage.co/query?function=SMA&symbol=NSE:HDIL&interval=weekly&time_period=10&series_type=open&apikey=MCAF9B429I44328U

https://www.alphavantage.co/query?function=SMA&symbol=NSE:JAGRAN&interval=daily&time_period=50&series_type=close&apikey=MCAF9B429I44328U
https://www.alphavantage.co/query?function=SMA&symbol=NSE:JAGRAN&interval=daily&time_period=200&series_type=close&apikey=MCAF9B429I44328U

RSI - 30 OVERSOLD 70- OVERBOUGHT 50- Neutral
https://www.alphavantage.co/query?function=RSI&symbol=NSE:JAGRAN&interval=daily&time_period=14&series_type=close&apikey=MCAF9B429I44328U

-- RSI Formula
http://cns.bu.edu/~gsc/CN710/fincast/Technical%20_indicators/Relative%20Strength%20Index%20(RSI).htm

-- OBV
-- MACD

Util 
StockIO
StockPriceIO
StockTechncalsIO

Projects
util
mq
cache
historical
transactional (model)
service-mt (middle-tier)
service-external
integration
web


Interest coverage ratio > 1.5

Research history
Notification history 



SELECT sm.NSE_SYMBOL, ul.PB, ul.PE, ul.NIFTY_TYPE, ul.RESEARCH_DATE FROM UNDERVALUE_LEDGER  ul, STOCK_MASTER sm
WHERE ul.STOCK_ID=sm.STOCK_ID

SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm
WHERE srl.STOCK_ID=sm.STOCK_ID order by sm.NSE_SYMBOL

https://dzone.com/articles/configuring-logback-with-spring-boot