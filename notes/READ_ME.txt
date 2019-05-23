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

NSE_100
https://www.nseindia.com/products/content/equities/indices/nifty_100.htm
https://www.nseindia.com/content/indices/ind_nifty100list.csv

MIDCAP150
https://www.nseindia.com/products/content/equities/indices/nifty_midcap_150.htm
https://www.nseindia.com/content/indices/ind_niftymidcap150list.csv

SMALLCAP150
https://www.nseindia.com/products/content/equities/indices/nifty_Smallcap_250.htm
https://www.nseindia.com/content/indices/ind_niftysmallcap250list.csv

References - 

https://investinganswers.com/education/ratio-analysis/15-financial-ratios-every-investor-should-use-3011

-- RSI Formula
http://cns.bu.edu/~gsc/CN710/fincast/Technical%20_indicators/Relative%20Strength%20Index%20(RSI).htm

-- OBV
https://www.investopedia.com/terms/o/onbalancevolume.asp
https://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:on_balance_volume_obv
-- MACD
https://www.nseindia.com/companytracker/cmtracker.jsp?symbol=BEL&cName=cmtracker_nsedef.css

https://www.nseindia.com/corporates/shldStructure/ShareholdingPattern/shp_table1_mkt_tracker.jsp?ndsId=132912&symbol=BEL&asOnDate=31-DEC-2018

https://www.nseindia.com/marketinfo/companyTracker/resultsCompare.jsp?symbol=BEL
https://www.nseindia.com/marketinfo/companyTracker/compInfo.jsp?symbol=BEL&series=EQ
https://www.nseindia.com/marketinfo/companyTracker/corpAction.jsp?symbol=BEL
https://www.nseindia.com/marketinfo/companyTracker/corpAnnounce.jsp?symbol=BEL

OBV
STOCK_SEARCH


https://quickfs.net/company/INFY

Interest coverage ratio > 1.5


SELECT SMA_50, SMA_200, PREV_SMA_50, PREV_SMA_200 FROM STOCK_TECHNICALS 

-- BEARISH
select sm.NSE_SYMBOL from STOCK_TECHNICALS st, STOCK_MASTER sm where sm.STOCK_ID=st.STOCK_ID
and PREV_SMA_50 >= PREV_SMA_200
and SMA_50 < SMA_200

select sm.NSE_SYMBOL from STOCK_TECHNICALS st, STOCK_MASTER sm where sm.STOCK_ID=st.STOCK_ID
and PREV_SMA_50 >= PREV_SMA_100
and SMA_50 < SMA_100

--BULLISH
select sm.NSE_SYMBOL from STOCK_TECHNICALS st, STOCK_MASTER sm where sm.STOCK_ID=st.STOCK_ID
and PREV_SMA_200 >= PREV_SMA_50
and SMA_50 > SMA_200

select sm.NSE_SYMBOL from STOCK_TECHNICALS st, STOCK_MASTER sm where sm.STOCK_ID=st.STOCK_ID
and PREV_SMA_100 >= PREV_SMA_50
and SMA_50 > SMA_100


select sm.NSE_SYMBOL, sp.CURRENT_PRICE, sf.LAST_MODIFIED from STOCK_PRICE sp, STOCK_MASTER sm, STOCK_FACTORS sf where sp.STOCK_ID=sm.STOCK_ID
and sp.STOCK_ID=sf.STOCK_ID 
and sp.CURRENT_PRICE > 40 and sp.CURRENT_PRICE < 1000
order by sf.LAST_MODIFIED desc

-- RESEARCH STATUS
SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE, srl.RESEARCH_STATUS, sm.INDICe FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm
WHERE srl.STOCK_ID=sm.STOCK_ID order by srl.RESEARCH_STATUS, sm.INDICe, sm.NSE_SYMBOL, srl.RESEARCH_TYPE

-- RECENT RESEARCH
SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE, srl.RESEARCH_STATUS, sm.INDICe FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm
WHERE srl.STOCK_ID=sm.STOCK_ID order by srl.ENTRY_DATE desc

-- RECENT BUY RESEARCH TECHNICAL
SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE, srl.RESEARCH_STATUS, sm.INDICe FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm
WHERE srl.STOCK_ID=sm.STOCK_ID and RESEARCH_STATUS='BUY'  and srl.RESEARCH_TYPE='TECHNICAL'  order by srl.ENTRY_DATE desc

-- RECENT BUY RESEARCH FUNDAMENTAL
SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE, srl.RESEARCH_STATUS, sm.INDICe FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm
WHERE srl.STOCK_ID=sm.STOCK_ID and RESEARCH_STATUS='BUY'  and srl.RESEARCH_TYPE='FUNDAMENTAL'  order by srl.ENTRY_DATE desc


 -- UNDERVALUE BY DIFF
 SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, s.SECTOR_PE, ul.PE, s.SECTOR_PE - ul.PE AS DIFF FROM UNDERVALUE_LEDGER ul, STOCK_MASTER sm, SECTORS s
