package com.example.external.dhan;

import com.example.external.dhan.model.*;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DhanIntegrationService {

    @Autowired private RestTemplate restTemplate;

    private static final String DHAN_API_BASE_URL = "https://api.dhan.co";
    private static final String DHAN_AUTH_BASE_URL = "https://auth.dhan.co";

    private <T> HttpEntity<T> createAuthEntity(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("access-token", accessToken);
        return new HttpEntity<>(headers);
    }

    private <T> HttpEntity<T> createAuthEntity(String accessToken, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("access-token", accessToken);
        return new HttpEntity<>(body, headers);
    }

    public ResponseEntity<List<Holding>> getHoldings(String accessToken) {
        return restTemplate.exchange(
                DHAN_API_BASE_URL + "/v2/holdings",
                HttpMethod.GET,
                createAuthEntity(accessToken),
                new ParameterizedTypeReference<List<Holding>>() {});
    }

    public ResponseEntity<FundLimit> getFundLimit(String accessToken) {
        return restTemplate.exchange(
                DHAN_API_BASE_URL + "/v2/fundlimit",
                HttpMethod.GET,
                createAuthEntity(accessToken),
                FundLimit.class);
    }

    public ResponseEntity<OrderResponse> placeOrder(String accessToken, OrderRequest request) {
        return restTemplate.exchange(
                DHAN_API_BASE_URL + "/v2/orders",
                HttpMethod.POST,
                createAuthEntity(accessToken, request),
                OrderResponse.class);
    }

    public ResponseEntity<List<Order>> getOrders(String accessToken) {
        return restTemplate.exchange(
                DHAN_API_BASE_URL + "/v2/orders",
                HttpMethod.GET,
                createAuthEntity(accessToken),
                new ParameterizedTypeReference<List<Order>>() {});
    }

    public ResponseEntity<Order> getOrder(String accessToken, String orderId) {
        return restTemplate.exchange(
                DHAN_API_BASE_URL + "/v2/orders/{order-id}",
                HttpMethod.GET,
                createAuthEntity(accessToken),
                Order.class,
                orderId);
    }

    public ResponseEntity<List<Trade>> getTrades(String accessToken) {
        return restTemplate.exchange(
                DHAN_API_BASE_URL + "/trades",
                HttpMethod.GET,
                createAuthEntity(accessToken),
                new ParameterizedTypeReference<List<Trade>>() {});
    }

    public ResponseEntity<ConsentResponse> generateConsent(
            String dhanClientId, String apiKey, String apiSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("app_id", apiKey);
        headers.set("app_secret", apiSecret);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                DHAN_AUTH_BASE_URL + "/app/generate-consent?client_id={client_id}",
                HttpMethod.POST,
                entity,
                ConsentResponse.class,
                dhanClientId);
    }

    /**
     * Step 2: Build the browser login URL for Dhan consent app login. Open this URL directly in a
     * browser; after successful login + 2FA, Dhan redirects to your configured redirect URL with
     * tokenId.
     */
    public String buildConsentLoginUrl(String consentAppId) {
        // Use partner login host per observed flow
        return UriComponentsBuilder.fromHttpUrl("https://partner-login.dhan.co/")
                .queryParam("consentAppId", consentAppId)
                .build()
                .toUriString();
    }
}
