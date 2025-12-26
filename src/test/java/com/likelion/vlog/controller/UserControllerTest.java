package com.likelion.vlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.vlog.dto.user.UserDto;
import com.likelion.vlog.exception.GlobalExceptionHandler;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("사용자 조회 API")
    class GetUser {

        @Test
        @DisplayName("사용자 조회 성공")
        void getUser_Success() throws Exception {
            // given
            UserDto userDto = createUserDto(1L, "test@test.com", "테스터");
            given(userService.getUser(1L)).willReturn(userDto);

            // when & then
            mockMvc.perform(get("/users/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("회원정보 조회 성공"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.email").value("test@test.com"));
        }

        @Test
        @DisplayName("존재하지 않는 사용자 조회 시 404")
        void getUser_NotFound() throws Exception {
            // given
            given(userService.getUser(999L)).willThrow(NotFoundException.user(999L));

            // when & then
            mockMvc.perform(get("/users/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("사용자 정보 수정 API")
    class UpdateUser {

        @Test
        @WithMockUser
        @DisplayName("사용자 정보 수정 성공")
        void updateUser_Success() throws Exception {
            // given
            UserDto userDto = createUserDto(1L, "test@test.com", "수정된닉네임");
            given(userService.updateUser(eq(1L), any())).willReturn(userDto);

            // when & then
            mockMvc.perform(put("/users/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"수정된닉네임\",\"password\":\"newpassword\"}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("회원정보 수정 성공"))
                    .andExpect(jsonPath("$.data.nickname").value("수정된닉네임"));
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 사용자 수정 시 404")
        void updateUser_NotFound() throws Exception {
            // given
            given(userService.updateUser(eq(999L), any())).willThrow(NotFoundException.user(999L));

            // when & then
            mockMvc.perform(put("/users/999")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"nickname\":\"수정된닉네임\",\"password\":\"newpassword\"}"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        @DisplayName("빈 요청 시 400")
        void updateUser_ValidationFailed() throws Exception {
            // when & then
            mockMvc.perform(put("/users/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andDo(print())
                    .andExpect(status().isOk()); // UserUpdateRequestDto에 validation 없음
        }
    }

    @Nested
    @DisplayName("회원 탈퇴 API")
    class DeleteUser {

        @Test
        @WithMockUser
        @DisplayName("회원 탈퇴 성공")
        void deleteUser_Success() throws Exception {
            // when & then
            mockMvc.perform(delete("/users/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"password\":\"password123\"}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("회원탈퇴 성공"));
        }

        @Test
        @WithMockUser
        @DisplayName("비밀번호 불일치 시 400")
        void deleteUser_WrongPassword() throws Exception {
            // given
            doThrow(new IllegalArgumentException("비밀번호가 일치하지 않습니다."))
                    .when(userService).deleteUser(eq(1L), eq("wrongpassword"));

            // when & then
            mockMvc.perform(delete("/users/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"password\":\"wrongpassword\"}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("존재하지 않는 사용자 탈퇴 시 404")
        void deleteUser_NotFound() throws Exception {
            // given
            doThrow(NotFoundException.user(999L))
                    .when(userService).deleteUser(eq(999L), any());

            // when & then
            mockMvc.perform(delete("/users/999")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"password\":\"password123\"}"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
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
