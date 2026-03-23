package com.remotejobs.controller;

import com.remotejobs.entity.CompanySource;
import com.remotejobs.entity.ScrapeRun;
import com.remotejobs.repository.CompanySourceRepository;
import com.remotejobs.repository.ScrapeRunRepository;
import com.remotejobs.scheduler.JobAggregationScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin endpoints for manual control")
@CrossOrigin(origins = "*")
public class AdminController {

    private final JobAggregationScheduler scheduler;
    private final ScrapeRunRepository scrapeRunRepo;
    private final CompanySourceRepository companySourceRepo;

    @PostMapping("/trigger")
    @Operation(summary = "Trigger full aggregation run immediately")
    public ResponseEntity<Map<String, Object>> triggerAll() {
        int inserted = scheduler.triggerAll();
        return ResponseEntity.ok(Map.of("status", "done", "newJobs", inserted));
    }

    @PostMapping("/trigger/{source}")
    @Operation(summary = "Trigger a single source by name")
    public ResponseEntity<ScrapeRun> triggerSource(@PathVariable String source) {
        return ResponseEntity.ok(scheduler.triggerSource(source));
    }

    @GetMapping("/runs")
    @Operation(summary = "Get recent scrape run history")
    public ResponseEntity<List<ScrapeRun>> getRuns() {
        return ResponseEntity.ok(scrapeRunRepo.findTop10ByOrderByStartedAtDesc());
    }

    // ── Company Sources CRUD ──────────────────────────────────────────────────

    @GetMapping("/company-sources")
    public ResponseEntity<List<CompanySource>> listCompanySources() {
        return ResponseEntity.ok(companySourceRepo.findAll());
    }

    @PostMapping("/company-sources")
    public ResponseEntity<CompanySource> addCompanySource(@RequestBody CompanySource source) {
        return ResponseEntity.ok(companySourceRepo.save(source));
    }

    @PutMapping("/company-sources/{id}")
    public ResponseEntity<CompanySource> updateCompanySource(
            @PathVariable Long id, @RequestBody CompanySource updated) {
        return companySourceRepo.findById(id).map(existing -> {
            existing.setCompanyName(updated.getCompanyName());
            existing.setCareersUrl(updated.getCareersUrl());
            existing.setJobListSelector(updated.getJobListSelector());
            existing.setTitleSelector(updated.getTitleSelector());
            existing.setLinkSelector(updated.getLinkSelector());
            existing.setActive(updated.getActive());
            return ResponseEntity.ok(companySourceRepo.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/company-sources/{id}")
    public ResponseEntity<Void> deleteCompanySource(@PathVariable Long id) {
        companySourceRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}