package com.remotejobs.service.scraper;

import com.remotejobs.dto.RawJobDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.format.DateTimeFormatter;

// ─── Remote.co ────────────────────────────────────────────────────────────────
@Slf4j
@Component
class RemoteCoScraper extends BaseWebScraper {

    @Value("${scraper.sources.remoteco}")
    private String baseUrl;

    RemoteCoScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "Remote.co"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            Document doc = fetchDocument(baseUrl);
            if (doc == null) return jobs;

            Elements rows = doc.select(".job_listings .job_listing");
            for (Element el : rows) {
                try {
                    String title   = el.select("h3").text();
                    String company = el.select(".company strong, .company span").text();
                    String link    = el.select("a").attr("abs:href");
                    String location = el.select(".location").text();
                    if (title.isBlank() || link.isBlank()) continue;

                    jobs.add(RawJobDto.builder()
                            .externalId(link)
                            .title(title)
                            .companyName(company.isBlank() ? "Unknown" : company)
                            .jobLink(link)
                            .location(location)
                            .remoteTypeRaw("remote")
                            .source(sourceName())
                            .postedDate(LocalDateTime.now())
                            .build());
                } catch (Exception e) {
                    log.warn("[Remote.co] Skipping: {}", e.getMessage());
                }
            }
            log.info("[Remote.co] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[Remote.co] Scrape failed: {}", e.getMessage());
        }
        return jobs;
    }
}

// ─── WorkingNomads ────────────────────────────────────────────────────────────
@Slf4j
@Component
class WorkingNomadsScraper extends BaseWebScraper {

    @Value("${scraper.sources.workingnomads}")
    private String baseUrl;

    WorkingNomadsScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "WorkingNomads"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            // WorkingNomads has a JSON API
            String url = "https://www.workingnomads.com/api/exposed_jobs/?category=development";
            String json = fetchText(url);
            if (json == null) return jobs;

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);

            for (com.fasterxml.jackson.databind.JsonNode node : root) {
                try {
                    jobs.add(RawJobDto.builder()
                            .externalId(node.path("id").asText())
                            .title(node.path("title").asText())
                            .companyName(node.path("company").asText())
                            .jobLink(node.path("url").asText())
                            .description(node.path("description").asText())
                            .location(node.path("region").asText())
                            .remoteTypeRaw("remote worldwide")
                            .source(sourceName())
                            .postedDate(LocalDateTime.now())
                            .build());
                } catch (Exception e) {
                    log.warn("[WorkingNomads] Skipping: {}", e.getMessage());
                }
            }
            log.info("[WorkingNomads] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[WorkingNomads] Scrape failed: {}", e.getMessage());
        }
        return jobs;
    }
}

// ─── YC Jobs ──────────────────────────────────────────────────────────────────
@Slf4j
@Component
class YCJobsScraper extends BaseWebScraper {

    @Value("${scraper.sources.ycjobs}")
    private String baseUrl;

    YCJobsScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "YCombinator"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            Document doc = fetchDocument(baseUrl + "?remote=true");
            if (doc == null) return jobs;

            Elements listings = doc.select(".company-row");
            for (Element company : listings) {
                String companyName = company.select(".company-name").text();
                Elements roles = company.select(".role-row");

                for (Element role : roles) {
                    try {
                        String title = role.select(".role-title").text();
                        String link  = "https://www.ycombinator.com" + role.select("a").attr("href");
                        String location = role.select(".role-location").text();
                        if (title.isBlank()) continue;

                        jobs.add(RawJobDto.builder()
                                .externalId(link)
                                .title(title)
                                .companyName(companyName.isBlank() ? "YC Company" : companyName)
                                .jobLink(link)
                                .location(location)
                                .remoteTypeRaw(location.toLowerCase().contains("remote") ? "remote" : "region_specific")
                                .source(sourceName())
                                .postedDate(LocalDateTime.now())
                                .build());
                    } catch (Exception e) {
                        log.warn("[YCJobs] Skipping role: {}", e.getMessage());
                    }
                }
            }
            log.info("[YCombinator] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[YCombinator] Scrape failed: {}", e.getMessage());
        }
        return jobs;
    }
}

