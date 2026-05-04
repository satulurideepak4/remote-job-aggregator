package com.remotejobs.outreach.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TwitterSignalRequest {

    private String tweetUrl;
    private String authorHandle;
    private String authorName;

    @NotBlank
    private String companyName;

    private String tweetSnippet;
    private String hiringSignal;
}
