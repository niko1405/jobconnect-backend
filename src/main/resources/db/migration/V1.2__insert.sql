DO $$
DECLARE
    -- Variablen für Job Descriptions
    jd1 UUID := '10000000-0000-0000-0000-000000000000';
    jd2 UUID := '10000000-0000-0000-0000-000000000001';
    jd3 UUID := '10000000-0000-0000-0000-000000000020';
    jd4 UUID := '10000000-0000-0000-0000-000000000030';
    jd5 UUID := '10000000-0000-0000-0000-000000000040';

    -- Variablen für Job Offers
    jo1 UUID := '00000000-0000-0000-0000-000000000000';
    jo2 UUID := '00000000-0000-0000-0000-000000000001';
    jo3 UUID := '00000000-0000-0000-0000-000000000020';
    jo4 UUID := '00000000-0000-0000-0000-000000000030';
    jo5 UUID := '00000000-0000-0000-0000-000000000040';
BEGIN

    -- 1. INSERT JOB DESCRIPTIONS
INSERT INTO job_description (id, title, location, salary, summary, responsibilities, requirements, employment) VALUES
(jd1, 'Backend Java Developer', 'Berlin', 72000.00, 'Core backend dev.', 'API Design, SQL optimization', 'Java 21, Spring Boot 3', 'FULLTIME'),
(jd2, 'Frontend React Engineer', 'Remote (EU)', 68000.00, 'UI Expert needed.', 'React Hooks, Tailwind', 'Typescript, CSS', 'FULLTIME'),
(jd3, 'DevOps Engineer', 'Munich', 85000.00, 'Cloud Infrastructure.', 'Terraform, AWS, CI/CD', 'Linux Pro', 'CONTRACT'),
(jd4, 'Marketing Intern', 'Hamburg', 1600.00, 'Social Media support.', 'Content Creation', 'Student', 'INTERNSHIP'),
(jd5, 'Data Analyst', 'Frankfurt', 55000.00, 'Bank data analysis.', 'Reporting, SQL', 'Excel, Tableau', 'PARTTIME');

-- 2. INSERT JOB OFFERS (verknüpft mit Descriptions über Variablen jd1-jd5)
INSERT INTO joboffer (id, version, company, publicationdate, expirationdate, viewscount, status, job_description_id) VALUES
(jo1, 0, 'TechCorp GmbH', CURRENT_DATE - 15, CURRENT_DATE + 15, 230, 'ACTIVE', jd1),
(jo2, 0, 'StartUp Rocket', CURRENT_DATE - 2,  CURRENT_DATE + 28, 45,  'ACTIVE', jd2),
(jo3, 0, 'CloudSystems',  CURRENT_DATE - 60, CURRENT_DATE - 10, 500, 'CLOSED', jd3), -- Abgelaufen
(jo4, 0, 'MediaGroup',    CURRENT_DATE,      CURRENT_DATE + 30, 0,   'DRAFT',  jd4), -- Entwurf
(jo5, 0, 'FinBank AG',    CURRENT_DATE - 5,  CURRENT_DATE + 25, 120, 'ACTIVE', jd5);

-- 3. INSERT APPLICATIONS (verknüpft mit Offers über Variablen jo1-jo5)
INSERT INTO application (id, applicant, resume, coverletter, application_date, documents, status, joboffer_id, idx) VALUES
 -- Bewerber 1
 (gen_random_uuid(), 'Max Mustermann', 's3://cv/max.pdf', 's3://cl/max.pdf', CURRENT_DATE - 10, ARRAY['s3://certs/java.pdf'], 'REVIEWED', jo1, 0),

 -- Bewerber 2
 (gen_random_uuid(), 'Sarah Connor', 's3://cv/sarah.pdf', NULL, CURRENT_DATE - 1, ARRAY[]::text[], 'APPLIED', jo2, 0),

 -- Bewerber 3
 (gen_random_uuid(), 'John Doe', 's3://cv/john.pdf', 's3://cl/john_motivation.pdf', CURRENT_DATE - 40, NULL, 'REJECTED', jo3, 0),

 -- Bewerber 4
 (gen_random_uuid(), 'Emily Blunt', 's3://cv/emily.docx', 's3://cl/emily.docx', CURRENT_DATE - 3, ARRAY['s3://refs/ref1.pdf', 's3://refs/ref2.pdf'], 'APPROVED', jo5, 0),

 -- Bewerber 5
 (gen_random_uuid(), 'Klaus Kleber', 's3://cv/klaus.pdf', NULL, CURRENT_DATE - 2, ARRAY['s3://certs/scrum.pdf'], 'APPLIED', jo1, 1);
END $$;
