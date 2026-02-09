package com.acme.jobconnect.controller;

import com.acme.jobconnect.entity.JobOfferStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * DTO für ein Stellenangebot.
 */
public record JobOfferDTO(
    @NotBlank(message = "Company name cannot be blank.")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH,
        message = "Company name must be between {min} and {max} characters long.")
    @Pattern(
        regexp = "^[a-zA-Z][a-zA-Z0-9\\s.,-]*$",
        message = "Company name must start with a letter and can only contain " +
            "letters, numbers, spaces, commas, periods, and hyphens."
    )
    String company,

    @Nullable
    @FutureOrPresent(message = "Publication date must be today or in the future.")
    LocalDate publicationdate,

    @NotNull(message = "Expiration date must be set.")
    @Future(message = "Expiration date must be in the future.")
    LocalDate expirationdate,

    @Min(value = MIN_VIEWSCOUNT, message = "View count must be at least {value}.")
    int viewscount,

    @NotNull(message = "Job status must be set.")
    JobOfferStatus status,

    @Nullable
    List<@Valid ApplicationDTO> applications,

    @Valid
    @NotNull(message = "Job description must be set.", groups = OnCreate.class)
    JobDescriptionDTO description
) {
    public interface OnCreate { }

    public static final int MIN_VIEWSCOUNT = 0;
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 100;
}
