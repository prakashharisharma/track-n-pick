package com.example.external.ta.service;

import com.example.dto.OHLCV;
import com.example.dto.io.MCResult;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
public class McService {

    public List<OHLCV> getMCOHLP(String nseSymbol, int years, int countback)
    {
        LocalDateTime to = LocalDateTime.of(2025, 3, 20, 00, 00, 00, 000);
        //LocalDateTime to = LocalDateTime.now();

        LocalDateTime from = to.minusYears(years);

        RestTemplate restTemplate = new RestTemplate();

        String baseUrl = "https://priceapi.moneycontrol.com/techCharts/indianMarket/stock/history";

        String resolution = "1D";
        String currencyCode = "INR";

        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("symbol", nseSymbol)  // Automatically encodes special characters
                .queryParam("resolution", resolution)
                .queryParam("from", from.toEpochSecond(ZoneOffset.UTC))
                .queryParam("to", to.toEpochSecond(ZoneOffset.UTC))
                .queryParam("countback", countback)
                .queryParam("currencyCode", currencyCode)
                .build()
                .encode()  // Ensure encoding is applied
                .toUri();


        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
        headers.set("Accept", "*/*");
        headers.set("Referer", "https://www.moneycontrol.com/");
        headers.set("Origin", "https://www.moneycontrol.com");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        System.out.println("URI: " + uri);
        // Make GET Request with Headers
        ResponseEntity<MCResult> response = restTemplate.exchange(uri, HttpMethod.GET, entity, MCResult.class);
        System.out.println("responseCode: " + response.getStatusCode());
        // Get the Response Body
        MCResult ohlc = response.getBody();

        List<OHLCV> ohlcvList = this.map(ohlc);

        return ohlcvList;

    }

    private List<OHLCV> map(MCResult ohlc){

        List<OHLCV> ohlcvList = new ArrayList<>();

        List<Long>  dates = ohlc.getT();

        List<Double> opens = ohlc.getO();

        List<Double> highs = ohlc.getH();

        List<Double> lows = ohlc.getL();

        List<Double> closes = ohlc.getC();

        List<Long> volumes = ohlc.getV();

        for(int i=0; i < dates.size(); i++){

            OHLCV ohlcv = new OHLCV(Instant.ofEpochSecond(dates.get(i)), opens.get(i), highs.get(i), lows.get(i), closes.get(i), volumes.get(i));
            System.out.println(ohlcv);
            ohlcvList.add(ohlcv);

        }

        return ohlcvList;
    }
}
