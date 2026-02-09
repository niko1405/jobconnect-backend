package com.acme.jobconnect.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import static com.acme.jobconnect.entity.JobOffer.DESCRIPTION_APPLICATIONS_GRAPH;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;

/// Data for Joboffer.
@Entity
@NamedEntityGraph(name = JobOffer.JOBDESCRIPTION_GRAPH, attributeNodes = @NamedAttributeNode("description"))
@NamedEntityGraph(name = DESCRIPTION_APPLICATIONS_GRAPH, attributeNodes = {
    @NamedAttributeNode("description"), @NamedAttributeNode("applications")
})
@Table(name = "joboffer")
@SuppressWarnings({
    "ClassFanOutComplexity",
    "RequireEmptyLineBeforeBlockTagGroup",
    "DeclarationOrder",
    "JavadocDeclaration",
    "MissingSummary",
    "RedundantSuppression",
    "PMD.UnusedPrivateMethod",
    "com.intellij.jpb.LombokEqualsAndHashCodeInspection"
})
public class JobOffer {
    /// NamedEntityGraph für das Attribut "jobdescription".
    public static final String JOBDESCRIPTION_GRAPH = "JobOffer.description";

    /// NamedEntityGraph für die Attribute "jobdescription" und "applications".
    public static final String DESCRIPTION_APPLICATIONS_GRAPH = "JobOffer.descriptionApplications";

    @Id
    @GeneratedValue
    private UUID id;

    @Version
    private int version;

    private String company;

    @CreationTimestamp
    private LocalDate publicationdate;

    private LocalDate expirationdate;
    private int viewscount;

    @Enumerated(STRING)
    private JobOfferStatus status;

    @OneToMany(cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    @JoinColumn(name = "joboffer_id", nullable = false)
    @OrderColumn(name = "idx", nullable = false)
    @JsonIgnore
    private List<Application> applications;

    @OneToOne(optional = false, cascade = {PERSIST, REMOVE}, fetch = LAZY, orphanRemoval = true)
    @JoinColumn(name = "job_description_id")
    private JobDescription description;

    public JobOffer(final UUID id, final int version, final String company, final LocalDate publicationdate, final LocalDate expirationdate, final JobOfferStatus status, final List<Application> applications, final JobDescription description) {
        this.id = id;
        this.version = version;
        this.company = company;
        this.publicationdate = publicationdate;
        this.expirationdate = expirationdate;
        this.viewscount = 0;
        this.status = status;
        this.applications = applications;
        this.description = description;
    }

    public JobOffer() {
    }

    public void set(final JobOffer jobOffer) {
        company = jobOffer.company;
        publicationdate = jobOffer.publicationdate;
        expirationdate = jobOffer.expirationdate;
        viewscount = jobOffer.viewscount;
        status = jobOffer.status;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof JobOffer jobOffer)) return false;
        return Objects.equals(id, jobOffer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(final int version) {
        this.version = version;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(final String company) {
        this.company = company;
    }

    public LocalDate getPublicationdate() {
        return publicationdate;
    }

    public void setPublicationdate(final LocalDate publicationdate) {
        this.publicationdate = publicationdate;
    }

    public LocalDate getExpirationdate() {
        return expirationdate;
    }

    public void setExpirationdate(final LocalDate expirationdate) {
        this.expirationdate = expirationdate;
    }

    public int getViewscount() {
        return viewscount;
    }

    public void setViewscount(final int viewscount) {
        this.viewscount = viewscount;
    }

    public JobOfferStatus getStatus() {
        return status;
    }

    public void setStatus(final JobOfferStatus status) {
        this.status = status;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public void setApplications(final List<Application> applications) {
        this.applications = applications;
    }

    public JobDescription getDescription() {
        return description;
    }

    public void setDescription(final JobDescription description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "JobOffer{" +
                "id=" + id +
                ", company='" + company + '\'' +
                ", publicationdate=" + publicationdate +
                ", expirationdate=" + expirationdate +
                ", viewscount=" + viewscount +
                ", status=" + status +
                '}';
    }
}
