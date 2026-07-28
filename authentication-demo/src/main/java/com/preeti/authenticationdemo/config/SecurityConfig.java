package com.preeti.authenticationdemo.config;

import com.preeti.authenticationdemo.security.OAuth2SuccessHandler;
import com.preeti.authenticationdemo.service.CustomOAuth2UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String githubClientId;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @PostConstruct
    public void logOAuth2Config() {
        log.info("OAuth2 Registration Status -> Google Client ID loaded: '{}'", maskClientId(googleClientId));
        log.info("OAuth2 Registration Status -> GitHub Client ID loaded: '{}'", maskClientId(githubClientId));
    }

    private String maskClientId(String clientId) {
        if (clientId == null || clientId.isBlank() || clientId.startsWith("dummy-") || clientId.startsWith("your-")) {
            return "NOT CONFIGURED (Placeholder: " + clientId + ")";
        }
        return clientId.length() > 8 ? clientId.substring(0, 8) + "..." : clientId;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/login",
                    "/index.html",
                    "/dashboard.html",
                    "/css/**",
                    "/js/**",
                    "/favicon.ico",
                    "/api/v1/auth/**",
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/index.html")
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            );

        return http.build();
    }
}
