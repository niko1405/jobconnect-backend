package com.acme.jobconnect.service;

import com.acme.jobconnect.entity.JobOffer;
import com.acme.jobconnect.mail.MailService;
import com.acme.jobconnect.repository.JobOfferRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class JobOfferWriteService {
    private final JobOfferRepository repo;
    private final MailService mailService;
    private final StableValue<Logger> logger = StableValue.of();

    JobOfferWriteService(final JobOfferRepository repo, final MailService mailService) {
        this.repo = repo;
        this.mailService = mailService;
    }

    @Transactional
    @SuppressWarnings("TrailingComment")
    public JobOffer create(final JobOffer jobOffer) {
        getLogger().debug("create: jobOffer={}, description={}, applications={}",
            jobOffer, jobOffer.getDescription(), jobOffer.getApplications());

        final var company = jobOffer.getCompany();
        final var title = jobOffer.getDescription().getTitle();

        if (repo.existsByCompanyAndTitle(company, title)) {
            throw new JobOfferExistsException(company, title);
        }

        // TODO Neuen Benutzer beim Authorization-Server anlegen
        // final var login = userService.save(user); // NOSONAR
        // jobOffer.setUsername("user");

        final var jobOfferDb = repo.save(jobOffer);

        getLogger().trace("create: Thread-ID={}", Thread.currentThread().threadId());
        mailService.send(jobOfferDb);

        getLogger().debug("create: jobOfferDB={}", jobOfferDb);
        return jobOfferDb;
    }

    @Transactional
    public JobOffer update(final JobOffer jobOffer, final UUID id, final int version) {
        getLogger().debug("update: jobOffer={}, id={}, version={}", jobOffer, id, version);

        var jobOfferDb = repo
            .findById(id)
            .orElseThrow(() -> new NotFoundException(id));

        getLogger().trace("update: version={}, jobOfferDb={}", version, jobOfferDb);

        if (version != jobOfferDb.getVersion()) {
            throw new VersionOutdatedException(version);
        }

        // Zu ueberschreibende Werte uebernehmen
        jobOfferDb.set(jobOffer);
        jobOfferDb = repo.save(jobOfferDb);

        getLogger().debug("update: {}", jobOfferDb);
        return jobOfferDb;
    }

    /// Eine JobOffer löschen.
    ///
    /// @param id Die ID der zu löschenden JobOffer.
    @Transactional
    public void deleteById(final UUID id) {
        getLogger().debug("deleteById: id={}", id);
        repo.findById(id).ifPresent(repo::delete);
    }



    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(JobOfferWriteService.class));
    }
}
