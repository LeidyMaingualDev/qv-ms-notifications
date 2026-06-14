package com.qvenly.notifications.model.dto.request;

import com.qvenly.notifications.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

/**
 * Request genérico para notificaciones de eventos.
 * Usado para: cancelación, actualización, cambio de rol, eliminación de miembro.
 */
@Data
public class EventNotificationRequest {

    @NotNull
    private NotificationType type;

    @NotBlank
    private String eventTitle;

    @NotNull
    private Long eventId;

    /** Lista de destinatarios {userId, email, name} */
    @NotNull
    private List<Recipient> recipients;

    /** Detalle adicional según el tipo: motivo de cancelación, nuevo rol, etc. */
    private String detail;

    @Data
    public static class Recipient {
        private Long userId;
        @NotBlank
        private String email;
        private String name;
    }
}
