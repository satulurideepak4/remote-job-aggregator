package com.remotejobs.service.impl;

import com.remotejobs.dto.JobFilterRequest;
import com.remotejobs.dto.JobResponseDto;
import com.remotejobs.dto.PagedResponse;
import com.remotejobs.entity.Job;
import com.remotejobs.repository.JobRepository;
import com.remotejobs.repository.JobSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobQueryService {

    private final JobRepository jobRepository;

    @Cacheable(value = "jobs", key = "#filter.toString()")
    public PagedResponse<JobResponseDto> findJobs(JobFilterRequest filter) {
        PageRequest pageable = PageRequest.of(
                filter.getPage(), filter.getSize(),
                Sort.by(Sort.Direction.DESC, "postedDate"));

        LocalDateTime postedAfter = filter.getDaysAgo() != null
                ? LocalDateTime.now().minusDays(filter.getDaysAgo())
                : null;

        Page<Job> page;

        // Tech stack filter uses native query
        if (filter.getTechStack() != null && !filter.getTechStack().isEmpty()) {
            String techs = "{" + String.join(",", filter.getTechStack()) + "}";
            page = jobRepository.findByAnyTechStack(techs, pageable);
        } else {
            Specification<Job> spec = JobSpecification.withFilters(
                    filter.getRemoteType() != null ? filter.getRemoteType().name() : null,
                    filter.getJobType()    != null ? filter.getJobType().name()    : null,
                    filter.getExpMin(),
                    filter.getExpMax(),
                    filter.getSalaryMin(),
                    filter.getSalaryMax(),
                    filter.getSource(),
                    filter.getKeyword(),
                    postedAfter
            );
            page = jobRepository.findAll(spec, pageable);
        }

        List<JobResponseDto> content = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return PagedResponse.<JobResponseDto>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    public JobResponseDto findById(Long id) {
        return jobRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
    }

    public List<String> getSources() {
        return jobRepository.findDistinctSources();
    }

    private JobResponseDto toDto(Job job) {
        return JobResponseDto.builder()
                .id(job.getId())
                .title(job.getTitle())
                .companyName(job.getCompanyName())
                .jobLink(job.getJobLink())
                .jobType(job.getJobType())
                .remoteType(job.getRemoteType())
                .experienceMin(job.getExperienceMin())
                .experienceMax(job.getExperienceMax())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())
                .salaryEstimated(job.getSalaryEstimated())
                .techStack(job.getTechStack())
                .source(job.getSource())
                .location(job.getLocation())
                .postedDate(job.getPostedDate())
                .scrapedAt(job.getScrapedAt())
                .build();
    }
}