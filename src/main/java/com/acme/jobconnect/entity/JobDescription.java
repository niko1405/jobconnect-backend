package com.acme.jobconnect.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import static jakarta.persistence.EnumType.STRING;

@Entity
public class JobDescription {
    @Id
    @GeneratedValue
    private UUID id;
    private String title;
    private String location;
    private BigDecimal salary;
    private String summary;
    private String responsibilities;
    private String requirements;

    @Enumerated(STRING)
    private EmploymentType employment;

    public JobDescription(final UUID id, final String tite, final String location, final BigDecimal salary, final String summary, final String responsibilities, final String requirements, final EmploymentType employment) {
        this.id = id;
        this.title = tite;
        this.location = location;
        this.salary = salary;
        this.summary = summary;
        this.responsibilities = responsibilities;
        this.requirements = requirements;
        this.employment = employment;
    }

    public JobDescription() {

    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof JobDescription that)) return false;
        return Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(title);
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(final BigDecimal salary) {
        this.salary = salary;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(final String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(final String requirements) {
        this.requirements = requirements;
    }

    public EmploymentType getEmployment() {
        return employment;
    }

    public void setEmployment(final EmploymentType employment) {
        this.employment = employment;
    }

    @Override
    public String toString() {
        return "JobDescription{" +
                "id='" + id + '\'' +
                '}';
    }
}
