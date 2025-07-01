package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
@ComponentScan(basePackages = {"com.example"})
public class WebApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(WebApplication.class, args);
    }
}
//RESEARCH CANDIDATE
/*
1. ZYDUSWELL 2025-May-17
    - MACD and RSI
    - Volume AVG ++ && vol > avg && vol > prevVol
    - LOWEST to HIGHEST diff 20%
    - LOWEST to LOW diff 2.01%
    - LOWEST to LOWEST diff 7.80%
    - Strong body
2. ANANDRATHI 2025-May-09
    - MACD and RSI
    - Volume AVG ++ && vol > avg && vol > prevVol
    - LOWEST to HIGHEST diff 11.08%
    - LOW to LOWEST diff 2.26%
    - Strong body
3. KRBL 2025-Mar-05
    - MACD and RSI
    - Volume AVG ++ && vol > avg && vol > prevVol
    - LOWEST to HIGHEST diff 13%
    - LOW to LOWEST diff 4.95%
    - Strong body
4. GODIGIT 2025-Apr-15
    - MACD and RSI
    - Volume AVG ++ && prevVol > prevAVG
    - LOWEST to HIGHEST diff 16%
    - LOW to LOWEST diff 2.80%
    - Strong body
5. SUDARSCHEM 2025-Mar-05
    - MACD and RSI
    - Volume AVG ++ && vol > AVG && vol > prevVol
    - LOWEST to HIGHEST diff 18%
    - LOW to LOWEST diff 6%
    - LOW to MEDIUM diff 7%
    - Strong body
6. ADANIENT
7. USHAMART 2025-May-02
    - MACD and RSI
    - Volume AVG ++ && vol > AVG
    - LOWEST to HIGHEST diff 18%
    - LOW to LOWEST diff 3.99%
    - Strong body
8. NORTHARC
9. ANGELONE 2025-Mar-18
    - MACD and RSI
    - Volume AVG ++ && vol > AVG
    - LOWEST to HIGHEST diff 27%
    - LOW to LOWEST diff 6.62%
    - Strong body
10. PIIND
11. APOLLOHOSP 2025-Mar-17
    - MACD and RSI
    - Volume AVG ++ && vol > AVG
    - LOWEST to HIGHEST diff 10.43%
    - LOW to LOWEST diff 0.8%
    - LOW to MEDIUM diff 4.05%
    - Strong body, gap up

12. ULTRACEMCO
13. POWERGRID
14. ICICIBANK
15. RELIANCE 2025-Apr-11
    - MACD and RSI
    - Volume AVG ++ && vol > prevVol
    - LOWEST to HIGHEST diff 12%
    - HIGHEST to HIGH diff 2.72%
    - Strong body, gap up
15. RELIANCE 2025-Jun-20 -- continuation
    - 4 MAs increasing
    - MACD and RSI
    - Volume AVG ++ && vol > AVG
    - LOWEST to HIGHEST diff 8.89%
    - HIGHEST to HIGH diff 0.42%
    - HIGHEST to next low diff 3.07%
    - Strong body
16. SUNPHARMA 2025-Mar-06
    - MACD and RSI
    - Volume AVG ++ && vol > prevVol && vol > AVG
    - LOWEST to HIGHEST diff 11.31%
    - LOWEST to LOW diff 4.57%
    - Hammer
17. SBIN 2025-Mar-04
    - MACD and RSI
    - Volume AVG ++ && vol > prevVol && vol > AVG
    - LOWEST to HIGHEST diff 13%
    - LOWEST to LOW diff 3.22%
    - String Body
18. BPCL
19. HINDALCO 2025-Jan-14
    - MACD and RSI
    - Volume AVG ++ && vol > AVG
    - LOWEST to HIGHEST diff 15%
    - LOWEST to LOW diff 4.5%
    - String Body
20. BHARTIARTL 2025-Jun-16 --Continuation
    - All Ma increasing
    - MACD and RSI
    - Volume AVG ++ && vol > prevVol
    - LOWEST to HIGHEST diff 9.55%
    - HIGH to HIGHEST diff 0.19%
    - HIGH to MEDIUM diff 1.75%
    - HIGH to LOW diff 6.07%
    - String Body
21. UDS 2025-May-07
    - MACD and RSI
    - Volume AVG ++ && prevVol > prevAvg
    - LOWEST to HIGHEST diff 22%
    - LOWEST to LOW diff 3.19%
    - String Body
22. AURIONPRO
23. TATACHEM 2025-May-12
    - MACD and RSI
    - Volume AVG ++ && prevVol > prevAvg
    - LOWEST to HIGHEST diff 19%
    - LOWEST to LOW diff 1.45%
    - LOWEST to nextHigh diff 3.47%
    - String Body, Open close above breakout level
23. TATACHEM 2025-Jun-25
    - 3 MAs increasing
    - MACD and RSI ( macd < signal ) but not less then zero
    - Volume AVG ++ && vol > 2XAVG
    - LOWEST to HIGHEST diff 11%
    - MEDIUM to HIGH diff 0.10%
    - MEDIUM to nextHigh diff 5.67%
    - Strong Body

24. KITEX - 2025-Mar-18
    - MACD and RSI
    - Volume AVG ++ && vol > prevVol
    - LOWEST to HIGHEST diff 27%
    - LOWEST to LOW diff 0.05%
    - LOWEST to nextHigh diff 7%
    - Open Close above breakout level
24. KITEX - 2025-Jun-24 Continuation
    - 4 MA increasing
    - MACD and RSI below 30
    - Volume AVG ++ && vol > prevVol && vol > 1.5X AVG
    - LOWEST to HIGHEST diff 22%
    - MEDIUM to HIGH diff 0.05%
    - MEDIUM to nextHigh diff 4.58%
    - MEDIUM to nextLow diff 13%
    - Strong Body
25. KRBL - 2025-Mar-04
    - MACD and RSI
    - Volume AVG ++ && vol > prevVol
    - LOWEST to HIGHEST diff 13%
    - LOWEST to LOW diff 4.95%
    - Strong Body
26. TORNTPOWER - 2025-Mar-18
     - MACD and RSI
     - Volume AVG ++ && vol > prevVol && vol > 1.25X AVG
     - LOWEST to HIGHEST diff 19%
     - LOWEST to LOW diff 0.076%
     - LOWEST to nextHigher 4.3%
     - Strong Body
27. LAURUSLABS  - 2024-Aug-14
    -
28. KOLTEPATIL - 2025-M14-04
    - MACD and RSI (38.87)
    - Volume AVG ++ && vol > prevVol
    - LOWEST to HIGHEST diff 39%
    - LOWEST to LOW diff 6.88%
    - Strong Body
28. KOLTEPATIL - 2025-May-14 -- continuation
    - 3 Below MA increasing
    - MACD and RSI 69.78 But macd crossed signal
    - Volume AVG ++ && Volume > 4X AVG
    - LOWEST to HIGHEST diff 10%
    - HIGHEST to HIGH diff 4%
    - Strong Body
29. KAJARIACER - 2025-Apr-11
    - MACD and RSI below 30
    - Volume AVG ++ && Volume > 4X AVG
    - LOWEST to HIGHEST diff 40%
    - LOWEST to LOW diff 5%
    - Open Close above Breakout, Hammer
29. KAJARIACER - 2025-Jun-23 -- continuation
    - All Below MA increasing
    - MACD and RSI
    - Volume > 2.3X PrevVolume && Volume > 1.75X AVG
    - LOWEST to HIGHEST diff 17%
    - HIGH to HIGHEST diff 5%
    - Nearest low < 1%
    - HIGH to nextNextLow diff 5%
    - Strong Body
30. DOMS - 2025-Mar-04
    - 3 MA increasing
    - MACD and RSI
    - Volume AVG ++ && Volume > 2X AVG
    - LOWEST to HIGHEST diff 9.6%
    - LOWEST to LOW diff 2%
    - Strong Body, Engulfing
* */
