package com.example.evolveumrest.controller;

import com.example.evolveumrest.dto.UserDto;
import com.example.evolveumrest.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{name}")
    public UserDto getUser(@PathVariable String name) {
        return userService.getUser(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserDto user) {
        return userService.createUser(user);
    }

    @PutMapping("/{name}")
    public UserDto updateUser(@PathVariable String name, @RequestBody UserDto user) {
        return userService.updateUser(name, user);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String name) {
        userService.deleteUser(name);
    }
}
