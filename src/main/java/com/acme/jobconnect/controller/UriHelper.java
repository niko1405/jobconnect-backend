package com.acme.jobconnect.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import static com.acme.jobconnect.controller.Constants.API_PATH;

/// Hilfsklasse um URIs für den Location-Header zu ermitteln, falls ein _API-Gateway_ verwendet
/// wird.
///
/// @author [Jürgen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)
@Component
class UriHelper {
    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final String X_FORWARDED_HOST = "x-forwarded-host";
    private static final String X_FORWARDED_PREFIX = "x-forwarded-prefix";
    private static final String KUNDEN_PREFIX = "/kunden";

    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit _package private_ für _Spring_.
    UriHelper() {
    }

    /// Basis-URI ermitteln, d.h. ohne Query-Parameter.
    ///
    /// @param request Servlet-Request
    /// @return Die Basis-URI als String
    URI getBaseUri(final HttpServletRequest request) {
        final var forwardedHost = request.getHeader(X_FORWARDED_HOST);
        if (forwardedHost != null) {
            // Forwarding durch Kubernetes Ingress Controller oder Spring Cloud Gateway
            return getBaseUriForwarded(request, forwardedHost);
        }

        // KEIN Forwarding von einem API-Gateway
        // URI aus Schema, Host, Port und Pfad
        final var uriComponents = ServletUriComponentsBuilder.fromRequestUri(request).build();
        final var baseUri = uriComponents.getScheme() + "://" + uriComponents.getHost() + ':' +
            uriComponents.getPort() + '/' + API_PATH;
        getLogger().debug("getBaseUri (ohne Forwarding): baseUri={}", baseUri);
        return URI.create(baseUri);
    }

    private URI getBaseUriForwarded(final HttpServletRequest request, final String forwardedHost) {
        // x-forwarded-host = Hostname des API-Gateways

        // "https" oder "http"
        final var forwardedProto = request.getHeader(X_FORWARDED_PROTO);
        if (forwardedProto == null) {
            throw new IllegalStateException("Kein '" + X_FORWARDED_PROTO + "' im Header");
        }

        var forwardedPrefix = request.getHeader(X_FORWARDED_PREFIX);
        // x-forwarded-prefix: null bei Kubernetes Ingress Controller bzw. "/kunden" bei Spring Cloud Gateway
        if (forwardedPrefix == null) {
            getLogger().trace("getBaseUriForwarded: Kein '{}' im Header", X_FORWARDED_PREFIX);
            forwardedPrefix = KUNDEN_PREFIX;
        }
        final var baseUri = forwardedProto + "://" + forwardedHost + forwardedPrefix + '/' + API_PATH;
        getLogger().debug("getBaseUriForwarded: baseUri={}", baseUri);
        return URI.create(baseUri);
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(UriHelper.class));
    }
}
