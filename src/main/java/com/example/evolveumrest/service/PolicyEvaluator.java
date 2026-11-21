package com.example.evolveumrest.service;

import com.example.evolveumrest.exception.ValidationException;
import com.example.evolveumrest.model.ConditionType;
import com.example.evolveumrest.model.Policy;
import com.example.evolveumrest.model.User;
import com.example.evolveumrest.service.condition.ConditionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Strategy context: for every condition of a policy finds the ConditionEvaluator
 * whose type() matches the condition's ConditionType and lets it evaluate the condition.
 */
@Service
@RequiredArgsConstructor
public class PolicyEvaluator {

    /** Spring injects all beans implementing ConditionEvaluator. */
    private final List<ConditionEvaluator> evaluators;

    /** A policy applies to a user when it has at least one condition and all of its conditions match. */
    public boolean appliesTo(Policy policy, User user) {
        Map<ConditionType, String> conditions = policy.getConditions();
        return !conditions.isEmpty()
                && conditions.entrySet().stream()
                        .allMatch(condition -> evaluatorFor(condition.getKey()).matches(user, condition.getValue()));
    }

    /** Throws ValidationException when the policy has no condition or some condition value is invalid. */
    public void validate(Policy policy) {
        if (policy.getConditions().isEmpty()) {
            throw new ValidationException("Policy '" + policy.getId() + "' must have at least one condition.");
        }
        policy.getConditions().forEach((type, value) -> evaluatorFor(type).validate(value));
    }

    /** Finds the implementation of ConditionEvaluator that handles the given condition type. */
    private ConditionEvaluator evaluatorFor(ConditionType type) {
        return evaluators.stream()
                .filter(evaluator -> evaluator.type() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No evaluator for condition type " + type));
    }
}
