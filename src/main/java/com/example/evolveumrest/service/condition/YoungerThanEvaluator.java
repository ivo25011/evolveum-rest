package com.example.evolveumrest.service.condition;

import com.example.evolveumrest.exception.ValidationException;
import com.example.evolveumrest.model.ConditionType;
import com.example.evolveumrest.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

/** "youngerThan": { "value": 18 } - user's age in whole years is lower than the value. */
@Component
public class YoungerThanEvaluator implements ConditionEvaluator {

    @Override
    public ConditionType type() {
        return ConditionType.YOUNGER_THAN;
    }

    @Override
    public boolean matches(User user, String value) {
        if (user.getBirthDate() == null) {
            return false;
        }
        int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        return age < Integer.parseInt(value);
    }

    @Override
    public void validate(String value) {
        ConditionEvaluator.super.validate(value);

        int limit;
        try {
            limit = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ValidationException("Condition 'youngerThan' must be a whole number, got '" + value + "'.");
        }
        if (limit < 0) {
            throw new ValidationException("Condition 'youngerThan' must not be negative, got '" + value + "'.");
        }
    }
}
