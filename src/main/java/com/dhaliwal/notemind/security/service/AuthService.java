package com.dhaliwal.notemind.security.service;

import com.dhaliwal.notemind.entity.RefreshToken;
import com.dhaliwal.notemind.entity.User;
import com.dhaliwal.notemind.exception.UsernameAlreadyExistsException;
import com.dhaliwal.notemind.security.refresh.RefreshTokenRepository;
import com.dhaliwal.notemind.security.util.AuthUtil;
import com.dhaliwal.notemind.repository.UserRepository;
import com.dhaliwal.notemind.security.dto.AuthRequestDto;
import com.dhaliwal.notemind.security.dto.AuthResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Creates users and issues access/refresh token pairs. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthUtil authUtil;
    private final RefreshTokenRepository refreshTokenRepository;


    /** Registers a new user and returns the initial token pair. */
    public AuthResponseDto signup(AuthRequestDto authRequestDto) {

        if (userRepository.findByUsername(authRequestDto.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException("Username already exists");
        }

        User user = new User();
        user.setUsername(authRequestDto.getUsername());
        user.setPassword(passwordEncoder.encode(authRequestDto.getPassword()));
        User savedUser = userRepository.save(user);

        String refreshTokenString = authUtil.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(authUtil.hashToken(refreshTokenString))
                .revoked(false)
                .user(user)
                .expiryDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);

        String token = authUtil.generateAccessToken(savedUser);

        AuthResponseDto response = new AuthResponseDto();
        response.setUsername(savedUser.getUsername());
        response.setUserId(savedUser.getId());
        response.setJwtToken(token);
        response.setRefreshToken(refreshTokenString);

        return response;
    }


    /** Verifies credentials and returns a newly issued token pair. */
    public AuthResponseDto login(AuthRequestDto authRequestDto) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                authRequestDto.getUsername(),
                                authRequestDto.getPassword()
                        )
                );


        User user = (User) userRepository.findByUsername(authRequestDto.getUsername())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found")
                );


        String token = authUtil.generateAccessToken(user);

        String refreshTokenString = authUtil.generateRefreshToken();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(authUtil.hashToken(refreshTokenString))
                .revoked(false)
                .user(user)
                .expiryDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(refreshToken);


        AuthResponseDto response = new AuthResponseDto();
        response.setUsername(user.getUsername());
        response.setUserId(user.getId());
        response.setJwtToken(token);
        response.setRefreshToken(refreshTokenString);

        return response;
    }
}