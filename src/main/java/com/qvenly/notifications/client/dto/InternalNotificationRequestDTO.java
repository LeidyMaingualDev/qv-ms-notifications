package com.qvenly.notifications.client.dto;

import lombok.Builder;
import lombok.Data;

/** DTO que se envía a qv-ms-auth para guardar en notification_inbox. */
@Data
@Builder
public class InternalNotificationRequestDTO {
    private Long userId;
    private String recipientEmail;
    private String recipientName;
    private String type;
    private String title;
    private String message;
}
