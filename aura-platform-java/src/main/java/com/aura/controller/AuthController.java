package com.aura.controller;

import com.aura.model.User;
import com.aura.service.LoggerService;
import com.aura.service.MongoDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private MongoDbService mongoDbService;

    @Autowired
    private LoggerService logger;

    private static final List<String> AVAILABLE_PHONE_NUMBERS = List.of(
            "1010101010", "1111111111", "1212121212", "1313131313", "1414141414",
            "2020202020", "2121212121", "2222222222", "2525252525", "3333333333",
            "4444444444", "5555555555", "6666666666", "7777777777", "8888888888", "9999999999"
    );

    public static String simpleHash(String str) {
        if (str == null) return "hash_0";
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            hash = ((hash << 5) - hash) + ch;
        }
        return "hash_" + Integer.toHexString(Math.abs(hash));
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        String name = body.get("name");

        if (email == null || password == null || name == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email, password, and name are required"));
        }

        String emailLower = email.toLowerCase().trim();
        Optional<User> existing = mongoDbService.findUser(emailLower);
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "An account with this email already exists. Please sign in."));
        }

        String uid = "user_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 9);
        String passwordHash = simpleHash(password);

        User newUser = new User();
        newUser.setFirebaseUid(uid);
        newUser.setEmail(emailLower);
        newUser.setDisplayName(name);
        newUser.setPasswordHash(passwordHash);
        newUser.setProvider("email");
        newUser.setOnboardingComplete(false);
        newUser.setLastLoginAt(Instant.now());

        User saved = mongoDbService.createUser(newUser);
        logger.success("AUTH", "New user registered: " + name + " (" + emailLower + ")");

        Map<String, Object> respUser = Map.of(
                "uid", saved.getFirebaseUid(),
                "email", saved.getEmail(),
                "displayName", saved.getDisplayName(),
                "createdAt", saved.getCreatedAt().toString(),
                "hasCompletedOnboarding", false
        );

        return ResponseEntity.ok(Map.of("success", true, "user", respUser));
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signin(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and password are required"));
        }

        String emailLower = email.toLowerCase().trim();
        Optional<User> userOpt = mongoDbService.findUser(emailLower);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No account found with this email. Please sign up first."));
        }

        User user = userOpt.get();
        String hashedPassword = simpleHash(password);
        if (!hashedPassword.equals(user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Incorrect password. Please try again."));
        }

        mongoDbService.updateUser(emailLower, Map.of("lastLoginAt", Instant.now()));
        logger.success("AUTH", "User signed in: " + user.getDisplayName() + " (" + emailLower + ")");

        Map<String, Object> respUser = Map.of(
                "uid", user.getFirebaseUid() != null ? user.getFirebaseUid() : user.getId(),
                "email", user.getEmail(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : "User",
                "createdAt", user.getCreatedAt().toString(),
                "hasCompletedOnboarding", Boolean.TRUE.equals(user.getOnboardingComplete())
        );

        return ResponseEntity.ok(Map.of("success", true, "user", respUser));
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> googleAuth(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        String emailLower = email.toLowerCase().trim();
        String uid = (String) body.getOrDefault("uid", "google_" + UUID.randomUUID());
        String displayName = (String) body.getOrDefault("displayName", "Google User");
        String photoURL = (String) body.get("photoURL");

        Optional<User> existing = mongoDbService.findUser(emailLower);
        if (existing.isPresent()) {
            User user = existing.get();
            mongoDbService.updateUser(emailLower, Map.of("lastLoginAt", Instant.now(), "photoURL", photoURL != null ? photoURL : ""));
            logger.success("AUTH", "Google signin: " + displayName + " (" + emailLower + ")");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "isNewUser", false,
                    "user", Map.of(
                            "uid", user.getFirebaseUid() != null ? user.getFirebaseUid() : uid,
                            "email", user.getEmail(),
                            "displayName", user.getDisplayName() != null ? user.getDisplayName() : displayName,
                            "photoURL", photoURL != null ? photoURL : "",
                            "hasCompletedOnboarding", Boolean.TRUE.equals(user.getOnboardingComplete()),
                            "provider", "google"
                    )
            ));
        } else {
            User newUser = new User();
            newUser.setFirebaseUid(uid);
            newUser.setEmail(emailLower);
            newUser.setDisplayName(displayName);
            newUser.setPhotoURL(photoURL);
            newUser.setProvider("google");
            newUser.setOnboardingComplete(false);
            newUser.setLastLoginAt(Instant.now());

            User saved = mongoDbService.createUser(newUser);
            logger.success("AUTH", "New Google signup: " + displayName + " (" + emailLower + ")");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "isNewUser", true,
                    "user", Map.of(
                            "uid", saved.getFirebaseUid(),
                            "email", saved.getEmail(),
                            "displayName", saved.getDisplayName(),
                            "photoURL", photoURL != null ? photoURL : "",
                            "hasCompletedOnboarding", false,
                            "provider", "google"
                    )
            ));
        }
    }

    @PostMapping("/verify-token")
    public ResponseEntity<Map<String, Object>> verifyToken(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, Object> userData = (Map<String, Object>) body.get("userData");
        String uid = userData != null && userData.get("uid") != null
                ? userData.get("uid").toString()
                : "user_" + System.currentTimeMillis();

        String email = userData != null && userData.get("email") != null
                ? userData.get("email").toString()
                : "demo@aura.ai";

        String displayName = userData != null && userData.get("displayName") != null
                ? userData.get("displayName").toString()
                : "Demo User";

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("uid", uid);
        profile.put("email", email);
        profile.put("displayName", displayName);
        profile.put("phoneNumber", null);
        profile.put("availableNumbers", AVAILABLE_PHONE_NUMBERS);

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, Object> body) {
        String uid = (String) body.getOrDefault("uid", "user_" + System.currentTimeMillis());
        String phoneNumber = (String) body.get("phoneNumber");

        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("uid", uid);
        profile.put("email", "demo@aura.ai");
        profile.put("displayName", "Demo User");
        profile.put("phoneNumber", phoneNumber);
        profile.put("availableNumbers", AVAILABLE_PHONE_NUMBERS);

        if (phoneNumber != null) {
            logger.info("AUTH", "Phone linked: " + phoneNumber);
        }

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/complete-onboarding")
    public ResponseEntity<Map<String, Object>> completeOnboarding(@RequestBody Map<String, Object> body) {
        String email = (String) body.get("email");
        @SuppressWarnings("unchecked")
        Map<String, Object> onboardingData = (Map<String, Object>) body.get("onboardingData");

        if (email != null) {
            Map<String, Object> updates = new HashMap<>(onboardingData != null ? onboardingData : Map.of());
            updates.put("onboardingComplete", true);
            mongoDbService.updateUser(email.toLowerCase(), updates);
            logger.success("AUTH", "Onboarding completed and saved for: " + email);
        }

        return ResponseEntity.ok(Map.of("success", true));
    }
}
