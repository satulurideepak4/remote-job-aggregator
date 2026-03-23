package com.remotejobs.repository;

import com.remotejobs.entity.Job;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    public static Specification<Job> withFilters(
            String remoteType,
            String jobType,
            Integer expMin,
            Integer expMax,
            Integer salaryMin,
            Integer salaryMax,
            String source,
            String keyword,
            LocalDateTime postedAfter
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter active jobs
            predicates.add(cb.isTrue(root.get("isActive")));

            if (remoteType != null && !remoteType.isBlank()) {
                predicates.add(cb.equal(
                        root.get("remoteType"),
                        Job.RemoteType.valueOf(remoteType)
                ));
            }

            if (jobType != null && !jobType.isBlank()) {
                predicates.add(cb.equal(
                        root.get("jobType"),
                        Job.JobType.valueOf(jobType)
                ));
            }

            if (expMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("experienceMin"), expMin));
            }

            if (expMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experienceMax"), expMax));
            }

            if (salaryMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMin"), salaryMin));
            }

            if (salaryMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryMax"), salaryMax));
            }

            if (source != null && !source.isBlank()) {
                predicates.add(cb.equal(root.get("source"), source));
            }

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("companyName")), pattern)
                ));
            }

            if (postedAfter != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("postedDate"), postedAfter));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}