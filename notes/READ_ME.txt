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

SMALLCAP100
https://www.nseindia.com/products/content/equities/indices/nifty_smallcap_100.htm
https://www.nseindia.com/content/indices/ind_niftysmallcap100list.csv

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

https://www.rediff.com/money/special/trading-volume-what-it-reveals-about-the-market/20090703.htm

https://www.nseindia.com/products/content/sec_bhavdata_full.csv
https://stockcharts.com/school/doku.php?id=chart_school:technical_indicators:moving_averages

https://tradingsim.com/blog/200-day-simple-moving-average/
https://tradingsim.com/blog/50-day-moving-average/

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

 -- UNDERVALUE BY DIFF -  CURENT_PRICE > SM200
select cq.NSE_SYMBOL, cq.INDICe, cq.SECTOR_NAME, cq.CURRENT_PRICE, cq.SMA_50,cq.SMA_200,cq.CURRENT_RATIO,cq.DEBT_EQUITY, cq.ROE, cq.ROCE, cq.PE, cq.SECTOR_PE, ( cq.SECTOR_PE - cq.PE) AS DIFF from 
(
	select sm.NSE_SYMBOL, sm.INDICe, s.SECTOR_NAME, sp.CURRENT_PRICE, st.SMA_50,st.SMA_200,sf.CURRENT_RATIO,sf.DEBT_EQUITY, sf.ROE, sf.ROCE, (sp.CURRENT_PRICE / sf.EPS) AS PE, s.SECTOR_PE
	from 
		STOCK_MASTER sm, STOCK_TECHNICALS st,STOCK_PRICE sp,STOCK_FACTORS sf, SECTORS s, STOCK_RESEARCH_LEDGER_FUNDAMENTAL srl
	where sm.STOCK_ID = st.STOCK_ID
	and srl.STOCK_ID = sm.STOCK_ID 
	and sm.STOCK_ID = sp.STOCK_ID
	and sm.STOCK_ID = sf.STOCK_ID
	and sm.SECTOR_ID=s.SECTOR_ID
	and st.SMA_200 < sp.CURRENT_PRICE
	and srl.RESEARCH_STATUS = 'BUY'
) cq where  cq.PE < cq.SECTOR_PE
order by DIFF desc
 
 -- UNDERVALUE BY DIFF ALL CURENT_PRICE > SM200
select cq.NSE_SYMBOL, cq.INDICe, cq.SECTOR_NAME, cq.CURRENT_PRICE, cq.SMA_50,cq.SMA_200,cq.CURRENT_RATIO,cq.DEBT_EQUITY, cq.ROE, cq.ROCE, cq.PE, cq.SECTOR_PE, ( cq.SECTOR_PE - cq.PE) AS DIFF from 
(
	select sm.NSE_SYMBOL, sm.INDICe, s.SECTOR_NAME, sp.CURRENT_PRICE, st.SMA_50,st.SMA_200,sf.CURRENT_RATIO,sf.DEBT_EQUITY, sf.ROE, sf.ROCE, (sp.CURRENT_PRICE / sf.EPS) AS PE, s.SECTOR_PE
	from 
		STOCK_MASTER sm, STOCK_TECHNICALS st,STOCK_PRICE sp,STOCK_FACTORS sf, SECTORS s, STOCK_RESEARCH_LEDGER_FUNDAMENTAL srl
	where sm.STOCK_ID = st.STOCK_ID
	and srl.STOCK_ID = sm.STOCK_ID 
	and sm.STOCK_ID = sp.STOCK_ID
	and sm.STOCK_ID = sf.STOCK_ID
	and sm.SECTOR_ID=s.SECTOR_ID
	and srl.RESEARCH_STATUS = 'BUY'
) cq where  cq.PE < cq.SECTOR_PE
order by DIFF desc 
 
  -- UNDERVALUE BY SECTOR AND DIFF PRICE > 200
