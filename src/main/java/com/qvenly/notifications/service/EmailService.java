package com.qvenly.notifications.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio de correos HTML para eventos y actividades.
 * Mismo patrón y paleta visual que EmailService en qv-ms-auth.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // Paleta Qvenly
    private static final String TEAL      = "#14b8a6";
    private static final String TEAL_DK   = "#0d9488";
    private static final String RED       = "#ef4444";
    private static final String WHITE     = "#ffffff";
    private static final String BG        = "#f9fafb";
    private static final String TEXT      = "#111827";
    private static final String TEXT_SOFT = "#6b7280";
    private static final String BORDER    = "#e5e7eb";

    // ── RF52 — Invitación a evento ─────────────────────────────────────────────

    @Async
    public void sendInvitationEmail(String toEmail, String toName, String eventTitle,
                                    String eventRole, String token, String expiresAt,
                                    String eventDescription, String eventLocation,
                                    String eventType, String startDatetime, String endDatetime) {
        try {
            String link = frontendUrl + "/auth/accept-invitation?token=" + token;
            String header = headerTitle("Tienes una invitaci&oacute;n");

            String eventInfo = "<strong style=\"color:" + TEXT + ";\">" + eventTitle + "</strong>";
            if (eventType != null && !eventType.isBlank()) {
                eventInfo += " <span style=\"color:" + TEXT_SOFT + ";\">(" + eventType + ")</span>";
            }

            StringBuilder details = new StringBuilder();
            if (startDatetime != null && !startDatetime.isBlank()) {
                details.append("Inicio: ").append(startDatetime);
            }
            if (endDatetime != null && !endDatetime.isBlank()) {
                details.append("<br>Fin: ").append(endDatetime);
            }
            if (eventLocation != null && !eventLocation.isBlank()) {
                details.append("<br>Lugar: ").append(eventLocation);
            }

            StringBuilder roleAndDetails = new StringBuilder();
            roleAndDetails.append("Rol: <strong>").append(translateRole(eventRole)).append("</strong>");
            if (details.length() > 0) {
                roleAndDetails.append("<br>").append(details);
            }

            String body = greeting(toName != null ? toName : "usuario")
                    + paragraph("Has sido invitado(a) a participar en el evento " + eventInfo + ".")
                    + (eventDescription != null && !eventDescription.isBlank()
                    ? paragraph(eventDescription) : "")
                    + infoBox(roleAndDetails.toString(), "#f0fdfa", "#99f6e4", TEAL_DK)
                    + ctaButton(link, "Aceptar invitaci&oacute;n", TEAL)
                    + divider()
                    + fallbackLink(link, TEAL)
                    + divider()
                    + (expiresAt != null
                    ? smallNote("Esta invitaci&oacute;n vence el " + expiresAt + ".")
                    : smallNote("Esta invitaci&oacute;n tiene una fecha l&iacute;mite de aceptaci&oacute;n."));
            send(toEmail, "Invitaci\u00f3n a evento en Qvenly: " + eventTitle,
                    baseTemplate(TEAL, header, body));
        } catch (Exception e) {
            log.error("Error al enviar invitación a {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Invitación cancelada ────────────────────────────────────────────────────

    @Async
    public void sendInvitationCancelledEmail(String toEmail, String toName,
                                             String eventTitle, String reason) {
        try {
            String header = headerTitle("Invitaci&oacute;n cancelada");
            String body = greeting(toName != null ? toName : "usuario")
                    + paragraph("La invitaci&oacute;n que recibiste para el evento <strong style=\"color:"
                    + TEXT + ";\">" + eventTitle + "</strong> ha sido <strong style=\"color:" + RED
                    + ";\">cancelada</strong> por el organizador.")
                    + (reason != null && !reason.isBlank()
                    ? infoBox("Motivo: " + reason, "#fef2f2", "#fecaca", RED) : "")
                    + paragraph("Si tienes dudas, puedes contactar directamente al organizador del evento.");
            send(toEmail, "Invitaci\u00f3n cancelada: " + eventTitle, baseTemplate(RED, header, body));
        } catch (Exception e) {
            log.error("Error al enviar cancelación de invitación a {}: {}", toEmail, e.getMessage());
        }
    }

    // ── RF42.1 — Evento cancelado ──────────────────────────────────────────────

    @Async
    public void sendEventCancelledEmail(String toEmail, String toName,
                                        String eventTitle, String reason) {
        try {
            String header = headerTitle("Evento cancelado");
            String body = greeting(toName != null ? toName : "usuario")
                    + paragraph("Te informamos que el evento <strong style=\"color:" + TEXT + ";\">"
                    + eventTitle + "</strong> ha sido <strong style=\"color:" + RED
                    + ";\">cancelado</strong>.")
                    + infoBox("Motivo: " + reason, "#fef2f2", "#fecaca", RED)
                    + paragraph("Lamentamos los inconvenientes. Si tienes dudas, contacta al organizador.")
                    + ctaButton(frontendUrl + "/dashboard-user/events", "Ver mis eventos", TEAL);
            send(toEmail, "Evento cancelado: " + eventTitle, baseTemplate(RED, header, body));
        } catch (Exception e) {
            log.error("Error al enviar cancelación de evento a {}: {}", toEmail, e.getMessage());
        }
    }

    // ── RF39.1 — Evento actualizado ────────────────────────────────────────────

    @Async
    public void sendEventUpdatedEmail(String toEmail, String toName,
                                      String eventTitle, String detail) {
        try {
            String header = headerTitle("Evento actualizado");
            String body = greeting(toName != null ? toName : "usuario")
                    + paragraph("El evento <strong style=\"color:" + TEXT + ";\">" + eventTitle
                    + "</strong> ha sido actualizado.")
                    + (detail != null ? infoBox(detail, "#f0fdfa", "#99f6e4", TEAL_DK) : "")
                    + ctaButton(frontendUrl + "/dashboard-user/events", "Ver el evento", TEAL);
            send(toEmail, "Actualizaci\u00f3n en el evento: " + eventTitle,
                    baseTemplate(TEAL, header, body));
        } catch (Exception e) {
            log.error("Error al enviar actualización de evento a {}: {}", toEmail, e.getMessage());
        }
    }

    // ── RF59.1 — Rol cambiado ──────────────────────────────────────────────────

    @Async
    public void sendMemberRoleChangedEmail(String toEmail, String toName,
                                           String eventTitle, String newRole) {
        try {
            String header = headerTitle("Tu rol fue actualizado");
            String body = greeting(toName != null ? toName : "usuario")
                    + paragraph("Tu rol en el evento <strong style=\"color:" + TEXT + ";\">"
                    + eventTitle + "</strong> ha sido actualizado.")
                    + infoBox("Nuevo rol: <strong>" + translateRole(newRole) + "</strong>",
                    "#f0fdfa", "#99f6e4", TEAL_DK)
                    + ctaButton(frontendUrl + "/dashboard-user/events", "Ver el evento", TEAL);
            send(toEmail, "Cambio de rol en: " + eventTitle, baseTemplate(TEAL, header, body));
        } catch (Exception e) {
            log.error("Error al enviar cambio de rol a {}: {}", toEmail, e.getMessage());
        }
    }

    // ── RF60.1 — Miembro eliminado ─────────────────────────────────────────────

    @Async
    public void sendMemberRemovedEmail(String toEmail, String toName,
                                       String eventTitle, String reason) {
        try {
            String header = headerTitle("Has sido removido de un evento");
            String body = greeting(toName != null ? toName : "usuario")
                    + paragraph("Has sido removido del evento <strong style=\"color:" + TEXT + ";\">"
                    + eventTitle + "</strong>.")
                    + (reason != null ? infoBox("Motivo: " + reason, "#fef2f2", "#fecaca", RED) : "")
                    + paragraph("Si crees que esto es un error, contacta al organizador.");
            send(toEmail, "Has sido removido del evento: " + eventTitle,
                    baseTemplate(RED, header, body));
        } catch (Exception e) {
            log.error("Error al enviar remoción a {}: {}", toEmail, e.getMessage());
        }
    }

    // ── RF70 — Asignado a actividad ────────────────────────────────────────────

    @Async
    public void sendActivityAssignedEmail(String toEmail, String toName, String activityTitle,
                                          String eventTitle, String role, String function,
                                          String activityDatetime) {
        try {
            String header = headerTitle("Asignado a una actividad");
            String detail = "Rol: <strong>" + translateRole(role) + "</strong>"
                    + (function != null ? "<br>Funci&oacute;n: " + function : "")
                    + (activityDatetime != null ? "<br>Fecha: " + activityDatetime : "");
            String body = greeting(toName != null ? toName : "usuario")
                    + paragraph("Has sido asignado(a) a la actividad <strong style=\"color:" + TEXT + ";\">"
                    + activityTitle + "</strong> del evento <strong>" + eventTitle + "</strong>.")
                    + infoBox(detail, "#f0fdfa", "#99f6e4", TEAL_DK)
                    + ctaButton(frontendUrl + "/dashboard-user/events", "Ver el evento", TEAL);
            send(toEmail, "Asignado a actividad: " + activityTitle, baseTemplate(TEAL, header, body));
        } catch (Exception e) {
            log.error("Error al enviar asignación de actividad a {}: {}", toEmail, e.getMessage());
        }
    }

    // ── RF73.1 — Actividad cancelada ───────────────────────────────────────────

    @Async
    public void sendActivityCancelledEmail(String toEmail, String toName,
                                           String activityTitle, String eventTitle,
                                           String reason) {
        try {
            String header = headerTitle("Actividad cancelada");
            String body = greeting(toName != null ? toName : "usuario")
                    + paragraph("La actividad <strong style=\"color:" + TEXT + ";\">"
                    + activityTitle + "</strong> del evento <strong>" + eventTitle
                    + "</strong> ha sido cancelada.")
                    + (reason != null ? infoBox("Motivo: " + reason, "#fef2f2", "#fecaca", RED) : "")
                    + ctaButton(frontendUrl + "/dashboard-user/events", "Ver el evento", TEAL);
            send(toEmail, "Actividad cancelada: " + activityTitle, baseTemplate(RED, header, body));
        } catch (Exception e) {
            log.error("Error al enviar cancelación de actividad a {}: {}", toEmail, e.getMessage());
        }
    }

    // ── Actividad actualizada ───────────────────────────────────────────────────

    @Async
    public void sendActivityUpdatedEmail(String toEmail, String toName,
                                         String activityTitle, String eventTitle,
                                         String detail) {
        try {
            String header = headerTitle("Actividad actualizada");
            String body = greeting(toName != null ? toName : "usuario")
                    + paragraph("La actividad <strong style=\"color:" + TEXT + ";\">"
                    + activityTitle + "</strong> del evento <strong>" + eventTitle
                    + "</strong> ha sido actualizada.")
                    + (detail != null && !detail.isBlank()
                    ? infoBox(detail, "#f0fdfa", "#99f6e4", TEAL_DK) : "")
                    + ctaButton(frontendUrl + "/dashboard-user/events", "Ver el evento", TEAL);
            send(toEmail, "Actualizaci\u00f3n en la actividad: " + activityTitle,
                    baseTemplate(TEAL, header, body));
        } catch (Exception e) {
            log.error("Error al enviar actualización de actividad a {}: {}", toEmail, e.getMessage());

        }
    }

    // - Envío de notificación por encuesta publicada 

    @Async
    public void sendSurveyPublishedEmail(String toEmail, String toName,
                                        String surveyTitle, String eventTitle,
                                        Long eventId, Long surveyId, String deadline) {
        try {
            String link = frontendUrl + "/dashboard-user/surveys";
            String header = headerTitle("Tienes una encuesta por responder");
            String body = greeting(toName != null ? toName : "usuario")
                    + paragraph("El organizador del evento <strong style=\"color:" + TEXT + ";\">"
                    + eventTitle + "</strong> publicó una nueva encuesta:")
                    + infoBox("<strong>" + surveyTitle + "</strong>"
                    + (deadline != null && !deadline.isBlank()
                            ? "<br>Fecha l&iacute;mite: " + deadline : ""),
                            "#f0fdfa", "#99f6e4", TEAL_DK)
                    + paragraph("Tu opini&oacute;n es importante. Toma un momento para completarla.")
                    + ctaButton(link, "Responder encuesta", TEAL);
            send(toEmail, "Nueva encuesta: " + surveyTitle, baseTemplate(TEAL, header, body));
        } catch (Exception e) {
            log.error("Error al enviar notificación de encuesta a {}: {}", toEmail, e.getMessage());
        }
    }

    // Encuesta cancelada

        @Async
        public void sendSurveyCancelledEmail(String toEmail, String toName,
                                        String surveyTitle, String eventTitle,
                                        String reason) {
        try {
                String header = headerTitle("Encuesta cancelada");
                String body = greeting(toName != null ? toName : "usuario")
                        + paragraph("La encuesta <strong style=\"color:" + TEXT + ";\">"
                        + surveyTitle + "</strong> del evento <strong>" + eventTitle
                        + "</strong> ha sido <strong style=\"color:" + RED + ";\">cancelada</strong>.")
                        + (reason != null && !reason.isBlank()
                        ? infoBox("Motivo: " + reason, "#fef2f2", "#fecaca", RED) : "")
                        + paragraph("Si tienes dudas, contacta al organizador del evento.");
                send(toEmail, "Encuesta cancelada: " + surveyTitle, baseTemplate(TEAL, header, body));
        } catch (Exception e) {
                log.error("Error al enviar cancelación de encuesta a {}: {}", toEmail, e.getMessage());
        }
        }

    // ═══════════════════════════════════════════════════════════════════════════
    // MÉTODOS HELPER — Construcción de HTML
    // ═══════════════════════════════════════════════════════════════════════════

    private void send(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Correo enviado a {}: {}", to, subject);
        } catch (Exception e) {
            log.error("Error al enviar correo a {}: {}", to, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String baseTemplate(String accentColor, String headerHtml, String bodyHtml) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'/></head><body style='"
                + "margin:0;padding:0;background:" + BG + ";font-family:DM Sans,sans-serif;'>"
                + "<table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center' style='padding:32px 16px;'>"
                + "<table width='600' cellpadding='0' cellspacing='0' style='"
                + "background:" + WHITE + ";border-radius:12px;border:1px solid " + BORDER + ";overflow:hidden;'>"
                + "<tr><td style='background:" + accentColor + ";padding:28px 32px;'>"
                + "<span style='font-size:22px;font-weight:700;color:" + WHITE + ";letter-spacing:-0.5px;'>Qvenly</span>"
                + "</td></tr>"
                + "<tr><td style='padding:32px;'>" + headerHtml + bodyHtml + "</td></tr>"
                + "<tr><td style='background:" + BG + ";padding:20px 32px;text-align:center;"
                + "font-size:12px;color:" + TEXT_SOFT + ";border-top:1px solid " + BORDER + ";'>"
                + "Este correo fue generado autom&aacute;ticamente por Qvenly. Por favor no respondas este mensaje."
                + "</td></tr></table></td></tr></table></body></html>";
    }

    private String headerTitle(String title) {
        return "<h1 style='margin:0 0 20px;font-size:20px;font-weight:700;color:" + TEXT
                + ";letter-spacing:-0.3px;'>" + title + "</h1>";
    }

    private String greeting(String name) {
        return "<p style='margin:0 0 16px;font-size:15px;color:" + TEXT + ";'>"
                + "Hola <strong>" + name + "</strong>,</p>";
    }

    private String paragraph(String content) {
        return "<p style='margin:0 0 16px;font-size:14px;color:" + TEXT + ";line-height:1.6;'>"
                + content + "</p>";
    }

    private String infoBox(String content, String bg, String borderColor, String textColor) {
        return "<div style='background:" + bg + ";border-left:4px solid " + borderColor
                + ";border-radius:6px;padding:14px 16px;margin:0 0 20px;font-size:14px;color:"
                + textColor + ";line-height:1.6;'>" + content + "</div>";
    }

    private String ctaButton(String href, String label, String color) {
        return "<div style='text-align:center;margin:24px 0;'>"
                + "<a href='" + href + "' style='display:inline-block;background:" + color
                + ";color:" + WHITE + ";font-size:14px;font-weight:600;padding:12px 28px;"
                + "border-radius:8px;text-decoration:none;letter-spacing:0.2px;'>"
                + label + "</a></div>";
    }

    private String divider() {
        return "<hr style='border:none;border-top:1px solid " + BORDER + ";margin:20px 0;'/>";
    }

    private String fallbackLink(String href, String color) {
        return "<p style='font-size:12px;color:" + TEXT_SOFT + ";text-align:center;margin:0;'>"
                + "Si el bot&oacute;n no funciona, copia este enlace en tu navegador:<br/>"
                + "<a href='" + href + "' style='color:" + color + ";word-break:break-all;'>"
                + href + "</a></p>";
    }

    private String smallNote(String note) {
        return "<p style='font-size:12px;color:" + TEXT_SOFT + ";text-align:center;margin:8px 0 0;'>"
                + note + "</p>";
    }

    public String translateRole(String role) {
        if (role == null) return "";
        return switch (role.toUpperCase()) {
            case "ORGANIZER"  -> "Organizador";
            case "STAFF"      -> "Personal de apoyo";
            case "MEMBER"     -> "Miembro";
            case "JUDGE"      -> "Juez";
            case "PARTICIPANT"-> "Participante";
            case "ATTENDEE"   -> "Asistente";
            default           -> role;
        };
    }
}