package com.acme.jobconnect.repository;

import com.acme.jobconnect.entity.Application;
import com.acme.jobconnect.entity.JobDescription;
import com.acme.jobconnect.entity.JobOffer;
import com.acme.jobconnect.entity.JobOfferStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class JobOfferBuilder {
    private UUID id;
    private int version;
    private String company;
    private LocalDate publicationdate;
    private LocalDate expirationdate;
    private JobOfferStatus status;
    private List<Application> applications;
    private JobDescription description;

    public static JobOfferBuilder getBuilder() {
        return new JobOfferBuilder();
    }

    public JobOfferBuilder setId(final UUID id) {
        this.id = id;
        return this;
    }

    public JobOfferBuilder setCompany(final String company) {
        this.company = company;
        return this;
    }

    public JobOfferBuilder setPublicationdate(final LocalDate publicationdate) {
        this.publicationdate = publicationdate;
        return this;
    }

    public JobOfferBuilder setExpirationdate(final LocalDate expirationdate) {
        this.expirationdate = expirationdate;
        return this;
    }

    public JobOfferBuilder setStatus(final JobOfferStatus status) {
        this.status = status;
        return this;
    }

    public JobOfferBuilder setApplications(final List<Application> applications) {
        this.applications = applications;
        return this;
    }

    public JobOfferBuilder setDescription(final JobDescription description) {
        this.description = description;
        return this;
    }

    public JobOfferBuilder setVersion(final int version) {
        this.version = version;
        return this;
    }

    public JobOffer build() {
        return new JobOffer(id, version, company, publicationdate, expirationdate, status, applications, description);
    }
}
