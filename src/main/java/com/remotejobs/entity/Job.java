package com.remotejobs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", length = 512)
    private String externalId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "job_link", nullable = false, columnDefinition = "TEXT")
    private String jobLink;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "job_type", length = 50)
    @Enumerated(EnumType.STRING)
    private JobType jobType;

    @Column(name = "remote_type", length = 50)
    @Enumerated(EnumType.STRING)
    private RemoteType remoteType;

    @Column(name = "experience_min")
    private Integer experienceMin;

    @Column(name = "experience_max")
    private Integer experienceMax;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    @Column(name = "salary_currency", length = 10)
    private String salaryCurrency;

    @Column(name = "salary_estimated")
    private Boolean salaryEstimated = false;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tech_stack", columnDefinition = "TEXT[]")
    private List<String> techStack;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(length = 255)
    private String location;

    @Column(name = "posted_date")
    private LocalDateTime postedDate;

    @CreationTimestamp
    @Column(name = "scraped_at", nullable = false, updatable = false)
    private LocalDateTime scrapedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "hash", unique = true, length = 64)
    private String hash;

    public enum JobType {
        FULL_TIME, CONTRACT, PART_TIME, INTERNSHIP
    }

    public enum RemoteType {
        GLOBAL, REGION_SPECIFIC, INDIA_ALLOWED, UNKNOWN
    }
}