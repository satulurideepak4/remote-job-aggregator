package com.remotejobs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "scrape_runs")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ScrapeRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String source;

    @CreationTimestamp
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "jobs_found")
    private int jobsFound;

    @Column(name = "jobs_inserted")
    private int jobsInserted;

    @Column(name = "jobs_updated")
    private int jobsUpdated;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Status status = Status.RUNNING;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public enum Status { RUNNING, SUCCESS, FAILED }
}