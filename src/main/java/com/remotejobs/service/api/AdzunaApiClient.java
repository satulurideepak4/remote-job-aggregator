//package com.remotejobs.service.api;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.remotejobs.dto.RawJobDto;
//import com.remotejobs.service.JobSource;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//import org.springframework.web.client.RestTemplate;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class AdzunaApiClient implements JobSource {
//
//    private final RestTemplate restTemplate;
//    private final ObjectMapper objectMapper;
//
//    @Value("${api.adzuna.app-id:}")
//    private String appId;
//
//    @Value("${api.adzuna.app-key:}")
//    private String appKey;
//
//    @Override
//    public String sourceName() { return "Adzuna"; }
//
//    @Override
//    public List<RawJobDto> fetchJobs() {
//        List<RawJobDto> jobs = new ArrayList<>();
//
//        if (appId.isBlank() || appKey.isBlank()) {
//            log.warn("[Adzuna] API credentials not configured – skipping");
//            return jobs;
//        }
//
//        try {
//            // Search for remote software jobs across multiple pages
//            for (int page = 1; page <= 3; page++) {
//                String url = String.format(
//                        "https://api.adzuna.com/v1/api/jobs/gb/search/%d" +
//                                "?app_id=%s&app_key=%s&results_per_page=50&what=software+engineer+remote&where=remote" +
//                                "&full_time=1&content-type=application/json",
//                        page, appId, appKey);
//
//                String json = restTemplate.getForObject(url, String.class);
//                JsonNode root = objectMapper.readTree(json);
//                JsonNode results = root.path("results");
//
//                if (!results.isArray() || results.isEmpty()) break;
//
//                for (JsonNode node : results) {
//                    try {
//                        jobs.add(RawJobDto.builder()
//                                .externalId(node.path("id").asText())
//                                .title(node.path("title").asText())
//                                .companyName(node.path("company").path("display_name").asText())
//                                .jobLink(node.path("redirect_url").asText())
//                                .description(node.path("description").asText())
//                                .jobTypeRaw(node.path("contract_type").asText())
//                                .location(node.path("location").path("display_name").asText())
//                                .salaryRaw(buildSalary(node))
//                                .source(sourceName())
//                                .postedDate(parseDate(node.path("created").asText()))
//                                .build());
//                    } catch (Exception e) {
//                        log.warn("[Adzuna] Skipping node: {}", e.getMessage());
//                    }
//                }
//                Thread.sleep(500);
//            }
//            log.info("[Adzuna] Fetched {} jobs", jobs.size());
//        } catch (Exception e) {
//            log.error("[Adzuna] API call failed: {}", e.getMessage());
//        }
//        return jobs;
//    }
//
//    private String buildSalary(JsonNode node) {
//        double min = node.path("salary_min").asDouble(0);
//        double max = node.path("salary_max").asDouble(0);
//        if (min > 0 && max > 0) return "£" + (int) min + " - £" + (int) max;
//        return "";
//    }
//
//    private LocalDateTime parseDate(String date) {
//        if (date == null || date.isBlank()) return LocalDateTime.now();
//        try {
//            return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
//        } catch (Exception e) {
//            return LocalDateTime.now();
//        }
//    }
//}