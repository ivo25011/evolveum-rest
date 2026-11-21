package com.example.evolveumrest.controller;

import com.example.evolveumrest.dto.PolicyDto;
import com.example.evolveumrest.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/policy")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    public List<PolicyDto> getAllPolicies() {
        return policyService.getAllPolicies();
    }

    @GetMapping("/{id}")
    public PolicyDto getPolicy(@PathVariable String id) {
        return policyService.getPolicy(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyDto createPolicy(@Valid @RequestBody PolicyDto policy) {
        return policyService.createPolicy(policy);
    }

    @PutMapping("/{id}")
    public PolicyDto updatePolicy(@PathVariable String id, @RequestBody PolicyDto policy) {
        return policyService.updatePolicy(id, policy);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePolicy(@PathVariable String id) {
        policyService.deletePolicy(id);
    }
}