select cq.NSE_SYMBOL, cq.INDICe, cq.SECTOR_NAME, cq.CURRENT_PRICE, cq.SMA_50,cq.SMA_200,cq.CURRENT_RATIO,cq.DEBT_EQUITY, cq.ROE, cq.ROCE, cq.PE, cq.SECTOR_PE, ( cq.SECTOR_PE - cq.PE) AS DIFF from 
(
	select sm.NSE_SYMBOL, sm.INDICe, s.SECTOR_NAME, sp.CURRENT_PRICE, st.SMA_50,st.SMA_200,sf.CURRENT_RATIO,sf.DEBT_EQUITY, sf.ROE, sf.ROCE, (sp.CURRENT_PRICE / sf.EPS) AS PE, s.SECTOR_PE
	from 
		STOCK_MASTER sm, STOCK_TECHNICALS st,STOCK_PRICE sp,STOCK_FACTORS sf, SECTORS s, STOCK_RESEARCH_LEDGER_FUNDAMENTAL srl
	where sm.STOCK_ID = st.STOCK_ID
	and srl.STOCK_ID = sm.STOCK_ID 
	and sm.STOCK_ID = sp.STOCK_ID
	and sm.STOCK_ID = sf.STOCK_ID
	and sm.SECTOR_ID=s.SECTOR_ID
	and st.SMA_200 < sp.CURRENT_PRICE
	and srl.RESEARCH_STATUS = 'BUY'
) cq where  cq.PE < cq.SECTOR_PE
order by SECTOR_NAME, DIFF desc
 
  -- UNDERVALUE BY SECTOR AND DIFF ALL
select cq.NSE_SYMBOL, cq.INDICe, cq.SECTOR_NAME, cq.CURRENT_PRICE, cq.SMA_50,cq.SMA_200,cq.CURRENT_RATIO,cq.DEBT_EQUITY, cq.ROE, cq.ROCE, cq.PE, cq.SECTOR_PE, ( cq.SECTOR_PE - cq.PE) AS DIFF from 
(
	select sm.NSE_SYMBOL, sm.INDICe, s.SECTOR_NAME, sp.CURRENT_PRICE, st.SMA_50,st.SMA_200,sf.CURRENT_RATIO,sf.DEBT_EQUITY, sf.ROE, sf.ROCE, (sp.CURRENT_PRICE / sf.EPS) AS PE, s.SECTOR_PE
	from 
		STOCK_MASTER sm, STOCK_TECHNICALS st,STOCK_PRICE sp,STOCK_FACTORS sf, SECTORS s, STOCK_RESEARCH_LEDGER_FUNDAMENTAL srl
	where sm.STOCK_ID = st.STOCK_ID
	and srl.STOCK_ID = sm.STOCK_ID 
	and sm.STOCK_ID = sp.STOCK_ID
	and sm.STOCK_ID = sf.STOCK_ID
	and sm.SECTOR_ID=s.SECTOR_ID
	and srl.RESEARCH_STATUS = 'BUY'
) cq where  cq.PE < cq.SECTOR_PE
order by SECTOR_NAME, DIFF desc 
 
--NITY50 UNDERVALUE 
SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, sp.CURRENT_PRICE, sf.CURRENT_RATIO, s.SECTOR_CURR_RATIO, s.SECTOR_PE, ul.PE, s.SECTOR_PE - ul.PE AS DIFF 
FROM VALUATION_LEDGER ul, STOCK_MASTER sm, SECTORS s, STOCK_PRICE sp, STOCK_FACTORS sf, STOCK_RESEARCH_LEDGER_FUNDAMENTAL srl
WHERE ul.STOCK_ID=sm.STOCK_ID
AND ul.STOCK_ID=srl.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
AND ul.STOCK_ID=sf.STOCK_ID
AND sm.INDICE = 'NIFTY50'
AND sm.STOCK_ID= sp.STOCK_ID
AND srl.RESEARCH_STATUS='BUY'
AND ul.STATUS='OPEN'
 order by  s.SECTOR_NAME, DIFF  desc

 --NITY100 UNDERVALUE 
SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, sp.CURRENT_PRICE, sf.CURRENT_RATIO, s.SECTOR_CURR_RATIO, s.SECTOR_PE, ul.PE, s.SECTOR_PE - ul.PE AS DIFF 
FROM VALUATION_LEDGER ul, STOCK_MASTER sm, SECTORS s, STOCK_PRICE sp, STOCK_FACTORS sf, STOCK_RESEARCH_LEDGER_FUNDAMENTAL srl
WHERE ul.STOCK_ID=sm.STOCK_ID
AND ul.STOCK_ID=srl.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
AND ul.STOCK_ID=sf.STOCK_ID
AND sm.INDICE = 'NIFTY100'
AND sm.STOCK_ID= sp.STOCK_ID
AND srl.RESEARCH_STATUS='BUY'
AND ul.STATUS='OPEN'
 order by  s.SECTOR_NAME, DIFF  desc
 
 --NITFY250 UNDERVALUE
SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, sp.CURRENT_PRICE, sf.CURRENT_RATIO, s.SECTOR_CURR_RATIO, s.SECTOR_PE, ul.PE, s.SECTOR_PE - ul.PE AS DIFF 
FROM VALUATION_LEDGER ul, STOCK_MASTER sm, SECTORS s, STOCK_PRICE sp, STOCK_FACTORS sf, STOCK_RESEARCH_LEDGER_FUNDAMENTAL srl
WHERE ul.STOCK_ID=sm.STOCK_ID
AND ul.STOCK_ID=srl.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
AND ul.STOCK_ID=sf.STOCK_ID
AND sm.INDICE = 'NIFTY250'
AND sm.STOCK_ID= sp.STOCK_ID
AND srl.RESEARCH_STATUS='BUY'
AND ul.STATUS='OPEN'
 order by  s.SECTOR_NAME, DIFF  desc 

-- NIFTY500  UNDERVALUE
SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, sp.CURRENT_PRICE, sf.CURRENT_RATIO, s.SECTOR_CURR_RATIO, s.SECTOR_PE, ul.PE, s.SECTOR_PE - ul.PE AS DIFF 
FROM VALUATION_LEDGER ul, STOCK_MASTER sm, SECTORS s, STOCK_PRICE sp, STOCK_FACTORS sf, STOCK_RESEARCH_LEDGER_FUNDAMENTAL srl
WHERE ul.STOCK_ID=sm.STOCK_ID
AND ul.STOCK_ID=srl.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
AND ul.STOCK_ID=sf.STOCK_ID
AND sm.INDICE = 'NIFTY500'
AND sm.STOCK_ID= sp.STOCK_ID
AND srl.RESEARCH_STATUS='BUY'
AND ul.STATUS='OPEN'
 order by  s.SECTOR_NAME, DIFF  desc 
 
-- SELL RESEARCH PERFORMANCE FUNDAMENTAL
SELECT 
	sm.NSE_SYMBOL,vl.RESEARCH_DATE, srl.RESEARCH_STATUS, sm.INDICe, vl.PRICE, sp.CURRENT_PRICE , ( (sp.CURRENT_PRICE - vl.PRICE) / vl.PRICE ) * 100 AS PERFORMANCE 
FROM 
	STOCK_RESEARCH_LEDGER_FUNDAMENTAL  srl, STOCK_MASTER sm, STOCK_PRICE sp , VALUATION_LEDGER vl, VALUATION_LEDGER v2
WHERE 
	srl.STOCK_ID=sm.STOCK_ID 
	AND sm.STOCK_ID=sp.STOCK_ID
	AND srl.RESEARCH_STATUS='SELL'
	AND srl.ENTRY_VALUATION = vl.UNDERVALUE_ID
	AND srl.EXIT_VALUATION = v2.UNDERVALUE_ID
order by PERFORMANCE

-- SELL RESEARCH PERFORMANCE TECHNICAL
SELECT 
	sm.NSE_SYMBOL, cl.RESEARCH_DATE, srl.RESEARCH_STATUS, cl.CATEGORY As ENTRYCATEGORY, c2.RESEARCH_DATE, c2.CATEGORY AS EXITCATEGORY, 
	sm.INDICe, cl.PRICE AS ENTRY_PRICE, c2.PRICE As EXIT_PRICE, sp.CURRENT_PRICE , ( (sp.CURRENT_PRICE - c2.PRICE) / c2.PRICE ) * 100 AS PERFORMANCE 
FROM 
	STOCK_RESEARCH_LEDGER_TECHNICAL  srl, STOCK_MASTER sm, STOCK_PRICE sp , CROSSOVER_LEDGER cl,CROSSOVER_LEDGER c2
WHERE 
	srl.STOCK_ID=sm.STOCK_ID 
	AND sm.STOCK_ID=sp.STOCK_ID
	AND srl.RESEARCH_STATUS='SELL'
	and srl.ENTRY_CROSS = cl.CROSSOVER_LEDGER_ID
	and srl.EXIT_CROSS = c2.CROSSOVER_LEDGER_ID
order by PERFORMANCE desc

