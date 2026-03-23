package com.remotejobs.service.impl;

import com.remotejobs.dto.RawJobDto;
import com.remotejobs.entity.Job;
import com.remotejobs.entity.ScrapeRun;
import com.remotejobs.repository.JobRepository;
import com.remotejobs.repository.ScrapeRunRepository;
import com.remotejobs.service.JobSource;
import com.remotejobs.util.JobNormalizer;
import com.remotejobs.util.SalaryEstimator;
import com.remotejobs.util.TechStackExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Orchestrates the full ingestion pipeline:
 * fetch → normalize → deduplicate → estimate salary → persist
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobProcessService {

    private final JobRepository jobRepository;
    private final ScrapeRunRepository scrapeRunRepository;
    private final JobNormalizer normalizer;
    private final TechStackExtractor techExtractor;
    private final SalaryEstimator salaryEstimator;

    /**
     * Run the full ingestion pipeline for a single source.
     */
    public ScrapeRun ingest(JobSource source) {
        ScrapeRun run = ScrapeRun.builder()
                .source(source.sourceName())
                .status(ScrapeRun.Status.RUNNING)
                .build();
        scrapeRunRepository.save(run);

        try {
            List<RawJobDto> rawJobs = source.fetchJobs();
            run.setJobsFound(rawJobs.size());

            int inserted = 0, updated = 0;
            for (RawJobDto raw : rawJobs) {
                try {
                    Result r = upsertJob(raw);
                    if (r == Result.INSERTED) inserted++;
                    else if (r == Result.UPDATED) updated++;
                } catch (Exception e) {
                    log.warn("[{}] Failed to persist job '{}': {}", source.sourceName(), raw.getTitle(), e.getMessage());
                }
            }

            run.setJobsInserted(inserted);
            run.setJobsUpdated(updated);
            run.setStatus(ScrapeRun.Status.SUCCESS);
            log.info("[{}] Done – found={}, inserted={}, updated={}", source.sourceName(), rawJobs.size(), inserted, updated);

        } catch (Exception e) {
            run.setStatus(ScrapeRun.Status.FAILED);
            run.setErrorMessage(e.getMessage());
            log.error("[{}] Ingestion failed: {}", source.sourceName(), e.getMessage());
        } finally {
            run.setFinishedAt(LocalDateTime.now());
            scrapeRunRepository.save(run);
        }
        return run;
    }

    @Transactional
    public Result upsertJob(RawJobDto raw) {
        Job normalized = normalizer.normalize(raw, techExtractor);
        salaryEstimator.estimate(normalized);

        // Deduplicate by hash (title + company + source)
        var existing = jobRepository.findByHash(normalized.getHash());
        if (existing.isPresent()) {
            // Update metadata but preserve ID
            Job j = existing.get();
            j.setIsActive(true);
            j.setScrapedAt(LocalDateTime.now());
            if (normalized.getPostedDate() != null) j.setPostedDate(normalized.getPostedDate());
            jobRepository.save(j);
            return Result.UPDATED;
        }

        // Also check by externalId+source to avoid partial hash collisions
        if (raw.getExternalId() != null
                && jobRepository.existsBySourceAndExternalId(raw.getSource(), raw.getExternalId())) {
            return Result.SKIPPED;
        }

        jobRepository.save(normalized);
        return Result.INSERTED;
    }

    public enum Result { INSERTED, UPDATED, SKIPPED }
}