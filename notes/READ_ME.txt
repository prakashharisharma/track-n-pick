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

SELECT SMA_50, SMA_200, PREV_SMA_50, PREV_SMA_200 FROM STOCK_TECHNICALS 

-- BEARISH
select sm.NSE_SYMBOL from STOCK_TECHNICALS st, STOCK_MASTER sm where sm.STOCK_ID=st.STOCK_ID
and PREV_SMA_50 >= PREV_SMA_200
and SMA_50 < SMA_200

--BULLISH
select sm.NSE_SYMBOL from STOCK_TECHNICALS st, STOCK_MASTER sm where sm.STOCK_ID=st.STOCK_ID
and PREV_SMA_200 >= PREV_SMA_50
and SMA_50 > SMA_200



select sm.NSE_SYMBOL, sp.CURRENT_PRICE, sf.LAST_MODIFIED from STOCK_PRICE sp, STOCK_MASTER sm, STOCK_FACTORS sf where sp.STOCK_ID=sm.STOCK_ID
and sp.STOCK_ID=sf.STOCK_ID 
and sp.CURRENT_PRICE > 40 and sp.CURRENT_PRICE < 1000
order by sf.LAST_MODIFIED desc

SELECT sm.NSE_SYMBOL, ul.PB, ul.PE, ul.NIFTY_TYPE, ul.RESEARCH_DATE FROM UNDERVALUE_LEDGER  ul, STOCK_MASTER sm
WHERE ul.STOCK_ID=sm.STOCK_ID

SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE, srl.RESEARCH_STATUS, sm.INDICe FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm
WHERE srl.STOCK_ID=sm.STOCK_ID order by srl.RESEARCH_STATUS, sm.INDICe

SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE, srl.RESEARCH_STATUS, sm.INDICe FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm
WHERE srl.STOCK_ID=sm.STOCK_ID order by srl.ENTRY_DATE desc

SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE, srl.RESEARCH_STATUS, sm.INDICe FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm
WHERE srl.STOCK_ID=sm.STOCK_ID and RESEARCH_STATUS='BUY' order by srl.ENTRY_DATE desc

SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, s.SECTOR_PE, ul.PE, ul.NEW_PE, s.SECTOR_PB, ul.PB, ul.NEW_PB FROM UNDERVALUE_LEDGER ul, STOCK_MASTER sm, SECTORS s
WHERE ul.STOCK_ID=sm.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
 order by ul.RESEARCH_DATE desc

 SELECT sm.NSE_SYMBOL, s.SECTOR_NAME, ul.RESEARCH_DATE, s.SECTOR_PE, ul.PE, ul.NEW_PE, s.SECTOR_PB, ul.PB, ul.NEW_PB FROM UNDERVALUE_LEDGER ul, STOCK_MASTER sm, SECTORS s
WHERE ul.STOCK_ID=sm.STOCK_ID
AND sm.SECTOR_ID=s.SECTOR_ID
AND ul.PE < s.SECTOR_PE
AND ul.PB < s.SECTOR_PB
 order by ul.RESEARCH_DATE desc
 
SELECT sm.NSE_SYMBOL, srl.RESEARCH_TYPE, srl.ENTRY_DATE,srl.EXIT_DATE, srl.RESEARCH_STATUS, sm.INDICe, srl.ENTRY_PRICE, srl.EXIT_PRICE, sp.CURRENT_PRICE FROM STOCK_RESEARCH_LEDGER  srl, STOCK_MASTER sm, STOCK_PRICE sp
WHERE srl.STOCK_ID=sm.STOCK_ID 
AND sm.STOCK_ID=sp.STOCK_ID
AND srl.RESEARCH_STATUS='SELL'
order by srl.RESEARCH_STATUS, sm.INDICe

https://dzone.com/articles/configuring-logback-with-spring-boot


DCMSHRIRAM
TECHM
MINDAIND
AVANTIFEED

UNDERVALUE_LEDGER / CROSSOVER_LEDGER

ALLOCATION
30 40 30 

SELECT * FROM STOCK_FACTORS order by MARKET_CAPITAL desc
a. Large Cap: 1-100 - 
SELECT top 100 * FROM STOCK_FACTORS order by MARKET_CAPITAL desc

b. Mid Cap: 101-250

select * from(
SELECT top 250 * FROM STOCK_FACTORS order by MARKET_CAPITAL desc)
as t250
where t250.STOCK_ID not in (
select STOCK_ID from (SELECT top 100 * FROM STOCK_FACTORS order by MARKET_CAPITAL desc
) as t100
)

c. Small Cap: 251 - onwards

select * from(
SELECT * FROM STOCK_FACTORS order by MARKET_CAPITAL desc)
as t
where t.STOCK_ID not in (
select STOCK_ID from (SELECT top 250 * FROM STOCK_FACTORS order by MARKET_CAPITAL desc
) as t250
)


SUVEN		15
GLENMARK	15
HINDZINC	15
NBCC		15
CASTROLIND	15
ORIENTBANK	15
UNIONBANK	15


NTPC		10
ENGINERSIN	10
NLCINDIA	10
CUMMINSIND	10
IOC			10
RECLTD		10
SBILIFE		10

BHEL		05
COALINDIA	05
ADVENZYMES	05
GPPL		05
NH			05
UCOBANK		05
INOXWIND	05
DISHTV		05
SPTL 		05

https://www.nseindia.com/companytracker/cmtracker.jsp?symbol=BEL&cName=cmtracker_nsedef.css

https://www.nseindia.com/corporates/shldStructure/ShareholdingPattern/shp_table1_mkt_tracker.jsp?ndsId=132912&symbol=BEL&asOnDate=31-DEC-2018

https://www.nseindia.com/marketinfo/companyTracker/resultsCompare.jsp?symbol=BEL
https://www.nseindia.com/marketinfo/companyTracker/compInfo.jsp?symbol=BEL&series=EQ
https://www.nseindia.com/marketinfo/companyTracker/corpAction.jsp?symbol=BEL
https://www.nseindia.com/marketinfo/companyTracker/corpAnnounce.jsp?symbol=BEL

OBV
STOCK_SEARCH


BEL 50 97.10
TECHM 6 777.95
SUVEN 2 255.10


BEL	NIFTY100

EXIDEIND	NIFTY250
AVANTIFEED	NIFTY250
VAKRANGEE	NIFTY250


M&M	NIFTY50	
INFY	NIFTY50	
TECHM NIFTY50
WIPRO	NIFTY50	

JAGRAN	NIFTY500
GUJALKALI	NIFTY500
COCHINSHIP	NIFTY500
BDL	NIFTY500
BALMLAWRIE	NIFTY500
DCMSHRIRAM	NIFTY500	
KSCL	NIFTY500	
FDC	NIFTY500	
CYIENT	NIFTY500
MINDAIND	NIFTY500	
PERSISTENT	NIFTY500	
DBCORP	NIFTY500	
WABAG	NIFTY500	
TVTODAY	NIFTY500	