package com.likelion.vlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.vlog.dto.request.CommentCreateRequest;
import com.likelion.vlog.dto.request.CommentUpdateRequest;
import com.likelion.vlog.dto.response.AuthorResponse;
import com.likelion.vlog.dto.response.CommentResponse;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.GlobalExceptionHandler;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.service.CommentService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("댓글 목록 조회 API")
    class GetComments {

        @Test
        @DisplayName("댓글 목록 조회 성공")
        void getComments_Success() throws Exception {
            // given
            CommentResponse response = createCommentResponse(1L, "테스트 댓글");
            given(commentService.getComments(1L)).willReturn(List.of(response));

            // when & then
            mockMvc.perform(get("/api/v1/posts/1/comments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].commentId").value(1))
                    .andExpect(jsonPath("$[0].content").value("테스트 댓글"));
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 404")
        void getComments_PostNotFound() throws Exception {
            // given
            given(commentService.getComments(999L)).willThrow(NotFoundException.post(999L));

            // when & then
            mockMvc.perform(get("/api/v1/posts/999/comments"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("빈 댓글 목록 조회")
        void getComments_Empty() throws Exception {
            // given
            given(commentService.getComments(1L)).willReturn(List.of());

            // when & then
            mockMvc.perform(get("/api/v1/posts/1/comments"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("댓글 작성 API")
    class CreateComment {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("일반 댓글 작성 성공")
        void createComment_Success() throws Exception {
            // given
            CommentResponse response = createCommentResponse(1L, "새 댓글");
            given(commentService.createComment(eq(1L), any(CommentCreateRequest.class), eq("test@test.com")))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/posts/1/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"새 댓글\"}"))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.commentId").value(1))
                    .andExpect(jsonPath("$.content").value("새 댓글"));
        }

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("대댓글 작성 성공")
        void createReply_Success() throws Exception {
            // given
            CommentResponse response = createCommentResponse(2L, "대댓글");
            given(commentService.createComment(eq(1L), any(CommentCreateRequest.class), eq("test@test.com")))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/posts/1/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"대댓글\",\"parentId\":1}"))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value("대댓글"));
        }

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("내용 없이 작성 시 400")
        void createComment_EmptyContent_BadRequest() throws Exception {
            // when & then
            mockMvc.perform(post("/api/v1/posts/1/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"\"}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("대댓글의 대댓글 작성 시 400")
        void createReplyToReply_BadRequest() throws Exception {
            // given
            given(commentService.createComment(eq(1L), any(CommentCreateRequest.class), eq("test@test.com")))
                    .willThrow(new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다."));

            // when & then
            mockMvc.perform(post("/api/v1/posts/1/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"대대댓글\",\"parentId\":2}"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("댓글 수정 API")
    class UpdateComment {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("댓글 수정 성공")
        void updateComment_Success() throws Exception {
            // given
            CommentResponse response = createCommentResponse(1L, "수정된 댓글");
            given(commentService.updateComment(eq(1L), eq(1L), any(CommentUpdateRequest.class), eq("test@test.com")))
                    .willReturn(response);

            // when & then
            mockMvc.perform(put("/api/v1/posts/1/comments/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"수정된 댓글\"}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").value("수정된 댓글"));
        }

        @Test
        @WithMockUser(username = "other@test.com")
        @DisplayName("다른 사용자가 수정 시 403")
        void updateComment_Forbidden() throws Exception {
            // given
            given(commentService.updateComment(eq(1L), eq(1L), any(CommentUpdateRequest.class), eq("other@test.com")))
                    .willThrow(ForbiddenException.commentUpdate());

            // when & then
            mockMvc.perform(put("/api/v1/posts/1/comments/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"content\":\"수정된 댓글\"}"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("댓글 삭제 API")
    class DeleteComment {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("댓글 삭제 성공")
        void deleteComment_Success() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/v1/posts/1/comments/1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "other@test.com")
        @DisplayName("다른 사용자가 삭제 시 403")
        void deleteComment_Forbidden() throws Exception {
            // given
            doThrow(ForbiddenException.commentDelete())
                    .when(commentService).deleteComment(eq(1L), eq(1L), eq("other@test.com"));

            // when & then
            mockMvc.perform(delete("/api/v1/posts/1/comments/1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // 헬퍼 메서드
    private CommentResponse createCommentResponse(Long id, String content) {
        return CommentResponse.builder()
                .commentId(id)
                .content(content)
                .author(AuthorResponse.builder()
                        .userId(1L)
                        .nickname("테스터")
                        .build())
                .children(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
