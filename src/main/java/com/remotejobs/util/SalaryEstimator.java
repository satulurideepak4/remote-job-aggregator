package com.remotejobs.util;

import com.remotejobs.entity.Job;
import org.springframework.stereotype.Component;

/**
 * Estimates salary range when not provided by the source,
 * based on job title keywords and remote type.
 */
@Component
public class SalaryEstimator {

    public void estimate(Job job) {
        if (job.getSalaryMin() != null && job.getSalaryMin() > 0) return; // already has salary

        String title = job.getTitle() == null ? "" : job.getTitle().toLowerCase();
        int[] range = baseRange(title);

        // Adjust for experience
        if (job.getExperienceMin() != null) {
            int bump = job.getExperienceMin() * 3000;
            range[0] += bump;
            range[1] += bump;
        }

        job.setSalaryMin(range[0]);
        job.setSalaryMax(range[1]);
        job.setSalaryCurrency("USD");
        job.setSalaryEstimated(true);
    }

    private int[] baseRange(String title) {
        if (contains(title, "staff", "principal", "distinguished")) return new int[]{180_000, 280_000};
        if (contains(title, "senior", "sr.", "sr "))                 return new int[]{130_000, 190_000};
        if (contains(title, "lead", "architect"))                     return new int[]{140_000, 210_000};
        if (contains(title, "manager", "director"))                   return new int[]{120_000, 180_000};
        if (contains(title, "junior", "jr.", "entry"))                return new int[]{60_000, 90_000};
        if (contains(title, "intern"))                                 return new int[]{30_000, 55_000};
        if (contains(title, "devops", "sre", "platform"))             return new int[]{110_000, 160_000};
        if (contains(title, "data scientist", "ml engineer"))         return new int[]{120_000, 175_000};
        if (contains(title, "data engineer"))                         return new int[]{110_000, 160_000};
        if (contains(title, "full stack", "fullstack"))               return new int[]{95_000, 145_000};
        if (contains(title, "frontend", "front-end"))                 return new int[]{90_000, 135_000};
        if (contains(title, "backend", "back-end"))                   return new int[]{95_000, 145_000};
        // default software engineer
        return new int[]{90_000, 140_000};
    }

    private boolean contains(String text, String... keywords) {
        for (String kw : keywords) if (text.contains(kw)) return true;
        return false;
    }
}