package com.example.evolveumrest.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/** A user identified by a unique name. The list of applicable policies is calculated by the system. */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    private String name;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private List<String> organizationUnit;
    private LocalDate birthDate;
    private LocalDate registeredOn;

    /** IDs of policies applicable to this user, calculated by the system. */
    private List<String> policy;

    /**
     * Optimistic locking.
     */
    @Version
    private Long version;
}
