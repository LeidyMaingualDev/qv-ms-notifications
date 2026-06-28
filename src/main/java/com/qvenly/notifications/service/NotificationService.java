package com.qvenly.notifications.service;

import com.qvenly.notifications.client.AuthInternalClient;
import com.qvenly.notifications.client.dto.InternalNotificationRequestDTO;
import com.qvenly.notifications.model.dto.request.ActivityNotificationRequest;
import com.qvenly.notifications.model.dto.request.EventNotificationRequest;
import com.qvenly.notifications.model.dto.request.SurveyNotificationRequest;
import com.qvenly.notifications.model.dto.request.InvitationNotificationRequest;
import com.qvenly.notifications.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orquesta el envío de correo + guardado de notificación interna.
 * Para cada evento recibido: envía el correo y luego guarda en notification_inbox.
 * Si el correo falla se loguea pero no lanza excepción — el flujo principal no se detiene.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final EmailService       emailService;
    private final AuthInternalClient authClient;

    // ── RF52, RF53 — Invitación enviada ───────────────────────────────────────

    public void processInvitationSent(InvitationNotificationRequest req) {
        // 1. Correo al invitado (sea o no usuario registrado)
        emailService.sendInvitationEmail(
                req.getInvitedEmail(),
                req.getInvitedName(),
                req.getEventTitle(),
                req.getEventRole(),
                req.getInvitationToken(),
                req.getExpiresAt(),
                req.getEventDescription(),
                req.getEventLocation(),
                req.getEventType(),
                req.getStartDatetime(),
                req.getEndDatetime());

        // 2. Notificación interna solo si tiene userId (usuario registrado)
        // 2. Notificación interna solo si tiene userId (usuario registrado)
        StringBuilder message = new StringBuilder();
        message.append("Fuiste invitado(a) al evento '").append(req.getEventTitle())
                .append("' como ").append(emailService.translateRole(req.getEventRole())).append(".");
        if (req.getEventType() != null && !req.getEventType().isBlank()) {
            message.append(" Tipo: ").append(req.getEventType()).append(".");
        }
        if (req.getStartDatetime() != null && !req.getStartDatetime().isBlank()) {
            message.append(" Inicio: ").append(req.getStartDatetime()).append(".");
        }
        if (req.getEventLocation() != null && !req.getEventLocation().isBlank()) {
            message.append(" Lugar: ").append(req.getEventLocation()).append(".");
        }

        authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                .userId(req.getInvitedUserId())
                .recipientEmail(req.getInvitedEmail())
                .recipientName(req.getInvitedName())
                .type(NotificationType.INVITATION_RECEIVED.name())
                .title("Invitación a evento: " + req.getEventTitle())
                .message(message.toString())
                .build());
    }

    // ── Invitación cancelada ────────────────────────────────────────────────────

    public void processInvitationCancelled(EventNotificationRequest req) {
        for (EventNotificationRequest.Recipient r : req.getRecipients()) {
            emailService.sendInvitationCancelledEmail(
                    r.getEmail(), r.getName(), req.getEventTitle(), req.getDetail());

            authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                    .userId(r.getUserId())
                    .recipientEmail(r.getEmail())
                    .recipientName(r.getName())
                    .type(NotificationType.INVITATION_CANCELLED.name())
                    .title("Invitación cancelada: " + req.getEventTitle())
                    .message("Tu invitación al evento '" + req.getEventTitle()
                            + "' fue cancelada por el organizador."
                            + (req.getDetail() != null && !req.getDetail().isBlank()
                            ? " Motivo: " + req.getDetail() : ""))
                    .build());
        }
    }

    // ── Actividad actualizada ──────────────────────────────────────────────────

    public void processActivityUpdated(ActivityNotificationRequest req) {
        emailService.sendActivityUpdatedEmail(
                req.getRecipientEmail(), req.getRecipientName(),
                req.getActivityTitle(), req.getEventTitle(), req.getDetail());

        Long resolvedUserId = req.getRecipientUserId() != null
                ? req.getRecipientUserId() : authClient.findUserIdByEmail(req.getRecipientEmail());

        authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                .userId(resolvedUserId)
                .recipientEmail(req.getRecipientEmail())
                .recipientName(req.getRecipientName())
                .type(NotificationType.ACTIVITY_UPDATED.name())
                .title("Actividad actualizada: " + req.getActivityTitle())
                .message("La actividad '" + req.getActivityTitle() + "' fue actualizada.")
                .build());
    }

    // ── RF42.1 — Evento cancelado ─────────────────────────────────────────────

    public void processEventCancelled(EventNotificationRequest req) {
        for (EventNotificationRequest.Recipient r : req.getRecipients()) {
            emailService.sendEventCancelledEmail(
                    r.getEmail(), r.getName(), req.getEventTitle(), req.getDetail());

            authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                    .userId(r.getUserId())
                    .recipientEmail(r.getEmail())
                    .recipientName(r.getName())
                    .type(NotificationType.EVENT_CANCELLED.name())
                    .title("Evento cancelado: " + req.getEventTitle())
                    .message("El evento '" + req.getEventTitle() + "' fue cancelado. Motivo: " + req.getDetail())
                    .build());
        }
    }

    // ── RF39.1 — Evento actualizado ───────────────────────────────────────────

    public void processEventUpdated(EventNotificationRequest req) {
        for (EventNotificationRequest.Recipient r : req.getRecipients()) {
            emailService.sendEventUpdatedEmail(
                    r.getEmail(), r.getName(), req.getEventTitle(), req.getDetail());

            authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                    .userId(r.getUserId())
                    .recipientEmail(r.getEmail())
                    .recipientName(r.getName())
                    .type(NotificationType.EVENT_UPDATED.name())
                    .title("Evento actualizado: " + req.getEventTitle())
                    .message("El evento '" + req.getEventTitle() + "' fue modificado.")
                    .build());
        }
    }

    // ── RF59.1 — Cambio de rol ────────────────────────────────────────────────

    public void processMemberRoleChanged(EventNotificationRequest req) {
        for (EventNotificationRequest.Recipient r : req.getRecipients()) {
            emailService.sendMemberRoleChangedEmail(
                    r.getEmail(), r.getName(), req.getEventTitle(), req.getDetail());

            authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                    .userId(r.getUserId())
                    .recipientEmail(r.getEmail())
                    .recipientName(r.getName())
                    .type(NotificationType.MEMBER_ROLE_CHANGED.name())
                    .title("Tu rol fue actualizado en: " + req.getEventTitle())
                    .message("Tu rol en '" + req.getEventTitle() + "' cambió a: " + req.getDetail())
                    .build());
        }
    }

    // ── RF60.1 — Miembro removido ─────────────────────────────────────────────

    public void processMemberRemoved(EventNotificationRequest req) {
        for (EventNotificationRequest.Recipient r : req.getRecipients()) {
            emailService.sendMemberRemovedEmail(
                    r.getEmail(), r.getName(), req.getEventTitle(), req.getDetail());

            authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                    .userId(r.getUserId())
                    .recipientEmail(r.getEmail())
                    .recipientName(r.getName())
                    .type(NotificationType.MEMBER_REMOVED.name())
                    .title("Removido del evento: " + req.getEventTitle())
                    .message("Fuiste removido del evento '" + req.getEventTitle() + "'. Motivo: " + req.getDetail())
                    .build());
        }
    }

    // ── RF70 — Asignado a actividad ───────────────────────────────────────────

    public void processActivityAssigned(ActivityNotificationRequest req) {
        emailService.sendActivityAssignedEmail(
                req.getRecipientEmail(), req.getRecipientName(),
                req.getActivityTitle(), req.getEventTitle(),
                req.getDetail(), null, req.getActivityDatetime());

        Long resolvedUserId = req.getRecipientUserId() != null
                ? req.getRecipientUserId() : authClient.findUserIdByEmail(req.getRecipientEmail());

        authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                .userId(resolvedUserId)
                .recipientEmail(req.getRecipientEmail())
                .recipientName(req.getRecipientName())
                .type(NotificationType.ACTIVITY_ASSIGNED.name())
                .title("Asignado a actividad: " + req.getActivityTitle())
                .message("Fuiste asignado(a) a la actividad '" + req.getActivityTitle()
                        + "' del evento '" + req.getEventTitle() + "'.")
                .build());
    }

    // ── RF73.1 — Actividad cancelada ──────────────────────────────────────────

    public void processActivityCancelled(ActivityNotificationRequest req) {
        emailService.sendActivityCancelledEmail(
                req.getRecipientEmail(), req.getRecipientName(),
                req.getActivityTitle(), req.getEventTitle(), req.getDetail());

        Long resolvedUserId = req.getRecipientUserId() != null
                ? req.getRecipientUserId() : authClient.findUserIdByEmail(req.getRecipientEmail());

        authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                .userId(resolvedUserId)
                .recipientEmail(req.getRecipientEmail())
                .recipientName(req.getRecipientName())
                .type(NotificationType.ACTIVITY_CANCELLED.name())
                .title("Actividad cancelada: " + req.getActivityTitle())
                .message("La actividad '" + req.getActivityTitle() + "' fue cancelada. Motivo: " + req.getDetail())
                .build());
    }

    // Encuesta publicada
    public void processSurveyPublished(SurveyNotificationRequest req) {
    for (EventNotificationRequest.Recipient r : req.getRecipients()) {
        emailService.sendSurveyPublishedEmail(
                r.getEmail(), r.getName(),
                req.getSurveyTitle(), req.getEventTitle(),
                req.getEventId(), req.getSurveyId(), req.getDeadline());

        authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                .userId(r.getUserId())
                .recipientEmail(r.getEmail())
                .recipientName(r.getName())
                .type(NotificationType.SURVEY_PUBLISHED.name())
                .title("Nueva encuesta: " + req.getSurveyTitle())
                .message("El organizador publicó la encuesta '"
                        + req.getSurveyTitle() + "' para el evento '"
                        + req.getEventTitle() + "'. ¡Respóndela antes de que venza!")
                .build());
        }
      }

        // Encuesta cancelada 
       public void processSurveyCancelled(SurveyNotificationRequest req) {
    for (EventNotificationRequest.Recipient r : req.getRecipients()) {
        emailService.sendSurveyCancelledEmail(
                r.getEmail(), r.getName(),
                req.getSurveyTitle(), req.getEventTitle(),
                req.getDeadline());

        if (r.getUserId() == null || r.getUserId() == 0) {
            log.warn("userId inválido para {}, se omite notificación interna", r.getEmail());
            continue;
        }

        authClient.saveInternalNotification(InternalNotificationRequestDTO.builder()
                .userId(r.getUserId())
                .recipientEmail(r.getEmail())
                .recipientName(r.getName())
                .type(NotificationType.SURVEY_CANCELLED.name())
                .title("Encuesta cancelada: " + req.getSurveyTitle())
                .message("La encuesta '" + req.getSurveyTitle() + "' del evento '"
                + req.getEventTitle() + "' fue cancelada. Motivo: " + req.getDeadline())
                .build());
    }
}
}
