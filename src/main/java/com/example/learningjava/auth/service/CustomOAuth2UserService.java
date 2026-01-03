package com.example.learningjava.auth.service;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class CustomOAuth2UserService
                implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

        private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest)
                        throws OAuth2AuthenticationException {

                OAuth2User oauthUser = delegate.loadUser(userRequest);

                String email = oauthUser.getAttribute("email");

                // 🔴 GitHub-specific fix
                if (email == null) {
                        email = fetchPrimaryEmail(userRequest);
                }

                return new DefaultOAuth2User(
                                oauthUser.getAuthorities(),
                                Map.of("email", email),
                                "email");
        }

        private String fetchPrimaryEmail(OAuth2UserRequest request) {

                String token = request.getAccessToken().getTokenValue();

                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);

                HttpEntity<?> entity = new HttpEntity<>(headers);

                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                                "https://api.github.com/user/emails",
                                HttpMethod.GET,
                                entity,
                                new ParameterizedTypeReference<>() {
                                });

                return response.getBody().stream()
                                .filter(email -> Boolean.TRUE.equals(email.get("primary")))
                                .map(email -> (String) email.get("email"))
                                .findFirst()
                                .orElseThrow(() -> new OAuth2AuthenticationException(
                                                "No email found from GitHub"));
        }
}
