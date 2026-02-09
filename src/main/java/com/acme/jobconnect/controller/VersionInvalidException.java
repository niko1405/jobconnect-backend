package com.acme.jobconnect.controller;

import java.io.Serial;
import org.springframework.http.HttpStatusCode;

/// Exception, falls die Versionsnummer im Request-Header bei `If-Match` fehlt oder syntaktisch ungültig ist.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
class VersionInvalidException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 6132845254335875944L;

    private final HttpStatusCode status;

    /// Konstruktor für die Verwendung in KundeWriteController
    ///
    /// @param status HTTP-Statuscode.
    /// @param message Die eigentliche Meldung.
    VersionInvalidException(final HttpStatusCode status, final String message) {
        super(message);
        this.status = status;
    }

    /// Konstruktor für die Verwendung in KundeWriteController
    ///
    /// @param status HTTP-Statuscode.
    /// @param message Die eigentliche Meldung.
    /// @param ex Verursachende Exception
    VersionInvalidException(final HttpStatusCode status, final String message, final Exception ex) {
        super(message, ex);
        this.status = status;
    }

    @Override
    public String getMessage() {
        return super.getMessage() == null ? "" : super.getMessage();
    }

    HttpStatusCode getStatus() {
        return status;
    }
}