WHERE ul.STOCK_ID=sm.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
 order by DIFF desc
 
  -- UNDERVALUE BY SECTOR AND DIFF
 SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, s.SECTOR_PE, ul.PE, s.SECTOR_PE - ul.PE AS DIFF FROM UNDERVALUE_LEDGER ul, STOCK_MASTER sm, SECTORS s
WHERE ul.STOCK_ID=sm.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
 order by  s.SECTOR_NAME, DIFF  desc
 
--NITY50 UNDERVALUE 
SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, s.SECTOR_PE, ul.PE, s.SECTOR_PE - ul.PE AS DIFF FROM UNDERVALUE_LEDGER ul, STOCK_MASTER sm, SECTORS s
WHERE ul.STOCK_ID=sm.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
AND sm.INDICE = 'NIFTY50'
 order by  s.SECTOR_NAME, DIFF  desc 

 --NITY100 UNDERVALUE 
  SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, s.SECTOR_PE, ul.PE, s.SECTOR_PE - ul.PE AS DIFF FROM UNDERVALUE_LEDGER ul, STOCK_MASTER sm, SECTORS s
WHERE ul.STOCK_ID=sm.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
AND sm.INDICE = 'NIFTY100'
 order by  s.SECTOR_NAME, DIFF  desc 
 
 --NITFY250 UNDERVALUE
 SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, s.SECTOR_PE, ul.PE, s.SECTOR_PE - ul.PE AS DIFF FROM UNDERVALUE_LEDGER ul, STOCK_MASTER sm, SECTORS s
WHERE ul.STOCK_ID=sm.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
AND sm.INDICE = 'NIFTY250'
 order by  s.SECTOR_NAME, DIFF  desc 

-- NIFTY500  UNDERVALUE
 SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, s.SECTOR_PE, ul.PE, s.SECTOR_PE - ul.PE AS DIFF FROM UNDERVALUE_LEDGER ul, STOCK_MASTER sm, SECTORS s
WHERE ul.STOCK_ID=sm.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
AND sm.INDICE = 'NIFTY500'
 order by  s.SECTOR_NAME, DIFF  desc
 
-- SELL RESEARCH PERFORMANCE FUNDAMENTAL
SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE,srl.EXIT_DATE, srl.RESEARCH_STATUS, sm.INDICe, srl.ENTRY_PRICE, srl.EXIT_PRICE, sp.CURRENT_PRICE,( (sp.CURRENT_PRICE - srl.EXIT_PRICE) /  srl.EXIT_PRICE) *100 AS PERFORMANCE  FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm, STOCK_PRICE sp
WHERE srl.STOCK_ID=sm.STOCK_ID 
AND sm.STOCK_ID=sp.STOCK_ID
AND srl.RESEARCH_STATUS='SELL'
and srl.RESEARCH_TYPE='FUNDAMENTAL'
order by PERFORMANCE 

-- SELL RESEARCH PERFORMANCE TECHNICAL
SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE,srl.EXIT_DATE, srl.RESEARCH_STATUS, sm.INDICe, srl.ENTRY_PRICE, srl.EXIT_PRICE, sp.CURRENT_PRICE,( (sp.CURRENT_PRICE - srl.EXIT_PRICE) /  srl.EXIT_PRICE) *100 AS PERFORMANCE  FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm, STOCK_PRICE sp
WHERE srl.STOCK_ID=sm.STOCK_ID 
AND sm.STOCK_ID=sp.STOCK_ID
AND srl.RESEARCH_STATUS='SELL'
and srl.RESEARCH_TYPE='TECHNICAL'
order by PERFORMANCE

-- BUY RESEARCH PERFORMANCE FUNDAMENTAL
SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE, srl.RESEARCH_STATUS, sm.INDICe, srl.ENTRY_PRICE, sp.CURRENT_PRICE , ( (sp.CURRENT_PRICE - srl.ENTRY_PRICE) / srl.ENTRY_PRICE ) * 100 AS PERFORMANCE FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm, STOCK_PRICE sp 
WHERE srl.STOCK_ID=sm.STOCK_ID 
AND sm.STOCK_ID=sp.STOCK_ID
AND srl.RESEARCH_STATUS='BUY'
and srl.RESEARCH_TYPE='FUNDAMENTAL'
order by PERFORMANCE desc


-- BUY RESEARCH PERFORMANCE TECHNICAL
SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE, srl.RESEARCH_STATUS, sm.INDICe, srl.ENTRY_PRICE, sp.CURRENT_PRICE , ( (sp.CURRENT_PRICE - srl.ENTRY_PRICE) / srl.ENTRY_PRICE ) * 100 AS PERFORMANCE FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm, STOCK_PRICE sp 
WHERE srl.STOCK_ID=sm.STOCK_ID 
AND sm.STOCK_ID=sp.STOCK_ID
AND srl.RESEARCH_STATUS='BUY'
and srl.RESEARCH_TYPE='TECHNICAL'
order by PERFORMANCE desc

