package com.likelion.vlog.repository;

import com.likelion.vlog.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Blog blog;
    private Post post;

    @BeforeEach
    void setUp() {
        user = createTestUser("test@test.com", "테스터");
        em.persist(user);

        blog = user.getBlog();

        post = Post.create("테스트 글", "테스트 내용", blog);
        em.persist(post);

        em.flush();
        em.clear();

        user = em.find(User.class, user.getId());
        blog = user.getBlog();
        post = em.find(Post.class, post.getId());
    }

    @Nested
    @DisplayName("댓글 조회")
    class FindComments {

        @Test
        @DisplayName("게시글의 최상위 댓글만 조회")
        void findAllByPostAndParentIsNull_Success() {
            // given
            Comment parent1 = Comment.create(user, post, "부모 댓글 1");
            Comment parent2 = Comment.create(user, post, "부모 댓글 2");
            em.persist(parent1);
            em.persist(parent2);

            Comment reply = Comment.createReply(user, post, parent1, "대댓글");
            em.persist(reply);
            em.flush();
            em.clear();

            post = em.find(Post.class, post.getId());

            // when
            List<Comment> result = commentRepository.findAllByPostAndParentIsNull(post);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(c -> c.getParent() == null);
        }

        @Test
        @DisplayName("Fetch Join으로 댓글과 대댓글 함께 조회")
        void findAllByPostWithChildren_Success() {
            // given
            Comment parent = Comment.create(user, post, "부모 댓글");
            em.persist(parent);

            Comment reply1 = Comment.createReply(user, post, parent, "대댓글 1");
            Comment reply2 = Comment.createReply(user, post, parent, "대댓글 2");
            em.persist(reply1);
            em.persist(reply2);
            em.flush();
            em.clear();

            post = em.find(Post.class, post.getId());

            // when
            List<Comment> result = commentRepository.findAllByPostWithChildren(post);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getChildren()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("댓글 수 조회")
    class CountComments {

        @Test
        @DisplayName("게시글의 댓글 수 조회")
        void countByPost_Success() {
            // given
            Comment c1 = Comment.create(user, post, "댓글 1");
            Comment c2 = Comment.create(user, post, "댓글 2");
            Comment c3 = Comment.create(user, post, "댓글 3");
            em.persist(c1);
            em.persist(c2);
            em.persist(c3);
            em.flush();
            em.clear();

            post = em.find(Post.class, post.getId());

            // when
            int count = commentRepository.countByPost(post);

            // then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("여러 게시글의 댓글 수 벌크 조회")
        void countByPosts_Success() {
            // given
            Post post2 = Post.create("테스트 글 2", "내용 2", blog);
            em.persist(post2);

            Comment c1 = Comment.create(user, post, "댓글 1");
            Comment c2 = Comment.create(user, post, "댓글 2");
            Comment c3 = Comment.create(user, post2, "댓글 3");
            em.persist(c1);
            em.persist(c2);
            em.persist(c3);
            em.flush();
            em.clear();

            post = em.find(Post.class, post.getId());
            post2 = em.find(Post.class, post2.getId());

            // when
            List<Object[]> result = commentRepository.countByPosts(List.of(post, post2));

            // then
            assertThat(result).hasSize(2);
        }
    }

    // 테스트 헬퍼 메서드
    private User createTestUser(String email, String nickname) {
        try {
            java.lang.reflect.Constructor<User> constructor = User.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            User user = constructor.newInstance();
            ReflectionTestUtils.setField(user, "email", email);
            ReflectionTestUtils.setField(user, "nickname", nickname);
            ReflectionTestUtils.setField(user, "password", "password");
            return user;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
