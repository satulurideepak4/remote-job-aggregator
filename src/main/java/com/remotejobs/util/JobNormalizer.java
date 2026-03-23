package com.remotejobs.util;

import com.remotejobs.dto.RawJobDto;
import com.remotejobs.entity.Job;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normalizes raw scraped/API job data into the unified Job entity.
 */
@Component
public class JobNormalizer {

    private static final Pattern EXP_PATTERN =
            Pattern.compile("(\\d+)\\s*[-–]\\s*(\\d+)\\s*(?:years?|yrs?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXP_MIN_PATTERN =
            Pattern.compile("(\\d+)\\+?\\s*(?:years?|yrs?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SALARY_RANGE =
            Pattern.compile("\\$?([\\d,]+)k?\\s*[-–]\\s*\\$?([\\d,]+)k?", Pattern.CASE_INSENSITIVE);
    private static final Pattern SALARY_SINGLE =
            Pattern.compile("\\$([\\d,]+)k?", Pattern.CASE_INSENSITIVE);

    public Job normalize(RawJobDto raw, TechStackExtractor extractor) {
        Job job = new Job();

        job.setExternalId(raw.getExternalId());
        job.setTitle(cleanText(raw.getTitle()));
        job.setCompanyName(cleanText(raw.getCompanyName()));
        job.setJobLink(raw.getJobLink());
        job.setDescription(stripHtml(raw.getDescription()));
        job.setSource(raw.getSource());
        job.setLocation(raw.getLocation());
        job.setPostedDate(raw.getPostedDate());

        // Job type
        job.setJobType(parseJobType(raw.getJobTypeRaw()));

        // Remote type
        job.setRemoteType(parseRemoteType(raw.getRemoteTypeRaw(), raw.getLocation(), raw.getDescription()));

        // Experience
        parseExperience(raw.getExperienceRaw(), job);

        // Salary
        parseSalary(raw.getSalaryRaw(), job);

        // Tech stack: merge provided list with extracted keywords from description
        var extracted = extractor.extract(raw.getDescription());
        if (raw.getTechStack() != null && !raw.getTechStack().isEmpty()) {
            extracted.addAll(raw.getTechStack());
        }
        job.setTechStack(extracted.stream().distinct().toList());

        // Hash for deduplication
        job.setHash(buildHash(raw.getTitle(), raw.getCompanyName(), raw.getSource()));

        return job;
    }

    // ── Parsers ──────────────────────────────────────────────────────────────

    public Job.JobType parseJobType(String raw) {
        if (raw == null) return Job.JobType.FULL_TIME;
        String lower = raw.toLowerCase();
        if (lower.contains("contract") || lower.contains("freelance")) return Job.JobType.CONTRACT;
        if (lower.contains("part")) return Job.JobType.PART_TIME;
        if (lower.contains("intern")) return Job.JobType.INTERNSHIP;
        return Job.JobType.FULL_TIME;
    }

    public Job.RemoteType parseRemoteType(String remoteRaw, String location, String description) {
        String combined = ((remoteRaw == null ? "" : remoteRaw) + " " +
                (location == null ? "" : location) + " " +
                (description == null ? "" : description)).toLowerCase();

        if (combined.contains("india") || combined.contains("apac") || combined.contains("asia")) {
            return Job.RemoteType.INDIA_ALLOWED;
        }
        if (combined.contains("worldwide") || combined.contains("global") ||
                combined.contains("anywhere") || combined.contains("remote worldwide")) {
            return Job.RemoteType.GLOBAL;
        }
        if (combined.contains("us only") || combined.contains("usa only") ||
                combined.contains("uk only") || combined.contains("eu only") ||
                combined.contains("europe only")) {
            return Job.RemoteType.REGION_SPECIFIC;
        }
        if (combined.contains("remote")) {
            return Job.RemoteType.GLOBAL; // default assumption
        }
        return Job.RemoteType.UNKNOWN;
    }

    public void parseExperience(String raw, Job job) {
        if (raw == null || raw.isBlank()) return;
        Matcher range = EXP_PATTERN.matcher(raw);
        if (range.find()) {
            job.setExperienceMin(Integer.parseInt(range.group(1)));
            job.setExperienceMax(Integer.parseInt(range.group(2)));
            return;
        }
        Matcher single = EXP_MIN_PATTERN.matcher(raw);
        if (single.find()) {
            int val = Integer.parseInt(single.group(1));
            job.setExperienceMin(val);
            job.setExperienceMax(val + 3);
        }
    }

    public void parseSalary(String raw, Job job) {
        if (raw == null || raw.isBlank()) return;
        String cleaned = raw.replace(",", "");

        Matcher range = SALARY_RANGE.matcher(cleaned);
        if (range.find()) {
            int min = parseAmount(range.group(1), cleaned);
            int max = parseAmount(range.group(2), cleaned);
            job.setSalaryMin(min);
            job.setSalaryMax(max);
            job.setSalaryCurrency(detectCurrency(raw));
            return;
        }
        Matcher single = SALARY_SINGLE.matcher(cleaned);
        if (single.find()) {
            int val = parseAmount(single.group(1), cleaned);
            job.setSalaryMin((int) (val * 0.9));
            job.setSalaryMax((int) (val * 1.1));
            job.setSalaryCurrency(detectCurrency(raw));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int parseAmount(String numStr, String context) {
        int val = Integer.parseInt(numStr.replaceAll("[^0-9]", ""));
        // "k" suffix or small number → multiply by 1000
        if (context.toLowerCase().contains("k") || val < 1000) val *= 1000;
        return val;
    }

    private String detectCurrency(String text) {
        if (text.contains("£")) return "GBP";
        if (text.contains("€")) return "EUR";
        if (text.contains("₹")) return "INR";
        return "USD";
    }

    public String buildHash(String title, String company, String source) {
        try {
            String key = (title + "|" + company + "|" + source).toLowerCase().trim();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(key.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            return String.valueOf((title + company + source).hashCode());
        }
    }

    public String cleanText(String text) {
        if (text == null) return "";
        return text.trim().replaceAll("\\s+", " ");
    }

    public String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]+>", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}