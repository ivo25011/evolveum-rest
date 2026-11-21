package com.example.evolveumrest.service.condition;

import com.example.evolveumrest.model.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Plain unit tests of the condition strategies, no Spring needed. */
class ConditionEvaluatorTest {

    private final YoungerThanEvaluator youngerThan = new YoungerThanEvaluator();
    private final EmailDomainIsEvaluator emailDomainIs = new EmailDomainIsEvaluator();
    private final IsMemberOfEvaluator isMemberOf = new IsMemberOfEvaluator();

    @Test
    void youngerThanComparesAge() {
        assertThat(youngerThan.matches(userBornYearsAgo(16), "18")).isTrue();
        assertThat(youngerThan.matches(userBornYearsAgo(18), "18")).isFalse();
        assertThat(youngerThan.matches(userBornYearsAgo(30), "18")).isFalse();
    }

    @Test
    void emailDomainIsComparesDomainIgnoringCase() {
        User user = new User();
        user.setEmailAddress("JDoe@Evolveum.com");

        assertThat(emailDomainIs.matches(user, "evolveum.com")).isTrue();
        assertThat(emailDomainIs.matches(user, "example.com")).isFalse();
        assertThat(emailDomainIs.matches(new User(), "evolveum.com")).isFalse();
    }

    @Test
    void isMemberOfChecksOrganizationUnits() {
        User user = new User();
        user.setOrganizationUnit(List.of("Software Development", "Support"));

        assertThat(isMemberOf.matches(user, "Support")).isTrue();
        assertThat(isMemberOf.matches(user, "HR")).isFalse();
        assertThat(isMemberOf.matches(new User(), "Support")).isFalse();
    }

    private static User userBornYearsAgo(int years) {
        User user = new User();
        user.setBirthDate(LocalDate.now().minusYears(years));
        return user;
    }
}
