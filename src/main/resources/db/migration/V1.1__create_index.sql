SET default_tablespace = jobconnectspace;

-- Indexe mit pgAdmin auflisten: "Query Tool" verwenden mit
--  SELECT   tablename, indexname, indexdef, tablespace
--  FROM     pg_indexes
--  WHERE    schemaname = 'kunde'
--  ORDER BY tablename, indexname;

CREATE INDEX IF NOT EXISTS job_description_tite_idx ON job_description(title);
CREATE INDEX IF NOT EXISTS joboffer_company_idx ON joboffer(company);
CREATE INDEX IF NOT EXISTS application_joboffer_id_idx ON application(joboffer_id);
