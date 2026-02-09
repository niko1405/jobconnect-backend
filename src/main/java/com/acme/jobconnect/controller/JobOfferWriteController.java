package com.acme.jobconnect.controller;

import com.acme.jobconnect.security.RolleAdmin;
import com.acme.jobconnect.service.JobOfferWriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.groups.Default;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import static com.acme.jobconnect.controller.Constants.API_PATH;
import static com.acme.jobconnect.controller.Constants.CREATE_TAG;
import static com.acme.jobconnect.controller.Constants.ID_PATTERN;
import static com.acme.jobconnect.controller.Constants.UPDATE_TAG;
import static com.acme.jobconnect.controller.Constants.VERSION_2;
import static com.acme.jobconnect.controller.Constants.VERSION_2_EXAMPLE;
import static com.acme.jobconnect.controller.Constants.X_VERSION;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.PRECONDITION_FAILED;
import static org.springframework.http.HttpStatus.PRECONDITION_REQUIRED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;

@Controller
@Validated
@RequestMapping(API_PATH)
class JobOfferWriteController {
    private static final String VERSIONSNUMMER_FEHLT = "Versionsnummer fehlt";

    private final JobOfferWriteService service;
    private final JobOfferMapper mapper;
    private final UriHelper uriHelper;
    private final StableValue<Logger> logger = StableValue.of();

    JobOfferWriteController(final JobOfferWriteService service, final JobOfferMapper mapper, final UriHelper uriHelper) {
        this.service = service;
        this.mapper = mapper;
        this.uriHelper = uriHelper;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, version = VERSION_2)
    @Operation(summary = "Eine neue JobOffer anlegen", tags = CREATE_TAG)
    @Parameter(name = X_VERSION, in = ParameterIn.HEADER, example = VERSION_2_EXAMPLE)
    @ApiResponse(responseCode = "201", description = "JobOffer neu angelegt")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte")
    @SuppressWarnings("TrailingComment")
    ResponseEntity<Void> post(
        @RequestBody @Validated({Default.class, JobOfferDTO.OnCreate.class}) final JobOfferDTO jobOfferDTO,
        final HttpServletRequest request
    ) throws URISyntaxException {
        getLogger().debug("post: jobOfferDTO={}", jobOfferDTO);

        final var jobOfferInput = mapper.toJobOffer(jobOfferDTO);
        final var jobOffer = service.create(jobOfferInput);
        final var baseUri = uriHelper.getBaseUri(request);
        final var location = new URI(baseUri.toString() + '/' + jobOffer.getId());
        return created(location).build();
    }

    /// Einen vorhandenen JobOffer-Datensatz überschreiben.
    ///
    /// @param id ID des zu aktualisierenden JobOffer.
    /// @param jobOfferDTO Das JobOffer-Objekt aus dem eingegangenen Request-Body.
    /// @param ifMatch Versionsnummer aus dem Header If-Match
    /// @return Response mit Statuscode 204 oder Statuscode 400, falls der JSON-Datensatz syntaktisch nicht korrekt ist
    ///      oder 422 falls Constraints verletzt sind
    ///      oder 412 falls die Versionsnummer nicht ok ist oder 428 falls die Versionsnummer fehlt.
    @PutMapping(path = "{id:" + ID_PATTERN + "}", consumes = APPLICATION_JSON_VALUE, version = VERSION_2)
    @Operation(summary = "Einen Kunden mit neuen Werten aktualisieren", tags = UPDATE_TAG)
    @Parameter(name = X_VERSION, in = ParameterIn.HEADER, example = VERSION_2_EXAMPLE)
    @ApiResponse(responseCode = "204", description = "Aktualisiert")
    @ApiResponse(responseCode = "400", description = "Syntaktische Fehler im Request-Body")
    @ApiResponse(responseCode = "404", description = "JobOffer nicht vorhanden")
    @ApiResponse(responseCode = "412", description = "Versionsnummer falsch")
    @ApiResponse(responseCode = "422", description = "Ungültige Werte")
    @ApiResponse(responseCode = "428", description = VERSIONSNUMMER_FEHLT)
    ResponseEntity<Void> put(
        @PathVariable final UUID id,
        @RequestBody @Validated final JobOfferDTO jobOfferDTO,
        @RequestHeader("If-Match") @Nullable final String ifMatch
    ) {
        getLogger().debug("put: id={}, kundeDTO={}, ifMatch={}", id, jobOfferDTO, ifMatch);
        final int version = getVersion(ifMatch);
        final var jobOfferInput = mapper.toJobOffer(jobOfferDTO);
        final var jobOffer = service.update(jobOfferInput, id, version);
        getLogger().debug("put: {}", jobOffer);
        return noContent().eTag("\"" + jobOffer.getVersion() + '"').build();
    }

