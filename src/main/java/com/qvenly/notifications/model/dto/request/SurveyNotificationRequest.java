package com.qvenly.notifications.model.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SurveyNotificationRequest {

    @NotBlank
    private String eventTitle;

    @NotNull
    private Long eventId;

    @NotNull
    private Long surveyId;

    @NotBlank
    private String surveyTitle;

    private String deadline;

    @NotNull
    private List<EventNotificationRequest.Recipient> recipients;

}
