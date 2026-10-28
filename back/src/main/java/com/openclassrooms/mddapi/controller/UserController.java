package com.openclassrooms.mddapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openclassrooms.mddapi.dto.user.UpdateUserProfileRequest;
import com.openclassrooms.mddapi.dto.user.UpdatedUserProfileResponse;
import com.openclassrooms.mddapi.dto.user.UserProfileResponse;
import com.openclassrooms.mddapi.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse profile() {
        return userService.profile();
    }

    @PutMapping("/me")
    public UpdatedUserProfileResponse update(@Valid @RequestBody UpdateUserProfileRequest request) {
        return userService.update(request);
    }
}
