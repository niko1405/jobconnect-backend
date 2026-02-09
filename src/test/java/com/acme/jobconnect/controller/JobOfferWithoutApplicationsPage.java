package com.acme.jobconnect.controller;

import java.util.Collections;
import java.util.List;
import org.springframework.data.web.PagedModel.PageMetadata;

public record JobOfferWithoutApplicationsPage(List<JobOfferWithoutApplications> content, PageMetadata page) {
    // https://stackoverflow.com/questions/77359073/how-to-solve-ei-expose-rep-in-records-for-lists
    public JobOfferWithoutApplicationsPage(final List<JobOfferWithoutApplications> content, final PageMetadata page) {
        this.content = Collections.unmodifiableList(content);
        this.page = page;
    }

    @Override
    public List<JobOfferWithoutApplications> content() {
        return Collections.unmodifiableList(content);
    }
}
