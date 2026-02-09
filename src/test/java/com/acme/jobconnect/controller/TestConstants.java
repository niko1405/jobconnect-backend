/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.jobconnect.controller;

import com.acme.jobconnect.security.KeycloakRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.time.Duration;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@SuppressWarnings({
    "MissingJavadoc",
    "MissingJavadocType",
    "JavadocVariable",
    "PMD.TestClassWithoutTestCases"
})
public final class TestConstants {
    public static final ClientHttpRequestFactory REQUEST_FACTORY;
    public static final String SCHEMA = "https";
    public static final String HOST = "localhost";

    public static final String ADMIN_AUTH;
    public static final String USER_AUTH;

    static final String VERSION_2 = "2.0.0";
    static final ApiVersionInserter API_VERSION_INSERTER = ApiVersionInserter.useHeader("X-Version");

    private static final int CONNECT_TIMEOUT_IN_SECONDS = 10;
    private static final int READ_TIMEOUT_IN_MILLIS = 5_000;

    private static final String KEYCLOAK_SCHEMA = System.getProperty("app.keycloak.schema", "http");
    private static final String KEYCLOAK_HOST = System.getProperty("app.keycloak.host", "localhost");
    private static final int KEYCLOAK_PORT = Integer.parseInt(System.getProperty("app.keycloak.port", "8880"));
    private static final String CLIENT_ID = "spring-client";
    private static final String CLIENT_SECRET = System.getProperty("app.keycloak.client-secret", "QJ7DHbA59w9AQYMQApfXz7W2tqLJrDlz");
    private static final String USER_ADMIN = "admin";
    private static final String USER = "user";
    private static final String PASSWORD = "p";

    static {
        final var path = Path.of("src", "main", "resources", "certificate.crt");
        // https://stackoverflow.com/questions/50025086/...
        //              ...in-java-what-is-the-simplest-way-to-create-an-sslcontext-with-just-a-pem-file#answer-61680878
        final SSLContext sslContext;
        try (var certificateStream = Files.lines(path)) {
            final var certificateBytes = certificateStream.collect(Collectors.joining("\n")).getBytes(UTF_8);
            // https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/security/cert/CertificateFactory.html
            final var certificateFactory = CertificateFactory.getInstance("X.509");
            final var certificate = certificateFactory.generateCertificate(new ByteArrayInputStream(certificateBytes));
            final var truststore = KeyStore.getInstance(KeyStore.getDefaultType());
            truststore.load(null, null);
            truststore.setCertificateEntry("microservice", certificate);
            // "X509"
            final var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(truststore);
            sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException |
                 KeyManagementException e) {
            throw new IllegalStateException(e);
        }

        final var httpClient = HttpClient.newBuilder()
            .sslContext(sslContext)
            .connectTimeout(Duration.ofSeconds(CONNECT_TIMEOUT_IN_SECONDS))
            .build();
        final var jdkRequestFactory = new JdkClientHttpRequestFactory(httpClient);
        jdkRequestFactory.setReadTimeout(READ_TIMEOUT_IN_MILLIS);
        REQUEST_FACTORY = jdkRequestFactory;

        final var uriComponents = UriComponentsBuilder.newInstance()
            .scheme(KEYCLOAK_SCHEMA)
            .host(KEYCLOAK_HOST)
            .port(KEYCLOAK_PORT)
            .build();
        final var baseUrl = uriComponents.toUriString();
        final var keycloakClient = RestClient
            .builder()
            .baseUrl(baseUrl)
            .build();
        final var keycloakAdapter = RestClientAdapter.create(keycloakClient);
        final var keycloakProxyFactory = HttpServiceProxyFactory.builderFor(keycloakAdapter).build();
        final var keycloakRepository = keycloakProxyFactory.createClient(KeycloakRepository.class);
        final var adminToken = keycloakRepository.token(
            "username=" + USER_ADMIN + "&password=" + PASSWORD + "&grant_type=password" +
                "&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET,
            APPLICATION_FORM_URLENCODED_VALUE
        );
        ADMIN_AUTH = "Bearer " + adminToken.accessToken();
        final var userToken = keycloakRepository.token(
            "username=" + USER + "&password=" + PASSWORD + "&grant_type=password" +
                "&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET,
            APPLICATION_FORM_URLENCODED_VALUE
        );
        USER_AUTH = "Bearer " + userToken.accessToken();
    }

    private TestConstants() {
        // privater Konstruktor, da nur Konstante
    }
}
