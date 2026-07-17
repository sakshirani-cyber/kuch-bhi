package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
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
            User user = service.login(identifier, password);
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("username", user.getUsername());
            userDetails.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            userDetails.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            userDetails.put("email", user.getEmail());
			userDetails.put("gender", user.getGender() != null ? user.getGender() : "");
            userDetails.put("contactNumber", user.getContactNumber() != null ? user.getContactNumber() : "");
            userDetails.put("dob", user.getDob() != null ? user.getDob().toString() : "");
            userDetails.put("age", user.getAge());
            userDetails.put("address", user.getAddress() != null ? user.getAddress() : "");
            userDetails.put("collegeName", user.getCollegeName() != null ? user.getCollegeName() : "");
            userDetails.put("schoolName", user.getSchoolName() != null ? user.getSchoolName() : "");
            userDetails.put("currentcompany", user.getCurrentcompany() != null ? user.getCurrentcompany() : "");

            return ResponseEntity.ok(Map.of("success", true, "message", "Hi there", "user", userDetails));
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
		String firstName = body.get("firstName");
		String lastName = body.get("lastName");
        String email = body.get("email");
        String password = body.get("password");
		String gender = body.get("gender");
        String contactNumber = body.get("contactNumber");
        String dobStr = body.get("dob");
        LocalDate dob = (dobStr != null && !dobStr.isEmpty()) ? LocalDate.parse(dobStr) : null;
        String address = body.get("address");
        String collegeName = body.get("collegeName");
        String schoolName = body.get("schoolName");
        String currentcompany = body.get("currentcompany");

        try {
            service.signup(username, firstName, lastName, email, gender, password, contactNumber, dob, address, collegeName, schoolName, currentcompany);
            return ResponseEntity.ok(Map.of("success", true, "message", "Account created successfully"));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> body) {
        String identifier = body.get("identifier"); // identifier = email or username
		String firstName = body.get("firstName");
		String lastName = body.get("lastname");
		String gender = body.get("gender");
        String password = body.get("password");
        String contactNumber = body.get("contactNumber");
        String dobStr = body.get("dob");
        LocalDate dob = (dobStr != null && !dobStr.isEmpty()) ? LocalDate.parse(dobStr) : null;
        String address = body.get("address");
        String collegeName = body.get("collegeName");
        String schoolName = body.get("schoolName");
        String currentcompany = body.get("currentcompany");

        try {
            User user = service.updateProfile(identifier, firstName, lastName, gender, password, contactNumber, dob, address, collegeName, schoolName, currentcompany);
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("username", user.getUsername());
			userDetails.put("firstName", user.getFirstName());
			userDetails.put("lastName", user.getLastName());
			userDetails.put("gender", user.getGender());
            userDetails.put("email", user.getEmail());
            userDetails.put("contactNumber", user.getContactNumber() != null ? user.getContactNumber() : "");
            userDetails.put("dob", user.getDob() != null ? user.getDob().toString() : "");
            userDetails.put("age", user.getAge());
            userDetails.put("address", user.getAddress() != null ? user.getAddress() : "");
            userDetails.put("collegeName", user.getCollegeName() != null ? user.getCollegeName() : "");
            userDetails.put("schoolName", user.getSchoolName() != null ? user.getSchoolName() : "");
            userDetails.put("currentcompany", user.getCurrentcompany() != null ? user.getCurrentcompany() : "");

            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully", "user", userDetails));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Authentication failed: " + e.getMessage()));
        }
    }
}
