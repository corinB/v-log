
package com.likelion.vlog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.vlog.dto.posts.PostCreatePostRequest;
import com.likelion.vlog.dto.posts.PostUpdatePutRequest;
import com.likelion.vlog.dto.posts.AuthorResponse;
import com.likelion.vlog.dto.posts.PostGetResponse;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.GlobalExceptionHandler;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.service.AuthService;
import com.likelion.vlog.service.PostService;
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

@WebMvcTest(PostController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private AuthService authService;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("게시글 상세 조회 API")
    class GetPost {

        @Test
        @DisplayName("게시글 조회 성공")
        void getPost_Success() throws Exception {
            // given
            PostGetResponse response = createPostGetResponse(1L, "테스트 제목", "테스트 내용");
            given(postService.getPost(1L)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/v1/posts/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("게시글 조회 성공"))
                    .andExpect(jsonPath("$.data.postId").value(1))
                    .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                    .andExpect(jsonPath("$.data.content").value("테스트 내용"));
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 404")
        void getPost_NotFound() throws Exception {
            // given
            given(postService.getPost(999L)).willThrow(NotFoundException.post(999L));

            // when & then
            mockMvc.perform(get("/api/v1/posts/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("게시글 작성 API")
    class CreatePost {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("게시글 작성 성공")
        void createPost_Success() throws Exception {
            // given
            PostCreatePostRequest request = new PostCreatePostRequest();
            PostGetResponse response = createPostGetResponse(1L, "새 게시글", "새 내용");

            given(postService.createPost(any(PostCreatePostRequest.class), eq("test@test.com")))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/posts")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"새 게시글\",\"content\":\"새 내용\",\"tags\":[]}"))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("게시글 작성 성공"))
                    .andExpect(jsonPath("$.data.postId").value(1))
                    .andExpect(jsonPath("$.data.title").value("새 게시글"));
        }

        // Note: 인증 테스트는 Security 필터가 비활성화된 상태에서 테스트 불가
        // 실제 인증 테스트는 통합 테스트에서 수행
    }

    @Nested
    @DisplayName("게시글 수정 API")
    class UpdatePost {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("게시글 수정 성공")
        void updatePost_Success() throws Exception {
            // given
            PostGetResponse response = createPostGetResponse(1L, "수정된 제목", "수정된 내용");
            given(postService.updatePost(eq(1L), any(PostUpdatePutRequest.class), eq("test@test.com")))
                    .willReturn(response);

            // when & then
            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"수정된 제목\",\"content\":\"수정된 내용\",\"tags\":[]}"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("게시글 수정 성공"))
                    .andExpect(jsonPath("$.data.title").value("수정된 제목"));
        }

        @Test
        @WithMockUser(username = "other@test.com")
        @DisplayName("작성자가 아닌 사용자가 수정 시 403")
        void updatePost_Forbidden() throws Exception {
            // given
            given(postService.updatePost(eq(1L), any(PostUpdatePutRequest.class), eq("other@test.com")))
                    .willThrow(ForbiddenException.postUpdate());

            // when & then
            mockMvc.perform(put("/api/v1/posts/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"title\":\"수정된 제목\",\"content\":\"수정된 내용\",\"tags\":[]}"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("게시글 삭제 API")
    class DeletePost {

        @Test
        @WithMockUser(username = "test@test.com")
        @DisplayName("게시글 삭제 성공")
        void deletePost_Success() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/v1/posts/1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "other@test.com")
        @DisplayName("작성자가 아닌 사용자가 삭제 시 403")
        void deletePost_Forbidden() throws Exception {
            // given
            doThrow(ForbiddenException.postDelete())
                    .when(postService).deletePost(eq(1L), eq("other@test.com"));

            // when & then
            mockMvc.perform(delete("/api/v1/posts/1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // 헬퍼 메서드
    private PostGetResponse createPostGetResponse(Long id, String title, String content) {
        return PostGetResponse.builder()
                .postId(id)
                .title(title)
                .content(content)
                .author(AuthorResponse.builder()
                        .userId(1L)
                        .nickname("테스터")
                        .build())
                .tags(List.of())
                .comments(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}