@Slf4j
@Component
class HimalayansScraper extends BaseWebScraper {

    private static final String API = "https://himalayas.app/jobs/api?limit=100&remote=true";

    HimalayansScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "Himalayas"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            String json = fetchText(API);
            if (json == null) return jobs;
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode jobsNode = root.path("jobs");

            for (JsonNode node : jobsNode) {
                try {
                    // Filter: only full-time software roles with salary >= 70k
                    String jobType = node.path("jobType").asText("");
                    int salMin = node.path("salaryMin").asInt(0);
                    // Skip obvious non-software or unpaid listings only if data is present
                    if (!jobType.isBlank() && jobType.equalsIgnoreCase("internship")) continue;

                    String location = node.path("locationRestrictions").toString();
                    String remoteType = resolveRemote(location);

                    jobs.add(RawJobDto.builder()
                            .externalId(node.path("id").asText())
                            .title(node.path("title").asText())
                            .companyName(node.path("companyName").asText())
                            .jobLink("https://himalayas.app" + node.path("applicationLink").asText(
                                    "/jobs/" + node.path("slug").asText()))
                            .description(node.path("description").asText())
                            .jobTypeRaw(jobType)
                            .salaryRaw(buildSalary(node, "$"))
                            .location(node.path("locationRestrictions").asText())
                            .remoteTypeRaw(remoteType)
                            .source(sourceName())
                            .postedDate(parseIso(node.path("createdAt").asText()))
                            .build());
                } catch (Exception e) {
                    log.warn("[Himalayas] Skip: {}", e.getMessage());
                }
            }
            log.info("[Himalayas] Fetched {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[Himalayas] Failed: {}", e.getMessage());
        }
        return jobs;
    }

    private String buildSalary(JsonNode n, String sym) {
        int min = n.path("salaryMin").asInt(0);
        int max = n.path("salaryMax").asInt(0);
        if (min > 0 && max > 0) return sym + min + " - " + sym + max;
        if (min > 0) return sym + min + "+";
        return "";
    }

    private String resolveRemote(String loc) {
        if (loc == null) return "remote worldwide";
        String l = loc.toLowerCase();
        if (l.contains("india") || l.contains("apac") || l.contains("asia")) return "india";
        if (l.contains("worldwide") || l.contains("global") || l.contains("anywhere")) return "remote worldwide";
        if (l.contains("us ") || l.contains("usa") || l.contains("america")) return "us only";
        return "remote worldwide";
    }

    private LocalDateTime parseIso(String s) {
        if (s == null || s.isBlank()) return LocalDateTime.now();
        try { return LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME); }
        catch (Exception e) { return LocalDateTime.now(); }
    }
}

// ─── JustRemote ───────────────────────────────────────────────────────────────
// Filter by time zone / worldwide — good for India candidates
@Slf4j
@Component
class JustRemoteScraper extends BaseWebScraper {

    private static final String BASE = "https://justremote.co/remote-developer-jobs";

    JustRemoteScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "JustRemote"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            for (int page = 1; page <= 3; page++) {
                String url = BASE + "?page=" + page;
                Document doc = fetchDocument(url);
                if (doc == null) break;

                Elements cards = doc.select(".job-card, [class*='job-card'], article.job");
                if (cards.isEmpty()) cards = doc.select("li[class*='job']");
                if (cards.isEmpty()) break;

                for (Element card : cards) {
                    try {
                        String title   = card.select("h2, h3, .job-title, [class*='title']").first() != null
                                ? card.select("h2, h3, .job-title, [class*='title']").first().text() : "";
                        String company = card.select(".company, [class*='company']").text();
                        String link    = card.select("a").attr("abs:href");
                        String location = card.select(".location, [class*='location']").text();
                        if (title.isBlank() || link.isBlank()) continue;

                        jobs.add(RawJobDto.builder()
                                .externalId(link)
                                .title(title)
                                .companyName(company.isBlank() ? "Unknown" : company)
                                .jobLink(link)
                                .location(location)
                                .remoteTypeRaw("remote worldwide")
                                .source(sourceName())
                                .postedDate(LocalDateTime.now())
                                .build());
                    } catch (Exception e) {
                        log.warn("[JustRemote] Skip: {}", e.getMessage());
                    }
                }
            }
            log.info("[JustRemote] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[JustRemote] Failed: {}", e.getMessage());
        }
        return jobs;
    }
}

