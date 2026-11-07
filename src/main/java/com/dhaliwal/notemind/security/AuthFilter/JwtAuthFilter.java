package com.dhaliwal.notemind.security.AuthFilter;

import com.dhaliwal.notemind.entity.User;
import com.dhaliwal.notemind.repository.UserRepository;
import com.dhaliwal.notemind.security.util.AuthUtil;
import com.dhaliwal.notemind.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Extracts and validates bearer tokens before protected requests reach controllers. */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final ObjectMapper objectMapper;


    @Override
    /** Writes a consistent JSON authentication error when token validation fails. */
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {


        String authorizationHeader = request.getHeader("Authorization");


        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }


        String token = authorizationHeader.substring(7);


        final String username;
        try {
            username = authUtil.getUsernameFromToken(token);
        } catch (ExpiredJwtException ex) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token has expired.");
            return;
        } catch (MalformedJwtException ex) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token is malformed.");
            return;
        } catch (JwtException | IllegalArgumentException ex) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT token is invalid.");
            return;
        }


        if (username != null &&
                SecurityContextHolder.getContext()
                        .getAuthentication() == null) {


            final User user;
            try {
                user = (User) userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException("User not found")
                        );
            } catch (UsernameNotFoundException ex) {
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "User is not authenticated.");
                return;
            }


            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );


            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }


        filterChain.doFilter(request, response);
    }

    private void writeError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}