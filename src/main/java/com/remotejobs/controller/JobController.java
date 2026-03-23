package com.remotejobs.controller;

import com.remotejobs.dto.JobFilterRequest;
import com.remotejobs.dto.JobResponseDto;
import com.remotejobs.dto.PagedResponse;
import com.remotejobs.entity.Job;
import com.remotejobs.service.impl.JobQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Remote Job search and filter endpoints")
@CrossOrigin(origins = "*")
public class JobController {

    private final JobQueryService queryService;

    @GetMapping
    @Operation(summary = "Search and filter remote jobs")
    public ResponseEntity<PagedResponse<JobResponseDto>> getJobs(
            @RequestParam(required = false) Job.RemoteType remoteType,
            @RequestParam(required = false) Job.JobType jobType,
            @RequestParam(required = false) Integer expMin,
            @RequestParam(required = false) Integer expMax,
            @RequestParam(required = false) Integer salaryMin,
            @RequestParam(required = false) Integer salaryMax,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> techStack,
            @RequestParam(required = false) Integer daysAgo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        JobFilterRequest filter = new JobFilterRequest();
        filter.setRemoteType(remoteType);
        filter.setJobType(jobType);
        filter.setExpMin(expMin);
        filter.setExpMax(expMax);
        filter.setSalaryMin(salaryMin);
        filter.setSalaryMax(salaryMax);
        filter.setSource(source);
        filter.setKeyword(keyword);
        filter.setTechStack(techStack);
        filter.setDaysAgo(daysAgo);
        filter.setPage(page);
        filter.setSize(size);

        return ResponseEntity.ok(queryService.findJobs(filter));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID")
    public ResponseEntity<JobResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(queryService.findById(id));
    }

    @GetMapping("/sources")
    @Operation(summary = "List all available sources")
    public ResponseEntity<List<String>> getSources() {
        return ResponseEntity.ok(queryService.getSources());
    }

    @GetMapping("/enums/remote-types")
    public ResponseEntity<Job.RemoteType[]> getRemoteTypes() {
        return ResponseEntity.ok(Job.RemoteType.values());
    }

    @GetMapping("/enums/job-types")
    public ResponseEntity<Job.JobType[]> getJobTypes() {
        return ResponseEntity.ok(Job.JobType.values());
    }
}