-- BUY RESEARCH PERFORMANCE FUNDAMENTAL
SELECT 
	sm.NSE_SYMBOL,vl.RESEARCH_DATE, srl.RESEARCH_STATUS, sm.INDICe,vl.PE,vl.SECTOR_PE, (sp.CURRENT_PRICE / sf.EPS) as CURR_PE,  vl.PB,vl.SECTOR_PB,(sp.CURRENT_PRICE / sf.BOOK_VALUE) as CURR_PB, vl.DEBT_EQUITY, vl.CURRENT_RATIO,  vl.PRICE,sp.CURRENT_PRICE , ( (sp.CURRENT_PRICE - vl.PRICE) / vl.PRICE ) * 100 AS PERFORMANCE 
FROM 
	STOCK_RESEARCH_LEDGER_FUNDAMENTAL  srl, STOCK_MASTER sm, STOCK_PRICE sp , STOCK_FACTORS sf, VALUATION_LEDGER vl
WHERE 
	srl.STOCK_ID=sm.STOCK_ID 
	AND sm.STOCK_ID=sf.STOCK_ID
	AND sm.STOCK_ID=sp.STOCK_ID
	AND srl.RESEARCH_STATUS='BUY'
	AND srl.ENTRY_VALUATION = vl.UNDERVALUE_ID
order by PERFORMANCE desc


-- BUY RESEARCH PERFORMANCE TECHNICAL
SELECT 
	sm.NSE_SYMBOL, cl.RESEARCH_DATE, cl.CATEGORY, srl.RESEARCH_STATUS, sm.INDICe, cl.PRICE, sp.CURRENT_PRICE , ( (sp.CURRENT_PRICE - cl.PRICE) / cl.PRICE ) * 100 AS PERFORMANCE 
FROM 
	STOCK_RESEARCH_LEDGER_TECHNICAL  srl, STOCK_MASTER sm, STOCK_PRICE sp , CROSSOVER_LEDGER cl
WHERE 
	srl.STOCK_ID=sm.STOCK_ID 
	AND sm.STOCK_ID=sp.STOCK_ID
	AND srl.RESEARCH_STATUS='BUY'
	and srl.ENTRY_CROSS = cl.CROSSOVER_LEDGER_ID
order by PERFORMANCE desc

-- OVERBAUGHT
select sm.NSE_SYMBOL, st.RSI from VALUATION_LEDGER ul ,STOCK_TECHNICALS st,  STOCK_MASTER sm
where
ul.STOCK_ID=sm.STOCK_ID
and ul.STOCK_ID=st.STOCK_ID
and  sm.STOCK_ID = st.STOCK_ID
and ul.STATUS='OPEN'
and ul.TYPE='UNDERVALUE'
 and  st.RSI > 70
 
--OVERSOLD
select sm.NSE_SYMBOL, st.RSI from VALUATION_LEDGER ul ,STOCK_TECHNICALS st,  STOCK_MASTER sm
where
ul.STOCK_ID=sm.STOCK_ID
and ul.STOCK_ID=st.STOCK_ID
and  sm.STOCK_ID = st.STOCK_ID
and ul.STATUS='OPEN'
and ul.TYPE='UNDERVALUE'
 and  st.RSI < 30

-- RSI between 40 and 60 
select sm.NSE_SYMBOL, st.RSI from VALUATION_LEDGER ul ,STOCK_TECHNICALS st,  STOCK_MASTER sm
where
ul.STOCK_ID=sm.STOCK_ID
and ul.STOCK_ID=st.STOCK_ID
and  sm.STOCK_ID = st.STOCK_ID
and ul.STATUS='OPEN'
and ul.TYPE='UNDERVALUE'
 and  st.RSI > 40
 and  st.RSI < 60 
 

SELECT cl.CROSSOVER_TYPE, sm.NSE_SYMBOL, cl.CATEGORY, cl.RESEARCH_DATE FROM CROSSOVER_LEDGER cl, STOCK_MASTER sm where cl.STOCK_ID=sm.STOCK_ID
AND cl.STATUS='OPEN'
AND cl.CROSSOVER_TYPE='BULLISH'
order by 
sm.NSE_SYMBOL,
CL.RESEARCH_DATE DESC

--Golden CROSS
SELECT cl.CROSSOVER_TYPE, sm.NSE_SYMBOL, cl.CATEGORY, cl.RESEARCH_DATE, sp.CURRENT_PRICE FROM CROSSOVER_LEDGER cl, STOCK_MASTER sm, STOCK_PRICE sp where cl.STOCK_ID=sm.STOCK_ID
AND sm.STOCK_ID=sp.STOCK_ID
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

-- Current Fundamental Research
SELECT 
	sm.NSE_SYMBOL, vl.PRICE,vl.PE, vl.SECTOR_PE, vl.PB, vl.SECTOR_PB,vl.CURRENT_RATIO, vl.DEBT_EQUITY 
