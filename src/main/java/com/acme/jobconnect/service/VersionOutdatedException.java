package com.acme.jobconnect.service;

import java.io.Serial;

/// Exception, falls die Versionsnummer nicht aktuell ist.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
public class VersionOutdatedException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -6446398552079178556L;

    /// Die verwaltete Version
    private final int version;

    /// Konstruktor für die Verwendung in `KundeWriteService`
    ///
    /// @param version Die veraltete Version
    VersionOutdatedException(final int version) {
        super("Die Versionsnummer " + version + " ist veraltet.");
        this.version = version;
    }

    /// Veraltete Version ermitteln.
    ///
    /// @return Die veraltete Version.
    public int getVersion() {
        return version;
    }

    @Override
    public String getMessage() {
        return super.getMessage() == null ? "Die Versionsnummer ist veraltet." : super.getMessage();
    }
}
