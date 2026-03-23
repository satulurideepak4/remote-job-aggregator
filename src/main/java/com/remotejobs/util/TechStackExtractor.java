package com.remotejobs.util;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Extracts tech stack keywords from job descriptions using a curated dictionary.
 */
@Component
public class TechStackExtractor {

    private static final Set<String> TECH_KEYWORDS = new LinkedHashSet<>(Arrays.asList(
            // Languages
            "Java", "Python", "JavaScript", "TypeScript", "Go", "Golang", "Rust", "Kotlin",
            "Scala", "Ruby", "PHP", "Swift", "C++", "C#", "Dart", "Elixir", "Clojure", "R",

            // Backend Frameworks
            "Spring Boot", "Spring", "Hibernate", "Micronaut", "Quarkus",
            "Django", "FastAPI", "Flask", "Express", "NestJS", "Node.js", "Rails",
            "Laravel", "Gin", "Echo", "Fiber",

            // Frontend
            "React", "Angular", "Vue", "Next.js", "Nuxt", "Svelte", "Redux", "GraphQL",
            "Tailwind", "Bootstrap", "Material UI",

            // Databases
            "PostgreSQL", "MySQL", "MongoDB", "Redis", "Elasticsearch", "Cassandra",
            "DynamoDB", "SQLite", "Oracle", "MSSQL", "Neo4j", "InfluxDB", "CockroachDB",

            // Cloud & DevOps
            "AWS", "GCP", "Azure", "Docker", "Kubernetes", "Terraform", "Ansible",
            "Jenkins", "GitHub Actions", "GitLab CI", "CircleCI", "ArgoCD", "Helm",

            // Messaging
            "Kafka", "RabbitMQ", "SQS", "Pulsar", "NATS", "ActiveMQ",

            // AI/ML
            "TensorFlow", "PyTorch", "Spark", "Hadoop", "Flink", "Airflow",
            "LangChain", "OpenAI", "Hugging Face",

            // API & Protocols
            "REST", "gRPC", "WebSocket", "GraphQL", "OAuth", "JWT",

            // Misc
            "Microservices", "Linux", "Git", "CI/CD", "Agile", "Scrum"
    ));

    // Build case-insensitive pattern once for efficiency
    private static final Map<String, Pattern> PATTERNS = new HashMap<>();

    static {
        for (String tech : TECH_KEYWORDS) {
            String escaped = Pattern.quote(tech);
            PATTERNS.put(tech, Pattern.compile("\\b" + escaped + "\\b", Pattern.CASE_INSENSITIVE));
        }
    }

    public List<String> extract(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();

        String cleaned = stripHtml(text);
        return TECH_KEYWORDS.stream()
                .filter(tech -> PATTERNS.get(tech).matcher(cleaned).find())
                .collect(Collectors.toList());
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", " ")
                .replaceAll("&[a-z]+;", " ");
    }
}