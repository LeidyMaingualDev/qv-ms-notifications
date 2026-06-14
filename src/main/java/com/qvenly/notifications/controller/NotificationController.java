package com.qvenly.notifications.controller;

import com.qvenly.notifications.model.dto.request.ActivityNotificationRequest;
import com.qvenly.notifications.model.dto.request.EventNotificationRequest;
import com.qvenly.notifications.model.dto.request.InvitationNotificationRequest;
import com.qvenly.notifications.model.dto.response.ApiResponse;
import com.qvenly.notifications.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints de notificaciones para qv-ms-events y qv-ms-activities.
 * Todos los endpoints son de red interna — no están expuestos por el Gateway.
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** RF52, RF53 — Invitación enviada a un usuario */
    @PostMapping("/invitation-sent")
    public ResponseEntity<ApiResponse<Void>> invitationSent(
            @Valid @RequestBody InvitationNotificationRequest request) {
        log.info("Procesando notificación de invitación para: {}", request.getInvitedEmail());
        notificationService.processInvitationSent(request);
        return ResponseEntity.ok(ApiResponse.success("Notificación de invitación procesada."));
    }

    /** RF42.1 — Evento cancelado */
    @PostMapping("/event-cancelled")
    public ResponseEntity<ApiResponse<Void>> eventCancelled(
            @Valid @RequestBody EventNotificationRequest request) {
        log.info("Procesando notificación de cancelación de evento: {}", request.getEventTitle());
        notificationService.processEventCancelled(request);
        return ResponseEntity.ok(ApiResponse.success("Notificaciones de cancelación procesadas."));
    }

    /** RF39.1 — Evento actualizado */
    @PostMapping("/event-updated")
    public ResponseEntity<ApiResponse<Void>> eventUpdated(
            @Valid @RequestBody EventNotificationRequest request) {
        log.info("Procesando notificación de actualización de evento: {}", request.getEventTitle());
        notificationService.processEventUpdated(request);
        return ResponseEntity.ok(ApiResponse.success("Notificaciones de actualización procesadas."));
    }

    /** RF59.1 — Rol de miembro cambiado */
    @PostMapping("/member-role-changed")
    public ResponseEntity<ApiResponse<Void>> memberRoleChanged(
            @Valid @RequestBody EventNotificationRequest request) {
        log.info("Procesando notificación de cambio de rol en evento: {}", request.getEventTitle());
        notificationService.processMemberRoleChanged(request);
        return ResponseEntity.ok(ApiResponse.success("Notificación de cambio de rol procesada."));
    }

    /** RF60.1 — Miembro removido del evento */
    @PostMapping("/member-removed")
    public ResponseEntity<ApiResponse<Void>> memberRemoved(
            @Valid @RequestBody EventNotificationRequest request) {
        log.info("Procesando notificación de remoción de miembro en evento: {}", request.getEventTitle());
        notificationService.processMemberRemoved(request);
        return ResponseEntity.ok(ApiResponse.success("Notificación de remoción procesada."));
    }

    /** RF70 — Asignado a una actividad */
    @PostMapping("/activity-assigned")
    public ResponseEntity<ApiResponse<Void>> activityAssigned(
            @Valid @RequestBody ActivityNotificationRequest request) {
        log.info("Procesando notificación de asignación a actividad: {}", request.getActivityTitle());
        notificationService.processActivityAssigned(request);
        return ResponseEntity.ok(ApiResponse.success("Notificación de asignación procesada."));
    }

    /** RF73.1 — Actividad cancelada */
    @PostMapping("/activity-cancelled")
    public ResponseEntity<ApiResponse<Void>> activityCancelled(
            @Valid @RequestBody ActivityNotificationRequest request) {
        log.info("Procesando notificación de cancelación de actividad: {}", request.getActivityTitle());
        notificationService.processActivityCancelled(request);
        return ResponseEntity.ok(ApiResponse.success("Notificación de cancelación de actividad procesada."));
    }
}
