package com.likelion.vlog.service;

import com.likelion.vlog.dto.request.CommentCreateRequest;
import com.likelion.vlog.dto.request.CommentUpdateRequest;
import com.likelion.vlog.dto.response.CommentResponse;
import com.likelion.vlog.entity.Blog;
import com.likelion.vlog.entity.Comment;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.ForbiddenException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.CommentRepository;
import com.likelion.vlog.repository.PostRepository;
import com.likelion.vlog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    private User user;
    private User otherUser;
    private Blog blog;
    private Post post;
    private Post otherPost;
    private Comment comment;
    private Comment reply;

    @BeforeEach
    void setUp() {
        user = createTestUser(1L, "test@test.com", "테스터");
        otherUser = createTestUser(2L, "other@test.com", "다른유저");
        blog = createTestBlog(1L, user);
        post = createTestPost(1L, "테스트 제목", "테스트 내용", blog);
        otherPost = createTestPost(2L, "다른 게시글", "다른 내용", blog);
        comment = createTestComment(1L, user, post, null, "테스트 댓글");
        reply = createTestComment(2L, user, post, comment, "테스트 대댓글");
    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class GetComments {

        @Test
        @DisplayName("댓글 목록 조회 성공")
        void getComments_Success() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findAllByPostWithChildren(post)).willReturn(List.of(comment));

            // when
            List<CommentResponse> responses = commentService.getComments(1L);

            // then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getCommentId()).isEqualTo(1L);
            assertThat(responses.get(0).getContent()).isEqualTo("테스트 댓글");
        }

        @Test
        @DisplayName("빈 댓글 목록 조회")
        void getComments_Empty() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findAllByPostWithChildren(post)).willReturn(List.of());

            // when
            List<CommentResponse> responses = commentService.getComments(1L);

            // then
            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 예외 발생")
        void getComments_PostNotFound() {
            // given
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.getComments(999L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("게시글을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("일반 댓글 작성")
    class CreateComment {

        @Test
        @DisplayName("일반 댓글 작성 성공")
        void createComment_Success() {
            // given
            CommentCreateRequest request = createCommentRequest("새 댓글", null);

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
                Comment saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 3L);
                ReflectionTestUtils.setField(saved, "children", new ArrayList<>());
                return saved;
            });

            // when
            CommentResponse response = commentService.createComment(1L, request, "test@test.com");

            // then
            assertThat(response.getContent()).isEqualTo("새 댓글");
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 작성 시 예외 발생")
        void createComment_UserNotFound() {
            // given
            CommentCreateRequest request = createCommentRequest("댓글", null);
            given(userRepository.findByEmail("unknown@test.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(1L, request, "unknown@test.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 작성 시 예외 발생")
        void createComment_PostNotFound() {
            // given
            CommentCreateRequest request = createCommentRequest("댓글", null);
            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(999L, request, "test@test.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("게시글을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("대댓글 작성")
    class CreateReply {

        @Test
        @DisplayName("대댓글 작성 성공")
        void createReply_Success() {
            // given
            CommentCreateRequest request = createCommentRequest("대댓글", 1L);

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));
            given(commentRepository.save(any(Comment.class))).willAnswer(invocation -> {
                Comment saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 3L);
                ReflectionTestUtils.setField(saved, "children", new ArrayList<>());
                return saved;
            });

            // when
            CommentResponse response = commentService.createComment(1L, request, "test@test.com");

            // then
            assertThat(response.getContent()).isEqualTo("대댓글");
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @DisplayName("존재하지 않는 부모 댓글에 대댓글 시 예외 발생")
        void createReply_ParentNotFound() {
            // given
            CommentCreateRequest request = createCommentRequest("대댓글", 999L);

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(1L, request, "test@test.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("댓글을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("대댓글의 대댓글 작성 시 예외 발생 (1단계 제한)")
        void createReply_ToReply_ThrowsException() {
            // given
            CommentCreateRequest request = createCommentRequest("대대댓글", 2L);

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(2L)).willReturn(Optional.of(reply));

            // when & then
            assertThatThrownBy(() -> commentService.createComment(1L, request, "test@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("대댓글에는 답글을 달 수 없습니다.");
        }

        @Test
        @DisplayName("다른 게시글의 댓글에 대댓글 시 예외 발생")
        void createReply_DifferentPost_ThrowsException() {
            // given
            Comment otherPostComment = createTestComment(10L, user, otherPost, null, "다른 게시글 댓글");
            CommentCreateRequest request = createCommentRequest("대댓글", 10L);

            given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(10L)).willReturn(Optional.of(otherPostComment));

            // when & then
            assertThatThrownBy(() -> commentService.createComment(1L, request, "test@test.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("부모 댓글이 해당 게시글에 속하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class UpdateComment {

        @Test
        @DisplayName("작성자 본인 수정 성공")
        void updateComment_Success() {
            // given
            CommentUpdateRequest request = createUpdateRequest("수정된 댓글");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

            // when
            CommentResponse response = commentService.updateComment(1L, 1L, request, "test@test.com");

            // then
            assertThat(response.getContent()).isEqualTo("수정된 댓글");
        }

        @Test
        @DisplayName("다른 사용자 수정 시 예외 발생")
        void updateComment_Forbidden() {
            // given
            CommentUpdateRequest request = createUpdateRequest("수정된 댓글");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(1L, 1L, request, "other@test.com"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("수정 권한이 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 댓글 수정 시 예외 발생")
        void updateComment_CommentNotFound() {
            // given
            CommentUpdateRequest request = createUpdateRequest("수정된 댓글");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(1L, 999L, request, "test@test.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("댓글을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("다른 게시글의 댓글 수정 시 예외 발생")
        void updateComment_DifferentPost() {
            // given
            Comment otherPostComment = createTestComment(10L, user, otherPost, null, "다른 게시글 댓글");
            CommentUpdateRequest request = createUpdateRequest("수정된 댓글");

            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(10L)).willReturn(Optional.of(otherPostComment));

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(1L, 10L, request, "test@test.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("댓글을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("작성자 본인 삭제 성공")
        void deleteComment_Success() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

            // when
            commentService.deleteComment(1L, 1L, "test@test.com");

            // then
            verify(commentRepository).delete(comment);
        }

        @Test
        @DisplayName("다른 사용자 삭제 시 예외 발생")
        void deleteComment_Forbidden() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(1L, 1L, "other@test.com"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("삭제 권한이 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시 예외 발생")
        void deleteComment_CommentNotFound() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(1L, 999L, "test@test.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("댓글을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
        void deleteComment_PostNotFound() {
            // given
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.deleteComment(999L, 1L, "test@test.com"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("게시글을 찾을 수 없습니다");
        }
    }

    // 테스트 헬퍼 메서드
    private User createTestUser(Long id, String email, String nickname) {
        try {
            java.lang.reflect.Constructor<User> constructor = User.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            User user = constructor.newInstance();
            ReflectionTestUtils.setField(user, "id", id);
            ReflectionTestUtils.setField(user, "email", email);
            ReflectionTestUtils.setField(user, "nickname", nickname);
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Blog createTestBlog(Long id, User user) {
        try {
            java.lang.reflect.Constructor<Blog> constructor = Blog.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Blog blog = constructor.newInstance();
            ReflectionTestUtils.setField(blog, "id", id);
            ReflectionTestUtils.setField(blog, "user", user);
            ReflectionTestUtils.setField(blog, "title", user.getNickname() + "의 블로그");
            return blog;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Post createTestPost(Long id, String title, String content, Blog blog) {
        Post post = Post.create(title, content, blog);
        ReflectionTestUtils.setField(post, "id", id);
        ReflectionTestUtils.setField(post, "tagMapList", new ArrayList<>());
        return post;
    }

    private Comment createTestComment(Long id, User user, Post post, Comment parent, String content) {
        Comment comment;
        if (parent == null) {
            comment = Comment.create(user, post, content);
        } else {
            comment = Comment.createReply(user, post, parent, content);
        }
        ReflectionTestUtils.setField(comment, "id", id);
        ReflectionTestUtils.setField(comment, "children", new ArrayList<>());
        return comment;
    }

    private CommentCreateRequest createCommentRequest(String content, Long parentId) {
        CommentCreateRequest request = new CommentCreateRequest();
        ReflectionTestUtils.setField(request, "content", content);
        ReflectionTestUtils.setField(request, "parentId", parentId);
        return request;
    }

    private CommentUpdateRequest createUpdateRequest(String content) {
        CommentUpdateRequest request = new CommentUpdateRequest();
        ReflectionTestUtils.setField(request, "content", content);
        return request;
    }
}
