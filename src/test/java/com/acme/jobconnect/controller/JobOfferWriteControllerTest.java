package com.acme.jobconnect.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;

import static com.acme.jobconnect.config.DevConfig.DEV;
import static com.acme.jobconnect.controller.Constants.API_PATH;
import static com.acme.jobconnect.controller.Constants.ID_PATTERN;
import static com.acme.jobconnect.controller.TestConstants.ADMIN_AUTH;
import static com.acme.jobconnect.controller.TestConstants.API_VERSION_INSERTER;
import static com.acme.jobconnect.controller.TestConstants.HOST;
import static com.acme.jobconnect.controller.TestConstants.REQUEST_FACTORY;
import static com.acme.jobconnect.controller.TestConstants.SCHEMA;
import static com.acme.jobconnect.entity.ApplicationStatus.APPLIED;
import static com.acme.jobconnect.entity.EmploymentType.PARTTIME;
import static com.acme.jobconnect.entity.JobOfferStatus.ACTIVE;
import static java.math.BigDecimal.ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_25;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Tag("integration")
@Tag("rest")
@Tag("rest-write")
@DisplayName("REST-Schnittstelle fuer Schreiben")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_25, max = JAVA_25)
@SuppressWarnings({"WriteTag", "PMD.AtLeastOneConstructor", "PMD.AvoidDuplicateLiterals"})
class JobOfferWriteControllerTest {
    private static final String NEW_COMPANY = "Mercedes-Benz AG";
    private static final String NEW_PUBLICATION_DATE = "2026-05-01";
    private static final String NEW_EXPIRATION_DATE = "2026-09-01";

    private static final String APPLICANT = "Nikolas";
    private static final String RESUME_PATH = "https://www.google.com";
    private static final String APPLICATION_DATE = "2025-10-20";

    private static final String TITLE = "Junior Developer";
    private static final String LOCATION = "Karlsruhe";
    private static final String SUMMARY = "Support our developer team";
    private static final String RESPONSIBILITIES = "Key Responsibilities include assisting senior staff with code development in Java/Spring Boot, participating in code reviews, writing unit and integration tests, debugging applications, and maintaining technical documentation. You will also contribute to deployment automation tasks. We expect proactive communication and problem-solving skills.";
    private static final String REQUIREMENTS = "Minimum requirements include a Bachelor's degree in Computer Science or a related field, fundamental knowledge of object-oriented programming (OOP), familiarity with at least one major programming language (e.g., Java, Python, C#), understanding of relational databases (SQL), and basic proficiency with Git version control. Previous internship experience is a plus.";

    private static final String ID_VORHANDEN = "00000000-0000-0000-0000-000000000001";
    private static final String ID_NICHT_VORHANDEN = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    private final JobOfferRepository jobOfferRepo;

    @InjectSoftAssertions
    @SuppressWarnings("NullAway.Init")
    private SoftAssertions softly;

    @SuppressFBWarnings("CT")
    JobOfferWriteControllerTest(@LocalServerPort final int port, final ApplicationContext ctx) {
        final var writeController = ctx.getBean(JobOfferWriteController.class);
        assertThat(writeController).isNotNull();

        final var uriComponents = UriComponentsBuilder.newInstance()
            .scheme(SCHEMA)
            .host(HOST)
            .port(port)
            .path(API_PATH)
            .build();
        final var baseUrl = uriComponents.toUriString();
        // RestClient mit REQUEST_FACTORY aus KundeControllerTest.java einschl. TLS bzw. SSLContext
        final var restClient = RestClient
            .builder()
            .requestFactory(REQUEST_FACTORY)
            .apiVersionInserter(API_VERSION_INSERTER)
            .baseUrl(baseUrl)
            .build();
        final var clientAdapter = RestClientAdapter.create(restClient);
        final var proxyFactory = HttpServiceProxyFactory.builderFor(clientAdapter).build();
        jobOfferRepo = proxyFactory.createClient(JobOfferRepository.class);
    }

    @SuppressWarnings("DataFlowIssue")
    @Nested
    @DisplayName("REST-Schnittstelle fuer POST")
    class Create {
        @ParameterizedTest(name = "[{index}] Create new job offer")
        @CsvSource(
            NEW_COMPANY + "," + NEW_PUBLICATION_DATE + "," + NEW_EXPIRATION_DATE + "," + APPLICATION_DATE + "," +
                RESUME_PATH + "," + TITLE + "," + LOCATION + "," + SUMMARY
        )
        @DisplayName("Create new job offer")
        @SuppressWarnings("BooleanExpressionComplexity")
        void post(final ArgumentsAccessor args) {
            // given
            final var company = args.getString(0);
            final var publicationDate = args.get(1, LocalDate.class);
            final var expirationDate = args.get(2, LocalDate.class);
            final var applicationDate = args.get(3, LocalDate.class);
            final var resumePath = args.get(4, URI.class);
            final var title = args.getString(5);
            final var loc = args.getString(6);
            final var summary = args.getString(7);

            final var jobOfferDTO = new JobOfferDTO(
                company,
                publicationDate,
                expirationDate,
                380,
                ACTIVE,
                List.of(new ApplicationDTO(APPLICANT, resumePath, null, applicationDate, List.of(), APPLIED)),
                new JobDescriptionDTO(title, loc, ONE, summary, RESPONSIBILITIES, REQUIREMENTS, PARTTIME)
            );

            // when
            final var response = jobOfferRepo.post(jobOfferDTO);

            // then
            assertThat(response).isNotNull();
            softly.assertThat(response.getStatusCode()).isEqualTo(CREATED);
            final var location = response.getHeaders().getLocation();
            assertThat(location)
                .isNotNull()
                .isInstanceOf(URI.class);
            assertThat(location.toString()).matches(".*/" + ID_PATTERN + '$');
        }
    }

    @Nested
    @DisplayName("REST-Schnittstelle fuer DELETE")
    class Loeschen {
        @ParameterizedTest(name = "[{index}] Loeschen einer vorhandenen JobOffer: id={0}")
        @ValueSource(strings = ID_VORHANDEN)
        @DisplayName("Loeschen eines vorhandenen JobOffer")
        void deleteById(final String id) {
            // when
            final var response = jobOfferRepo.deleteById(id, ADMIN_AUTH);

            // then
            assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        }

        @ParameterizedTest(name = "[{index}] Loeschen eines nicht-vorhandenen JobOffer: id={0}")
        @ValueSource(strings = ID_NICHT_VORHANDEN)
        @DisplayName("Loeschen eines nicht-vorhandenen JobOffer")
        void deleteByIdNichtVorhanden(final String id) {
            // when
            final var response = jobOfferRepo.deleteById(id, ADMIN_AUTH);

            // then
            assertThat(response.getStatusCode()).isEqualTo(NO_CONTENT);
        }
    }
}
