package com.qvenly.notifications.client;

import com.qvenly.notifications.client.dto.InternalNotificationRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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
}
