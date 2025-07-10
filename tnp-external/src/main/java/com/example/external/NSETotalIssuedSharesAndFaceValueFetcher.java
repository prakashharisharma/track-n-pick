package com.example.external;

import com.example.dto.io.FinancialsSummaryDto;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NSETotalIssuedSharesAndFaceValueFetcher {

    public FinancialsSummaryDto getIssuedSharesAndFaceValue(String symbol) {
        String baseUrl = "https://www.nseindia.com";
        String quotesUrl = baseUrl + "/get-quotes/equity?symbol=" + symbol;
        String apiUrl = baseUrl + "/api/quote-equity?symbol=" + symbol;

        HttpClient client =
                HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();

        try {
            // First request to get session cookies
            HttpRequest quotesRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(quotesUrl))
                            .header("User-Agent", "Mozilla/5.0")
                            .header("Accept", "*/*")
                            .header("Accept-Encoding", "gzip, deflate")
                            .GET()
                            .build();

            HttpResponse<String> quotesResponse =
                    client.send(quotesRequest, HttpResponse.BodyHandlers.ofString());

            List<String> cookies = quotesResponse.headers().map().get("set-cookie");
            if (cookies == null || cookies.isEmpty()) {
                System.err.println("Failed to get session cookies!");
                return null; // Return null if we fail to get session cookies
            }

            String cookieHeader = String.join("; ", cookies);

            // Second request to fetch data
            HttpRequest apiRequest =
                    HttpRequest.newBuilder()
                            .uri(URI.create(apiUrl))
                            .header("User-Agent", "Mozilla/5.0")
                            .header("Accept", "application/json")
                            .header("Accept-Encoding", "gzip, deflate")
                            .header("Referer", quotesUrl)
                            .header("Cookie", cookieHeader)
                            .GET()
                            .build();

            HttpResponse<byte[]> apiResponse =
                    client.send(apiRequest, HttpResponse.BodyHandlers.ofByteArray());

            if (apiResponse.statusCode() == 200) {
                byte[] responseBody = apiResponse.body();
                String encoding = apiResponse.headers().firstValue("Content-Encoding").orElse("");

                InputStream is = new ByteArrayInputStream(responseBody);
                if ("gzip".equalsIgnoreCase(encoding)) {
                    is = new GZIPInputStream(is);
                }

                String jsonText = new String(is.readAllBytes());
                JSONObject json = new JSONObject(jsonText);

                if (json.has("securityInfo")) {
                    JSONObject securityInfo = json.getJSONObject("securityInfo");

                    long issuedSize = securityInfo.optLong("issuedSize", -1);
                    double faceValue = securityInfo.optDouble("faceValue", -1);

                    // Return the FinancialsSummaryDto with issuedSize and faceValue
                    return FinancialsSummaryDto.builder()
                            .issuedSize(issuedSize)
                            .faceValue(faceValue)
                            .build();
                } else {
                    System.err.println("Missing 'securityInfo' in JSON.");
                    return null;
                }
            } else {
                System.err.println(
                        "Failed to fetch data. Status Code: " + apiResponse.statusCode());
                return null; // Return null if the request fails
            }
        } catch (Exception e) {
            System.err.println("Exception while fetching issued shares and face value:");
            e.printStackTrace();
            return null; // Return null in case of exception
        }
    }
}
