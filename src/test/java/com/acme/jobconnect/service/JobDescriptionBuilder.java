package com.acme.jobconnect.service;

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

    public static JobDescriptionBuilder getBuilder() { return new JobDescriptionBuilder(); }

    public JobDescriptionBuilder setId(UUID id) {
        this.id = id;
        return this;
    }

    public JobDescriptionBuilder setTitle(String tite) {
        this.tite = tite;
        return this;
    }

    public JobDescriptionBuilder setLocation(String location) {
        this.location = location;
        return this;
    }

    public JobDescriptionBuilder setSalary(BigDecimal salary) {
        this.salary = salary;
        return this;
    }

    public JobDescriptionBuilder setSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public JobDescriptionBuilder setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
        return this;
    }

    public JobDescriptionBuilder setRequirements(String requirements) {
        this.requirements = requirements;
        return this;
    }

    public JobDescriptionBuilder setEmployment(EmploymentType employment) {
        this.employment = employment;
        return this;
    }

    public JobDescription build() {
        return new JobDescription(id, tite, location, salary, summary, responsibilities, requirements, employment);
    }
}
