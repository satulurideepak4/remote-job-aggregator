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
public class RemoteOkApiClient implements JobSource {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${api.remoteok.base-url}")
    private String baseUrl;

    @Override
    public String sourceName() { return "RemoteOK"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            // RemoteOK requires a non-browser UA — returns JSON array
            String json = restTemplate.getForObject(baseUrl, String.class);
            JsonNode root = objectMapper.readTree(json);

            for (JsonNode node : root) {
                // First element is metadata, skip it
                if (!node.has("id") || node.path("id").asText().equals("legal")) continue;
                try {
                    List<String> tags = new ArrayList<>();
                    node.path("tags").forEach(t -> tags.add(t.asText()));

                    jobs.add(RawJobDto.builder()
                            .externalId(node.path("id").asText())
                            .title(node.path("position").asText())
                            .companyName(node.path("company").asText())
                            .jobLink("https://remoteok.com" + node.path("url").asText())
                            .description(node.path("description").asText())
                            .location(node.path("location").asText())
                            .salaryRaw(buildSalaryString(node))
                            .techStack(tags)
                            .source(sourceName())
                            .postedDate(epochToDate(node.path("epoch").asLong()))
                            .build());
                } catch (Exception e) {
                    log.warn("[RemoteOK] Skipping node: {}", e.getMessage());
                }
            }
            log.info("[RemoteOK] Fetched {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[RemoteOK] API call failed: {}", e.getMessage());
        }
        return jobs;
    }

    private String buildSalaryString(JsonNode node) {
        String min = node.path("salary_min").asText("");
        String max = node.path("salary_max").asText("");
        if (!min.isBlank() && !max.isBlank()) return "$" + min + " - $" + max;
        return "";
    }

    private LocalDateTime epochToDate(long epoch) {
        if (epoch == 0) return LocalDateTime.now();
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
    }
}