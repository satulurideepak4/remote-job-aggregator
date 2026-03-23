-- ===========================
-- Remote Job Aggregator Schema
-- ===========================

-- Jobs table
CREATE TABLE IF NOT EXISTS jobs (
    id              BIGSERIAL PRIMARY KEY,
    external_id     VARCHAR(512),
    title           VARCHAR(500)        NOT NULL,
    company_name    VARCHAR(255)        NOT NULL,
    job_link        TEXT                NOT NULL,
    description     TEXT,
    job_type        VARCHAR(50),             -- FULL_TIME, CONTRACT, PART_TIME
    remote_type     VARCHAR(50),             -- GLOBAL, REGION_SPECIFIC, INDIA_ALLOWED
    experience_min  INTEGER,
    experience_max  INTEGER,
    salary_min      INTEGER,
    salary_max      INTEGER,
    salary_currency VARCHAR(10),
    salary_estimated BOOLEAN DEFAULT FALSE,
    tech_stack      TEXT[],                  -- Array of tech keywords
    source          VARCHAR(100)        NOT NULL,
    location        VARCHAR(255),
    posted_date     TIMESTAMP,
    scraped_at      TIMESTAMP           NOT NULL DEFAULT NOW(),
    is_active       BOOLEAN             NOT NULL DEFAULT TRUE,
    hash            VARCHAR(64)         UNIQUE,   -- for dedup
    CONSTRAINT uq_external UNIQUE (source, external_id)
);

-- Indexes for fast filtering
CREATE INDEX IF NOT EXISTS idx_jobs_source       ON jobs(source);
CREATE INDEX IF NOT EXISTS idx_jobs_posted_date  ON jobs(posted_date DESC);
CREATE INDEX IF NOT EXISTS idx_jobs_remote_type  ON jobs(remote_type);
CREATE INDEX IF NOT EXISTS idx_jobs_job_type     ON jobs(job_type);
CREATE INDEX IF NOT EXISTS idx_jobs_exp          ON jobs(experience_min, experience_max);
CREATE INDEX IF NOT EXISTS idx_jobs_salary       ON jobs(salary_min, salary_max);
CREATE INDEX IF NOT EXISTS idx_jobs_company      ON jobs(company_name);
CREATE INDEX IF NOT EXISTS idx_jobs_active       ON jobs(is_active);
CREATE INDEX IF NOT EXISTS idx_jobs_tech_stack   ON jobs USING GIN(tech_stack);
CREATE INDEX IF NOT EXISTS idx_jobs_scraped_at   ON jobs(scraped_at DESC);

-- Full-text search index on title + description
CREATE INDEX IF NOT EXISTS idx_jobs_fts ON jobs
    USING GIN(to_tsvector('english', title || ' ' || COALESCE(description, '')));

-- Notification subscriptions
CREATE TABLE IF NOT EXISTS subscriptions (
    id              BIGSERIAL PRIMARY KEY,
    channel         VARCHAR(20)  NOT NULL,   -- EMAIL, TELEGRAM
    destination     VARCHAR(255) NOT NULL,   -- email address or chat_id
    filters         JSONB,                   -- stored filter criteria
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Scrape run audit log
CREATE TABLE IF NOT EXISTS scrape_runs (
    id              BIGSERIAL PRIMARY KEY,
    source          VARCHAR(100)  NOT NULL,
    started_at      TIMESTAMP     NOT NULL DEFAULT NOW(),
    finished_at     TIMESTAMP,
    jobs_found      INTEGER       DEFAULT 0,
    jobs_inserted   INTEGER       DEFAULT 0,
    jobs_updated    INTEGER       DEFAULT 0,
    status          VARCHAR(20)   NOT NULL DEFAULT 'RUNNING',  -- RUNNING, SUCCESS, FAILED
    error_message   TEXT
);

CREATE INDEX IF NOT EXISTS idx_runs_source ON scrape_runs(source);
CREATE INDEX IF NOT EXISTS idx_runs_started ON scrape_runs(started_at DESC);

-- Dynamic company career pages config
CREATE TABLE IF NOT EXISTS company_sources (
    id              BIGSERIAL PRIMARY KEY,
    company_name    VARCHAR(255)  NOT NULL,
    careers_url     TEXT          NOT NULL,
    job_list_selector VARCHAR(500),
    title_selector  VARCHAR(500),
    link_selector   VARCHAR(500),
    active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP     NOT NULL DEFAULT NOW()
);