FROM 
	STOCK_RESEARCH_LEDGER_FUNDAMENTAL  srl, VALUATION_LEDGER vl, STOCK_MASTER sm
where 
	srl.STOCK_ID=sm.STOCK_ID
	and srl.ENTRY_VALUATION = vl.UNDERVALUE_ID

-- Current Technicals Research
SELECT 
	sm.NSE_SYMBOL, cl.PRICE, cl.PREV_CLOSE, cl.CATEGORY , cl.SHORT_AVG, cl.LONG_AVG, cl.VOLUME, cl.AVG_VOLUME
FROM 
	STOCK_RESEARCH_LEDGER_TECHNICAL  srl, CROSSOVER_LEDGER cl, STOCK_MASTER sm
where 
	srl.STOCK_ID=sm.STOCK_ID
	and srl.ENTRY_CROSS = cl.CROSSOVER_LEDGER_ID


-- VOLUME INCREASE PRICE RISE BULLISH
SELECT 
	sm.NSE_SYMBOL,  st.VOLUME, st.AVG_VOLUME, sp.CURRENT_PRICE, sp.PREV_CLOSE
FROM 
	STOCK_TECHNICALS st,STOCK_PRICE sp, STOCK_MASTER sm
where
	sm.STOCK_ID=st.STOCK_ID
	AND sm.STOCK_ID = sp.STOCK_ID
	AND st.VOLUME > 499
	AND  st.VOLUME > st.AVG_VOLUME*2
	AND sp.CURRENT_PRICE > sp.PREV_CLOSE
	AND st.SMA_50 > st.SMA_200
	AND sp.CURRENT_PRICE > st.SMA_50
	
-- VOLUME DECREASE PRICE FALL BULLISH
SELECT 
	sm.NSE_SYMBOL,  st.VOLUME, st.AVG_VOLUME, sp.CURRENT_PRICE, sp.PREV_CLOSE
FROM 
	STOCK_TECHNICALS st,STOCK_PRICE sp, STOCK_MASTER sm
where
	sm.STOCK_ID=st.STOCK_ID
	AND sm.STOCK_ID = sp.STOCK_ID
	AND st.VOLUME > 499
	AND  st.VOLUME < st.AVG_VOLUME*2
	AND sp.CURRENT_PRICE < sp.PREV_CLOSE
	AND st.SMA_50 > st.SMA_200
	AND sp.CURRENT_PRICE > st.SMA_50
	
-- VOLUME INCREASE PRICE FALL BEARISH
SELECT 
	sm.NSE_SYMBOL,  st.VOLUME, st.AVG_VOLUME, sp.CURRENT_PRICE, sp.PREV_CLOSE
FROM 
	STOCK_TECHNICALS st,STOCK_PRICE sp, STOCK_MASTER sm
where
	sm.STOCK_ID=st.STOCK_ID
	AND sm.STOCK_ID = sp.STOCK_ID
	AND st.VOLUME > 499
	AND  st.VOLUME > st.AVG_VOLUME*2
	AND sp.CURRENT_PRICE < sp.PREV_CLOSE
	AND st.SMA_50 > st.SMA_200
	AND sp.CURRENT_PRICE > st.SMA_50
	
-- VOLUME DECREASE PRICE RISE BEARISH
SELECT 
	sm.NSE_SYMBOL,  st.VOLUME, st.AVG_VOLUME, sp.CURRENT_PRICE, sp.PREV_CLOSE
FROM 
	STOCK_TECHNICALS st,STOCK_PRICE sp, STOCK_MASTER sm
where
	sm.STOCK_ID=st.STOCK_ID
	AND sm.STOCK_ID = sp.STOCK_ID
	AND st.VOLUME > 499
	AND  st.VOLUME < st.AVG_VOLUME*2
	AND sp.CURRENT_PRICE > sp.PREV_CLOSE
	
	
-- POSITIVE BREAKOUT 200 WITH VOLUME RISE
SELECT sm.NSE_SYMBOL,bl.TYPE, bl.CATEGORY, sp.CURRENT_PRICE,st.SMA_50, st.SMA_200 FROM BREAKOUT_LEDGER bl, STOCK_MASTER sm, STOCK_PRICE sp, STOCK_TECHNICALS st
where to_char(BREAKOUT_DATE,'YYYY-MM-DD')=to_char(sysdate, 'YYYY-MM-DD')
AND sm.STOCK_ID=bl.STOCK_ID
AND sm.STOCK_ID=sp.STOCK_ID
AND sm.STOCK_ID=st.STOCK_ID
AND bl.TYPE='POSITIVE'
AND bl.CATEGORY='CROSS200'
AND  st.VOLUME > st.AVG_VOLUME	* 2


