package com.likelion.vlog.service;

import com.likelion.vlog.dto.auth.SignupRequestDto;
import com.likelion.vlog.entity.entity.User;
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
    public String signup(SignupRequestDto dto){
        if(userRepository.existsByEmail(dto.getEmail())){
            throw new IllegalArgumentException("이미 존재하는 이메일");
        }
        User user = User.of(dto, passwordEncoder);
        userRepository.save(user);
        return "회원가입 성공";
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
