package com.authentication.AuthProject.controller;

import com.authentication.AuthProject.dto.LoginRequest;
import com.authentication.AuthProject.dto.SignupRequest;
import com.authentication.AuthProject.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @RequestMapping("/")
    @ResponseBody
    public String greet(){
        return "Project is Running";
    }

    @GetMapping("/signup")
    public String signUpPage(){
        return "signup";
    }

    @PostMapping("/signup")
    @ResponseBody
    public String signup(@RequestBody SignupRequest request){

        return service.signup(request);

    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    @ResponseBody
    public String login(@RequestBody LoginRequest request) {
        return service.login(request);
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }
}
