package com.acme.jobconnect.service;

import com.acme.jobconnect.entity.JobOffer;
import com.acme.jobconnect.repository.JobOfferRepository;
import com.acme.jobconnect.repository.SpecificationBuilder;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JobOfferService {
    private final JobOfferRepository repo;
    private final SpecificationBuilder specificationBuilder;
    private final StableValue<Logger> logger = StableValue.of();

    JobOfferService(final JobOfferRepository repo, final SpecificationBuilder specificationBuilder) {
        this.repo = repo;
        this.specificationBuilder = specificationBuilder;
    }

    public Page<JobOffer> findByCompany(
        final String company,
        final Pageable pageable
    ) {
        getLogger().trace("findByCompany: {}", company);
        final var jobOfferPage = repo.findByCompay(company, pageable);
        if (jobOfferPage.isEmpty()) {
            throw new NotFoundException();
        }
        getLogger().trace("findByCompany: {}, {}", jobOfferPage, jobOfferPage.getContent());
        return jobOfferPage;
    }

    public JobOffer findByIdWithDescription(final UUID id) {
        getLogger().debug("findByIdWithDescription: id={}", id);

        final var jobOffer = repo.findByIdFetchJobDescription(id);
        getLogger().trace("findByIdWithDescription: jobOffer={}", jobOffer);

        if (jobOffer == null) {
            throw new NotFoundException(id);
        }
        getLogger().debug("findByIdWithDescription: jobOffer={}", jobOffer);
        return jobOffer;
    }

    public JobOffer findByIdWithDescriptionAndApplications(final UUID id) {
        getLogger().debug("findByIdWithDescriptionAndApplications: id={}", id);

        final var jobOffer = repo.findByIdFetchJobDescriptionAndApplications(id);
        getLogger().trace("findByIdWithDescriptionAndApplications: jobOffer={}", jobOffer);

        if (jobOffer == null) {
            throw new NotFoundException(id);
        }
        getLogger().debug("findByIdWithDescriptionAndApplications: jobOffer={}, applications={}", jobOffer, jobOffer.getApplications());
        return jobOffer;
    }

    @SuppressWarnings({"ReturnCount", "PMD.AvoidLiteralsInIfCondition", "PMD.CyclomaticComplexity"})
    public Page<JobOffer> find(final Map<String, List<String>> suchparameter, final Pageable pageable) {
        getLogger().debug("find: suchparameter={}, pageable={}", suchparameter, pageable);

        if (suchparameter.isEmpty()) {
            return repo.findAll(pageable);
        }


        final var specification = specificationBuilder.build(suchparameter);
        if (specification == null) {
            throw new NotFoundException(suchparameter);
        }
        final var jobOfferPage = repo.findAll(specification, pageable);
        if (jobOfferPage.isEmpty()) {
            throw new NotFoundException(suchparameter);
        }
        getLogger().debug("find: {}, {}", jobOfferPage, jobOfferPage.getContent());
        return jobOfferPage;
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(JobOfferService.class));
    }
}
