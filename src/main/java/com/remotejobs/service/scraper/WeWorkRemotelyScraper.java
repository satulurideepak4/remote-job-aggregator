package com.remotejobs.service.scraper;

import com.remotejobs.dto.RawJobDto;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class WeWorkRemotelyScraper extends BaseWebScraper {

    @Value("${scraper.sources.weworkremotely}")
    private String rssUrl;

    public WeWorkRemotelyScraper(OkHttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public String sourceName() { return "WeWorkRemotely"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            String xml = fetchText(rssUrl);
            if (xml == null) return jobs;

            // Parse RSS as XML via Jsoup
            Document doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser());
            Elements items = doc.select("item");

            for (Element item : items) {
                try {
                    String title = item.select("title").first().text();
                    String link = item.select("link").first().text();
                    // WWR title format: "Company: Job Title"
                    String[] parts = title.split(":", 2);
                    String company = parts.length > 1 ? parts[0].trim() : "Unknown";
                    String jobTitle = parts.length > 1 ? parts[1].trim() : title;

                    String description = item.select("description").first().text();
                    String pubDate = item.select("pubDate").first().text();

                    jobs.add(RawJobDto.builder()
                            .externalId(link)
                            .title(jobTitle)
                            .companyName(company)
                            .jobLink(link)
                            .description(description)
                            .remoteTypeRaw("remote worldwide")
                            .source(sourceName())
                            .postedDate(parseRssDate(pubDate))
                            .build());
                } catch (Exception e) {
                    log.warn("[WWR] Skipping item: {}", e.getMessage());
                }
            }
            log.info("[WeWorkRemotely] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[WeWorkRemotely] Scrape failed: {}", e.getMessage());
        }
        return jobs;
    }

    private LocalDateTime parseRssDate(String date) {
        if (date == null || date.isBlank()) return LocalDateTime.now();
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                    "EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            return LocalDateTime.parse(date.trim(), fmt);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}