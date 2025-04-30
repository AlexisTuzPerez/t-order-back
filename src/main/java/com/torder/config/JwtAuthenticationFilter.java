package com.torder.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;


    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }


    // In JwtAuthenticationFilter.java
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
//        System.out.println("=== JWT Filter Started ===");
//        System.out.println("Request URI: " + request.getRequestURI());

        //final String authHeader = request.getHeader("Authorization");
        final String cookieJwt = extractJwtFromCookies(request);
        final String jwt;
        final String userEmail;

//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            //System.out.println("No Bearer token found in Authorization header");
//            filterChain.doFilter(request, response);
//            return;
//        }

        //System.out.println("Cookie: " +  cookieJwt );

//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
////
////            jwt = authHeader.substring(7);
////        } else
        if (cookieJwt != null) {
            jwt = cookieJwt;
        } else {

        filterChain.doFilter(request, response);
        return;
        }


        //System.out.println("JWT Token extracted: " + jwt.substring(0, 10) + "...");

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
            }
        }

        filterChain.doFilter(request, response);
        //System.out.println("=== JWT Filter Completed ===");
    }
    private String extractJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("toDoAppCookie".equals(cookie.getName())) {  // Ensure this matches your cookie name
                    return cookie.getValue();  // Return JWT from cookie
                }
            }
        }
        return null;
    }
}