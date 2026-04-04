package com.api.auth.Application.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GeoLocationService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getLocation(String ip) {
        try {
            if (ip == null || ip.equals("127.0.0.1")) {
                return "Localhost";
            }

            String url = "http://ip-api.com/json/" + ip +
                    "?fields=status,message,countryCode,region,city";

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

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
            return "Unknown";
        }
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