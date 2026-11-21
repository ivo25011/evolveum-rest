package com.example.evolveumrest.mapper;

import com.example.evolveumrest.dto.ConditionValue;
import com.example.evolveumrest.dto.PolicyDto;
import com.example.evolveumrest.exception.ValidationException;
import com.example.evolveumrest.model.ConditionType;
import com.example.evolveumrest.model.Policy;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Converts policies between the REST API format (PolicyDto) and the database entity (Policy).
 * MapStruct generates the mapping of "id" and "name"; conditions need custom logic, see the @AfterMapping methods.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface PolicyMapper {

    /**
     * Throws ValidationException when the DTO contains an unknown condition or a value of the wrong JSON type.
     * "version" is managed by JPA.
     */
    @Mapping(target = "conditions", ignore = true)
    @Mapping(target = "version", ignore = true)
    Policy toEntity(PolicyDto dto);

    PolicyDto toDto(Policy policy);

    /**
     * "youngerThan": { "value": 18 } -> YOUNGER_THAN -> "18".
     * The JSON type of the value must match the condition: a number for youngerThan, text for the others.
     */
    @AfterMapping
    default void mapConditionsToEntity(PolicyDto dto, @MappingTarget Policy policy) {
        dto.conditions().forEach((jsonName, condition) -> {
            ConditionType type = ConditionType.fromJsonName(jsonName)
                    .orElseThrow(() -> new ValidationException("Unknown policy condition '" + jsonName + "'."));
            Object value = condition == null ? null : condition.value();

            if (value != null && !type.getValueType().matches(value)) {
                throw new ValidationException("Condition '" + jsonName + "' must be "
                        + type.getValueType().getDescription() + ", got " + value + ".");
            }

            policy.getConditions().put(type, value == null ? null : value.toString());
        });
    }

    /** YOUNGER_THAN -> "18" -> "youngerThan": { "value": 18 } */
    @AfterMapping
    default void mapConditionsToDto(Policy policy, @MappingTarget PolicyDto dto) {
        policy.getConditions().forEach((type, value) ->
                dto.addCondition(type.getJsonName(), new ConditionValue(type.toJsonValue(value))));
    }
}
