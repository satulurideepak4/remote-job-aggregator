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

@Slf4j
@Component
public class JobspressoScraper extends BaseWebScraper {

    @Value("${scraper.sources.jobspresso}")
    private String baseUrl;

    public JobspressoScraper(OkHttpClient httpClient) { super(httpClient); }

    @Override
    public String sourceName() { return "Jobspresso"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> jobs = new ArrayList<>();
        try {
            Document doc = fetchDocument(baseUrl);
            if (doc == null) return jobs;

            Elements listings = doc.select("li.job_listing");

            for (Element el : listings) {
                try {
                    String title = el.select("h3.job-title").text();
                    String company = el.select(".company strong").text();
                    String link = el.select("a").attr("abs:href");
                    String location = el.select(".location").text();
                    String jobType = el.select(".job-type").text();

                    if (title.isBlank() || link.isBlank()) continue;

                    jobs.add(RawJobDto.builder()
                            .externalId(link)
                            .title(title)
                            .companyName(company.isBlank() ? "Unknown" : company)
                            .jobLink(link)
                            .location(location)
                            .jobTypeRaw(jobType)
                            .remoteTypeRaw("remote")
                            .source(sourceName())
                            .postedDate(LocalDateTime.now())
                            .build());
                } catch (Exception e) {
                    log.warn("[Jobspresso] Skipping listing: {}", e.getMessage());
                }
            }
            log.info("[Jobspresso] Scraped {} jobs", jobs.size());
        } catch (Exception e) {
            log.error("[Jobspresso] Scrape failed: {}", e.getMessage());
        }
        return jobs;
    }
}