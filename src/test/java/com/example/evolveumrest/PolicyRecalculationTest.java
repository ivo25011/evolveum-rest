package com.example.evolveumrest;

import com.example.evolveumrest.repository.PolicyRepo;
import com.example.evolveumrest.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Users must be recalculated when they are created or modified and whenever a policy is added or modified.
 * Policies use exactly the JSON format from the assignment.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PolicyRecalculationTest {

    private static final String UNDERAGED = """
            { "id": "underaged", "name": "Underaged User", "youngerThan": { "value": 18 } }
            """;

    private static final String INTERNAL_USER = """
            { "id": "internal-user", "name": "Internal User", "emailDomainIs": { "value": "evolveum.com" } }
            """;

    private static final String DEVELOPER_FULL_ACCESS = """
            { "id": "developer-full-access", "name": "Developer (Full Access)", "isMemberOf": { "value": "Software Development" } }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PolicyRepo policyRepo;

    @BeforeEach
    void cleanDatabase() {
        userRepo.deleteAll();
        policyRepo.deleteAll();
    }

    @Test
    void newUserGetsAllMatchingPolicies() throws Exception {
        createPolicy(UNDERAGED);
        createPolicy(INTERNAL_USER);
        createPolicy(DEVELOPER_FULL_ACCESS);

        createUser("jdoe", "jdoe@evolveum.com", LocalDate.now().minusYears(16), "Software Development", "Support");

        assertThat(policiesOf("jdoe"))
                .containsExactlyInAnyOrder("underaged", "internal-user", "developer-full-access");
    }

    @Test
    void addingPolicyRecalculatesExistingUsers() throws Exception {
        createUser("jdoe", "jdoe@evolveum.com", LocalDate.now().minusYears(30), "Support");
        assertThat(policiesOf("jdoe")).isEmpty();

        createPolicy(INTERNAL_USER);

        assertThat(policiesOf("jdoe")).containsExactly("internal-user");
    }

    @Test
    void modifyingPolicyRecalculatesExistingUsers() throws Exception {
        createUser("jdoe", "jdoe@evolveum.com", LocalDate.now().minusYears(30), "Support");
        createUser("asmith", "asmith@example.com", LocalDate.now().minusYears(30), "Support");
        createPolicy(INTERNAL_USER);
        assertThat(policiesOf("jdoe")).containsExactly("internal-user");
        assertThat(policiesOf("asmith")).isEmpty();

        mockMvc.perform(put("/policy/internal-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "id": "internal-user", "name": "Internal User", "emailDomainIs": { "value": "example.com" } }
                                """))
                .andExpect(status().isOk());

        assertThat(policiesOf("jdoe")).isEmpty();
        assertThat(policiesOf("asmith")).containsExactly("internal-user");
    }

    @Test
    void modifyingUserRecalculatesHisPolicies() throws Exception {
        createPolicy(INTERNAL_USER);
        createPolicy(DEVELOPER_FULL_ACCESS);
        createUser("jdoe", "jdoe@evolveum.com", LocalDate.now().minusYears(30), "Support");
        assertThat(policiesOf("jdoe")).containsExactly("internal-user");

        mockMvc.perform(put("/users/jdoe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "emailAddress": "jdoe@example.com",
                                  "organizationUnit": ["Software Development"],
                                  "birthDate": "%s"
                                }
                                """.formatted(LocalDate.now().minusYears(30))))
                .andExpect(status().isOk());

        assertThat(policiesOf("jdoe")).containsExactly("developer-full-access");
    }

    private void createUser(String name, String email, LocalDate birthDate, String... organizationUnits) throws Exception {
        String units = Arrays.stream(organizationUnits)
                .map(unit -> "\"" + unit + "\"")
                .collect(Collectors.joining(", ", "[", "]"));

        String json = """
                {
                  "name": "%s",
                  "firstName": "John",
                  "lastName": "Doe",
                  "emailAddress": "%s",
                  "organizationUnit": %s,
                  "birthDate": "%s",
                  "registeredOn": "2024-05-07"
                }
                """.formatted(name, email, units, birthDate);

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    private void createPolicy(String json) throws Exception {
        mockMvc.perform(post("/policy").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
    }

    private List<String> policiesOf(String userName) {
        return Optional.ofNullable(userRepo.findById(userName).orElseThrow().getPolicy())
                .orElse(List.of());
    }
}