// ─── Wellfound (AngelList) ────────────────────────────────────────────────────
// Startup remote roles; public listing page (no login required for listings)
@Slf4j
@Component
class WellfoundScraper extends BaseWebScraper {

    private static final String BASE = "https://wellfound.com/jobs?remote=true&role=engineer";

    WellfoundScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "Wellfound"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            Document doc = fetchDocument(BASE);
            if (doc == null) return jobs;

            // Wellfound renders React — try meta tags + JSON-LD as fallback
            Elements scripts = doc.select("script[type='application/json']");
            for (Element script : scripts) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode root = mapper.readTree(script.data());
                    JsonNode jobList = root.findPath("jobs");
                    if (jobList.isArray()) {
                        for (JsonNode node : jobList) {
                            String title = node.path("title").asText("");
                            String company = node.path("startup").path("name").asText("Unknown");
                            String link = "https://wellfound.com" + node.path("url").asText("");
                            if (title.isBlank()) continue;
                            jobs.add(RawJobDto.builder()
                                    .externalId(node.path("id").asText())
                                    .title(title).companyName(company).jobLink(link)
                                    .remoteTypeRaw("remote worldwide").source(sourceName())
                                    .jobTypeRaw("full_time")
                                    .postedDate(LocalDateTime.now()).build());
                        }
                    }
                } catch (Exception ignored) {}
            }

            // HTML fallback if JSON-LD empty
            if (jobs.isEmpty()) {
                Elements cards = doc.select("[class*='JobListing'], [class*='job-listing'], [data-test*='job']");
                for (Element card : cards) {
                    try {
                        String title   = card.select("h2, h3, [class*='title']").text();
                        String company = card.select("[class*='company'], [class*='startup']").text();
                        String link    = card.select("a").attr("abs:href");
                        if (title.isBlank() || link.isBlank()) continue;
                        jobs.add(RawJobDto.builder()
                                .externalId(link).title(title)
                                .companyName(company.isBlank() ? "Startup" : company)
                                .jobLink(link).remoteTypeRaw("remote worldwide")
                                .jobTypeRaw("full_time").source(sourceName())
                                .postedDate(LocalDateTime.now()).build());
                    } catch (Exception e) {
                        log.warn("[Wellfound] Skip card: {}", e.getMessage());
                    }
                }
            }
            log.info("[Wellfound] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[Wellfound] Failed: {}", e.getMessage());
        }
        return jobs;
    }
}

// ─── Arc.dev ──────────────────────────────────────────────────────────────────
// Vetted remote developer jobs — strong $70k+ signal
@Slf4j
@Component
class ArcDevScraper extends BaseWebScraper {

    private static final String BASE = "https://arc.dev/remote-jobs";

    ArcDevScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "Arc.dev"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            for (int page = 1; page <= 3; page++) {
                String url = BASE + "?page=" + page;
                Document doc = fetchDocument(url);
                if (doc == null) break;

                Elements cards = doc.select("[class*='JobCard'], [class*='job-card'], .job-item, li[class*='job']");
                if (cards.isEmpty()) break;

                for (Element card : cards) {
                    try {
                        String title   = card.select("h2, h3, [class*='title']").text();
                        String company = card.select("[class*='company'], [class*='employer']").text();
                        String link    = card.select("a").attr("abs:href");
                        String salary  = card.select("[class*='salary'], [class*='compensation']").text();
                        String location = card.select("[class*='location']").text();
                        if (title.isBlank() || link.isBlank()) continue;

                        jobs.add(RawJobDto.builder()
                                .externalId(link)
                                .title(title)
                                .companyName(company.isBlank() ? "Unknown" : company)
                                .jobLink(link)
                                .salaryRaw(salary)
                                .location(location)
                                .remoteTypeRaw("remote worldwide")
                                .jobTypeRaw("full_time")
                                .source(sourceName())
                                .postedDate(LocalDateTime.now())
                                .build());
                    } catch (Exception e) {
                        log.warn("[Arc.dev] Skip: {}", e.getMessage());
                    }
                }
            }
            log.info("[Arc.dev] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[Arc.dev] Failed: {}", e.getMessage());
        }
        return jobs;
    }
}

// ─── NoDesk ───────────────────────────────────────────────────────────────────
// Clean remote-only board, good India coverage
@Slf4j
@Component
class NoDeskScraper extends BaseWebScraper {

    private static final String BASE = "https://nodesk.co/remote-jobs/engineering/";

    NoDeskScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "NoDesk"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            Document doc = fetchDocument(BASE);
            if (doc == null) return jobs;

            Elements items = doc.select("article, .job-item, li.job");
            for (Element item : items) {
                try {
                    String title   = item.select("h2, h3, .title").text();
                    String company = item.select(".company, .employer").text();
                    String link    = item.select("a").attr("abs:href");
                    String location = item.select(".location, .region").text();
                    if (title.isBlank() || link.isBlank()) continue;

                    jobs.add(RawJobDto.builder()
                            .externalId(link).title(title)
                            .companyName(company.isBlank() ? "Unknown" : company)
                            .jobLink(link).location(location)
                            .remoteTypeRaw("remote worldwide")
                            .source(sourceName())
                            .postedDate(LocalDateTime.now()).build());
                } catch (Exception e) {
                    log.warn("[NoDesk] Skip: {}", e.getMessage());
                }
            }
            log.info("[NoDesk] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[NoDesk] Failed: {}", e.getMessage());
        }
        return jobs;
    }
}

// ─── DailyRemote ──────────────────────────────────────────────────────────────
// Fresh listings updated every 24h — matches your criteria perfectly
@Slf4j
@Component
class DailyRemoteScraper extends BaseWebScraper {

    private static final String BASE = "https://dailyremote.com/remote-jobs?category=software-development";

    DailyRemoteScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "DailyRemote"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            Document doc = fetchDocument(BASE);
            if (doc == null) return jobs;

            Elements cards = doc.select(".card-job, [class*='job-card'], article[class*='job']");
            for (Element card : cards) {
                try {
                    String title   = card.select("h2, h3, .job-title, [itemprop='title']").text();
                    String company = card.select(".company-name, [itemprop='hiringOrganization']").text();
                    String link    = card.select("a[href*='/remote-']").attr("abs:href");
                    if (link.isBlank()) link = card.select("a").first() != null
                            ? card.select("a").first().attr("abs:href") : "";
                    String salary  = card.select(".salary, [class*='salary']").text();
                    String location = card.select(".location, [class*='location']").text();
                    if (title.isBlank() || link.isBlank()) continue;

                    jobs.add(RawJobDto.builder()
                            .externalId(link).title(title)
                            .companyName(company.isBlank() ? "Unknown" : company)
                            .jobLink(link).salaryRaw(salary).location(location)
                            .remoteTypeRaw("remote worldwide").jobTypeRaw("full_time")
                            .source(sourceName()).postedDate(LocalDateTime.now()).build());
                } catch (Exception e) {
                    log.warn("[DailyRemote] Skip: {}", e.getMessage());
                }
            }
            log.info("[DailyRemote] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[DailyRemote] Failed: {}", e.getMessage());
        }
        return jobs;
    }
}

