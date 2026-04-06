package com.api.auth.Application.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Slf4j
@Service
public class GeoLocationService {

    private final RestTemplate restTemplate;

    @Value("${auth.geolocation.retry.max-attempts:2}")
    private int maxAttempts;

    @Value("${auth.geolocation.retry.backoff-ms:150}")
    private long retryBackoffMs;

    public GeoLocationService(@Value("${auth.geolocation.connect-timeout-ms:700}") long connectTimeoutMs,
                              @Value("${auth.geolocation.read-timeout-ms:1000}") long readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) connectTimeoutMs);
        requestFactory.setReadTimeout((int) readTimeoutMs);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public String getLocation(String ip) {
        try {
            if (ip == null || ip.equals("127.0.0.1")) {
                return "Localhost";
            }

            String url = "http://ip-api.com/json/" + ip +
                    "?fields=status,message,countryCode,region,city";

            Map<String, Object> response = fetchWithRetry(url);

            if (response == null) {
                return "Unknown";
            }

            if (!"success".equals(response.get("status"))) {
                return "Unknown";
            }

            String city = (String) response.get("city");
            String region = (String) response.get("region");
            String country = (String) response.get("countryCode");

            // monta string mais completa
            return buildLocation(city, region, country);

        } catch (Exception e) {
            log.warn("[GEO] Location lookup failed - ip={} reason={}", ip, e.getMessage());
            return "Unknown";
        }
    }

    private Map<String, Object> fetchWithRetry(String url) {
        int attempts = Math.max(1, maxAttempts);

        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<>() {
                        }
                );
                return response.getBody();
            } catch (RestClientException ex) {
                if (attempt == attempts) {
                    throw ex;
                }

                log.warn("[GEO] Lookup attempt failed - attempt={} maxAttempts={} reason={}",
                        attempt, attempts, ex.getMessage());

                try {
                    Thread.sleep(retryBackoffMs * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Geo lookup interrupted", interrupted);
                }
            }
        }

        return null;
    }

    private String buildLocation(String city, String region, String country) {
        StringBuilder location = new StringBuilder();

        if (city != null && !city.isBlank()) {
            location.append(city);
        }

        if (region != null && !region.isBlank()) {
            if (!location.isEmpty()) location.append(", ");
            location.append(region);
        }

        if (country != null && !country.isBlank()) {
            if (!location.isEmpty()) location.append(", ");
            location.append(country);
        }

        return location.isEmpty() ? "Unknown" : location.toString();
    }
}