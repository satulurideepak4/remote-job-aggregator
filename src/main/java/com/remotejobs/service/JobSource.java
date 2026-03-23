package com.remotejobs.service;

import com.remotejobs.dto.RawJobDto;

import java.util.List;

/**
 * Marker interface for all job data sources (API clients + scrapers).
 */
public interface JobSource {

    /** Unique source identifier stored in the DB */
    String sourceName();

    /** Fetch/scrape jobs and return raw, un-normalized data */
    List<RawJobDto> fetchJobs();
}