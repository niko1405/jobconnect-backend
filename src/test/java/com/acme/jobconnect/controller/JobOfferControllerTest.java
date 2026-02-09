package com.acme.jobconnect.controller;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import static com.acme.jobconnect.config.DevConfig.DEV;
import static com.acme.jobconnect.controller.Constants.API_PATH;
import static com.acme.jobconnect.controller.TestConstants.ADMIN_AUTH;
import static com.acme.jobconnect.controller.TestConstants.API_VERSION_INSERTER;
import static com.acme.jobconnect.controller.TestConstants.HOST;
import static com.acme.jobconnect.controller.TestConstants.REQUEST_FACTORY;
import static com.acme.jobconnect.controller.TestConstants.SCHEMA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowableOfType;
import static org.junit.jupiter.api.condition.JRE.JAVA_25;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;

@Tag("integration")
@Tag("rest")
@Tag("rest-get")
@DisplayName("REST-Schnittstelle fuer GET-Requests")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_25, max = JAVA_25)
@SuppressWarnings({
    "WriteTag",
    "ClassFanOutComplexity",
    "MissingJavadoc",
    "MissingJavadocType",
    "JavadocVariable",
    "PMD.AtLeastOneConstructor",
    "PMD.LinguisticNaming"
})
class JobOfferControllerTest {
        private static final String EXISTING_ID = "00000000-0000-0000-0000-000000000001";
    private static final String EXISTING_COMPANY = "StartUp Rocket";

    private static final String ID_VORHANDEN = "00000000-0000-0000-0000-000000000001";
    private static final String ID_VORHANDEN_20 = "00000000-0000-0000-0000-000000000020";
    private static final String ID_NICHT_VORHANDEN = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    private static final String COMPANY_1 = "MediaGroup";
    private static final String COMPANY_2 = "TechCorp GmbH";

    private final JobOfferRepository jobOfferRepo;

    @InjectSoftAssertions
    @SuppressWarnings("NullAway.Init")
    private SoftAssertions softly;

    @SuppressFBWarnings("CT")
    JobOfferControllerTest(@LocalServerPort final int port, final ApplicationContext ctx) {
        final var controller = ctx.getBean(JobOfferController.class);
        assertThat(controller).isNotNull();

        final var uriComponents = UriComponentsBuilder.newInstance()
            .scheme(SCHEMA)
            .host(HOST)
            .port(port)
            .path(API_PATH)
            .build();
        final var baseUrl = uriComponents.toUriString();

        // https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#io.rest-client.webclient.ssl
        // siehe org.springframework.web.client.DefaultRestClient
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

    @Test
    @DisplayName("Immer erfolgreich")
    void immerErfolgreich() {
        assertThat(true).isTrue(); // NOSONAR
    }

    @Test
    @DisplayName("Suche nach allen JobOffers")
    void getAll() {
        // given
        final MultiValueMap<@NonNull String, String> suchparameter = MultiValueMap.fromSingleValue(Map.of());

        // when
        final var jobOffers = jobOfferRepo.get(suchparameter);

        // then
        softly.assertThat(jobOffers.content())
            .isNotNull()
            .isNotEmpty();
    }

    @ParameterizedTest(name = "[{index}] Search by query params: id={0}")
    @MethodSource("queryParamsProvider")
    @DisplayName("Search by query params")
    void find(MultiValueMap<@NonNull String, String> queryParams) {
        // given

        // when
        final var jobOffers = jobOfferRepo.get(queryParams);

        // then
        softly.assertThat(jobOffers.content())
            .isNotNull()
            .isNotEmpty();
    }

    @Nested
    @DisplayName("REST-Schnittstelle fuer die Suche anhand der ID")
    class GetById {
        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID: id={0}")
        @ValueSource(strings = {ID_VORHANDEN, ID_VORHANDEN_20})
        @DisplayName("Suche mit vorhandener ID")
        void getById(final String id) {
            // when
            final var response = jobOfferRepo.getByIdOhneVersion(id, ADMIN_AUTH);

            // then
            final var jobOffer = response.getBody();
            assertThat(jobOffer).isNotNull();
            softly.assertThat(jobOffer.id()).isEqualTo(UUID.fromString(id));
            softly.assertThat(jobOffer.company()).isNotNull();
            softly.assertThat(jobOffer.description().getTitle()).isNotNull();
        }

        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID und vorhandener Version: id={0}, version={1}")
        @CsvSource(ID_VORHANDEN + ", 0")
        @DisplayName("Suche mit vorhandener ID und vorhandener Version")
        void getByIdVersionVorhanden(final String id, final String version) {
            // when
            final var response = jobOfferRepo.getById(id, "\"" + version + '"', ADMIN_AUTH);

            // then
            assertThat(response.getStatusCode()).isEqualTo(NOT_MODIFIED);
        }

        @ParameterizedTest(name = "[{index}] Suche mit nicht-vorhandener ID: {0}")
        @ValueSource(strings = ID_NICHT_VORHANDEN)
        @DisplayName("Suche mit nicht-vorhandener ID")
        void getByIdNichtVorhanden(final String id) {
            // when
            final var exc = catchThrowableOfType(
                HttpClientErrorException.NotFound.class,
                () -> jobOfferRepo.getByIdOhneVersion(id, ADMIN_AUTH)
            );

            // then
            assertThat(exc.getStatusCode()).isEqualTo(NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("REST-Schnittstelle fuer die Suche nach Strings")
    class SucheNachStrings {
        @ParameterizedTest(name = "[{index}] Suche JobOffers mit Company prefix={0}")
        @ValueSource(strings = {COMPANY_1, COMPANY_2})
        @DisplayName("Suche JobOffer mit company")
        void getByCompany(final String company) {
            final var jobOffers = jobOfferRepo.getByCompany(company);

            // then
            softly.assertThat(jobOffers.content())
                .isNotNull()
                .isNotEmpty();
        }
    }

    static Stream<MultiValueMap<@NonNull String, String>> queryParamsProvider() {
        return Stream.of(
                // 1. Keine Parameter (leere Map)
                Map.<String, String>of(),

                // 2. Nur ID
                Map.of("id", EXISTING_ID),

                // 3. ID + Company
                Map.of(
                    "id", EXISTING_ID,
                    "company", EXISTING_COMPANY
                ),

                // 4. Nur Company
                Map.of("company", EXISTING_COMPANY)
            )
            // Hier wandeln wir die einfache Map in die Spring MultiValueMap um:
            .map(params -> {
                var multiMap = new LinkedMultiValueMap<@NonNull String, String>();
                params.forEach(multiMap::add);
                return multiMap;
            });
    }
}
