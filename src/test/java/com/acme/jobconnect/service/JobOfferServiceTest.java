/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 */
package com.acme.jobconnect.service;

import com.acme.jobconnect.entity.ApplicationStatus;
import com.acme.jobconnect.entity.EmploymentType;
import com.acme.jobconnect.entity.JobOffer;
import com.acme.jobconnect.entity.JobOfferStatus;
import com.acme.jobconnect.repository.ApplicationBuilder;
import com.acme.jobconnect.repository.JobDescriptionBuilder;
import com.acme.jobconnect.repository.JobOfferBuilder;
import com.acme.jobconnect.repository.JobOfferRepository;
import com.acme.jobconnect.repository.SpecificationBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.condition.JRE.JAVA_25;
import static org.mockito.Mockito.when;

@Tag("unit")
@Tag("service-read")
@DisplayName("Geschaeftslogik fuer Lesen (JobOffer)")
@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
@EnabledForJreRange(min = JAVA_25, max = JAVA_25)
@SpringBootTest
@Transactional
class JobOfferServiceTest {
    private static final String ID_VORHANDEN = "00000000-0000-0000-0000-000000000001";
    private static final String ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999";
    private static final String COMPANY = "TechCorp GmbH";
    private static final String TITLE = "Backend Java Developer";

    @Mock
    private JobOfferRepository repo;

    private final SpecificationBuilder specificationBuilder;
    private JobOfferService service;

    @InjectSoftAssertions
    private SoftAssertions softly;

