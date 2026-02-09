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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.jobconnect.controller;

import com.acme.jobconnect.service.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/// Handler für allgemeine Exceptions.
///
/// @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
@ControllerAdvice
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final StableValue<Logger> logger = StableValue.of();

    /// Konstruktor mit _package private_ für _Spring_.
    GlobalExceptionHandler() {
    }

    /// [ExceptionHandler], wenn ein JobOffer gesucht wird, aber nicht vorhanden ist.
    ///
    /// @param ex Die zugehörige [NotFoundException].
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    public ResponseEntity<Void> handleNotFound(final NotFoundException ex) {
        getLogger().debug("NotFoundException: {}", ex.getMessage());
        return ResponseEntity.notFound().build();
    }

    private Logger getLogger() {
        return logger.orElseSet(() -> LoggerFactory.getLogger(GlobalExceptionHandler.class));
    }
}
