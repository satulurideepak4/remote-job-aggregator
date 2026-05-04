package com.remotejobs.outreach.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.remotejobs.outreach.entity.ProductHuntCompany;
import com.remotejobs.outreach.repository.ProductHuntCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductHuntService {

    private static final String PRODUCT_HUNT_GRAPHQL_URL = "https://api.producthunt.com/v2/api/graphql";
    private static final List<String> RELEVANT_TOPICS = List.of(
            "developer-tools", "saas", "api", "infrastructure", "productivity", "tech"
    );

    private final ProductHuntCompanyRepository repository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${outreach.product-hunt.api-key:}")
    private String apiKey;

    @Value("${outreach.product-hunt.enabled:false}")
    private boolean enabled;

    @Scheduled(cron = "${outreach.product-hunt.cron:0 0 7 * * *}")
    public void scheduledFetch() {
        if (!enabled || apiKey.isBlank()) {
            log.info("[ProductHunt] Skipping fetch – disabled or no API key");
            return;
        }
        fetchAndStore();
    }

    public List<ProductHuntCompany> fetchAndStore() {
        List<ProductHuntCompany> saved = new ArrayList<>();
        for (String topic : RELEVANT_TOPICS) {
            try {
                List<ProductHuntCompany> fetched = fetchByTopic(topic);
                for (ProductHuntCompany company : fetched) {
                    if (repository.findByExternalId(company.getExternalId()).isEmpty()) {
                        saved.add(repository.save(company));
                    }
                }
                log.info("[ProductHunt] Topic={} → fetched={}, new={}", topic, fetched.size(), saved.size());
            } catch (Exception e) {
                log.error("[ProductHunt] Failed for topic {}: {}", topic, e.getMessage());
            }
        }
        return saved;
    }

    private List<ProductHuntCompany> fetchByTopic(String topic) {
        String query = """
                {
                  posts(order: NEWEST, first: 20, topic: "%s") {
                    edges {
                      node {
                        id
                        name
                        tagline
                        website
                        votesCount
                        createdAt
                        topics {
                          edges { node { slug } }
                        }
                        makers { name }
                      }
                    }
                  }
                }
                """.formatted(topic);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, String> body = Map.of("query", query);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                PRODUCT_HUNT_GRAPHQL_URL, HttpMethod.POST, request, String.class);

        return parseResponse(response.getBody());
    }

    private List<ProductHuntCompany> parseResponse(String json) {
        List<ProductHuntCompany> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode edges = root.path("data").path("posts").path("edges");
            for (JsonNode edge : edges) {
                JsonNode node = edge.path("node");
                try {
                    String makerNames = StreamSupport.stream(node.path("makers").spliterator(), false)
                            .map(m -> m.path("name").asText())
                            .collect(Collectors.joining(", "));

                    List<String> topics = StreamSupport.stream(
                                    node.path("topics").path("edges").spliterator(), false)
                            .map(e -> e.path("node").path("slug").asText())
                            .collect(Collectors.toList());

                    result.add(ProductHuntCompany.builder()
                            .externalId(node.path("id").asText())
                            .productName(node.path("name").asText())
                            .companyName(node.path("name").asText())
                            .tagline(node.path("tagline").asText())
                            .websiteUrl(node.path("website").asText())
                            .upvotes(node.path("votesCount").asInt(0))
                            .makerNames(makerNames)
                            .topics(topics)
                            .launchDate(parseDate(node.path("createdAt").asText()))
                            .build());
                } catch (Exception e) {
                    log.warn("[ProductHunt] Failed to parse node: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[ProductHunt] Failed to parse response: {}", e.getMessage());
        }
        return result;
    }

    private LocalDateTime parseDate(String date) {
        if (date == null || date.isBlank()) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    public List<ProductHuntCompany> getRecent(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return repository.findByLaunchDateAfterOrderByLaunchDateDesc(since);
    }

    public List<ProductHuntCompany> getAll() {
        return repository.findAllByOrderByLaunchDateDesc();
    }
}
