package com.openclassrooms.mddapi.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.openclassrooms.mddapi.dto.user.UpdateUserProfileRequest;
import com.openclassrooms.mddapi.dto.user.UpdatedUserProfileResponse;
import com.openclassrooms.mddapi.dto.user.UserProfileResponse;
import com.openclassrooms.mddapi.exception.EmailAlreadyUsedException;
import com.openclassrooms.mddapi.exception.UserNotFoundException;
import com.openclassrooms.mddapi.exception.UsernameAlreadyUsedException;
import com.openclassrooms.mddapi.model.User;
import com.openclassrooms.mddapi.repository.UserRepository;
import com.openclassrooms.mddapi.security.JwtService;
import com.openclassrooms.mddapi.security.SecurityCurrentUserProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SecurityCurrentUserProvider currentUserProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public UserProfileResponse profile() {
        return toResponse(currentUser());
    }

    @Transactional
    public UpdatedUserProfileResponse update(UpdateUserProfileRequest request) {
        User user = currentUser();

        if (!user.getEmail().equals(request.email())
                && userRepository.existsByEmailAndIdNot(request.email(), user.getId())) {
            throw new EmailAlreadyUsedException();
        }
        if (!user.getUsername().equals(request.username())
                && userRepository.existsByUsernameAndIdNot(request.username(), user.getId())) {
            throw new UsernameAlreadyUsedException();
        }

        user.setEmail(request.email());
        user.setUsername(request.username());
        if (StringUtils.hasText(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getEmail());
        return new UpdatedUserProfileResponse(saved.getId(), saved.getEmail(), saved.getUsername(), token);
    }

    private User currentUser() {
        Long userId = currentUserProvider.currentUserId();
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getUsername());
    }
}