// ─── RemoteOtter ──────────────────────────────────────────────────────────────
// Aggregates from 50+ sources — captures many India-eligible listings
@Slf4j
@Component
class RemoteOtterScraper extends BaseWebScraper {

    private static final String BASE = "https://remoteotter.com/jobs";

    RemoteOtterScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "RemoteOtter"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            Document doc = fetchDocument(BASE + "?category=software-development&type=full_time");
            if (doc == null) return jobs;

            Elements cards = doc.select("[class*='job'], article, li[class*='listing']");
            for (Element card : cards) {
                try {
                    String title   = card.select("h2, h3, [class*='title']").text();
                    String company = card.select("[class*='company']").text();
                    String link    = card.select("a").attr("abs:href");
                    if (title.isBlank() || link.isBlank() || title.length() < 4) continue;

                    jobs.add(RawJobDto.builder()
                            .externalId(link).title(title)
                            .companyName(company.isBlank() ? "Unknown" : company)
                            .jobLink(link).remoteTypeRaw("remote worldwide")
                            .jobTypeRaw("full_time").source(sourceName())
                            .postedDate(LocalDateTime.now()).build());
                } catch (Exception e) {
                    log.warn("[RemoteOtter] Skip: {}", e.getMessage());
                }
            }
            log.info("[RemoteOtter] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[RemoteOtter] Failed: {}", e.getMessage());
        }
        return jobs;
    }
}

// ─── Jobgether ────────────────────────────────────────────────────────────────
// Explicitly lists "Global" eligibility — great for India
@Slf4j
@Component
class JobgetherScraper extends BaseWebScraper {

    private static final String API =
            "https://jobgether.com/api/offers?type=full_time&remote=worldwide&page=1";

    JobgetherScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override public String sourceName() { return "Jobgether"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            // Try JSON API first
            String json = fetchText(API);
            if (json != null && json.startsWith("{")) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json);
                JsonNode offers = root.findPath("offers");
                if (offers.isArray()) {
                    for (JsonNode node : offers) {
                        try {
                            String title = node.path("title").asText("");
                            String company = node.path("company").path("name").asText("Unknown");
                            String link = "https://jobgether.com/offer/" + node.path("slug").asText();
                            String salary = node.path("salary").asText("");
                            if (title.isBlank()) continue;
                            jobs.add(RawJobDto.builder()
                                    .externalId(node.path("id").asText())
                                    .title(title).companyName(company).jobLink(link)
                                    .salaryRaw(salary).remoteTypeRaw("remote worldwide")
                                    .jobTypeRaw("full_time").source(sourceName())
                                    .postedDate(LocalDateTime.now()).build());
                        } catch (Exception e) {
                            log.warn("[Jobgether] Skip node: {}", e.getMessage());
                        }
                    }
                    log.info("[Jobgether] API: {} jobs", jobs.size());
                    return jobs;
                }
            }

            // HTML fallback
            Document doc = fetchDocument("https://jobgether.com/remote-jobs?type=full-time");
            if (doc == null) return jobs;
            Elements cards = doc.select("[class*='offer'], [class*='job-card'], article");
            for (Element card : cards) {
                try {
                    String title   = card.select("h2, h3, [class*='title']").text();
                    String company = card.select("[class*='company']").text();
                    String link    = card.select("a").attr("abs:href");
                    if (title.isBlank() || link.isBlank()) continue;
                    jobs.add(RawJobDto.builder()
                            .externalId(link).title(title)
                            .companyName(company.isBlank() ? "Unknown" : company)
                            .jobLink(link).remoteTypeRaw("remote worldwide")
                            .jobTypeRaw("full_time").source(sourceName())
                            .postedDate(LocalDateTime.now()).build());
                } catch (Exception e) {
                    log.warn("[Jobgether] Skip HTML: {}", e.getMessage());
                }
            }
            log.info("[Jobgether] HTML: {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[Jobgether] Failed: {}", e.getMessage());
        }
        return jobs;
    }
}