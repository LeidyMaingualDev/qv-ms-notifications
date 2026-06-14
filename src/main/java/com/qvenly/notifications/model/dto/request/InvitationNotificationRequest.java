package com.qvenly.notifications.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Enviado por qv-ms-events cuando se envía una invitación (RF52, RF53). */
@Data
public class InvitationNotificationRequest {

    /** Email del invitado — destino del correo */
    @NotBlank @Email
    private String invitedEmail;

    /** Nombre del invitado (si está registrado) */
    private String invitedName;

    /** ID del usuario invitado en auth (null si no está registrado) */
    private Long invitedUserId;

    /** Nombre del evento */
    @NotBlank
    private String eventTitle;

    /** ID del evento */
    @NotNull
    private Long eventId;

    /** Rol asignado: PARTICIPANT, JUDGE, STAFF, ATTENDEE, ORGANIZER */
    @NotBlank
    private String eventRole;

    /** Token UUID del enlace de aceptación */
    @NotBlank
    private String invitationToken;

    /** Fecha de inicio del evento */
    private String eventStartDatetime;

    /** Fecha de vencimiento de la invitación */
    private String expiresAt;
}
