package com.remotejobs.dto;

import com.remotejobs.entity.Job;
import lombok.Data;

import java.util.List;

@Data
public class JobFilterRequest {
    private Job.RemoteType remoteType;
    private Job.JobType jobType;
    private Integer expMin;
    private Integer expMax;
    private Integer salaryMin;
    private Integer salaryMax;
    private String source;
    private String keyword;
    private List<String> techStack;
    private Integer daysAgo;
    private int page = 0;
    private int size = 20;
}