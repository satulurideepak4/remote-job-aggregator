package com.remotejobs.repository;

import com.remotejobs.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>,
        JpaSpecificationExecutor<Job> {

    Optional<Job> findByHash(String hash);

    boolean existsBySourceAndExternalId(String source, String externalId);

    @Query(value = """
        SELECT * FROM jobs
        WHERE is_active = true
          AND :tech = ANY(tech_stack)
        ORDER BY jobs.posted_date DESC NULLS LAST
        """, nativeQuery = true)
    Page<Job> findByTechStack(@Param("tech") String tech, Pageable pageable);

    @Query(value = """
        SELECT * FROM jobs
        WHERE is_active = true
          AND tech_stack && CAST(:techs AS TEXT[])
        ORDER BY jobs.posted_date DESC NULLS LAST
        """, nativeQuery = true)
    Page<Job> findByAnyTechStack(@Param("techs") String techs, Pageable pageable);

    @Query("SELECT DISTINCT j.source FROM Job j ORDER BY j.source")
    List<String> findDistinctSources();

    @Query("SELECT COUNT(j) FROM Job j WHERE j.source = :source AND j.scrapedAt >= :since")
    long countBySourceSince(@Param("source") String source, @Param("since") LocalDateTime since);

    @Query("SELECT j FROM Job j WHERE j.scrapedAt >= :since AND j.isActive = true ORDER BY j.scrapedAt DESC")
    List<Job> findRecentlyScraped(@Param("since") LocalDateTime since);
}