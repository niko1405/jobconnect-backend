package com.acme.jobconnect.controller;

import com.acme.jobconnect.entity.EmploymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO für die Job-Beschreibung.
 */
public record JobDescriptionDTO(
    @NotBlank(message = "Title cannot be blank.")
    @Size(max = MAX_TITLE_LENGTH, message = "Title must be less than {max} characters.")
    String title,

    @NotBlank(message = "Location cannot be blank.")
    @Size(max = MAX_LOCATION_LENGTH, message = "Location must be less than {max} characters.")
    String location,

    @NotNull(message = "Salary must be set.")
    @DecimalMin(value = MIN_SALARY, inclusive = false, message = "Salary must be greater than zero.")
    BigDecimal salary,

    @NotBlank(message = "Summary cannot be blank.")
    @Size(min = MIN_SUMMARY_LENGTH, max = MAX_SUMMARY_LENGTH, message = "Summary must be between {min} and {max} characters.")
    String summary,

    @NotBlank(message = "Responsibilities cannot be blank.")
    @Size(min = MIN_SECTION_LENGTH, message = "Responsibilities section is too short.")
    String responsibilities,

    @NotBlank(message = "Requirements cannot be blank.")
    @Size(min = MIN_SECTION_LENGTH, message = "Requirements section is too short.")
    String requirements,

    @NotNull(message = "Employment type must be specified.")
    EmploymentType employment
) {
    public static final String MIN_SALARY = "0.00";
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_LOCATION_LENGTH = 100;
    private static final int MIN_SUMMARY_LENGTH = 20;
    private static final int MAX_SUMMARY_LENGTH = 500;
    private static final int MIN_SECTION_LENGTH = 20;
}
