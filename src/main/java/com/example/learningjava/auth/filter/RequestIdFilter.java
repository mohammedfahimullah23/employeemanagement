package com.example.learningjava.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws IOException, ServletException {

        // Generate a unique ID for this request
        String requestId = UUID.randomUUID().toString();

        // Attach it to the request (safe, request-scoped)
        request.setAttribute("requestId", requestId);

        // Also return it in the response (optional but useful)
        response.setHeader("X-Request-Id", requestId);

        // Log it
        System.out.println("RequestId=" + requestId +
                " " + request.getMethod() +
                " " + request.getRequestURI());

        chain.doFilter(request, response);
    }
}

