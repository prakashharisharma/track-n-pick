package com.example.external;

import com.example.dto.integration.StockOverviewResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class Research360Client {

    private final RestTemplate restTemplate = new RestTemplate();

    public StockOverviewResponse fetchStockOverview(String isin) throws Exception {
        String url = "https://www.research360.in/ajax/stockOverviewApiHandler.php?companyISIN=" + isin + "&tbl_flag=extraActivity";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("X-Requested-With", "XMLHttpRequest");
        headers.set("Referer", "https://www.research360.in/");
        headers.set("User-Agent", "Mozilla/5.0");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        // Debug: print the raw HTML if needed
        // System.out.println(response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new Exception("Failed to fetch stock overview: " + response.getStatusCode());
        }

        try {
            return new ObjectMapper().readValue(response.getBody(), StockOverviewResponse.class);
        } catch (JsonProcessingException e) {
            throw new Exception("Failed to parse stock overview JSON", e);
        }
    }

}
