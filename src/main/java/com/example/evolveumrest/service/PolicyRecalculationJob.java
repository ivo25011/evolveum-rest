package com.example.evolveumrest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Some conditions depend on time (e.g. "youngerThan" - a user turns 18 without any change of data),
 * so all users are recalculated regularly, as set by "policy.recalculation.cron" in application.properties
 * (every night at 01:00).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyRecalculationJob {

    private final PolicyAssignmentService policyAssignment;

    @Scheduled(cron = "${policy.recalculation.cron}")
    public void recalculateAllUsers() {
        log.info("Scheduled recalculation of user policies started");
        policyAssignment.recalculateAllUsers();
        log.info("Scheduled recalculation of user policies finished");
    }
}
