package com.sciencebot.pos.auth;

import com.sciencebot.pos.auth.internal.controllers.AuthController;
import com.sciencebot.pos.auth.internal.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController.setCookieProperties("jwt_token", false, "Strict");
    }

    @Test
    void login_ReturnsTokenAndSetsHttpOnlyCookie() {
        AuthService.LoginResponse mockResponse = new AuthService.LoginResponse(
                "mocked-jwt-token",
                "admin",
                "ADMINISTRATOR",
                28800L
        );
        when(authService.login("admin", "admin123")).thenReturn(mockResponse);

        ResponseEntity<AuthService.LoginResponse> response = authController.login(
                Map.of("username", "admin", "password", "admin123")
        );

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("admin", response.getBody().username());
        assertEquals("mocked-jwt-token", response.getBody().token());

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("jwt_token=mocked-jwt-token"));
        assertTrue(setCookieHeader.contains("HttpOnly"));
        assertTrue(setCookieHeader.contains("SameSite=Strict"));
        assertTrue(setCookieHeader.contains("Max-Age=28800"));
        assertTrue(setCookieHeader.contains("Path=/"));
    }

    @Test
    void logout_ClearsHttpOnlyCookieWithMaxAgeZero() {
        ResponseEntity<Void> response = authController.logout();

        assertNotNull(response);
        assertEquals(204, response.getStatusCode().value());

        String setCookieHeader = response.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertNotNull(setCookieHeader);
        assertTrue(setCookieHeader.contains("jwt_token="));
        assertTrue(setCookieHeader.contains("Max-Age=0"));
        assertTrue(setCookieHeader.contains("HttpOnly"));
        assertTrue(setCookieHeader.contains("SameSite=Strict"));
    }
}
