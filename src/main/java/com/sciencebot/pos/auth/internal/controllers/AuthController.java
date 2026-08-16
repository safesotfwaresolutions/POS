package com.sciencebot.pos.auth.internal.controllers;

import com.sciencebot.pos.auth.internal.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "🔐 Autenticación", description = "Login y gestión de sesión JWT con Cookies HttpOnly y soporte Bearer Token")
public class AuthController {

    private final AuthService authService;

    @Value("${security.jwt.cookie-name:jwt_token}")
    private String cookieName = "jwt_token";

    @Value("${security.jwt.cookie-secure:false}")
    private boolean cookieSecure = false;

    @Value("${security.jwt.cookie-same-site:Strict}")
    private String cookieSameSite = "Strict";

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void setCookieProperties(String cookieName, boolean cookieSecure, String cookieSameSite) {
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Autentica al usuario con credenciales `username` y `password`.
                    Emite una **Cookie HttpOnly** segura (`SameSite=Strict`, `Secure`) con el token JWT para máxima
                    protección contra ataques XSS, y devuelve la respuesta JSON con metadatos del usuario y token.
                    
                    El token tiene vigencia de 8 horas por defecto.
                    """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                     example = "{\"username\": \"admin\", \"password\": \"admin123\"}"
                            ),
                            examples = {
                                    @ExampleObject(name = "Administrador", value = "{\"username\": \"admin\", \"password\": \"admin123\"}"),
                                    @ExampleObject(name = "Vendedor", value = "{\"username\": \"seller01\", \"password\": \"pass1234\"}")
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso — emite Cookie HttpOnly y devuelve datos de sesión",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthService.LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content),
            @ApiResponse(responseCode = "400", description = "Cuerpo de solicitud inválido", content = @Content)
    })
    public ResponseEntity<AuthService.LoginResponse> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        AuthService.LoginResponse response = authService.login(username, password);

        ResponseCookie cookie = ResponseCookie.from(cookieName, response.token())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(response.expiresIn())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = """
                    Invalida la sesión del usuario eliminando la cookie HttpOnly (`Max-Age=0`).
                    No requiere cuerpo de solicitud.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sesión cerrada correctamente y cookie eliminada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado", content = @Content)
    })
    public ResponseEntity<Void> logout() {
        ResponseCookie cleanCookie = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }
}
