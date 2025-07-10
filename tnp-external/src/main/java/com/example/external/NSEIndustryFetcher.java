package com.example.external;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NSEIndustryFetcher {

    public String getIndustry(String symbol) {
        String baseUrl = "https://www.nseindia.com";
        String quotesUrl = baseUrl + "/get-quotes/equity?symbol=" + symbol;
        String apiUrl = baseUrl + "/api/equity-meta-info?symbol=" + symbol;

        HttpClient client =
                HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

        try {
            // Step 1: Hit NSE quotes page to get session cookies
            HttpRequest quotesRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(quotesUrl))
                            .header(
                                    "User-Agent",
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                                        + " (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .header("Accept", "*/*")
                            .GET()
                            .build();

            HttpResponse<String> quotesResponse =
                    client.send(quotesRequest, HttpResponse.BodyHandlers.ofString());

            // Extract cookies from response headers
            Map<String, List<String>> headers = quotesResponse.headers().map();
            List<String> cookies = headers.get("set-cookie");

            if (cookies == null || cookies.isEmpty()) {
                return "Failed to get session cookies!";
            }

            String cookieHeader = String.join("; ", cookies);

            // Step 2: Hit the actual API with cookies from the quotes page
            HttpRequest apiRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .header(
                                    "User-Agent",
                                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                                        + " (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .header("Accept", "application/json, text/plain, */*")
                            .header("Referer", quotesUrl)
                            .header("Cookie", cookieHeader)
                            .GET()
                            .build();

            HttpResponse<String> apiResponse =
                    client.send(apiRequest, HttpResponse.BodyHandlers.ofString());

            if (apiResponse.statusCode() == 200) {
                JSONObject json = new JSONObject(apiResponse.body());
                return json.optString("industry", "Industry not found");
            } else {
                return "Failed to fetch data, Status Code: " + apiResponse.statusCode();
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
