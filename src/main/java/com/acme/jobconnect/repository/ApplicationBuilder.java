package com.acme.jobconnect.repository;

import com.acme.jobconnect.entity.Application;
import com.acme.jobconnect.entity.ApplicationStatus;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ApplicationBuilder {
    private UUID id;
    private String applicant;
    private URI resume;
    private URI coverletter;
    private LocalDate date;
    private List<URI> documents;
    private ApplicationStatus status;

    public static ApplicationBuilder getBuilder() {
        return new  ApplicationBuilder();
    }

    public ApplicationBuilder setId(final UUID id) {
        this.id = id;
        return this;
    }

    public ApplicationBuilder setApplicant(final String applicant) {
        this.applicant = applicant;
        return this;
    }

    public ApplicationBuilder setResume(final URI resume) {
        this.resume = resume;
        return this;
    }

    public ApplicationBuilder setCoverletter(final URI coverletter) {
        this.coverletter = coverletter;
        return this;
    }

    public ApplicationBuilder setDate(final LocalDate date) {
        this.date = date;
        return this;
    }

    public ApplicationBuilder setDocuments(final List<URI> documents) {
        this.documents = documents;
        return this;
    }

    public ApplicationBuilder setStatus(final ApplicationStatus status) {
        this.status = status;
        return this;
    }

    public Application build() {
        return new Application(id, applicant, resume, coverletter, date, documents, status);
    }
}