-- OVERSOLD TECHNICAL
select sm.NSE_SYMBOL, st.RSI from STOCK_TECHNICALS st, STOCK_MASTER sm,STOCK_RESEARCH_LEDGER srl where st.STOCK_ID = sm.STOCK_ID and srl.STOCK_ID=sm.STOCK_ID
and srl.RESEARCH_STATUS='BUY'
and srl.RESEARCH_TYPE='TECHNICAL'
 and  st.RSI < 30
 
-- OVERSOLD FUNDAMENTAL
select sm.NSE_SYMBOL, st.RSI from STOCK_TECHNICALS st, STOCK_MASTER sm,STOCK_RESEARCH_LEDGER srl where st.STOCK_ID = sm.STOCK_ID and srl.STOCK_ID=sm.STOCK_ID
and srl.RESEARCH_STATUS='BUY'
and srl.RESEARCH_TYPE='FUNDAMENTAL'
 and  st.RSI < 30 
 
-- OVERBAUGHT
select sm.NSE_SYMBOL, st.RSI from STOCK_TECHNICALS st, STOCK_MASTER sm,UNDERVALUE_LEDGER ul where st.STOCK_ID = sm.STOCK_ID and ul.STOCK_ID=sm.STOCK_ID and  st.RSI > 70

https://medium.com/@sourav.pati09/how-to-use-java-high-level-rest-client-with-spring-boot-to-talk-to-aws-elasticsearch-2b6106f2e2c

SELECT cl.CROSSOVER_TYPE, sm.NSE_SYMBOL, cl.CATEGORY, cl.RESEARCH_DATE FROM CROSSOVER_LEDGER cl, STOCK_MASTER sm where cl.STOCK_ID=sm.STOCK_ID
AND cl.STATUS='OPEN'
AND cl.CROSSOVER_TYPE='BULLISH'
order by 
sm.NSE_SYMBOL,
CL.RESEARCH_DATE DESC

--Golden CROSS
SELECT cl.CROSSOVER_TYPE, sm.NSE_SYMBOL, cl.CATEGORY, cl.RESEARCH_DATE FROM CROSSOVER_LEDGER cl, STOCK_MASTER sm where cl.STOCK_ID=sm.STOCK_ID
AND cl.STATUS='OPEN'
AND cl.CROSSOVER_TYPE='BULLISH'
AND cl.CATEGORY='CROSS200'
order by 
sm.NSE_SYMBOL,
CL.RESEARCH_DATE DESC

SELECT cl.CROSSOVER_TYPE, sm.NSE_SYMBOL, cl.CATEGORY, cl.RESEARCH_DATE FROM CROSSOVER_LEDGER cl, STOCK_MASTER sm where cl.STOCK_ID=sm.STOCK_ID
AND cl.STATUS='OPEN'
AND cl.CROSSOVER_TYPE='BEARISH'
order by 
sm.NSE_SYMBOL,
CL.RESEARCH_DATE DESC

--Death Cross
SELECT cl.CROSSOVER_TYPE, sm.NSE_SYMBOL, cl.CATEGORY, cl.RESEARCH_DATE FROM CROSSOVER_LEDGER cl, STOCK_MASTER sm where cl.STOCK_ID=sm.STOCK_ID
AND cl.STATUS='OPEN'
AND cl.CROSSOVER_TYPE='BEARISH'
AND cl.CATEGORY='CROSS200'
order by 
sm.NSE_SYMBOL,
CL.RESEARCH_DATE DESC

SELECT sm.NSE_SYMBOL, bl.CATEGORY, bl.TYPE, bl.BREAKOUT_DATE FROM BREAKOUT_LEDGER bl , STOCK_MASTER sm 
where bl.STOCK_ID=sm.STOCK_ID 
AND bl.TYPE='POSITIVE'
order by
sm.NSE_SYMBOL,
 bl.BREAKOUT_DATE desc

SELECT sm.NSE_SYMBOL, bl.CATEGORY, bl.TYPE, bl.BREAKOUT_DATE FROM BREAKOUT_LEDGER bl , STOCK_MASTER sm 
where bl.STOCK_ID=sm.STOCK_ID 
AND bl.TYPE='NEGATIVE'
order by 
sm.NSE_SYMBOL,
bl.BREAKOUT_DATE desc


https://www.rediff.com/money/special/trading-volume-what-it-reveals-about-the-market/20090703.htm
https://www.nseindia.com/products/content/sec_bhavdata_full.csv
https://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:moving_averages
