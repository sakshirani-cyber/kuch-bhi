package com.authentication.AuthProject.controller;

import com.authentication.AuthProject.dto.request.LoginRequest;
import com.authentication.AuthProject.dto.request.SignupRequest;
import com.authentication.AuthProject.dto.response.LoginResponse;
import com.authentication.AuthProject.dto.response.SignupResponse;
import com.authentication.AuthProject.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @GetMapping
    public String greet() {
        return "Authentication API is Running";
    }

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(
            @Valid @RequestBody SignupRequest request) {

        SignupResponse response = service.signup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = service.login(request);

        return ResponseEntity.ok(response);
    }
}

//    @PostMapping("/signup")
//    public String signup(@ModelAttribute SignupRequest request, RedirectAttributes redirectAttributes) {
//
//        //ModelAt
//        //change the error name
//        //replace var keyword
//
//
//        //Check for the success message
//        var error = service.signup(request);
//
//        if (error.isPresent()) {
//            redirectAttributes.addFlashAttribute("error", error.get());
//            return "redirect:/signup";
//        }
//
//        redirectAttributes.addFlashAttribute("message", "Signup successful. Please login.");
//        return "redirect:/login";
//    }



//    @GetMapping("/login")
//    public String loginPage() {
//        return "login";
//    }
//
//
//
//    @PostMapping("/login")
//    public String login(@ModelAttribute LoginRequest request, RedirectAttributes redirectAttributes) {
//        var userId = service.login(request);
//
//        if (userId.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", service.getLoginError(request).orElse("Login failed."));
//            return "redirect:/login";
//        }
//
//        return "redirect:/home/" + userId.get();
//    }



//    @GetMapping("/home")
//    public String homeRedirect() {
//        return "redirect:/login";
//    }
//
//
//
//    @GetMapping("/home/{id}")
//    public String homePage(@PathVariable Long id, Model model) {
//        model.addAttribute("userId", id);
//        return "home";
//    }

