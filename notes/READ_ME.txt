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

https://www.nseindia.com/corporates/corporateHome.html?id=shldinfo_annual_reports&radio_btn=company&param=HDIL

https://money.rediff.com/companies/V-Guard-Industries-Ltd/13150060/ratio

https://money.rediff.com/companies/V-Guard-Industries-Ltd/13150060/balance-sheet

https://money.rediff.com/companies/V-Guard-Industries-Ltd/13150060/profit-and-loss

https://money.rediff.com/companies/V-Guard-Industries-Ltd/13150060/results-annual

References - 

https://investinganswers.com/education/ratio-analysis/15-financial-ratios-every-investor-should-use-3011

http://www.smartpaisa.in/charges-other-than-brokerage-stock-exchange-share-future-options-transactions-stt-transaction-charges-sebi-turnover-charges-stamp-duty/

http://www.mkyong.com/spring-boot/spring-boot-spring-security-thymeleaf-example/

https://www.thymeleaf.org/doc/articles/springsecurity.html

https://www.baeldung.com/spring-security-custom-access-denied-page

https://stackoverflow.com/questions/33739359/combining-basic-authentication-and-form-login-for-the-same-rest-api

https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=NSE:HDIL&outputsize=full&apikey=MCAF9B429I44328U

https://dzone.com/articles/spring-31-caching-and-0

https://docs.hazelcast.org/docs/3.6/manual/html-single/index.html#custom-serialization
https://blog.hazelcast.com/use-kryo-serialization-hazelcast/
https://stackoverflow.com/questions/48428131/hazelcast-with-global-serializer-kryo-there-is-no-suitable-de-serializer-for
https://github.com/hazelcast/hazelcast/blob/master/hazelcast/src/main/java/com/hazelcast/internal/serialization/impl/ArrayListStreamSerializer.java

https://docs.hazelcast.org/docs/latest-dev/manual/html-single/index.html#serialization
https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=NSE:HDIL&outputsize=full&apikey=MCAF9B429I44328U
https://www.alphavantage.co/query?function=SMA&symbol=NSE:HDIL&interval=weekly&time_period=10&series_type=open&apikey=MCAF9B429I44328U

https://www.alphavantage.co/query?function=SMA&symbol=NSE:JAGRAN&interval=daily&time_period=50&series_type=close&apikey=MCAF9B429I44328U
https://www.alphavantage.co/query?function=SMA&symbol=NSE:JAGRAN&interval=daily&time_period=200&series_type=close&apikey=MCAF9B429I44328U

RSI - 30 OVERSOLD 70- OVERBOUGHT 50- Neutral
https://www.alphavantage.co/query?function=RSI&symbol=NSE:JAGRAN&interval=daily&time_period=14&series_type=close&apikey=MCAF9B429I44328U
https://www.investopedia.com/terms/r/rsi.asp
https://www.fidelity.com/learning-center/trading-investing/technical-analysis/technical-indicator-guide/RSI

-- RSI Formula
http://cns.bu.edu/~gsc/CN710/fincast/Technical%20_indicators/Relative%20Strength%20Index%20(RSI).htm
--Stochastic Oscillator formula
https://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:stochastic_oscillator_fast_slow_and_full

https://www.investopedia.com/ask/answers/012815/why-50-simple-moving-average-sma-so-common-traders-and-analysts.asp
-- UP In a sustained uptrend, price generally remains above the 50-day moving average, and the 50-day moving average remains above the 100-day moving average.
-- DN If price moves significantly below the 50-period moving average, and especially if it closes below that level, it is commonly interpreted by analysts as signaling a possible trend change to the downside.
--DN The 50-day moving average crossing below and remaining below the 100-day moving average gives the same signal.
https://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:moving_averages
--A bullish crossover occurs when the shorter moving average crosses above the longer moving average. This is also known as a golden cross. 
--A bearish crossover occurs when the shorter moving average crosses below the longer moving average. This is known as a dead cross.


https://nseguide.com/stock.php?UNIONBANK
https://in.tradingview.com/symbols/NSE-BDL/technicals/

http://appsdeveloperblog.com/spring-boot-and-mongotemplate-tutorial-with-mongodb/

TadingSessions
https://stackoverflow.com/questions/45892242/storing-java-8-localdate-in-mongo-db
http://lifeinide.com/post/2018-05-15-spring-data-mongo-date-without-timezone/
https://stackoverflow.com/questions/23972002/java-8-date-time-jsr-310-types-mapping-with-spring-data-mongodb



SELECT sm.NSE_SYMBOL, sm.SECTOR_NAME, sf.MARKET_CAPITAL, sp.YEAR_LOW, sp.CURRENT_PRICE, sp.YEAR_HIGH FROM STOCK_MASTER sm
JOIN STOCK_FACTORS sf on sf.STOCK_ID=sm.STOCK_ID
JOIN STOCK_PRICe sp on sp.STOCK_ID=sm.STOCK_ID
 WHERE sm.IS_NIFTY50=1
and sp.CURRENT_PRICe < 1000
and sf.ROE > 10.0
and sf.ROCE > 15.0
and sf.DEBT_EQUITY < 0.50
ORDER BY sf.MARKET_CAPITAL DESC