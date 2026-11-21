package com.example.evolveumrest.service;

import com.example.evolveumrest.model.Policy;
import com.example.evolveumrest.model.User;
import com.example.evolveumrest.repository.PolicyRepo;
import com.example.evolveumrest.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps the users' "policy" field up to date.
 * Shared by UserService (a single user changed) and PolicyService (a policy changed,
 * so every user has to be recalculated).
 */
@Service
@RequiredArgsConstructor
public class PolicyAssignmentService {

    private final UserRepo userRepo;
    private final PolicyRepo policyRepo;
    private final PolicyEvaluator policyEvaluator;

    /** Recalculates the policies of a single user (the caller saves the user). */
    public void assignPolicies(User user) {
        user.setPolicy(evaluate(user, policyRepo.findAll()));
    }

    /** Recalculates and saves the policies of all users, e.g. after a policy was added, modified or deleted. */
    @Transactional
    public void recalculateAllUsers() {
        List<Policy> policies = policyRepo.findAll();
        List<User> users = userRepo.findAll();
        users.forEach(user -> user.setPolicy(evaluate(user, policies)));
        userRepo.saveAll(users);
    }

    private List<String> evaluate(User user, List<Policy> policies) {
        return policies.stream()
                .filter(policy -> policyEvaluator.appliesTo(policy, user))
                .map(Policy::getId)
                .collect(Collectors.toList());
    }
}