-- NEGATIVE BREAKOUT 200 WITH VOLUME RISE
SELECT sm.NSE_SYMBOL,bl.TYPE, bl.CATEGORY, sp.CURRENT_PRICE,st.SMA_50, st.SMA_200 FROM BREAKOUT_LEDGER bl, STOCK_MASTER sm, STOCK_PRICE sp, STOCK_TECHNICALS st
where to_char(BREAKOUT_DATE,'YYYY-MM-DD')=to_char(sysdate, 'YYYY-MM-DD')
AND sm.STOCK_ID=bl.STOCK_ID
AND sm.STOCK_ID=sp.STOCK_ID
AND sm.STOCK_ID=st.STOCK_ID
AND bl.TYPE='NEGATIVE'
AND bl.CATEGORY='CROSS200'
AND  st.VOLUME > st.AVG_VOLUME	* 2

-- POSITIVE BREAKOUT 50 WITH CURRENT_PRICE > SMA200 
SELECT sm.NSE_SYMBOL,bl.TYPE, bl.CATEGORY, sp.CURRENT_PRICE, sp.PREV_CLOSE, st.SMA_50, st.PREV_SMA_50, st.SMA_200 FROM BREAKOUT_LEDGER bl, STOCK_MASTER sm, STOCK_PRICE sp, STOCK_TECHNICALS st
where to_char(BREAKOUT_DATE,'YYYY-MM-DD')=to_char(sysdate, 'YYYY-MM-DD')
AND sm.STOCK_ID=bl.STOCK_ID
AND sm.STOCK_ID=sp.STOCK_ID
AND sm.STOCK_ID=st.STOCK_ID
AND bl.TYPE='POSITIVE'
AND bl.CATEGORY='CROSS50'
AND sp.CURRENT_PRICE > st.SMA_200
AND  st.VOLUME > st.AVG_VOLUME	* 2

-- NEGATIVE BREAKOUT 50 WITH CURRENT_PRICE > SMA200 
SELECT sm.NSE_SYMBOL,bl.TYPE, bl.CATEGORY, sp.CURRENT_PRICE, sp.PREV_CLOSE, st.SMA_50, st.PREV_SMA_50, st.SMA_200 FROM BREAKOUT_LEDGER bl, STOCK_MASTER sm, STOCK_PRICE sp, STOCK_TECHNICALS st
where to_char(BREAKOUT_DATE,'YYYY-MM-DD')=to_char(sysdate, 'YYYY-MM-DD')
AND sm.STOCK_ID=bl.STOCK_ID
AND sm.STOCK_ID=sp.STOCK_ID
AND sm.STOCK_ID=st.STOCK_ID
AND bl.TYPE='NEGATIVE'
AND bl.CATEGORY='CROSS50'
AND sp.CURRENT_PRICE > st.SMA_200
AND  st.VOLUME > st.AVG_VOLUME	* 2


-- Current Uptrend >200 and > 50
select * from (
select sm.NSE_SYMBOL, sm.INDICe, s.SECTOR_NAME, sp.CURRENT_PRICE, st.SMA_50,st.SMA_200,sf.CURRENT_RATIO,sf.DEBT_EQUITY, sf.ROE, sf.ROCE, (sp.CURRENT_PRICE / sf.EPS) AS PE, s.SECTOR_PE 
from STOCK_MASTER sm, STOCK_TECHNICALS st,STOCK_PRICE sp,STOCK_FACTORS sf, SECTORS s
where sm.STOCK_ID = st.STOCK_ID
and sm.STOCK_ID = sp.STOCK_ID
and sm.STOCK_ID = sf.STOCK_ID
and sm.SECTOR_ID=s.SECTOR_ID
and st.SMA_200 < sp.CURRENT_PRICE
and st.SMA_50 < sp.CURRENT_PRICE
and sm.IS_ACTIVE = 'TRUE'
and sf.CURRENT_RATIO > 1.0
and sf.DEBT_EQUITY < 0.50
and sf.ROE > 15.0
and sf.ROCE > 20.0
) cq where 
cq.PE < 20
and cq.PE < cq.SECTOR_PE
order by cq.INDICE

