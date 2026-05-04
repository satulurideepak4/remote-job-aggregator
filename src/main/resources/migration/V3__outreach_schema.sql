-- ===========================
-- Outreach Dashboard Schema
-- ===========================

-- Product Hunt company launches
CREATE TABLE IF NOT EXISTS product_hunt_companies (
    id              BIGSERIAL PRIMARY KEY,
    external_id     VARCHAR(255)    UNIQUE NOT NULL,
    product_name    VARCHAR(500)    NOT NULL,
    company_name    VARCHAR(500),
    tagline         TEXT,
    website_url     TEXT,
    maker_names     TEXT,
    upvotes         INTEGER         DEFAULT 0,
    topics          TEXT[],
    launch_date     TIMESTAMP,
    fetched_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_ph_launch_date  ON product_hunt_companies(launch_date DESC);
CREATE INDEX IF NOT EXISTS idx_ph_external_id  ON product_hunt_companies(external_id);

-- LinkedIn hiring signals (manual ingestion)
CREATE TABLE IF NOT EXISTS linkedin_signals (
    id              BIGSERIAL PRIMARY KEY,
    post_url        TEXT,
    author_name     VARCHAR(255),
    company_name    VARCHAR(500)    NOT NULL,
    post_snippet    TEXT,
    hiring_signal   VARCHAR(500),
    added_at        TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_linkedin_company   ON linkedin_signals(company_name);
CREATE INDEX IF NOT EXISTS idx_linkedin_added_at  ON linkedin_signals(added_at DESC);

-- Twitter/X hiring signals (manual ingestion)
CREATE TABLE IF NOT EXISTS twitter_signals (
    id              BIGSERIAL PRIMARY KEY,
    tweet_url       TEXT,
    author_handle   VARCHAR(255),
    author_name     VARCHAR(255),
    company_name    VARCHAR(500)    NOT NULL,
    tweet_snippet   TEXT,
    hiring_signal   VARCHAR(500),
    added_at        TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_twitter_company   ON twitter_signals(company_name);
CREATE INDEX IF NOT EXISTS idx_twitter_added_at  ON twitter_signals(added_at DESC);

-- Outreach tracking table
CREATE TABLE IF NOT EXISTS outreach_targets (
    id               BIGSERIAL PRIMARY KEY,
    company_name     VARCHAR(500)    NOT NULL,
    source           VARCHAR(50)     NOT NULL DEFAULT 'MANUAL',
    company_website  TEXT,
    contact_name     VARCHAR(255),
    contact_role     VARCHAR(255),
    contact_email    VARCHAR(255),
    outreach_status  VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    notes            TEXT,
    created_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_outreach_status     ON outreach_targets(outreach_status);
CREATE INDEX IF NOT EXISTS idx_outreach_source     ON outreach_targets(source);
CREATE INDEX IF NOT EXISTS idx_outreach_created_at ON outreach_targets(created_at DESC);
