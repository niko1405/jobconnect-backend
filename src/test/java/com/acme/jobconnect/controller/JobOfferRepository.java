package com.acme.jobconnect.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import static com.acme.jobconnect.controller.TestConstants.VERSION_2;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.IF_MATCH;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;

@HttpExchange
@SuppressWarnings({"WriteTag", "PMD.AvoidDuplicateLiterals"})
interface JobOfferRepository {
    @GetExchange(url = "/{id}", version = VERSION_2)
    ResponseEntity<Void> getById(
        @PathVariable String id,
        @RequestHeader(IF_NONE_MATCH) String version,
        @RequestHeader(AUTHORIZATION) String authorization
    );

    @GetExchange(url = "/{id}", version = VERSION_2)
    ResponseEntity<JobOfferWithoutApplications> getByIdOhneVersion(@PathVariable String id, @RequestHeader(AUTHORIZATION) String authorization);

    @GetExchange(version = VERSION_2)
    JobOfferWithoutApplicationsPage get(@RequestParam MultiValueMap<String, String> suchparameter);

    @PostExchange(version = VERSION_2)
    ResponseEntity<Void> post(@RequestBody JobOfferDTO jobOffer);

    @PutExchange(url = "/{id}", version = VERSION_2)
    ResponseEntity<Void> put(
        @PathVariable String id,
        @RequestBody JobOfferDTO jobOffer,
        @RequestHeader(IF_MATCH) String version
    );

    @PutExchange(url = "/{id}", version = VERSION_2)
    void putOhneVersion(
        @PathVariable String id,
        @RequestBody JobOfferDTO jobOffer
    );

    @DeleteExchange(url = "/{id}", version = VERSION_2)
    ResponseEntity<Void> deleteById(@PathVariable String id, @RequestHeader(AUTHORIZATION) String authorization);

    @GetExchange(url = "/company/{company}", version = VERSION_2)
    JobOfferWithoutApplicationsPage getByCompany(@PathVariable String company);
}
