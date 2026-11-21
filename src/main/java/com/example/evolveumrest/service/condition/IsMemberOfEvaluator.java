package com.example.evolveumrest.service.condition;

import com.example.evolveumrest.model.ConditionType;
import com.example.evolveumrest.model.User;
import org.springframework.stereotype.Component;

import java.util.List;

/** "isMemberOf": { "value": "Software Development" } - user belongs to the given organization unit. */
@Component
public class IsMemberOfEvaluator implements ConditionEvaluator {

    @Override
    public ConditionType type() {
        return ConditionType.IS_MEMBER_OF;
    }

    @Override
    public boolean matches(User user, String value) {
        List<String> units = user.getOrganizationUnit();
        return units != null && units.contains(value);
    }
}