    /// Einen vorhandenen JobOffer anhand seiner ID löschen.
    ///
    /// @param id ID des zu löschenden JobOffer.
    @DeleteMapping(path = "{id:" + ID_PATTERN + "}", version = VERSION_2)
    @RolleAdmin
    @ResponseStatus(NO_CONTENT)
    @Operation(summary = "Eine JobOffer anhand der ID loeschen", tags = "Loeschen")
    @Parameter(name = X_VERSION, in = ParameterIn.HEADER, example = VERSION_2_EXAMPLE)
    @ApiResponse(responseCode = "204", description = "Gelöscht")
    void deleteById(@PathVariable final UUID id)  {
        getLogger().debug("deleteById: id={}", id);
        service.deleteById(id);
    }

    @SuppressWarnings({"MagicNumber", "RedundantSuppression"})
    private int getVersion(@Nullable final String versionStr) {
        getLogger().trace("getVersion: {}", versionStr);
        if (versionStr == null) {
            throw new VersionInvalidException(PRECONDITION_REQUIRED, VERSIONSNUMMER_FEHLT);
        }
        if (versionStr.length() < 3 ||
            versionStr.charAt(0) != '"' ||
            versionStr.charAt(versionStr.length() - 1) != '"') {
            throw new VersionInvalidException(PRECONDITION_FAILED, "Ungueltiges ETag " + versionStr);
        }

        final int version;
        try {
            version = Integer.parseInt(versionStr.substring(1, versionStr.length() - 1));
        } catch (final NumberFormatException ex) {
            throw new VersionInvalidException(PRECONDITION_FAILED, "Ungueltiges ETag " + versionStr, ex);
        }

        getLogger().trace("getVersion: version={}", version);
        return version;
    }

    /// [ExceptionHandler] für [MethodArgumentNotValidException]
    ///
    /// @param ex Exception für Fehler im Request-Body bei `POST` oder `PUT` gemäß _Jakarta Validation_.
    /// @return ErrorResponse mit `ProblemDetail` gemäß _RFC 9457_.
    @ExceptionHandler
    ErrorResponse onConstraintViolations(final MethodArgumentNotValidException ex) {
        final var detailMessages = ex.getDetailMessageArguments();
        final var detail = detailMessages.length == 0 || detailMessages[1] == null
            ? "Constraint Violation"
            : ((String) detailMessages[1]).replace(", and ", ", ");
        return ErrorResponse.create(ex, UNPROCESSABLE_CONTENT, detail);
    }

    /// [ExceptionHandler] für [HttpMessageNotReadableException]
    ///
    /// @param ex Exception für den syntaktisch falschen Request-Body bei `POST` oder `PUT`.
    /// @return ErrorResponse mit `ProblemDetail` gemäß _RFC 9457_.
    @ExceptionHandler
    ErrorResponse onMessageNotReadable(final HttpMessageNotReadableException ex) {
        final var msg = ex.getMessage() == null ? "N/A" : ex.getMessage();
        getLogger().debug("onMessageNotReadable: {}", msg);
        return ErrorResponse.create(ex, BAD_REQUEST, msg);
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(JobOfferWriteController.class));
    }
}
