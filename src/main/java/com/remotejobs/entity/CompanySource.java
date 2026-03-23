package com.remotejobs.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_sources")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CompanySource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "careers_url", nullable = false, columnDefinition = "TEXT")
    private String careersUrl;

    @Column(name = "job_list_selector", length = 500)
    private String jobListSelector;

    @Column(name = "title_selector", length = 500)
    private String titleSelector;

    @Column(name = "link_selector", length = 500)
    private String linkSelector;

    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}