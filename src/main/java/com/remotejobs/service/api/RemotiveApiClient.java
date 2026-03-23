package com.remotejobs.service.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remotejobs.dto.RawJobDto;
import com.remotejobs.service.JobSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemotiveApiClient implements JobSource {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.remotive.base-url}")
    private String baseUrl;

    @Override
    public String sourceName() { return "Remotive"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            String url = baseUrl + "?limit=100";
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(json);
            JsonNode jobsNode = root.path("jobs");

            for (JsonNode node : jobsNode) {
                try {
                    jobs.add(RawJobDto.builder()
                            .externalId(node.path("id").asText())
                            .title(node.path("title").asText())
                            .companyName(node.path("company_name").asText())
                            .jobLink(node.path("url").asText())
                            .description(node.path("description").asText())
                            .jobTypeRaw(node.path("job_type").asText())
                            .location(node.path("candidate_required_location").asText())
                            .salaryRaw(node.path("salary").asText())
                            .source(sourceName())
                            .postedDate(parseDate(node.path("publication_date").asText()))
                            .build());
                } catch (Exception e) {
                    log.warn("[Remotive] Failed to parse job node: {}", e.getMessage());
                }
            }
            log.info("[Remotive] Fetched {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[Remotive] API call failed: {}", e.getMessage());
        }
        return jobs;
    }

    private LocalDateTime parseDate(String date) {
        if (date == null || date.isBlank()) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}