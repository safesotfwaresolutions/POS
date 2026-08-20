package com.sciencebot.pos.auth;

import com.sciencebot.pos.auth.internal.services.AuthService;
import com.sciencebot.pos.auth.internal.services.RefreshTokenService;
import com.sciencebot.pos.config.JwtService;
import com.sciencebot.pos.config.PosUserDetails;
import com.sciencebot.pos.users.UserDto;
import com.sciencebot.pos.users.UserFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock private UserDetailsService userDetailsService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private UserFacade userFacade;
    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private static UserDto userDto(boolean active) {
        return new UserDto(7L, "Admin", "admin", "admin@pos.com", "ADMINISTRATOR", active, 1L);
    }

    @Test
    void login_Success() {
        PosUserDetails userDetails = new PosUserDetails(
                "admin", "encodedPassword", true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRATOR")),
                1L
        );

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(passwordEncoder.matches("Password123", "encodedPassword")).thenReturn(true);
        when(userFacade.findByUsername("admin")).thenReturn(Optional.of(userDto(true)));
        when(jwtService.generateToken(any(org.springframework.security.core.userdetails.UserDetails.class), eq("ADMINISTRATOR"), eq(1L))).thenReturn("testToken");
        when(jwtService.getExpirationTime()).thenReturn(900L);
        when(refreshTokenService.issue(7L)).thenReturn("testRefreshToken");

        AuthService.LoginResponse response = authService.login("admin", "Password123");

        assertNotNull(response);
        assertEquals("testToken", response.token());
        assertEquals("testRefreshToken", response.refreshToken());
        assertEquals("admin", response.username());
        assertEquals("ADMINISTRATOR", response.role());
        assertEquals(1L, response.storeId());
        verify(refreshTokenService).issue(7L);
    }

    @Test
    void refresh_RotatesTokenAndIssuesNewAccessToken() {
        var rotation = new RefreshTokenService.RotationResult("newRefresh", 7L, "fam-1");

        when(refreshTokenService.rotate("oldRefresh")).thenReturn(rotation);
        when(userFacade.getById(7L)).thenReturn(userDto(true));
        when(jwtService.generateToken("admin", "ADMINISTRATOR", 1L)).thenReturn("newAccess");
        when(jwtService.getExpirationTime()).thenReturn(900L);

        AuthService.LoginResponse response = authService.refresh("oldRefresh");

        assertEquals("newAccess", response.token());
        assertEquals("newRefresh", response.refreshToken());
        assertEquals("admin", response.username());
        assertEquals(1L, response.storeId());
        verify(refreshTokenService).rotate("oldRefresh");
    }

    @Test
    void refresh_InactiveUser_RevokesFamilyAndThrows() {
        var rotation = new RefreshTokenService.RotationResult("newRefresh", 7L, "fam-1");

        when(refreshTokenService.rotate("oldRefresh")).thenReturn(rotation);
        when(userFacade.getById(7L)).thenReturn(userDto(false));

        assertThrows(DisabledException.class, () -> authService.refresh("oldRefresh"));
        verify(refreshTokenService).revokeFamily("fam-1");
    }

    @Test
    void logout_RevokesRefreshToken() {
        authService.logout("someRefresh");
        verify(refreshTokenService).revoke("someRefresh");
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