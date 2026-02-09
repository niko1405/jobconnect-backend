/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 * ... (License header wie im Original)
 */
package com.acme.jobconnect.repository;

import com.acme.jobconnect.entity.JobOffer;
import com.acme.jobconnect.entity.JobOfferStatus;
import com.acme.jobconnect.entity.JobOffer_;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.PredicateSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

/// Singleton-Klasse, um Specifications für Queries in Spring Data JPA zu bauen.
@Component
public class SpecificationBuilder {
    // Annahme: StableValue ist eine Hilfsklasse in deinem Projekt, wie im Prof-Code
    private final StableValue<Logger> logger = StableValue.of();

    SpecificationBuilder() {
    }

    /// Specification für eine Query mit Spring Data bauen.
    ///
    /// @param suchparameter als MultiValueMap
    /// @return Specification für eine Query mit Spring Data
    @Nullable
    public Specification<JobOffer> build(final Map<String, ? extends List<String>> suchparameter) {
        getLogger().debug("build: suchparameter={}", suchparameter);

        if (suchparameter.isEmpty()) {
            return null;
        }

        final var specs = suchparameter
            .entrySet()
            .stream()
            .map(this::toPredicateSpecification)
            .toList();

        if (specs.isEmpty() || specs.contains(null)) {
            return null;
        }

        return Specification.where(PredicateSpecification.allOf(specs));
    }

    @Nullable
    @SuppressWarnings({"CyclomaticComplexity", "PMD.CyclomaticComplexity", "PMD.AvoidLiteralsInIfCondition"})
    private PredicateSpecification<JobOffer> toPredicateSpecification(
        final Map.Entry<String, ? extends List<String>> entry
    ) {
        getLogger().trace("toSpec: entry={}", entry);
        final var key = entry.getKey();
        final var values = entry.getValue();

        // Falls du später Listen-Logik brauchst (z.B. für Tags in JobDescription),
        // würde das hier ähnlich wie im "interessen"-Block des Profs stehen.

        if (values.size() != 1) {
            return null;
        }

        final var value = values.getFirst();

        // Switch Expression für deine JobOffer Attribute
        return switch (key) {
            case "id" -> id(value);
            case "company", "firma" -> company(value);
            case "status" -> status(value);
            case "minViews" -> minViews(value);
            // case "keyword" -> keywordInDescription(value); // Wenn JobDescription bekannt wäre
            default -> null;
        };
    }

    // --- Filter Methoden ---

    /**
     * Filtert nach Firmenname (Case-Insensitive, Teilstring).
     * Mapped auf JobOffer_.company
     */
    private PredicateSpecification<JobOffer> company(final String teil) {
        return (root, builder) -> builder.like(
            builder.lower(root.get(JobOffer_.company)),
            builder.lower(builder.literal("%" + teil + '%'))
        );
    }

    /**
     * Filtert nach Status (Enum).
     * Mapped auf JobOffer_.status
     */
    @Nullable
    private PredicateSpecification<JobOffer> status(final String statusStr) {
        try {
            // Versucht den String in das Enum zu parsen.
            // Falls JobOfferStatus eine eigene .of() Methode hat, nutze diese stattdessen.
            final var statusEnum = JobOfferStatus.valueOf(statusStr);
            return (root, builder) -> builder.equal(
                root.get(JobOffer_.status),
                statusEnum
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            getLogger().warn("Ungültiger Status für Filterung übergeben: {}", statusStr);
            return null;
        }
    }

    /**
     * Filtert nach minimaler Anzahl an Views.
     * Mapped auf JobOffer_.viewscount
     */
    @Nullable
    private PredicateSpecification<JobOffer> minViews(final String viewsStr) {
        try {
            final int minViews = Integer.parseInt(viewsStr);
            return (root, builder) -> builder.greaterThanOrEqualTo(
                root.get(JobOffer_.viewscount),
                minViews
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Filtert nach ID (exakte Übereinstimmung).
     * Mapped auf JobOffer_.id
     */
    @Nullable
    private PredicateSpecification<JobOffer> id(final String idStr) {
        try {
            final var uuid = java.util.UUID.fromString(idStr);
            return (root, builder) -> builder.equal(
                root.get(JobOffer_.id),
                uuid
            );
        } catch (IllegalArgumentException e) {
            getLogger().warn("Ungültige UUID für ID-Filterung: {}", idStr);
            return null;
        }
    }

    /* * Optional: Falls du das Metamodel für JobDescription hast (JobDescription_.title),
     * könntest du so etwas hinzufügen:
     *
     * private PredicateSpecification<JobOffer> keywordInDescription(final String keyword) {
     * return (root, builder) -> builder.like(
     * builder.lower(root.get(JobOffer_.description).get(JobDescription_.title)),
     * builder.lower(builder.literal("%" + keyword + "%"))
     * );
     * }
     */

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(SpecificationBuilder.class));
    }
}
