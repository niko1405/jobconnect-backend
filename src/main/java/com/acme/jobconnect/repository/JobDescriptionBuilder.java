package com.acme.jobconnect.repository;

import com.acme.jobconnect.entity.EmploymentType;
import com.acme.jobconnect.entity.JobDescription;
import java.math.BigDecimal;
import java.util.UUID;

public class JobDescriptionBuilder {
    private UUID id;
    private String tite;
    private String location;
    private BigDecimal salary;
    private String summary;
    private String responsibilities;
    private String requirements;
    private EmploymentType employment;

    public static JobDescriptionBuilder getBuilder() {
        return new JobDescriptionBuilder();
    }

    public JobDescriptionBuilder setId(final UUID id) {
        this.id = id;
        return this;
    }

    public JobDescriptionBuilder setTite(final String tite) {
        this.tite = tite;
        return this;
    }

    public JobDescriptionBuilder setLocation(final String location) {
        this.location = location;
        return this;
    }

    public JobDescriptionBuilder setSalary(final BigDecimal salary) {
        this.salary = salary;
        return this;
    }

    public JobDescriptionBuilder setSummary(final String summary) {
        this.summary = summary;
        return this;
    }

    public JobDescriptionBuilder setResponsibilities(final String responsibilities) {
        this.responsibilities = responsibilities;
        return this;
    }

    public JobDescriptionBuilder setRequirements(final String requirements) {
        this.requirements = requirements;
        return this;
    }

    public JobDescriptionBuilder setEmployment(final EmploymentType employment) {
        this.employment = employment;
        return this;
    }

    public JobDescription build() {
        return new JobDescription(id, tite, location, salary, summary, responsibilities, requirements, employment);
    }
}
