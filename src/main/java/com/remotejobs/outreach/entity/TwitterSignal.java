package com.remotejobs.outreach.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "twitter_signals")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TwitterSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tweet_url", columnDefinition = "TEXT")
    private String tweetUrl;

    @Column(name = "author_handle", length = 255)
    private String authorHandle;

    @Column(name = "author_name", length = 255)
    private String authorName;

    @Column(name = "company_name", nullable = false, length = 500)
    private String companyName;

    @Column(name = "tweet_snippet", columnDefinition = "TEXT")
    private String tweetSnippet;

    @Column(name = "hiring_signal", length = 500)
    private String hiringSignal;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
