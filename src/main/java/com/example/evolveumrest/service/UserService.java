package com.example.evolveumrest.service;

import com.example.evolveumrest.mapper.UserMapper;
import com.example.evolveumrest.dto.UserDto;
import com.example.evolveumrest.exception.AlreadyExistsException;
import com.example.evolveumrest.exception.NotFoundException;
import com.example.evolveumrest.model.User;
import com.example.evolveumrest.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final UserMapper userMapper;
    private final PolicyAssignmentService policyAssignment;

    public List<UserDto> getAllUsers() {
        return userRepo.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public UserDto getUser(String name) {
        return userMapper.toDto(findUser(name));
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        if (userRepo.existsById(userDto.name())) {
            throw new AlreadyExistsException("User with name '" + userDto.name() + "' already exists.");
        }

        User user = userMapper.toEntity(userDto);
        if (user.getRegisteredOn() == null) {
            user.setRegisteredOn(LocalDate.now());
        }
        policyAssignment.assignPolicies(user);
        userRepo.save(user);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUser(String name, UserDto userDto) {
        User existingUser = findUser(name);
        User updatedUser = userMapper.toEntity(userDto);

        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmailAddress(updatedUser.getEmailAddress());
        existingUser.setOrganizationUnit(updatedUser.getOrganizationUnit());
        existingUser.setBirthDate(updatedUser.getBirthDate());
        // name (the ID) and registeredOn do not change when the user is updated

        policyAssignment.assignPolicies(existingUser);
        userRepo.save(existingUser);
        return userMapper.toDto(existingUser);
    }

    @Transactional
    public void deleteUser(String name) {
        userRepo.delete(findUser(name));
    }

    private User findUser(String name) {
        return userRepo.findById(name)
                .orElseThrow(() -> new NotFoundException("User with name '" + name + "' not found."));
    }
}
