-- ============================================================
-- V2: Default subscription for India-allowed $70k+ full-time
-- ============================================================

-- Insert a default "India $70k+" subscription as reference
-- User can DELETE or UPDATE this row via the API
INSERT INTO subscriptions (channel, destination, filters, active)
VALUES (
    'EMAIL',
    'your@email.com',
    '{
        "remoteType": ["INDIA_ALLOWED", "GLOBAL"],
        "jobType": "FULL_TIME",
        "salaryMin": 70000,
        "techStack": ["Java", "Python", "JavaScript", "Go", "Kotlin"]
    }'::jsonb,
    false   -- disabled by default; set active=true after adding real email
)
ON CONFLICT DO NOTHING;

-- Index to speed up the India + salary combo query (your most common use case)
CREATE INDEX IF NOT EXISTS idx_jobs_india_salary
    ON jobs(remote_type, salary_min)
    WHERE is_active = true
      AND remote_type IN ('INDIA_ALLOWED', 'GLOBAL')
      AND salary_min >= 70000;

-- Partial index: full-time only
CREATE INDEX IF NOT EXISTS idx_jobs_fulltime
    ON jobs(posted_date DESC)
    WHERE is_active = true AND job_type = 'FULL_TIME';