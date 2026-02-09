package com.acme.jobconnect.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import static jakarta.persistence.EnumType.STRING;

@Entity
public class Application {
    @Id
    @GeneratedValue
    private UUID id;
    private String applicant;

    private URI resume;

    private URI coverletter;

    @CreationTimestamp
    @Column(name = "application_date")
    private LocalDate date;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<URI> documents;

    @Enumerated(STRING)
    private ApplicationStatus status;

    public Application(final UUID id, final String applicant, final URI resume, final URI coverletter, final LocalDate date, final List<URI> documents, final ApplicationStatus status) {
        this.id = id;
        this.applicant = applicant;
        this.resume = resume;
        this.coverletter = coverletter;
        this.date = date;
        this.documents = documents;
        this.status = status;
    }

    public Application() {

    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Application that)) return false;
        return Objects.equals(id, that.id);
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

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(final String applicant) {
        this.applicant = applicant;
    }

    public URI getResume() {
        return resume;
    }

    public void setResume(final URI resume) {
        this.resume = resume;
    }

    public URI getCoverletter() {
        return coverletter;
    }

    public void setCoverletter(final URI coverletter) {
        this.coverletter = coverletter;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(final LocalDate date) {
        this.date = date;
    }

    public List<URI> getDocuments() {
        return documents;
    }

    public void setDocuments(final List<URI> documents) {
        this.documents = documents;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(final ApplicationStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Application{" +
                "id=" + id +
                ", applicant='" + applicant + '\'' +
                ", resume=" + resume +
                ", coverletter=" + coverletter +
                ", date=" + date +
                ", documents=" + documents +
                ", status=" + status +
                '}';
    }
}
