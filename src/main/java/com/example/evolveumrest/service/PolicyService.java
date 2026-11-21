package com.example.evolveumrest.service;

import com.example.evolveumrest.mapper.PolicyMapper;
import com.example.evolveumrest.dto.PolicyDto;
import com.example.evolveumrest.exception.AlreadyExistsException;
import com.example.evolveumrest.exception.NotFoundException;
import com.example.evolveumrest.model.Policy;
import com.example.evolveumrest.repository.PolicyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepo policyRepo;
    private final PolicyMapper policyMapper;
    private final PolicyEvaluator policyEvaluator;
    private final PolicyAssignmentService policyAssignment;

    public List<PolicyDto> getAllPolicies() {
        return policyRepo.findAll().stream()
                .map(policyMapper::toDto)
                .toList();
    }

    public PolicyDto getPolicy(String policyId) {
        return policyMapper.toDto(findPolicy(policyId));
    }

    @Transactional
    public PolicyDto createPolicy(PolicyDto policyDto) {
        if (policyRepo.existsById(policyDto.getId())) {
            throw new AlreadyExistsException("Policy with ID '" + policyDto.getId() + "' already exists.");
        }

        Policy policy = toValidEntity(policyDto);
        policyRepo.save(policy);
        policyAssignment.recalculateAllUsers();
        return policyMapper.toDto(policy);
    }

    @Transactional
    public PolicyDto updatePolicy(String policyId, PolicyDto policyDto) {
        Policy policy = findPolicy(policyId);
        policyDto.setId(policyId);
        Policy updatedPolicy = toValidEntity(policyDto);

        policy.setName(updatedPolicy.getName());
        policy.getConditions().clear();
        policy.getConditions().putAll(updatedPolicy.getConditions());
        policyRepo.save(policy);
        policyAssignment.recalculateAllUsers();
        return policyMapper.toDto(policy);
    }

    @Transactional
    public void deletePolicy(String policyId) {
        policyRepo.delete(findPolicy(policyId));
        policyAssignment.recalculateAllUsers();
    }

    private Policy findPolicy(String policyId) {
        return policyRepo.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Policy with ID '" + policyId + "' not found."));
    }

    /** Converts the DTO to an entity and validates its conditions; throws ValidationException when invalid. */
    private Policy toValidEntity(PolicyDto policyDto) {
        Policy policy = policyMapper.toEntity(policyDto);
        policyEvaluator.validate(policy);
        return policy;
    }
}
