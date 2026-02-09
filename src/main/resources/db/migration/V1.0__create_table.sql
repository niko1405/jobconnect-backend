SET default_tablespace = jobconnectspace;

-- 1. Erst Job Description (hat keine Abhängigkeiten)
CREATE TABLE IF NOT EXISTS job_description (
    id UUID PRIMARY KEY,
    title TEXT NOT NULL CHECK (length(trim(title)) > 0),
    location TEXT NOT NULL,
    salary NUMERIC(12, 2) CHECK (salary > 0),
    summary TEXT,
    responsibilities TEXT,
    requirements TEXT,
    employment TEXT NOT NULL DEFAULT 'FULLTIME' CHECK (employment IN ('FULLTIME', 'PARTTIME', 'INTERNSHIP', 'CONTRACT'))
    );

CREATE TABLE IF NOT EXISTS joboffer (
    id UUID PRIMARY KEY,
    version       INTEGER NOT NULL DEFAULT 0,
    company TEXT NOT NULL CHECK (length(trim(company)) > 0),
    publicationdate DATE NOT NULL DEFAULT CURRENT_DATE,
    expirationdate DATE,
    viewscount INTEGER NOT NULL DEFAULT 0 CHECK (viewscount >= 0),
    status TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('ACTIVE', 'CLOSED', 'DRAFT')),
    job_description_id UUID UNIQUE REFERENCES job_description(id),

    CONSTRAINT check_dates_logical CHECK (expirationdate > publicationdate)
    );

CREATE TABLE IF NOT EXISTS application (
    id                  UUID PRIMARY KEY,
    applicant TEXT      NOT NULL CHECK (length(trim(applicant)) > 0),
    resume TEXT         NOT NULL,
    coverletter         TEXT,
    application_date    DATE NOT NULL DEFAULT CURRENT_DATE,
    documents           TEXT[],
    status              TEXT check (status in ('APPLIED', 'REVIEWED', 'REJECTED', 'APPROVED')),
    joboffer_id         UUID REFERENCES joboffer(id),
    idx                 INTEGER NOT NULL DEFAULT 0
    );

-- -- docker compose exec postgres bash
-- -- psql --dbname=jobconnect --username=jobconnect [--file=/sql/V1.0__create_table.sql]
--
-- SET default_tablespace = jobconnectspace;
--
-- CREATE TABLE IF NOT EXISTS application (
--     id                  UUID PRIMARY KEY DEFAULT,
--     applicant TEXT      NOT NULL CHECK (length(trim(applicant)) > 0),
--     resume_uri TEXT     NOT NULL,
--     cover_letter_uri    TEXT,
--     application_date    DATE NOT NULL DEFAULT CURRENT_DATE,
--     document_uris       TEXT[],
--     status              TEXT check (status in ('APPLIED', 'REVIEWED', 'REJECTED', 'APPROVED')),
--     joboffer_id         UUID REFERENCES joboffer,
--     idx                 INTEGER NOT NULL DEFAULT 0
-- );
--
-- CREATE TABLE IF NOT EXISTS joboffer (
--     id UUID PRIMARY KEY,
--     company TEXT NOT NULL CHECK (length(trim(company)) > 0),
--     publication_date DATE NOT NULL DEFAULT CURRENT_DATE,
--     expiration_date DATE,
--     views_count INTEGER NOT NULL DEFAULT 0 CHECK (views_count >= 0),
--     status TEXT NOT NULL DEFAULT 'DRAFT' CHECK (status IN ('ACTIVE', 'CLOSED', 'DRAFT')),
--     job_description_id UUID UNIQUE REFERENCES job_description,
--
--     CONSTRAINT check_dates_logical CHECK (expiration_date > publication_date)
-- );
--
-- CREATE TABLE IF NOT EXISTS job_description (
--     id UUID PRIMARY KEY,
--     title TEXT NOT NULL CHECK (length(trim(title)) > 0),
--     location TEXT NOT NULL,
--     salary NUMERIC(12, 2) CHECK (salary > 0),
--     summary TEXT,
--     responsibilities TEXT,
--     requirements TEXT,
--     employment_type TEXT NOT NULL DEFAULT 'FULL_TIME' CHECK (employment_type IN ('FULLTIME', 'PARTTIME', 'INTERNSHIP', 'CONTRACT')),
-- );
