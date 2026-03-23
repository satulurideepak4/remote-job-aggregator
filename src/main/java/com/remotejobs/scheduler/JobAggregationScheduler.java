package com.remotejobs.scheduler;

import com.remotejobs.entity.ScrapeRun;
import com.remotejobs.notification.NotificationService;
import com.remotejobs.repository.JobRepository;
import com.remotejobs.service.JobSource;
import com.remotejobs.service.impl.JobProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobAggregationScheduler {

    private final List<JobSource> jobSources;
    private final JobProcessService jobProcessService;
    private final NotificationService notificationService;
    private final JobRepository jobRepository;

    @Value("${scheduler.enabled:true}")
    private boolean enabled;

    /**
     * Runs daily at 6 AM (configurable via scheduler.cron).
     * Spring auto-injects ALL beans implementing JobSource.
     */
    @Scheduled(cron = "${scheduler.cron:0 0 6 * * *}")
    public void runDailyAggregation() {
        if (!enabled) {
            log.info("[Scheduler] Disabled – skipping");
            return;
        }
        log.info("[Scheduler] Starting daily aggregation – {} sources", jobSources.size());

        int totalInserted = 0;
        for (JobSource source : jobSources) {
            try {
                ScrapeRun run = jobProcessService.ingest(source);
                totalInserted += run.getJobsInserted();
                log.info("[Scheduler] {} → inserted={}, updated={}, status={}",
                        source.sourceName(), run.getJobsInserted(), run.getJobsUpdated(), run.getStatus());
            } catch (Exception e) {
                log.error("[Scheduler] Source {} failed: {}", source.sourceName(), e.getMessage());
            }
        }

        // Send notifications for new jobs
        if (totalInserted > 0) {
            List<?> newJobs = jobRepository.findRecentlyScraped(LocalDateTime.now().minusHours(2));
            notificationService.notifyNewJobs(newJobs.size());
        }

        log.info("[Scheduler] Aggregation complete – total new jobs: {}", totalInserted);
    }

    /**
     * Manually trigger ingestion from a single source by name (for admin use).
     */
    public ScrapeRun triggerSource(String sourceName) {
        return jobSources.stream()
                .filter(s -> s.sourceName().equalsIgnoreCase(sourceName))
                .findFirst()
                .map(jobProcessService::ingest)
                .orElseThrow(() -> new RuntimeException("Unknown source: " + sourceName));
    }

    /**
     * Trigger ALL sources immediately (manual run).
     */
    public int triggerAll() {
        int total = 0;
        for (JobSource source : jobSources) {
            ScrapeRun run = jobProcessService.ingest(source);
            total += run.getJobsInserted();
        }
        return total;
    }
}