package com.acme.jobconnect.service;

import com.acme.jobconnect.entity.ApplicationStatus;
import com.acme.jobconnect.entity.EmploymentType;
import com.acme.jobconnect.entity.JobOffer;
import com.acme.jobconnect.entity.JobOfferStatus;
import com.acme.jobconnect.mail.MailConfig;
import com.acme.jobconnect.mail.MailService;
import com.acme.jobconnect.repository.ApplicationBuilder;
import com.acme.jobconnect.repository.JobDescriptionBuilder;
import com.acme.jobconnect.repository.JobOfferBuilder;
import com.acme.jobconnect.repository.JobOfferRepository;
import jakarta.mail.internet.MimeMessage;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_25;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Tag("unit")
@Tag("service-write")
@DisplayName("Geschaeftslogik fuer Schreiben (JobOffer)")
@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
@EnabledForJreRange(min = JAVA_25, max = JAVA_25)
class JobOfferWriteServiceTest {
    private static final String COMPANY = "Mercedes-Benz AG";
    private static final String TITLE = "Junior Developer";
    private static final String LOCATION = "Karlsruhe";

    @Mock
    private JobOfferRepository repo;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MailConfig mailConfig;

    @Mock
    private MimeMessage mimeMessage;

    private JobOfferWriteService service;

    @InjectSoftAssertions
    private SoftAssertions softly;

    @BeforeEach
    void beforeEach() throws InvocationTargetException, InstantiationException, IllegalAccessException {
        final var mailServiceConstrs = MailService.class.getDeclaredConstructors();
        final var mailServiceConstr = mailServiceConstrs[0];
        mailServiceConstr.setAccessible(true);
        final var mailService = (MailService) mailServiceConstr.newInstance(mailSender, mailConfig);

        service = new JobOfferWriteService(repo, mailService);
    }

    @Nested
    @DisplayName("Geschaeftslogik fuer Erzeugen")
    class Create {
        @ParameterizedTest(name = "[{index}] Neuanlegen eines JobOffers: company={0}, title={1}")
        @CsvSource(COMPANY + ',' + TITLE)
        @DisplayName("Neuanlegen eines JobOffers")
        void create(final ArgumentsAccessor args) {
            // given
            final var company = args.getString(0);
            final var title = args.getString(1);

            if (company == null || title == null) {
                throw new IllegalStateException("Testdaten sind null");
            }

            final var jobOfferSaved = createJobOfferMock(randomUUID(), company, title);

            // Mock Repository: save liefert das Objekt mit ID zurück
            when(repo.save(org.mockito.ArgumentMatchers.any(JobOffer.class))).thenReturn(jobOfferSaved);

            // Mock Mail: HIER WAR DER FEHLER - sales() (oder to()) hat gefehlt
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(mailConfig.from()).thenReturn("noreply@jobconnect.acme.com");
            // !!! WICHTIG: Empfänger-Adresse mocken !!!
            when(mailConfig.sales()).thenReturn("admin@jobconnect.acme.com");

            doNothing().when(mailSender).send(mimeMessage);

            // when
            final var result = service.create(jobOfferSaved);

            // then
            assertThat(result).isNotNull();
            softly.assertThat(result.getId()).isNotNull();
            softly.assertThat(result.getCompany()).isEqualTo(company);
        }
    }

    private JobOffer createJobOfferMock(@Nullable final UUID id, final String company, final String title) {
        final var description = JobDescriptionBuilder.getBuilder()
            .setId(randomUUID())
            .setTite(title)
            .setLocation(LOCATION)
            .setSalary(BigDecimal.valueOf(50000))
            .setSummary("Summary")
            .setResponsibilities("Resp")
            .setRequirements("Req")
            .setEmployment(EmploymentType.FULLTIME)
            .build();

        final var applications = List.of(
            ApplicationBuilder.getBuilder()
                .setId(randomUUID())
                .setApplicant("Nikolas")
                .setResume(URI.create("https://example.com/resume"))
                .setDate(LocalDate.now())
                .setStatus(ApplicationStatus.APPLIED)
                .build()
        );

        return JobOfferBuilder.getBuilder()
            .setId(id)
            .setVersion(0)
            .setCompany(company)
            .setPublicationdate(LocalDate.now())
            .setExpirationdate(LocalDate.now().plusMonths(3))
            .setStatus(JobOfferStatus.ACTIVE)
            .setDescription(description)
            .setApplications(applications)
            .build();
    }
}
