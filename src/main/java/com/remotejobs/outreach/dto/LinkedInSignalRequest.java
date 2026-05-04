package com.remotejobs.outreach.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkedInSignalRequest {

    private String postUrl;
    private String authorName;

    @NotBlank
    private String companyName;

    private String postSnippet;
    private String hiringSignal;
}
