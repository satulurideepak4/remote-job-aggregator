package com.remotejobs.outreach.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "linkedin_signals")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LinkedInSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_url", columnDefinition = "TEXT")
    private String postUrl;

    @Column(name = "author_name", length = 255)
    private String authorName;

    @Column(name = "company_name", nullable = false, length = 500)
    private String companyName;

    @Column(name = "post_snippet", columnDefinition = "TEXT")
    private String postSnippet;

    @Column(name = "hiring_signal", length = 500)
    private String hiringSignal;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
