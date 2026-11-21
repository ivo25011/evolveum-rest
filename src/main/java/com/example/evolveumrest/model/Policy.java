package com.example.evolveumrest.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/** A policy is a named set of conditions. It applies to a user when all of its conditions match. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Policy {

    @Id
    private String id;
    private String name;

    /** Condition type -> condition value, e.g. YOUNGER_THAN -> "18". */
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyEnumerated(EnumType.STRING)
    private Map<ConditionType, String> conditions = new HashMap<>();

    /** Optimistic locking */
    @Version
    private Long version;
}
