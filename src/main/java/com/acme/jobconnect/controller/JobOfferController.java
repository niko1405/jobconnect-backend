package com.acme.jobconnect.controller;

import com.acme.jobconnect.security.RolleAdminOrUser;
import com.acme.jobconnect.service.JobOfferService;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Nullable;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.acme.jobconnect.controller.Constants.API_PATH;
import static com.acme.jobconnect.controller.Constants.COMPANY_PATH;
import static com.acme.jobconnect.controller.Constants.ID_PATTERN;
import static com.acme.jobconnect.controller.Constants.SEARCH_TAG;
import static com.acme.jobconnect.controller.Constants.VERSION_1;
import static com.acme.jobconnect.controller.Constants.VERSION_2;
import static com.acme.jobconnect.controller.Constants.VERSION_2_EXAMPLE;
import static com.acme.jobconnect.controller.Constants.X_VERSION;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

@RestController
@RequestMapping(API_PATH)
@OpenAPIDefinition(info = @Info(title = "JobOffer API", version = VERSION_1))
class JobOfferController {
    private static final String DEFAULT_APPLICATIONS = "false";
    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_PAGE_SIZE = "5";

    private final JobOfferService service;
    private final StableValue<Logger> logger = StableValue.of();

    JobOfferController(final JobOfferService service) {
        this.service = service;
    }

    @GetMapping(path = "{id:" + ID_PATTERN + "}", produces = APPLICATION_JSON_VALUE, version = VERSION_2)
    @RolleAdminOrUser
    @Operation(summary = "Suche mit JobOffer Id", tags = SEARCH_TAG)
    @Parameter(name = X_VERSION, in = ParameterIn.HEADER, example = VERSION_2_EXAMPLE)
    @ApiResponse(responseCode = "200", description = "JobOffer gefunden")
    @ApiResponse(responseCode = "404", description = "JobOffer nicht gefunden")
    @SuppressWarnings("ReturnCount")
    ResponseEntity<Object> getById(
        @PathVariable final UUID id,
        @RequestParam(defaultValue = DEFAULT_APPLICATIONS) final boolean applications,
        @RequestHeader("If-None-Match") @Nullable final String ifNoneMatch
    ) {
        getLogger().debug("getById: id={}, applications={}, ifNonMatch={}", id, applications, ifNoneMatch);

        if (applications) {
            return getByIdWithDescriptionAndApplications(id, ifNoneMatch);
        }
        return getByIdWithDescription(id, ifNoneMatch);
    }

    private ResponseEntity<Object> getByIdWithDescription(
        final UUID id,
        @Nullable final String ifNoneMatch
    ) {
        getLogger().trace("getByIdWithDescription: id={}, ifNonMatch={}", id, ifNoneMatch);

        final var jobOffer = service.findByIdWithDescription(id);
        final var versionStr = "\"" + jobOffer.getVersion() + '"';
        if (versionStr.equals(ifNoneMatch)) {
            getLogger().trace("getByIdWithDescription: version={}", ifNoneMatch);
            return status(NOT_MODIFIED).build();
        }

        getLogger().trace("getByIdWithDescription: jobOffer={}, description={}", jobOffer, jobOffer.getDescription());

        return ok().eTag(versionStr).body(JobOfferWithoutApplications.of(jobOffer));
    }

    private ResponseEntity<Object> getByIdWithDescriptionAndApplications(
        final UUID id,
        @Nullable final String ifNoneMatch
    ) {
        getLogger().trace("getByIdWithDescriptionAndApplications: id={}, ifNonMatch={}", id, ifNoneMatch);

        final var jobOffer = service.findByIdWithDescriptionAndApplications(id);
        final var versionStr = "\"" + jobOffer.getVersion() + '"';
        if (versionStr.equals(ifNoneMatch)) {
            getLogger().trace("getByIdWithDescriptionAndApplications: version={}", ifNoneMatch);
            return status(NOT_MODIFIED).build();
        }

        getLogger().trace(
            "getByIdWithDescriptionAndApplications: jobOffer={}, description={}, applications={}",
            jobOffer, jobOffer.getDescription(), jobOffer.getApplications()
        );
        return ok().eTag(versionStr).body(jobOffer);
    }

    @GetMapping(path = COMPANY_PATH + "/{company}", produces = APPLICATION_JSON_VALUE, version = VERSION_2)
    @Operation(summary = "Search JobOffers by company", tags = SEARCH_TAG)
    @Parameter(name = X_VERSION, in = ParameterIn.HEADER, example = VERSION_2_EXAMPLE)
    PagedModel<JobOfferWithoutApplications> getByCompany(
        @PathVariable final String company,
        @RequestParam(defaultValue = DEFAULT_PAGE) final int page,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int size
    ) {
        getLogger().debug("getByCompany: {}", company);
        final var pageable = PageRequest.of(page, size);
        final var jobOfferPage = service.findByCompany(company, pageable).map(JobOfferWithoutApplications::of);
        getLogger().debug("getByCompany: {}", jobOfferPage);
        return new PagedModel<>(jobOfferPage);
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE, version = VERSION_2)
    @Operation(summary = "Suche mit Query-Parameter", tags = SEARCH_TAG)
    @Parameter(name = X_VERSION, in = ParameterIn.HEADER, example = VERSION_2_EXAMPLE)
    @ApiResponse(responseCode = "200", description = "JobOffers found")
    @ApiResponse(responseCode = "404", description = "JobOffers not found")
    PagedModel<JobOfferWithoutApplications> get(
        @RequestParam final MultiValueMap<String, String> queryparam,
        @RequestParam(defaultValue = DEFAULT_PAGE) final int page,
        @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) final int size
    ) {
        getLogger().debug("get: queryparam={}, page={}, size={}", queryparam, page, size);
        queryparam.remove("page");
        queryparam.remove("size");
        getLogger().trace("get: queryparam={}", queryparam);
        final var pageRequest = PageRequest.of(page, size);
        final var jobOfferPage = service.find(queryparam, pageRequest).map(JobOfferWithoutApplications::of);
        getLogger().debug("get: {}, {}", jobOfferPage, jobOfferPage.getContent());
        return new PagedModel<>(jobOfferPage);
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(JobOfferController.class));
    }
}
