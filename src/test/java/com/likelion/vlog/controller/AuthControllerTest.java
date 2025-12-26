package com.likelion.vlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.vlog.dto.user.UserDto;
import com.likelion.vlog.exception.DuplicateException;
import com.likelion.vlog.exception.GlobalExceptionHandler;
import com.likelion.vlog.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private SecurityContextRepository securityContextRepository;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("회원가입 API")
    class Signup {

        @Test
        @DisplayName("회원가입 성공")
        void signup_Success() throws Exception {
            // given
            UserDto userDto = createUserDto(1L, "test@test.com", "테스터");
            given(authService.signup(any())).willReturn(userDto);

            // when & then
            mockMvc.perform(post("/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"test@test.com\",\"password\":\"password123\",\"nickname\":\"테스터\"}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("회원가입 성공"))
                    .andExpect(jsonPath("$.data.email").value("test@test.com"));
        }

        @Test
        @DisplayName("중복 이메일 시 409")
        void signup_DuplicateEmail() throws Exception {
            // given
            given(authService.signup(any())).willThrow(DuplicateException.email("test@test.com"));

            // when & then
            mockMvc.perform(post("/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"test@test.com\",\"password\":\"password123\",\"nickname\":\"테스터\"}"))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("필수 필드 누락 시 400")
        void signup_ValidationFailed() throws Exception {
            // when & then
            mockMvc.perform(post("/auth/signup")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"\",\"password\":\"\",\"nickname\":\"\"}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("로그인 API")
    class Login {

        @Test
        @DisplayName("로그인 성공")
        void login_Success() throws Exception {
            // given
            UserDto userDto = createUserDto(1L, "test@test.com", "테스터");
            Authentication authentication = new UsernamePasswordAuthenticationToken("test@test.com", null, List.of());

            given(authenticationManager.authenticate(any())).willReturn(authentication);
            given(authService.getUserInfo("test@test.com")).willReturn(userDto);

            // when & then
            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"test@test.com\",\"password\":\"password123\"}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("로그인 성공"))
                    .andExpect(jsonPath("$.data.email").value("test@test.com"));
        }

        @Test
        @DisplayName("잘못된 비밀번호 시 401")
        void login_BadCredentials() throws Exception {
            // given
            given(authenticationManager.authenticate(any()))
                    .willThrow(new BadCredentialsException("자격 증명에 실패하였습니다."));

            // when & then
            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"test@test.com\",\"password\":\"wrongpassword\"}"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("로그아웃 API")
    class Logout {

        @Test
        @WithMockUser
        @DisplayName("로그아웃 성공")
        void logout_Success() throws Exception {
            // when & then
            mockMvc.perform(post("/auth/logout")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("로그아웃 성공"));
        }

        @Test
        @DisplayName("세션 없이 로그아웃 시도")
        void logout_NoSession() throws Exception {
            // when & then
            mockMvc.perform(post("/auth/logout")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("로그아웃 성공"));
        }
    }

    // 헬퍼 메서드
    private UserDto createUserDto(Long id, String email, String nickname) {
        try {
            java.lang.reflect.Constructor<UserDto> constructor =
                UserDto.class.getDeclaredConstructor(Long.class, String.class, String.class, Long.class, String.class);
            constructor.setAccessible(true);
            return constructor.newInstance(id, email, nickname, 1L, nickname + "의 블로그");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
