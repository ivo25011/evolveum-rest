package com.example.evolveumrest.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Policy as it is sent and returned by the REST API, in the JSON format:
 * { "id": "underaged", "name": "Underaged User", "youngerThan": { "value": 18 } }
 * Every JSON property other than "id" and "name" is a condition.
 */
public class PolicyDto {

    @Getter
    @Setter
    @NotBlank(message = "must not be blank")
    private String id;

    @Getter
    @Setter
    private String name;

    private final Map<String, ConditionValue> conditions = new LinkedHashMap<>();

    /** JSON -> DTO: every other property ("youngerThan", "emailDomainIs", ...) is collected here. */
    @JsonAnySetter
    public void addCondition(String jsonName, ConditionValue condition) {
        conditions.put(jsonName, condition);
    }

    /** DTO -> JSON: conditions are written back as top-level properties. */
    @JsonAnyGetter
    public Map<String, ConditionValue> conditions() {
        return conditions;
    }
}
