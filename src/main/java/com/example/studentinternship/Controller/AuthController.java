package com.example.studentinternship.Controller;

import com.example.studentinternship.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> user) {
        System.out.println("[AuthController] Received register request: " + user);

        String token = authService.getAdminAccessToken();
        System.out.println("[AuthController] Admin Token retrieved successfully.");

        String url = "http://localhost:8180/admin/realms/internship-portal-realm/users"; // ✅ Correct admin endpoint
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // ✅ Build user payload properly
        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", user.get("username"));
        newUser.put("email", user.get("email"));
        newUser.put("firstName", user.get("firstName"));
        newUser.put("lastName", user.get("lastName"));
        newUser.put("enabled", true);

        // ✅ Proper credentials map
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", user.get("password"));
        credentials.put("temporary", "false"); // ✅ boolean value

        newUser.put("credentials", List.of(credentials));

        System.out.println("[AuthController] Sending new user data: " + newUser);

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(newUser, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            System.out.println("[AuthController] Keycloak Response: " + response.getStatusCode() + " " + response.getBody());

            if (response.getStatusCode() == HttpStatus.CREATED) {
                return ResponseEntity.ok("User created successfully!");
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Failed to create user: " + response.getBody());
            }

        } catch (Exception e) {
            System.err.println("[AuthController] Error while registering user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> user) {
        System.out.println("[AuthController] Login request received for username: " + user.get("username"));

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("client_id", "internship-backend-client");
            formData.add("client_secret", "LgtaAuPxzfoTrkTPzuDVR8XltXo74AuE");
            formData.add("username", user.get("username"));
            formData.add("password", user.get("password"));

            System.out.println("[AuthController] Sending login request to Keycloak with body: " + formData);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://localhost:8180/realms/internship-portal-realm/protocol/openid-connect/token",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            System.out.println("[AuthController] Keycloak Response Status: " + response.getStatusCode());
            System.out.println("[AuthController] Keycloak Response Body: " + response.getBody());

            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            System.err.println("[AuthController] Keycloak Error Response: " + e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }
}
