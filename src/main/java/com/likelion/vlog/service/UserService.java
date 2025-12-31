package com.likelion.vlog.service;

import com.likelion.vlog.dto.users.UserGetResponse;
import com.likelion.vlog.dto.users.UserUpdateRequest;

import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.InvalidCredentialsException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserGetResponse getUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));
        return UserGetResponse.of(user);
    }

    @Transactional
    public UserGetResponse updateUser(Long userId, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        user.upDateInfo(userUpdateRequest, passwordEncoder);

        userRepository.save(user);
        return UserGetResponse.of(user);
    }


    @Transactional
    public void deleteUser(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw InvalidCredentialsException.password();
        }
        userRepository.delete(user);
    }

}
