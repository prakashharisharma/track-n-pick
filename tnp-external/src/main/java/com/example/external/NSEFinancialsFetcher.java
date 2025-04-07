package com.example.external;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NSEFinancialsFetcher {

    public String fetchXbrl(String symbol, LocalDate quarterEnded) {
        String baseUrl = "https://www.nseindia.com";
        String quotesUrl = baseUrl + "/get-quotes/equity?symbol=" + symbol;
        String apiUrl =
                baseUrl
                        + "/api/corporates-financial-results?index=equities&symbol="
                        + symbol
                        + "&period=Quarterly";

        HttpClient client =
                HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(15))
                        .build();

        try {
            // Step 1: Request quotes page to get session cookies
            HttpRequest quotesRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(quotesUrl))
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                            .header(
                                    "Accept",
                                    "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                            .header("Accept-Language", "en-US,en;q=0.5")
                            .GET()
                            .build();

            HttpResponse<String> quotesResponse =
                    client.send(quotesRequest, HttpResponse.BodyHandlers.ofString());

            List<String> cookies = quotesResponse.headers().map().get("set-cookie");
            if (cookies == null || cookies.isEmpty()) {
                return "Failed to get session cookies!";
            }

            String cookieHeader =
                    cookies.stream()
                            .map(cookie -> cookie.split(";", 2)[0])
                            .collect(Collectors.joining("; "));

            Thread.sleep(1500); // Small delay to mimic human behavior

            // Step 2: Use cookies to access the actual financials API
            HttpRequest apiRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                            .header("Accept", "application/json")
                            .header("Referer", quotesUrl)
                            .header("Cookie", cookieHeader)
                            .GET()
                            .build();

            HttpResponse<String> apiResponse =
                    client.send(apiRequest, HttpResponse.BodyHandlers.ofString());

            if (apiResponse.statusCode() == 200) {
                JSONArray results = new JSONArray(apiResponse.body());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
                String fallbackXbrl = null;

                for (int i = 0; i < results.length(); i++) {
                    JSONObject record = results.getJSONObject(i);

                    String toDateStr = record.optString("toDate");
                    String consolidated = record.optString("consolidated", "");
                    String xbrl = record.optString("xbrl", "");

                    if (toDateStr == null || xbrl == null || xbrl.isBlank()) {
                        continue;
                    }

                    LocalDate toDate;
                    try {
                        toDate = LocalDate.parse(toDateStr, formatter);
                    } catch (Exception e) {
                        continue;
                    }

                    if (toDate.equals(quarterEnded)) {
                        if ("Consolidated".equalsIgnoreCase(consolidated)) {
                            return xbrl; // ✅ Best match
                        } else {
                            fallbackXbrl = xbrl; // 🕒 Potential fallback
                        }
                    }
                }

                if (fallbackXbrl != null) {
                    return fallbackXbrl; // Return non-consolidated match
                }

                return "No matching record found for date: " + quarterEnded;
            } else {
                return "API request failed. Status code: " + apiResponse.statusCode();
            }

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
