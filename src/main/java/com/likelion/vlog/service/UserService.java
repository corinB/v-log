package com.likelion.vlog.service;

import com.likelion.vlog.dto.user.UserUpdateDto;
import com.likelion.vlog.entity.entity.User;
import com.likelion.vlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User updateUser(Long userId, UserUpdateDto userUpdateDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (userUpdateDto.getNickname() != null) {
            user.updateNickName(userUpdateDto.getNickname());
        }

        if (userUpdateDto.getPassword() != null) {
            user.updatePassword(userUpdateDto.getPassword());
        }

        user.updateUpdatedAt();

        userRepository.save(user);
        return user;
    }

}
