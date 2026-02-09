package com.acme.jobconnect.controller;

import com.acme.jobconnect.entity.JobDescription;
import com.acme.jobconnect.entity.JobOffer;
import com.acme.jobconnect.entity.JobOfferStatus;
import java.time.LocalDate;
import java.util.UUID;

/// ValueObject für eine gefundenen JobOffer ohne nicht-mitgeladene Applications, d.h. nicht-serialisierbares Proxy-Objekt.
@SuppressWarnings("RecordComponentNumber")
public record JobOfferWithoutApplications(
    UUID id,
    String company,
    LocalDate publicationdate,
    LocalDate expirationdate,
    int viewscount,
    JobOfferStatus status,
    JobDescription description
) {
    static JobOfferWithoutApplications of(final JobOffer jobOffer) {
        return new JobOfferWithoutApplications(jobOffer.getId(), jobOffer.getCompany(), jobOffer.getPublicationdate(), jobOffer.getExpirationdate(),
            jobOffer.getViewscount(), jobOffer.getStatus(), jobOffer.getDescription());
    }
}
