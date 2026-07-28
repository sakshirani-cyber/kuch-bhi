package com.preeti.authenticationdemo.security;

import com.preeti.authenticationdemo.model.User;
import com.preeti.authenticationdemo.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public OAuth2SuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = authToken.getPrincipal();
        String registrationId = authToken.getAuthorizedClientRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(registrationId, attributes);
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";

        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        String username = userOptional.map(User::getUsername).orElse("User");

        log.info("OAuth2 login successful for email '{}', resolving to username '{}'", normalizedEmail, username);

        getRedirectStrategy().sendRedirect(request, response, "/dashboard.html?username=" + username);
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
}
