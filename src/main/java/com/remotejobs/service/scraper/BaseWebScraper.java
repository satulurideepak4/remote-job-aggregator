package com.remotejobs.service.scraper;

import com.remotejobs.service.JobSource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public abstract class BaseWebScraper implements JobSource {

    protected final OkHttpClient httpClient;

    @Value("${scraper.user-agent}")
    protected String userAgent;

    @Value("${scraper.request-delay-ms:1500}")
    protected long delayMs;

    @Value("${scraper.max-retries:3}")
    protected int maxRetries;

    protected BaseWebScraper(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Fetches a URL and returns a Jsoup Document.
     * Respects rate limiting (delay between requests) and retries on failure.
     */
    protected Document fetchDocument(String url) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                throttle();
                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", userAgent)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .header("Connection", "keep-alive")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.warn("[{}] HTTP {} for URL: {}", sourceName(), response.code(), url);
                        if (response.code() == 429) {
                            Thread.sleep(delayMs * attempt * 2); // back off on rate limit
                        }
                        continue;
                    }
                    String body = response.body().string();
                    return Jsoup.parse(body, url);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("[{}] Attempt {}/{} failed for {}: {}", sourceName(), attempt, maxRetries, url, e.getMessage());
                if (attempt == maxRetries) {
                    log.error("[{}] All retries exhausted for {}", sourceName(), url);
                }
            }
        }
        return null;
    }

    /**
     * Fetches plain text/JSON from a URL.
     */
    protected String fetchText(String url) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                throttle();
                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", userAgent)
                        .header("Accept", "application/json, text/plain, */*")
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        return response.body().string();
                    }
                }
            } catch (Exception e) {
                log.warn("[{}] fetchText attempt {}/{} failed: {}", sourceName(), attempt, maxRetries, e.getMessage());
            }
        }
        return null;
    }

    private void throttle() throws InterruptedException {
        Thread.sleep(delayMs);
    }
}