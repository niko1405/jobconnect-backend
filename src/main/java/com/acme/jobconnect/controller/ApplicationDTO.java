package com.acme.jobconnect.controller;

import com.acme.jobconnect.entity.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record ApplicationDTO(
    @NotBlank(message = "Applicant name cannot be blank.")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Applicant name contains invalid characters.")
    String applicant,

    @NotNull(message = "Resume link must be provided.")
    URI resume,

    // Cover letter is often optional.
    @Nullable
    URI coverletter,

    @Nullable
    @PastOrPresent(message = "Application date cannot be in the future.")
    LocalDate date,

    @NotNull
    List<URI> documents,

    @NotNull(message = "Application status must be set.")
    ApplicationStatus status
) {

}