-- Current Uptrend >200 and < 50
select * from (
select sm.NSE_SYMBOL, sm.INDICe, s.SECTOR_NAME, sp.CURRENT_PRICE, st.SMA_50,st.SMA_200,sf.CURRENT_RATIO,sf.DEBT_EQUITY, sf.ROE, sf.ROCE, (sp.CURRENT_PRICE / sf.EPS) AS PE, s.SECTOR_PE 
from STOCK_MASTER sm, STOCK_TECHNICALS st,STOCK_PRICE sp,STOCK_FACTORS sf, SECTORS s
where sm.STOCK_ID = st.STOCK_ID
and sm.STOCK_ID = sp.STOCK_ID
and sm.STOCK_ID = sf.STOCK_ID
and sm.SECTOR_ID=s.SECTOR_ID
and st.SMA_200 < sp.CURRENT_PRICE
and st.SMA_50 > sp.CURRENT_PRICE
and sm.IS_ACTIVE = 'TRUE'
and sf.CURRENT_RATIO > 1.0
and sf.DEBT_EQUITY < 0.50
and sf.ROE > 15.0
and sf.ROCE > 20.0
) cq where 
cq.PE < 20
and cq.PE < cq.SECTOR_PE
order by cq.INDICE

-- SMA50 > SMA200 and VOLUME HIGH
select * from (
select sm.NSE_SYMBOL, sm.INDICe, s.SECTOR_NAME, sp.PREV_CLOSE, sp.CURRENT_PRICE, st.SMA_50,st.SMA_200,sf.CURRENT_RATIO,sf.DEBT_EQUITY, sf.ROE, sf.ROCE, (sp.CURRENT_PRICE / sf.EPS) AS PE, s.SECTOR_PE, st.VOLUME, st.AVG_VOLUME 
from STOCK_MASTER sm, STOCK_TECHNICALS st,STOCK_PRICE sp,STOCK_FACTORS sf, SECTORS s
where sm.STOCK_ID = st.STOCK_ID
and sm.STOCK_ID = sp.STOCK_ID
and sm.STOCK_ID = sf.STOCK_ID
and sm.SECTOR_ID=s.SECTOR_ID
and st.SMA_200 < st.SMA_50
and sp.CURRENT_PRICE > sp.PREV_CLOSE
and sm.IS_ACTIVE = 'TRUE'
and sf.CURRENT_RATIO > 1.0
and sf.DEBT_EQUITY < 0.50
and sf.ROE > 10.0
--and sf.ROCE > 20.0
) cq where 
cq.PE < 20
and cq.PE < cq.SECTOR_PE
and cq.VOLUME >250
and cq.VOLUME > cq.AVG_VOLUME * 2
order by cq.INDICE


select * from (
select sm.NSE_SYMBOL, sm.INDICE, s.SECTOR_NAME, sf.CURRENT_RATIO, S.SECTOR_CURR_RATIO, sf.DEBT_EQUITY, sp.CURRENT_PRICE, (sp.CURRENT_PRICE / sf.EPS) as PE, s.SECTOR_PE,(sp.CURRENT_PRICE / sf.BOOK_VALUE) as PB,s.SECTOR_PB, sf.ROE, sf.ROCE
from STOCK_MASTER sm, SECTORS s, STOCK_FACTORS sf, STOCK_PRICE sp
where sm.SECTOR_ID=s.SECTOR_ID
AND sm.STOCK_ID=sf.STOCK_ID
AND sm.STOCK_ID=sp.STOCK_ID
AND sm.NSE_SYMBOL in (
'HCLTECH',
'TVTODAY',
'HSCL',
'NATCOPHARM',
'GRAPHITE'
)
)q
order by q.INDICE,q.SECTOR_NAME

HCLTECH++
TVTODAY++
HSCL++
NATCOPHARM++
SUNTV++
GRAPHITE++
KSCL +

https://rubygarage.org/blog/neo4j-database-guide-with-use-cases
https://medium.com/neo4j/how-do-you-know-if-a-graph-database-solves-the-problem-a7da10393f5

https://neo4j.com/developer/java/

https://github.com/neo4j/neo4j/issues/9361

https://stackoverflow.com/questions/38237237/neo4j-finding-the-shortest-path-between-two-nodes-based-on-relationship-proper/38237424
https://neo4j.com/docs/graph-algorithms/current/introduction/#_installation
https://neo4j.com/docs/graph-algorithms/current/algorithms/shortest-path/

