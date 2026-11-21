package com.example.evolveumrest.dto;

/**
 * Value of one policy condition in JSON, e.g. { "value": 18 }.
 * The type is Object because it is a number or text, depending on the condition (see ConditionType.ValueType).
 */
public record ConditionValue(Object value) {
}
