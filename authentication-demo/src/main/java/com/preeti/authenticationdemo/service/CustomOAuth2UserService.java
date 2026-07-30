package com.preeti.authenticationdemo.service;

import com.preeti.authenticationdemo.model.User;
import com.preeti.authenticationdemo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        processOAuth2User(registrationId, oAuth2User);

        return oAuth2User;
    }

    private void processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(registrationId, attributes);
        String name = extractName(registrationId, attributes);

        log.info("Processing OAuth2 login for provider '{}', email '{}'", registrationId, email);

        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);

        if (existingUser.isEmpty()) {
            String username = generateUniqueUsername(name, registrationId);
            String dummyPassword = passwordEncoder.encode(UUID.randomUUID().toString());

            User newUser = new User(
                    null,
                    username,
                    dummyPassword,
                    normalizedEmail,
                    null,
                    LocalDate.of(2000, 1, 1),
                    26,
                    true
            );

            userRepository.save(newUser);
            log.info("Created new OAuth2 user: '{}' with email '{}'", username, normalizedEmail);
        } else {
            log.info("OAuth2 user with email '{}' logged in successfully", normalizedEmail);
        }
    }

    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return (String) attributes.get("email");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            String email = (String) attributes.get("email");
            if (email == null) {
                String login = (String) attributes.get("login");
                return login + "@github.com";
            }
            return email;
        }
        return (String) attributes.get("email");
    }

    private String extractName(String registrationId, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(registrationId)) {
            return (String) attributes.get("name");
        } else if ("github".equalsIgnoreCase(registrationId)) {
            String name = (String) attributes.get("name");
            return name != null ? name : (String) attributes.get("login");
        }
        return "User";
    }

    private String generateUniqueUsername(String name, String registrationId) {
        String baseName = name != null ? name.replaceAll("[^a-zA-Z0-9]", "") : "OAuthUser";
        if (baseName.length() < 3) {
            baseName = registrationId + baseName;
        }
        if (baseName.length() > 15) {
            baseName = baseName.substring(0, 15);
        }
        String candidate = baseName;
        int count = 1;
        while (userRepository.findByUsername(candidate).isPresent()) {
            candidate = baseName + count++;
        }
        return candidate;
    }
}
