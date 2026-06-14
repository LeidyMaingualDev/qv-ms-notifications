package com.qvenly.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Punto de entrada de qv-ms-notifications.
 * Gestiona correos y notificaciones internas para todos los microservicios Qvenly.
 */
@SpringBootApplication
@EnableAsync
public class QvMsNotificationsApplication {
    public static void main(String[] args) {
        SpringApplication.run(QvMsNotificationsApplication.class, args);
    }
}
