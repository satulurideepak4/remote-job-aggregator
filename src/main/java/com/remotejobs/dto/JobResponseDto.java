package com.remotejobs.dto;

import com.remotejobs.entity.Job;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponseDto {
    private Long id;
    private String title;
    private String companyName;
    private String jobLink;
    private Job.JobType jobType;
    private Job.RemoteType remoteType;
    private Integer experienceMin;
    private Integer experienceMax;
    private Integer salaryMin;
    private Integer salaryMax;
    private String salaryCurrency;
    private Boolean salaryEstimated;
    private List<String> techStack;
    private String source;
    private String location;
    private LocalDateTime postedDate;
    private LocalDateTime scrapedAt;
}