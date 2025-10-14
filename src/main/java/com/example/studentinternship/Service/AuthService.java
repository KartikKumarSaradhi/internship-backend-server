package com.example.studentinternship.Service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthService {

    // Method to get admin access token
    public String getAdminAccessToken() {
        String url = "http://localhost:8180/realms/internship-portal-realm/protocol/openid-connect/token"; // Keycloak token endpoint

        // ✅ Use MultiValueMap for form data (Keycloak only supports URL-encoded format)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", "internship-backend-client");
        formData.add("client_secret", "LgtaAuPxzfoTrkTPzuDVR8XltXo74AuE");

        // Headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Build the HTTP entity with headers and body
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            System.out.println("[AuthService] Sending admin token request to Keycloak...");

            // Send POST request to Keycloak token endpoint
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            // Extract the access token from the response
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                System.out.println("[AuthService] Received admin token response: " + responseBody);

                if (responseBody != null && responseBody.containsKey("access_token")) {
                    return (String) responseBody.get("access_token"); // Return the access token
                } else {
                    throw new RuntimeException("Access token not found in Keycloak response.");
                }
            } else {
                throw new RuntimeException("Failed to fetch access token. Keycloak responded with status: " + response.getStatusCode());
            }

        } catch (Exception ex) {
            System.err.println("[AuthService] Failed to get admin token: " + ex.getMessage());
            throw new RuntimeException("Error while fetching admin token: " + ex.getMessage(), ex);
        }
    }
}