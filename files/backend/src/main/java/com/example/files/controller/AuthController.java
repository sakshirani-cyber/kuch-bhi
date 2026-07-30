package com.example.files.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session, @AuthenticationPrincipal OAuth2User user) {
        Map<String, Object> result = new HashMap<>();
        if (user != null) {
            result.put("loggedIn", true);
            result.put("name", user.getAttribute("login"));
            result.put("avatar", user.getAttribute("avatar_url"));
            result.put("userId", "github_" + user.getAttribute("id"));
        } else {
            String guestId = (String) session.getAttribute("guestId");
            if (guestId == null) {
                guestId = "guest_" + UUID.randomUUID().toString();
                session.setAttribute("guestId", guestId);
            }
            result.put("loggedIn", false);
            result.put("name", "Guest");
            result.put("userId", guestId);
        }
        return result;
    }
}