select * from (
select sm.NSE_SYMBOL, sm.INDICE, s.SECTOR_NAME, sf.CURRENT_RATIO, S.SECTOR_CURR_RATIO, sf.DEBT_EQUITY, sp.CURRENT_PRICE, (sp.CURRENT_PRICE / sf.EPS) as PE, s.SECTOR_PE,(sp.CURRENT_PRICE / sf.BOOK_VALUE) as PB,s.SECTOR_PB, sf.ROE, sf.ROCE
from STOCK_MASTER sm, SECTORS s, STOCK_FACTORS sf, STOCK_PRICE sp
where sm.SECTOR_ID=s.SECTOR_ID
AND sm.STOCK_ID=sf.STOCK_ID
AND sm.STOCK_ID=sp.STOCK_ID
AND sm.INDICE in ('NIFTY50','NIFTY100')
--AND sf.CURRENT_RATIO > 0.80
AND sf.DEBT_EQUITY < 0.50
)q
where q.PE < q.SECTOR_PE
--AND q.CURRENT_PRICE < 1500.0
AND  (q.PB < 3.0  OR q.PB <q.SECTOR_PB ) 
AND q.ROE > 10.0
order by q.INDICE, q.SECTOR_NAME

--PORTFOLIO AVERAGING
select  * from (
select sm.NSE_SYMBOL, up.USER_ID, sm.INDICE, s.SECTOR_NAME, sf.CURRENT_RATIO, S.SECTOR_CURR_RATIO, sf.DEBT_EQUITY, sp.CURRENT_PRICE , ROUND(up.PRICE,2), 
ROUND((((up.PRICE - sp.CURRENT_PRICE)*100) /up.PRICE),2) PER, ROUND((up.PRICE * up.QUANTITY),2) VAL    , (sp.CURRENT_PRICE / sf.EPS) as PE, 
s.SECTOR_PE,(sp.CURRENT_PRICE / sf.BOOK_VALUE) as PB,s.SECTOR_PB, sf.ROE, sf.ROCE
from USER_PORTFOLIO up, STOCK_MASTER sm, SECTORS s, STOCK_FACTORS sf, STOCK_PRICE sp
where
up.STOCK_ID = sm.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
AND sm.STOCK_ID=sf.STOCK_ID
AND sm.STOCK_ID=sp.STOCK_ID
--AND sm.INDICE in ('NIFTY50','NIFTY100')
AND sf.CURRENT_RATIO > 0.80
AND sf.DEBT_EQUITY < 0.50
--AND up.USER_ID=1
)q
where q.PE < q.SECTOR_PE
AND q.CURRENT_PRICE < 1500.0
AND  (q.PB < 3.0  OR q.PB <q.SECTOR_PB ) 
AND q.ROE > 10.0
AND q.PER > 5.0
--AND q.VAL < 25000.0
order by q.NSE_SYMBOL

Hammer - > Confirmation occurs if the candle following the hammer closes above the closing price of the hammer. Ideally, this confirmation candle shows strong buying. 


https://codeburst.io/i-believe-it-really-depends-on-your-environment-and-how-well-protected-the-different-pieces-are-7919bfa6bc86


db.getCollection('candlesticks_history').find({pattern:'HAMMER',bhavDate: ISODate('2019-07-26T00:00:00.000Z'),close:{$gte:50.0}})

http://blog.arungupta.me/microservice-design-patterns/
https://dzone.com/articles/design-patterns-for-microservices




select * from (
select sm.NSE_SYMBOL, sm.INDICE, s.SECTOR_NAME, sf.CURRENT_RATIO, S.SECTOR_CURR_RATIO, sf.DEBT_EQUITY, sp.CURRENT_PRICE, (sp.CURRENT_PRICE / sf.EPS) as PE, s.SECTOR_PE,(sp.CURRENT_PRICE / sf.BOOK_VALUE) as PB,s.SECTOR_PB, sf.ROE, sf.ROCE
from STOCK_MASTER sm, SECTORS s, STOCK_FACTORS sf, STOCK_PRICE sp
where sm.SECTOR_ID=s.SECTOR_ID
AND sm.STOCK_ID=sf.STOCK_ID
AND sm.STOCK_ID=sp.STOCK_ID
AND sm.NSE_SYMBOL in (
'AUROPHARMA',
'BDL',
'HSCL',
'ITC'
)
)q
order by q.INDICE,q.SECTOR_NAME


