package com.remotejobs.dto;

import com.remotejobs.entity.Job;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Raw job data parsed by scrapers/API clients before normalization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawJobDto {
    private String externalId;
    private String title;
    private String companyName;
    private String jobLink;
    private String description;
    private String jobTypeRaw;
    private String remoteTypeRaw;
    private String experienceRaw;
    private String salaryRaw;
    private List<String> techStack;
    private String source;
    private String location;
    private LocalDateTime postedDate;
}