package com.remotejobs.outreach.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_hunt_companies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ProductHuntCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, nullable = false, length = 255)
    private String externalId;

    @Column(name = "product_name", nullable = false, length = 500)
    private String productName;

    @Column(name = "company_name", length = 500)
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String tagline;

    @Column(name = "website_url", columnDefinition = "TEXT")
    private String websiteUrl;

    @Column(name = "maker_names", columnDefinition = "TEXT")
    private String makerNames;

    @Column
    private Integer upvotes = 0;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "TEXT[]")
    private List<String> topics;

    @Column(name = "launch_date")
    private LocalDateTime launchDate;

    @CreationTimestamp
    @Column(name = "fetched_at", nullable = false, updatable = false)
    private LocalDateTime fetchedAt;
}
