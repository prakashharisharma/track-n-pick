package com.example.external.ta.service;

import com.example.dto.OHLCV;
import com.example.util.io.model.MCResult;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class McService {

    public List<OHLCV> getMCOHLP(String nseSymbol, int years, int count)
    {
        long startTime = System.currentTimeMillis();

        LocalDateTime to = LocalDateTime.of(2025, 2, 25, 00, 00, 00, 000);
        //LocalDateTime to = LocalDateTime.now();

        LocalDateTime from = to.minusYears(years);


        final String uri = "https://priceapi.moneycontrol.com/techCharts/indianMarket/stock/history?symbol="+ URLEncoder.encode(nseSymbol, StandardCharsets.UTF_8)+"&resolution=1D&from="+from.toEpochSecond(ZoneOffset.UTC)+"&to="+ to.toEpochSecond(ZoneOffset.UTC) +"&countback="+count+"&currencyCode=INR";

        System.out.println("mc url: " + uri);

        RestTemplate restTemplate = new RestTemplate();

        MCResult ohlc = restTemplate.getForObject(uri, MCResult.class);

        long endTime = System.currentTimeMillis();

        System.out.println("Time took to get MC data for " + nseSymbol +" is "+ (endTime - startTime) +"ms");

        List<OHLCV> ohlcvList = this.map(ohlc);

        return ohlcvList;

    }

    private List<OHLCV> map(MCResult ohlc){

        long startTime = System.currentTimeMillis();

        List<OHLCV> ohlcvList = new ArrayList<>();

        List<Long>  dates = ohlc.getT();

        List<Double> opens = ohlc.getO();

        List<Double> highs = ohlc.getH();

        List<Double> lows = ohlc.getL();

        List<Double> closes = ohlc.getC();

        List<Long> volumes = ohlc.getV();

        for(int i=0; i < dates.size(); i++){

            OHLCV ohlcv = new OHLCV(Instant.ofEpochSecond(dates.get(i)), opens.get(i), highs.get(i), lows.get(i), closes.get(i), volumes.get(i));

            ohlcvList.add(ohlcv);

            System.out.println(ohlcv);
        }
        long endTime = System.currentTimeMillis();

        System.out.println("Time took to map OHLCV " + (endTime - startTime) + "ms");

        return ohlcvList;
    }
}
