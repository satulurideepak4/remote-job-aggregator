package com.remotejobs.service.scraper;

import com.remotejobs.dto.RawJobDto;
import com.remotejobs.entity.CompanySource;
import com.remotejobs.repository.CompanySourceRepository;
import com.remotejobs.service.JobSource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Scrapes dynamically configured company career pages stored in the DB.
 */
@Slf4j
@Component
public class CompanyCareersScaper extends BaseWebScraper implements JobSource {

    private final CompanySourceRepository companySourceRepo;

    public CompanyCareersScaper(OkHttpClient httpClient, CompanySourceRepository companySourceRepo) {
        super(httpClient);
        this.companySourceRepo = companySourceRepo;
    }

    @Override
    public String sourceName() { return "CompanyCareer"; }

    @Override
    public List<RawJobDto> fetchJobs() {
        List<RawJobDto> allJobs = new ArrayList<>();
        List<CompanySource> sources = companySourceRepo.findByActiveTrue();

        for (CompanySource cs : sources) {
            try {
                log.info("[CompanyCareer] Scraping: {} - {}", cs.getCompanyName(), cs.getCareersUrl());
                List<RawJobDto> jobs = scrapeCompany(cs);
                allJobs.addAll(jobs);
                log.info("[CompanyCareer] {} jobs from {}", jobs.size(), cs.getCompanyName());
            } catch (Exception e) {
                log.error("[CompanyCareer] Failed to scrape {}: {}", cs.getCompanyName(), e.getMessage());
            }
        }
        return allJobs;
    }

    private List<RawJobDto> scrapeCompany(CompanySource cs) {
        List<RawJobDto> jobs = new ArrayList<>();
        Document doc = fetchDocument(cs.getCareersUrl());
        if (doc == null) return jobs;

        // Use provided selectors, or try common patterns
        String listSelector = cs.getJobListSelector() != null
                ? cs.getJobListSelector()
                : detectListSelector(doc);
        String titleSel = cs.getTitleSelector() != null ? cs.getTitleSelector() : "h2, h3, .title, .job-title";
        String linkSel  = cs.getLinkSelector() != null ? cs.getLinkSelector() : "a";

        Elements items = doc.select(listSelector);
        for (Element item : items) {
            try {
                String title = item.select(titleSel).first() != null
                        ? item.select(titleSel).first().text() : item.text();
                Element linkEl = item.select(linkSel).first();
                String link = linkEl != null ? linkEl.attr("abs:href") : cs.getCareersUrl();

                if (title.isBlank() || title.length() < 4) continue;

                jobs.add(RawJobDto.builder()
                        .externalId(cs.getCompanyName() + ":" + link)
                        .title(title)
                        .companyName(cs.getCompanyName())
                        .jobLink(link.isBlank() ? cs.getCareersUrl() : link)
                        .remoteTypeRaw("remote")
                        .source(sourceName() + ":" + cs.getCompanyName())
                        .postedDate(LocalDateTime.now())
                        .build());
            } catch (Exception e) {
                log.warn("[CompanyCareer] Skipping item for {}: {}", cs.getCompanyName(), e.getMessage());
            }
        }
        return jobs;
    }

    /** Heuristic to detect job listing container selector */
    private String detectListSelector(Document doc) {
        String[] candidates = {
                ".jobs-list li", ".job-listing", ".careers-list li",
                ".positions li", "table.jobs tr", ".opening", ".job-row",
                "[class*='job'] li", "[class*='position']"
        };
        for (String sel : candidates) {
            if (!doc.select(sel).isEmpty()) return sel;
        }
        return "li"; // fallback
    }
}