package com.remotejobs.repository;

import com.remotejobs.entity.ScrapeRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScrapeRunRepository extends JpaRepository<ScrapeRun, Long> {
    List<ScrapeRun> findTop10ByOrderByStartedAtDesc();
    List<ScrapeRun> findBySourceOrderByStartedAtDesc(String source);
}