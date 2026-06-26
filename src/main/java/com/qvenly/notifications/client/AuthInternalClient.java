package com.qvenly.notifications.client;

import com.qvenly.notifications.client.dto.InternalNotificationRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Cliente HTTP para guardar notificaciones internas en qv-ms-auth.
 * Llama a POST /auth/internal/notifications (sin JWT — red interna).
 * Si el usuario no está registrado (userId null) omite la llamada.
 */
@Slf4j
@Component
public class AuthInternalClient {

    private final WebClient webClient;

    public AuthInternalClient(
            WebClient.Builder builder,
            @Value("${services.auth.url}") String authUrl) {
        this.webClient = builder.baseUrl(authUrl).build();
    }

    public void saveInternalNotification(InternalNotificationRequestDTO dto) {
        if (dto.getUserId() == null) {
            log.debug("userId nulo — usuario no registrado, omitiendo notificación interna para {}",
                    dto.getRecipientEmail());
            return;
        }
        try {
            webClient.post()
                    .uri("/auth/internal/notifications")
                    .bodyValue(dto)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            log.info("Notificación interna guardada — userId={}, tipo={}", dto.getUserId(), dto.getType());
        } catch (Exception e) {
            log.warn("No se pudo guardar notificación interna para userId={}: {}", dto.getUserId(), e.getMessage());
        }
    }

    /**
     * Busca el userId de un usuario registrado a partir de su email.
     * Devuelve null si no está registrado o si la consulta falla.
     */
    @SuppressWarnings("unchecked")
    public Long findUserIdByEmail(String email) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/auth/internal/users/by-email")
                            .queryParam("email", email)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null || response.get("data") == null) return null;
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            Object userId = data.get("userId");
            return userId != null ? Long.valueOf(userId.toString()) : null;
        } catch (Exception e) {
            log.debug("No se encontró usuario registrado para {}: {}", email, e.getMessage());
            return null;
        }
    }
}