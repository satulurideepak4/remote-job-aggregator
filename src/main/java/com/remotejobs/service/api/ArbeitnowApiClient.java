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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArbeitnowApiClient implements JobSource {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.arbeitnow.base-url}")
    private String baseUrl;

    @Override
    public String sourceName() { return "Arbeitnow"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            // Paginate up to 3 pages
            for (int page = 1; page <= 3; page++) {
                String url = baseUrl + "?page=" + page;
                String json = restTemplate.getForObject(url, String.class);
                JsonNode root = objectMapper.readTree(json);
                JsonNode data = root.path("data");

                if (!data.isArray() || data.isEmpty()) break;

                for (JsonNode node : data) {
                    try {
                        // Only include remote jobs
                        if (!node.path("remote").asBoolean(false)) continue;

                        List<String> tags = new ArrayList<>();
                        node.path("tags").forEach(t -> tags.add(t.asText()));

                        jobs.add(RawJobDto.builder()
                                .externalId(node.path("slug").asText())
                                .title(node.path("title").asText())
                                .companyName(node.path("company_name").asText())
                                .jobLink(node.path("url").asText())
                                .description(node.path("description").asText())
                                .jobTypeRaw(node.path("job_types").isArray()
                                        ? node.path("job_types").get(0).asText()
                                        : "full_time")
                                .location(node.path("location").asText())
                                .techStack(tags)
                                .source(sourceName())
                                .postedDate(epochToDate(node.path("created_at").asLong()))
                                .build());
                    } catch (Exception e) {
                        log.warn("[Arbeitnow] Skipping node: {}", e.getMessage());
                    }
                }

                // Check if there's a next page
                if (root.path("links").path("next").isNull()) break;
                Thread.sleep(1000);
            }
            log.info("[Arbeitnow] Fetched {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[Arbeitnow] API call failed: {}", e.getMessage());
        }
        return jobs;
    }

    private LocalDateTime epochToDate(long epoch) {
        if (epoch == 0) return LocalDateTime.now();
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
    }
}