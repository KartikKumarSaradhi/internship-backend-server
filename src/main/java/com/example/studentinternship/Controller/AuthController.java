package com.example.studentinternship.Controller;

import com.example.studentinternship.Entity.StudentEntity;
import com.example.studentinternship.Entity.UserEntity;
import com.example.studentinternship.Repository.StudentRepository;
import com.example.studentinternship.Repository.UserRepository;
import com.example.studentinternship.Service.AuthService;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public AuthController(AuthService authService,
                          UserRepository userRepository,
                          StudentRepository studentRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    // -------------------- REGISTER --------------------
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody Map<String, String> user) {
        System.out.println("[AuthController] Received register request: " + user);

        String token = authService.getAdminAccessToken();
        System.out.println("[AuthController] Admin Token retrieved successfully.");

        String url = "http://localhost:8180/admin/realms/internship-portal-realm/users";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("username", user.get("username"));
        newUser.put("email", user.get("email"));
        newUser.put("firstName", user.get("firstName"));
        newUser.put("lastName", user.get("lastName"));
        newUser.put("enabled", true);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", user.get("password"));
        credentials.put("temporary", false);

        newUser.put("credentials", List.of(credentials));

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(newUser, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                // ✅ Save to local DB
                UserEntity newDbUser = new UserEntity();
                newDbUser.setEmail(user.get("email"));
                newDbUser.setUsername(user.get("username"));
                newDbUser.setRole("student");
                newDbUser.setEnabled(true);// ✅ automatically assign student role
                userRepository.save(newDbUser);

                return ResponseEntity.ok("User created successfully with role 'student'!");
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


    // -------------------- LOGIN --------------------
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("client_id", "internship-backend-client");
            formData.add("client_secret", "LgtaAuPxzfoTrkTPzuDVR8XltXo74AuE");
            formData.add("username", username);
            formData.add("password", password);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:8180/realms/internship-portal-realm/protocol/openid-connect/token",
                    new HttpEntity<>(formData, headers),
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @PutMapping("/role/update")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> updateUserRole(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newRole = request.get("role");

        if (email == null || newRole == null) {
            return ResponseEntity.badRequest().body("Email and role are required");
        }

        return userRepository.findByEmail(email)
                .map(user -> {
                    user.setRole(newRole);
                    userRepository.save(user);
                    return ResponseEntity.ok("Role updated successfully for " + email + " to " + newRole);
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found with email: " + email));
    }

}
