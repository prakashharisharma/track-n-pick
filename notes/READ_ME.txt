select * from stock_master sm where sm.nse_symbol ='E2E'

select * from stock_factors sf where sf.stock_id =3545;
select * from stock_technicals st where st.stock_id =3545;
select * from stock_price sp where sp.stock_id =3545;

select COUNT(1) from stock_technicals st where bhav_date ="2024-07-12";
select * from stock_technicals st where last_modified ="2024-08-27";

select * from stock_technicals st order by last_modified DESC , bhav_date desc;

update stock_technicals set bhav_date ='2024-08-05';

-- BUY Short Term 
select sm.nse_symbol, st.avg_volume, st.volume, sp.current_price, st.ema_5, st.ema_20, st.sma_50, st.sma_200, st.macd, st.signal_line, st.rsi from stock_technicals st 
left join stock_master sm on sm.stock_id =st.stock_id 
left join stock_research_ledger_fundamental srlf on st.stock_id = srlf.stock_id
left join stock_price sp on sp.stock_id = st.stock_id 
where  srlf.research_status = 'BUY'
and st.ema_20 < st.ema_5 
and st.macd > st.signal_line and st.macd > st.prev_macd and st.signal_line > 0
and st.rsi > 55.0  and st.prev_rsi < st.rsi ;

-- BUY Short Term  Volume
select sm.nse_symbol, st.avg_volume, st.volume, sp.current_price, st.ema_5, st.ema_20, st.sma_50, st.sma_200, st.macd, st.signal_line, st.rsi from stock_technicals st 
left join stock_master sm on sm.stock_id =st.stock_id 
left join stock_research_ledger_fundamental srlf on st.stock_id = srlf.stock_id
left join stock_price sp on sp.stock_id = st.stock_id 
where  srlf.research_status = 'BUY'
and st.ema_20 < st.ema_5 
and st.volume > st.avg_volume * 5 ;

-- SELL Short Term
select sm.nse_symbol, st.avg_volume, st.volume, up.price, sp.current_price, st.ema_5, st.ema_20, st.sma_50, st.sma_200, st.macd, st.signal_line, st.rsi  from user_portfolio up 
left join stock_master sm on sm.stock_id =up.stock_id 
left join stock_technicals st on st.stock_id = up.stock_id 
left join stock_price sp on sp.stock_id = st.stock_id 
where st.ema_20 > st.ema_5 
and st.macd < st.signal_line and st.macd < st.prev_macd and st.signal_line < 0;



