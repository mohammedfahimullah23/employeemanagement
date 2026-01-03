package com.example.learningjava.auth.handler;

import com.example.learningjava.auth.util.JwtUtil;
import com.example.learningjava.model.User;
import com.example.learningjava.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuthSuccessHandler
        implements AuthenticationSuccessHandler {

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    private final UserService userService;
    private final JwtUtil jwtUtil;




    public OAuthSuccessHandler(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        User user = userService.findOrCreateOauthUser(email);
        String jwt = jwtUtil.generateAccessToken(user);

        response.sendRedirect(
                redirectUri + "?token=" + jwt
        );

    }
}
