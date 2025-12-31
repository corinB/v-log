package com.likelion.vlog.service;

import com.likelion.vlog.dto.auth.SignupRequest;
import com.likelion.vlog.dto.users.UserGetResponse;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.DuplicateException;
import com.likelion.vlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("없는 이메일"));
        return toUserDetail(user);
    }

    @Transactional
    public UserGetResponse signup(SignupRequest dto){
        if(userRepository.existsByEmail(dto.getEmail())){
            throw DuplicateException.email(dto.getEmail());
        }
        User user = User.of(dto, passwordEncoder);
        userRepository.save(user);
        return UserGetResponse.of(user);
    }

    public UserGetResponse getUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("없는 이메일"));
        return UserGetResponse.of(user);
    }

    private UserDetails toUserDetail(User user){
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
}
