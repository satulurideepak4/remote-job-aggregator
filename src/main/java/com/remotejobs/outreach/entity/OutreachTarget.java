package com.remotejobs.outreach.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "outreach_targets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OutreachTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false, length = 500)
    private String companyName;

    @Column(name = "source", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Source source = Source.MANUAL;

    @Column(name = "company_website", columnDefinition = "TEXT")
    private String companyWebsite;

    @Column(name = "contact_name", length = 255)
    private String contactName;

    @Column(name = "contact_role", length = 255)
    private String contactRole;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "outreach_status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private OutreachStatus outreachStatus = OutreachStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Source {
        PRODUCT_HUNT, LINKEDIN, TWITTER, MANUAL
    }

    public enum OutreachStatus {
        PENDING, SENT, REPLIED, NOT_INTERESTED, GHOSTED
    }
}
