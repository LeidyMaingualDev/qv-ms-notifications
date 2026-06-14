package com.qvenly.notifications.model.dto.request;

import com.qvenly.notifications.model.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Request para notificaciones de actividades (RF70, RF73.1, RF78.2). */
@Data
public class ActivityNotificationRequest {

    @NotNull
    private NotificationType type;

    @NotBlank
    private String activityTitle;

    @NotNull
    private Long activityId;

    @NotNull
    private Long eventId;

    private String eventTitle;

    /** Email del destinatario */
    @NotBlank
    private String recipientEmail;

    private String recipientName;

    /** ID del usuario en auth (null si no está registrado) */
    private Long recipientUserId;

    /** Detalle: función asignada, motivo de cancelación, etc. */
    private String detail;

    /** Fecha/hora de la actividad */
    private String activityDatetime;
}
