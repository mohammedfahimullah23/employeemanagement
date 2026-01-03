package com.example.learningjava.auth.security;

import com.example.learningjava.auth.filter.JwtFilter;
import com.example.learningjava.auth.filter.RequestIdFilter;
import com.example.learningjava.auth.handler.OAuthSuccessHandler;
import com.example.learningjava.auth.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.List;

@Configuration
public class SecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http,
                        JwtFilter jwtFilter,
                        RequestIdFilter requestIdFilter,
                        OAuthSuccessHandler oAuthSuccessHandler,
                        CustomOAuth2UserService customOAuth2UserService,
                        NotFoundAuthenticationEntryPoint notFoundAuthenticationEntryPoint,
                        ForbiddenAccessDeniedHandler forbiddenAccessDeniedHandler) throws Exception {

                http
                                .cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                // I am using JWT so we will add this above line
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                // Ensures every request is independent by telling it is stateless
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/auth/**").permitAll()
                                                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                                                .requestMatchers("/api/**").authenticated()
                                                .anyRequest().permitAll())
                                .exceptionHandling(ex -> ex
                                                .defaultAuthenticationEntryPointFor(
                                                                notFoundAuthenticationEntryPoint,
                                                                new AntPathRequestMatcher("/api/**"))
                                                .accessDeniedHandler(forbiddenAccessDeniedHandler))
                                // If we go to different path which is not found then we need to show not found,
                                // without this it was showing github error page

                                .oauth2Login(oauth -> oauth
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(oAuthSuccessHandler))
                                .addFilterBefore(requestIdFilter, AuthorizationFilter.class)
                                .addFilterBefore(jwtFilter,
                                                UsernamePasswordAuthenticationFilter.class)
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable);

                return http.build();
        }

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
                var config = new org.springframework.web.cors.CorsConfiguration();

                config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);

                var source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);

                return source;
        }

}
