package com.example.evolveumrest.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

/**
 * User as it is sent and returned by the REST API, in the JSON format.
 * The "policy" field is calculated by the system.
 */
public record UserDto(
        @NotBlank(message = "must not be blank") String name,
        String firstName,
        String lastName,
        String emailAddress,
        List<String> organizationUnit,
        LocalDate birthDate,
        LocalDate registeredOn,
        List<String> policy) {
}
