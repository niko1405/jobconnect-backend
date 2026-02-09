package com.acme.jobconnect.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.net.URI;

@Converter(autoApply = true)
public class UriConverter implements AttributeConverter<URI, String> {

    @Override
    public String convertToDatabaseColumn(final URI uri) {
        return (uri == null) ? null : uri.toString();
    }

    @Override
    public URI convertToEntityAttribute(final String dbData) {
        return (dbData == null || dbData.isBlank()) ? null : URI.create(dbData);
    }
}
