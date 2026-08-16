package com.sciencebot.pos.auth;

import com.sciencebot.pos.auth.internal.services.AuthService;
import com.sciencebot.pos.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_Success() {
        UserDetails userDetails = User.builder()
                .username("admin")
                .password("encodedPassword")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")))
                .build();

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(passwordEncoder.matches("Password123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(eq(userDetails), eq("ADMINISTRATOR"))).thenReturn("testToken");
        when(jwtService.getExpirationTime()).thenReturn(28800L);

        AuthService.LoginResponse response = authService.login("admin", "Password123");

        assertNotNull(response);
        assertEquals("testToken", response.token());
        assertEquals("admin", response.username());
        assertEquals("ADMINISTRATOR", response.role());
    }

    @Test
    void login_Failure_LocksOutAfter5Attempts() {
        when(userDetailsService.loadUserByUsername("admin")).thenThrow(new UsernameNotFoundException(""));

        for (int i = 0; i < 4; i++) {
            assertThrows(BadCredentialsException.class, () -> authService.login("admin", "wrong"));
        }

        assertThrows(BadCredentialsException.class, () -> authService.login("admin", "wrong"));

        LockedException ex = assertThrows(LockedException.class, () -> authService.login("admin", "wrong"));
        assertTrue(ex.getMessage().contains("bloqueada"));
    }
}
