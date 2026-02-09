package com.acme.jobconnect.service;

import java.io.Serial;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/// [RuntimeException], in case no JobOffer is found.
public final class NotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1101909572340666200L;

    @Nullable
    private final UUID id;

    @Nullable
    private final Map<String, List<String>> queryparameter;

    NotFoundException() {
        super("No JobOffers found.");
        id = null;
        queryparameter = null;
    }

    NotFoundException(final UUID id) {
        super("No JobOffer found with id " + id);
        this.id = id;
        queryparameter = null;
    }

    NotFoundException(final Map<String, List<String>> queryparameter) {
        super("No JobOffers found.");
        id = null;
        this.queryparameter = queryparameter;
    }

    public @Nullable UUID getId() {
        return id;
    }

    public @Nullable Map<String, List<String>> getQueryparameter() {
        return queryparameter;
    }
}
