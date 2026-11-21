package com.example.evolveumrest.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * All condition types a policy can use.
 * Adding a new type = add a constant here + a new ConditionEvaluator implementation.
 */
@Getter
@RequiredArgsConstructor
public enum ConditionType {

    YOUNGER_THAN("youngerThan", ValueType.INTEGER),
    EMAIL_DOMAIN_IS("emailDomainIs", ValueType.TEXT),
    IS_MEMBER_OF("isMemberOf", ValueType.TEXT);

    /** Property name used in the policy JSON, e.g. "youngerThan": { "value": 18 } */
    private final String jsonName;

    /** Type of the condition value in JSON. */
    private final ValueType valueType;

    /** JSON type of a condition value; a value of another type is rejected. */
    @Getter
    @RequiredArgsConstructor
    public enum ValueType {
        INTEGER(Integer.class, "a whole number"),
        TEXT(String.class, "text");

        /** Java type Jackson creates for this JSON type: 18 -> Integer, "evolveum.com" -> String. */
        private final Class<?> javaType;

        /** Used in the error message, e.g. "must be a whole number". */
        private final String description;

        public boolean matches(Object value) {
            return javaType.isInstance(value);
        }
    }

    /** Finds the condition type by its JSON name, e.g. "youngerThan" -> YOUNGER_THAN; empty when the name is unknown. */
    public static Optional<ConditionType> fromJsonName(String jsonName) {
        return Arrays.stream(values())
                .filter(type -> type.jsonName.equals(jsonName))
                .findFirst();
    }

    /** Converts the stored value back to its JSON type, e.g. "18" -> 18 for YOUNGER_THAN. */
    public Object toJsonValue(String value) {
        if (value == null || valueType == ValueType.TEXT) {
            return value;
        }
        return Integer.valueOf(value);
    }
}
