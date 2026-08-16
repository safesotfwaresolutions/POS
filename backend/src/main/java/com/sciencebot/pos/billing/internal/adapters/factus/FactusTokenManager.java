package com.sciencebot.pos.billing.internal.adapters.factus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class FactusTokenManager {

    @Value("${factus.url:https://api-sandbox.factus.com.co}")
    private String factusUrl;

    @Value("${factus.client-id:}")
    private String clientId;

    @Value("${factus.client-secret:}")
    private String clientSecret;

    @Value("${factus.username:}")
    private String username;

    @Value("${factus.password:}")
    private String password;

    private final RestClient restClient;

    private String tokenGuardado;
    private long expiracionTiempoMilis = 0;

    public FactusTokenManager() {
        this.restClient = RestClient.create();
    }

    // Constructor para testing
    public FactusTokenManager(RestClient restClient, String factusUrl, String clientId, String clientSecret, String username, String password) {
        this.restClient = restClient;
        this.factusUrl = factusUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.username = username;
        this.password = password;
    }

    /**
     * Obtiene un access_token válido para la API de Factus.
     * Si el token en memoria aún es válido, lo retorna inmediatamente.
     * Si no existe o ya expiró (considerando un margen de seguridad de 60s), solicita uno nuevo.
     */
    public synchronized String getAccessToken() {
        // 1. Si el token actual aún es válido, lo devolvemos sin llamar a Factus
        if (tokenGuardado != null && System.currentTimeMillis() < expiracionTiempoMilis) {
            return tokenGuardado;
        }

        // 2. Si es nulo o ya expiró, pedimos uno nuevo
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId != null ? clientId : "");
        formData.add("client_secret", clientSecret != null ? clientSecret : "");
        formData.add("username", username != null ? username : "");
        formData.add("password", password != null ? password : "");

        String targetUrl = (factusUrl != null && !factusUrl.isBlank())
                ? factusUrl
                : "https://api-sandbox.factus.com.co";

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restClient.post()
                .uri(targetUrl + "/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(formData)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("No se pudo obtener el token de autenticación desde Factus API. Respuesta nula o inválida.");
        }

        // 3. Guardamos el token
        this.tokenGuardado = (String) response.get("access_token");

        // 4. Calculamos cuándo caduca (con conversión segura de tipos)
        Object expiresInRaw = response.getOrDefault("expires_in", 3600);
        int expiresInSeconds = (expiresInRaw instanceof Number n) ? n.intValue() : 3600;

        // Le restamos 60 segundos como margen de seguridad anti-expiración en tránsito
        this.expiracionTiempoMilis = System.currentTimeMillis() + ((expiresInSeconds - 60) * 1000L);

        return this.tokenGuardado;
    }
}
