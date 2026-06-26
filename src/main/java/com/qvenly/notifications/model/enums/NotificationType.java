package com.qvenly.notifications.model.enums;

/**
 * Tipos de notificación. Deben coincidir exactamente con
 * NotificationType en qv-ms-auth para que la inserción en
 * notification_inbox funcione correctamente.
 */
public enum NotificationType {
    // Eventos
    INVITATION_RECEIVED,
    INVITATION_CANCELLED,
    EVENT_CANCELLED,
    EVENT_UPDATED,
    MEMBER_ROLE_CHANGED,
    MEMBER_REMOVED,
    MEMBER_LEFT,
    // Actividades
    ACTIVITY_ASSIGNED,
    ACTIVITY_CANCELLED,
    ACTIVITY_UPDATED
}