//package com.remotejobs.service.api;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.remotejobs.dto.RawJobDto;
//import com.remotejobs.service.JobSource;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class JSearchApiClient implements JobSource {
//
//    private final OkHttpClient httpClient;
//    private final ObjectMapper objectMapper;
//
//    @Value("${api.jsearch.api-key:}")
//    private String apiKey;
//
//    @Override
//    public String sourceName() { return "JSearch"; }
//
//    @Override
//    public List<RawJobDto> fetchJobs() {
//        List<RawJobDto> jobs = new ArrayList<>();
//
//        if (apiKey.isBlank()) {
//            log.warn("[JSearch] API key not configured – skipping");
//            return jobs;
//        }
//
//        String[] queries = {"remote software engineer", "remote java developer", "remote backend developer"};
//
//        for (String query : queries) {
//            try {
//                String url = "https://jsearch.p.rapidapi.com/search?query="
//                        + query.replace(" ", "%20")
//                        + "&page=1&num_pages=2&date_posted=week";
//
//                Request request = new Request.Builder()
//                        .url(url)
//                        .header("X-RapidAPI-Key", apiKey)
//                        .header("X-RapidAPI-Host", "jsearch.p.rapidapi.com")
//                        .build();
//
//                try (Response response = httpClient.newCall(request).execute()) {
//                    if (!response.isSuccessful()) {
//                        log.warn("[JSearch] HTTP {} for query: {}", response.code(), query);
//                        continue;
//                    }
//                    String json = response.body().string();
//                    JsonNode root = objectMapper.readTree(json);
//                    JsonNode data = root.path("data");
//
//                    for (JsonNode node : data) {
//                        try {
//                            if (!node.path("job_is_remote").asBoolean(false)) continue;
//
//                            jobs.add(RawJobDto.builder()
//                                    .externalId(node.path("job_id").asText())
//                                    .title(node.path("job_title").asText())
//                                    .companyName(node.path("employer_name").asText())
//                                    .jobLink(node.path("job_apply_link").asText())
//                                    .description(node.path("job_description").asText())
//                                    .jobTypeRaw(node.path("job_employment_type").asText())
//                                    .location(node.path("job_country").asText())
//                                    .experienceRaw(node.path("job_required_experience")
//                                            .path("required_experience_in_months").asText())
//                                    .salaryRaw(buildSalary(node))
//                                    .source(sourceName())
//                                    .postedDate(epochToDate(node.path("job_posted_at_timestamp").asLong()))
//                                    .build());
//                        } catch (Exception e) {
//                            log.warn("[JSearch] Skipping node: {}", e.getMessage());
//                        }
//                    }
//                }
//                Thread.sleep(1000);
//            } catch (Exception e) {
//                log.error("[JSearch] Query '{}' failed: {}", query, e.getMessage());
//            }
//        }
//        log.info("[JSearch] Fetched {} jobs", jobs.size());
//        return jobs;
//    }
//
//    private String buildSalary(JsonNode node) {
//        double min = node.path("job_min_salary").asDouble(0);
//        double max = node.path("job_max_salary").asDouble(0);
//        String currency = node.path("job_salary_currency").asText("USD");
//        if (min > 0 && max > 0) return "$" + (int) min + " - $" + (int) max;
//        return "";
//    }
//
//    private LocalDateTime epochToDate(long epoch) {
//        if (epoch == 0) return LocalDateTime.now();
//        return LocalDateTime.ofInstant(
//                java.time.Instant.ofEpochSecond(epoch),
//                java.time.ZoneOffset.UTC);
//    }
//}