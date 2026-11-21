package com.example.evolveumrest.service.condition;

import com.example.evolveumrest.model.ConditionType;
import com.example.evolveumrest.model.User;
import org.springframework.stereotype.Component;

import java.util.Locale;

/** "emailDomainIs": { "value": "evolveum.com" } - user's e-mail ends with @value (case-insensitive). */
@Component
public class EmailDomainIsEvaluator implements ConditionEvaluator {

    @Override
    public ConditionType type() {
        return ConditionType.EMAIL_DOMAIN_IS;
    }

    @Override
    public boolean matches(User user, String value) {
        String email = user.getEmailAddress();
        if (email == null) {
            return false;
        }
        return email.toLowerCase(Locale.ROOT).endsWith("@" + value.toLowerCase(Locale.ROOT));
    }
}
