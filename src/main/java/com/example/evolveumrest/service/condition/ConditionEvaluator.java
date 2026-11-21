package com.example.evolveumrest.service.condition;

import com.example.evolveumrest.exception.ValidationException;
import com.example.evolveumrest.model.ConditionType;
import com.example.evolveumrest.model.User;

/**
 * Strategy for one condition type.
 * Every implementation is a Spring bean; PolicyEvaluator finds the right one by comparing type()
 * with the condition type of the policy.
 */
public interface ConditionEvaluator {

    /** Which condition type this strategy evaluates. */
    ConditionType type();

    /** Does the condition with the given value match the user? */
    boolean matches(User user, String value);

    /**
     * Checks the configured value when a policy is created or updated.
     * Throws ValidationException with a readable message when the value is invalid.
     */
    default void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Condition '" + type().getJsonName() + "' must have a non-empty value.");
        }
    }
}
