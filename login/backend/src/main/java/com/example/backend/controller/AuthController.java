package com.example.backend.controller;

import com.example.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    
    private UserService service;

	public AuthController(UserService service) {
		this.service = service;
	}

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String identifier = body.get("identifier");
        String password = body.get("password");

        try {
            service.login(identifier, password);
            return ResponseEntity.ok(Map.of("success", true, "message", "Hi there"));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if ("USER_NOT_FOUND".equals(msg)) {
                return ResponseEntity.status(404).body(Map.of("success", false, "message", msg));
            }
            return ResponseEntity.status(401).body(Map.of("success", false, "message", msg));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");

        try {
            service.signup(username, email, password);
            return ResponseEntity.ok(Map.of("success", true, "message", "Account created successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