    private final PageRequest pageRequest0 = PageRequest.of(0, 5);

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    JobOfferServiceTest() {
        final var constructor = SpecificationBuilder.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        try {
            specificationBuilder = (SpecificationBuilder) constructor.newInstance();
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @BeforeEach
    void beforeEach() {
        service = new JobOfferService(repo, specificationBuilder);
    }

    @Test
    @DisplayName("Suche alle JobOffers (ohne Parameter)")
    void findAll() {
        // given
        final var jobOffer = createJobOfferMock(COMPANY);
        final var jobOffersMock = new PageImpl<>(List.of(jobOffer));
        final Map<String, List<String>> keineSuchparameter = MultiValueMap.fromSingleValue(Map.of());

        // FIX: Mocke die Methode mit EINEM Argument (Pageable), da der Service diese aufruft, wenn keine Specs da sind.
        when(repo.findAll(ArgumentMatchers.any(Pageable.class)))
            .thenReturn(jobOffersMock);

        // when
        final var result = service.find(keineSuchparameter, pageRequest0);

        // then
        assertThat(result).isNotEmpty();
    }

    @Nested
    @DisplayName("Geschaeftslogik fuer Suche mit Parametern")
    class FindByParams {

        @ParameterizedTest(name = "[{index}] Suche mit ID: id={0}")
        @ValueSource(strings = ID_VORHANDEN)
        @DisplayName("Suche mit Query-Parameter ID")
        void findByParamId(final String idStr) {
            // given
            final var id = UUID.fromString(idStr);
            final var jobOffer = createJobOfferMock(id, COMPANY);
            final var jobOffersMock = new PageImpl<>(List.of(jobOffer));
            final var suchparameter = MultiValueMap.fromSingleValue(Map.of("id", idStr));

            // FIX: Mocke die Methode mit ZWEI Argumenten (Specification, Pageable)
            when(repo.findAll(ArgumentMatchers.<Specification<JobOffer>>any(), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(jobOffersMock);

            // when
            final var result = service.find(suchparameter, pageRequest0);

            // then
            assertThat(result).hasSize(1);
            softly.assertThat(result.getContent().get(0).getId()).isEqualTo(id);
        }

        @ParameterizedTest(name = "[{index}] Suche mit Company: company={0}")
        @ValueSource(strings = COMPANY)
        @DisplayName("Suche mit Query-Parameter Company")
        void findByParamCompany(final String company) {
            // given
            final var jobOffer = createJobOfferMock(company);
            final var jobOffersMock = new PageImpl<>(List.of(jobOffer));
            final var suchparameter = MultiValueMap.fromSingleValue(Map.of("company", company));

            when(repo.findAll(ArgumentMatchers.<Specification<JobOffer>>any(), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(jobOffersMock);

            // when
            final var result = service.find(suchparameter, pageRequest0);

            // then
            assertThat(result).isNotEmpty();
            softly.assertThat(result.getContent().get(0).getCompany()).isEqualTo(company);
        }

        @ParameterizedTest(name = "[{index}] Suche mit ID und Company: id={0}, company={1}")
        @CsvSource(ID_VORHANDEN + ',' + COMPANY)
        @DisplayName("Suche mit Query-Parameter ID und Company")
        void findByParamIdAndCompany(final String idStr, final String company) {
            // given
            final var id = UUID.fromString(idStr);
            final var jobOffer = createJobOfferMock(id, company);
            final var jobOffersMock = new PageImpl<>(List.of(jobOffer));
            final var suchparameter = MultiValueMap.fromSingleValue(Map.of("id", idStr, "company", company));

            when(repo.findAll(ArgumentMatchers.<Specification<JobOffer>>any(), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(jobOffersMock);

            // when
            final var result = service.find(suchparameter, pageRequest0);

            // then
            assertThat(result).isNotEmpty();
            softly.assertThat(result.getContent().get(0).getId()).isEqualTo(id);
            softly.assertThat(result.getContent().get(0).getCompany()).isEqualTo(company);
        }
    }

    @Nested
    @DisplayName("Geschaeftslogik fuer die Suche anhand der ID (Pfadvariable)")
    class FindById {
        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID: id={0}")
        @CsvSource(ID_VORHANDEN + ',' + COMPANY)
        @DisplayName("Suche mit vorhandener ID")
        void findById(final String idStr, final String company) {
            // given
            final var id = UUID.fromString(idStr);
            final var jobOfferMock = createJobOfferMock(id, company);

            // Annahme: Dein Repo nutzt findByIdFetch... oder ähnliches. Falls nicht, einfach findById nutzen.
            when(repo.findByIdFetchJobDescription(id)).thenReturn(jobOfferMock);

            // when
            final var jobOffer = service.findByIdWithDescription(id);

            // then
            assertThat(jobOffer.getId()).isEqualTo(jobOfferMock.getId());
            softly.assertThat(jobOffer.getCompany()).isEqualTo(company);
        }

        @ParameterizedTest(name = "[{index}] Suche mit nicht vorhandener ID: id={0}")
        @ValueSource(strings = ID_NICHT_VORHANDEN)
        @DisplayName("Suche mit nicht vorhandener ID")
        void findByIdNichtVorhanden(final String idStr) {
            // given
            final var id = UUID.fromString(idStr);

            // when
            final var notFoundException = catchThrowableOfType(
                NotFoundException.class,
                () -> service.findByIdWithDescription(id)
            );

            // then
            assertThat(notFoundException)
                .isNotNull()
                .extracting(NotFoundException::getId)
                .isEqualTo(id);
        }
    }

    private JobOffer createJobOfferMock(final String company) {
        return createJobOfferMock(randomUUID(), company);
    }

    private JobOffer createJobOfferMock(final UUID id, final String company) {
        final var description = JobDescriptionBuilder.getBuilder()
            .setId(randomUUID())
            .setTite(TITLE)
            .setLocation("Karlsruhe")
            .setSalary(BigDecimal.valueOf(60000))
            .setSummary("Summary")
            .setResponsibilities("Resp")
            .setRequirements("Req")
            .setEmployment(EmploymentType.FULLTIME)
            .build();

        final var applications = List.of(
            ApplicationBuilder.getBuilder()
                .setId(randomUUID())
                .setApplicant("Bewerber 1")
                .setResume(URI.create("https://example.com/resume"))
                .setDate(LocalDate.now())
                .setStatus(ApplicationStatus.REVIEWED)
                .build()
        );

        return JobOfferBuilder.getBuilder()
            .setId(id)
            .setVersion(0)
            .setCompany(company)
            .setPublicationdate(LocalDate.now())
            .setExpirationdate(LocalDate.now().plusDays(30))
            .setStatus(JobOfferStatus.ACTIVE)
            .setDescription(description)
            .setApplications(applications)
            .build();
    }
}

///*
// * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
// * ... (Lizenz-Header analog zu Kunde)
// */
//package com.acme.jobconnect.service;
//
//import com.acme.jobconnect.entity.ApplicationStatus;
//import com.acme.jobconnect.entity.EmploymentType;
//import com.acme.jobconnect.entity.JobOffer;
//import com.acme.jobconnect.entity.JobOfferStatus;
//import com.acme.jobconnect.repository.ApplicationBuilder;
//import com.acme.jobconnect.repository.JobDescriptionBuilder;
//import com.acme.jobconnect.repository.JobOfferBuilder;
//import com.acme.jobconnect.repository.JobOfferRepository;
//import com.acme.jobconnect.repository.SpecificationBuilder;
//import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
//import java.lang.reflect.InvocationTargetException;
//import java.math.BigDecimal;
//import java.net.URI;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import org.assertj.core.api.SoftAssertions;
//import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
//import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
//import org.jspecify.annotations.NonNull;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.condition.EnabledForJreRange;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.CsvSource;
//import org.junit.jupiter.params.provider.ValueSource;
//import org.mockito.ArgumentMatchers;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.util.MultiValueMap;
//
//import static java.util.UUID.randomUUID;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
//import static org.junit.jupiter.api.condition.JRE.JAVA_25;
//import static org.mockito.Mockito.when;
//
//@Tag("unit")
//@Tag("service-read")
//@DisplayName("Geschaeftslogik fuer Lesen (JobOffer)")
//@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
//@EnabledForJreRange(min = JAVA_25, max = JAVA_25)
//@SuppressWarnings({
//    "ClassFanOutComplexity",
//    "InnerTypeLast",
//    "WriteTag",
//    "TypeMayBeWeakened",
//    "PMD.AtLeastOneConstructor",
//    "PMD.AvoidAccessibilityAlteration"
//})
//class JobOfferServiceTest {
//    private static final String ID_VORHANDEN = "00000000-0000-0000-0000-000000000001";
//    private static final String ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999";
//    private static final String COMPANY = "Future Solutions";
//    private static final String TITLE = "Senior Java Dev";
//
//    @Mock
//    @SuppressWarnings("NullAway.Init")
//    private JobOfferRepository repo;
//
//    private final SpecificationBuilder specificationBuilder;
//
//    private JobOfferService service;
//
//    @InjectSoftAssertions
//    @SuppressWarnings("NullAway.Init")
//    private SoftAssertions softly;
//
//    private final PageRequest pageRequest0 = PageRequest.of(0, 5);
//
//    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
//    JobOfferServiceTest() {
//        // Reflection Hack analog zu KundeServiceTest für den SpecificationBuilder
//        final var constructor = SpecificationBuilder.class.getDeclaredConstructors()[0];
//        constructor.setAccessible(true);
//        try {
//            specificationBuilder = (SpecificationBuilder) constructor.newInstance();
//        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException ex) {
//            throw new IllegalStateException(ex);
//        }
//    }
//
//    @BeforeEach
//    void beforeEach() {
//        service = new JobOfferService(repo, specificationBuilder);
//    }
//
//    @Test
//    @DisplayName("Suche alle JobOffers (ohne Parameter)")
//    void findAll() {
//        // given
//        final var jobOffer = createJobOfferMock(COMPANY);
//        final var jobOffersMock = new PageImpl<>(List.of(jobOffer));
//
//        // Wenn keine Parameter kommen, wird findAll ohne Specification aufgerufen (oder mit leerer Spec)
//        // Je nach Implementierung deines Services wird entweder repo.findAll(pageable) oder repo.findAll(spec, pageable) aufgerufen.
//        // Hier simulieren wir den Spec-Fall, da meistens find(params) genutzt wird.
//        when(repo.findAll(ArgumentMatchers.<Specification<@NonNull JobOffer>>any(), ArgumentMatchers.<Pageable>any()))
//            .thenReturn(jobOffersMock);
//
//        final Map<String, List<String>> keineSuchparameter = MultiValueMap.fromSingleValue(Map.of());
//
//        // when
//        final var result = service.find(keineSuchparameter, pageRequest0);
//
//        // then
//        assertThat(result).isNotEmpty();
//    }
//
//    @Nested
//    @DisplayName("Geschaeftslogik fuer Suche mit Parametern")
//    class FindByParams {
//
//        @ParameterizedTest(name = "[{index}] Suche mit ID: id={0}")
//        @ValueSource(strings = ID_VORHANDEN)
//        @DisplayName("Suche mit Query-Parameter ID")
//        void findByParamId(final String idStr) {
//            // given
//            final var id = UUID.fromString(idStr);
//            final var jobOffer = createJobOfferMock(id, COMPANY);
//            final var jobOffersMock = new PageImpl<>(List.of(jobOffer));
//
//            when(repo.findAll(ArgumentMatchers.<Specification<@NonNull JobOffer>>any(), ArgumentMatchers.<Pageable>any()))
//                .thenReturn(jobOffersMock);
//
//            final var suchparameter = MultiValueMap.fromSingleValue(Map.of("id", idStr));
//
//            // when
//            final var result = service.find(suchparameter, pageRequest0);
//
//            // then
//            assertThat(result).hasSize(1);
//            softly.assertThat(result.getContent().get(0).getId()).isEqualTo(id);
//        }
//
//        @ParameterizedTest(name = "[{index}] Suche mit Company: company={0}")
//        @ValueSource(strings = COMPANY)
//        @DisplayName("Suche mit Query-Parameter Company")
//        void findByParamCompany(final String company) {
//            // given
//            final var jobOffer = createJobOfferMock(company);
//            final var jobOffersMock = new PageImpl<>(List.of(jobOffer));
//
//            when(repo.findAll(ArgumentMatchers.<Specification<@NonNull JobOffer>>any(), ArgumentMatchers.<Pageable>any()))
//                .thenReturn(jobOffersMock);
//
//            final var suchparameter = MultiValueMap.fromSingleValue(Map.of("company", company));
//
//            // when
//            final var result = service.find(suchparameter, pageRequest0);
//
//            // then
//            assertThat(result).isNotEmpty();
//            softly.assertThat(result.getContent().get(0).getCompany()).isEqualTo(company);
//        }
//
//        @ParameterizedTest(name = "[{index}] Suche mit ID und Company: id={0}, company={1}")
//        @CsvSource(ID_VORHANDEN + ',' + COMPANY)
//        @DisplayName("Suche mit Query-Parameter ID und Company")
//        void findByParamIdAndCompany(final String idStr, final String company) {
//            // given
//            final var id = UUID.fromString(idStr);
//            final var jobOffer = createJobOfferMock(id, company);
//            final var jobOffersMock = new PageImpl<>(List.of(jobOffer));
//
//            when(repo.findAll(ArgumentMatchers.<Specification<@NonNull JobOffer>>any(), ArgumentMatchers.<Pageable>any()))
//                .thenReturn(jobOffersMock);
//
//            final var suchparameter = MultiValueMap.fromSingleValue(Map.of(
//                "id", idStr,
//                "company", company
//            ));
//
//            // when
//            final var result = service.find(suchparameter, pageRequest0);
//
//            // then
//            assertThat(result).isNotEmpty();
//            softly.assertThat(result.getContent().get(0).getId()).isEqualTo(id);
//            softly.assertThat(result.getContent().get(0).getCompany()).isEqualTo(company);
//        }
//    }
//
//    @Nested
//    @DisplayName("Geschaeftslogik fuer die Suche anhand der ID (Pfadvariable)")
//    @SuppressWarnings("DirectInvocationOnMock")
//    class FindById {
//        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID: id={0}")
//        @CsvSource(ID_VORHANDEN + ',' + COMPANY)
//        @DisplayName("Suche mit vorhandener ID")
//        void findById(final String idStr, final String company) {
//            // given
//            final var id = UUID.fromString(idStr);
//            final var jobOfferMock = createJobOfferMock(id, company);
//
//            // Annahme: Methode im Repo heißt analog zu Kunde 'findByIdFetch...' um Lazy Loading zu handhaben
//            // Falls dein Repo einfach nur findById nutzt, hier anpassen.
//            when(repo.findByIdFetchJobDescription(id)).thenReturn(jobOfferMock);
//
//            // when
//            final var jobOffer = service.findByIdWithDescription(id);
//
//            // then
//            assertThat(jobOffer.getId()).isEqualTo(jobOfferMock.getId());
//            softly.assertThat(jobOffer.getCompany()).isEqualTo(company);
//        }
//
//        @ParameterizedTest(name = "[{index}] Suche mit nicht vorhandener ID: id={0}")
//        @ValueSource(strings = ID_NICHT_VORHANDEN)
//        @DisplayName("Suche mit nicht vorhandener ID")
//        void findByIdNichtVorhanden(final String idStr) {
//            // given
//            final var id = UUID.fromString(idStr);
//
//            // when
//            final var notFoundException = catchThrowableOfType(
//                NotFoundException.class,
//                () -> service.findByIdWithDescription(id)
//            );
//
//            // then
//            assertThat(notFoundException)
//                .isNotNull()
//                .extracting(NotFoundException::getId)
//                .isEqualTo(id);
//        }
//    }
//
//    // -------------------------------------------------------------------------
//    // Hilfsmethoden fuer Mock-Objekte (JobOffer)
//    // -------------------------------------------------------------------------
//
//    private JobOffer createJobOfferMock(final String company) {
//        return createJobOfferMock(randomUUID(), company);
//    }
//
//    private JobOffer createJobOfferMock(final UUID id, final String company) {
//        return createJobOfferMock(id, company, JobOfferStatus.ACTIVE);
//    }
//
//    private JobOffer createJobOfferMock(
//        final UUID id,
//        final String company,
//        final JobOfferStatus status
//    ) {
//        final var description = JobDescriptionBuilder.getBuilder()
//            .setId(randomUUID())
//            .setTite(TITLE)
//            .setLocation("Karlsruhe")
//            .setSalary(BigDecimal.valueOf(60000))
//            .setSummary("Spannende Aufgaben...")
//            .setResponsibilities("DevOps, Java")
//            .setRequirements("Spring Boot")
//            .setEmployment(EmploymentType.FULLTIME)
//            .build();
//
//        final var applications = List.of(
//            ApplicationBuilder.getBuilder()
//                .setId(randomUUID())
//                .setApplicant("Bewerber 1")
//                .setResume(URI.create("https://example.com/resume.pdf"))
//                .setCoverletter(URI.create("https://example.com/cover.pdf"))
//                .setDate(LocalDate.now())
//                .setDocuments(List.of(URI.create("https://example.com/cert.pdf")))
//                .setStatus(ApplicationStatus.APPLIED)
//                .build()
//        );
//
//        return JobOfferBuilder.getBuilder()
//            .setId(id)
//            .setVersion(0)
//            .setCompany(company)
//            .setPublicationdate(LocalDate.now())
//            .setExpirationdate(LocalDate.now().plusDays(30))
//            .setStatus(status)
//            .setDescription(description)
//            .setApplications(applications)
//            .build();
//    }
//}
