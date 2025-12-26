package com.likelion.vlog.repository;

import com.likelion.vlog.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Blog blog;
    private Tag springTag;
    private Tag jpaTag;

    @BeforeEach
    void setUp() {
        // User 생성 (ReflectionTestUtils 사용)
        user = createTestUser("test@test.com", "테스터");
        em.persist(user);

        // User의 @PrePersist에서 Blog가 자동 생성됨
        blog = user.getBlog();

        // Tag 생성
        springTag = Tag.create("Spring");
        jpaTag = Tag.create("JPA");
        em.persist(springTag);
        em.persist(jpaTag);

        em.flush();
        em.clear();

        // 영속성 컨텍스트 초기화 후 다시 조회
        user = em.find(User.class, user.getId());
        blog = user.getBlog();
    }

    @Nested
    @DisplayName("블로그별 게시글 조회")
    class FindAllByBlogId {

        @Test
        @DisplayName("블로그별 게시글 페이징 조회 성공")
        void findAllByBlogId_Success() {
            // given
            Post post1 = Post.create("제목1", "내용1", blog);
            Post post2 = Post.create("제목2", "내용2", blog);
            em.persist(post1);
            em.persist(post2);
            em.flush();
            em.clear();

            // when
            Page<Post> result = postRepository.findAllByBlogId(blog.getId(), PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("게시글이 없는 블로그 조회 시 빈 결과")
        void findAllByBlogId_Empty() {
            // when
            Page<Post> result = postRepository.findAllByBlogId(blog.getId(), PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("태그별 게시글 조회")
    class FindAllByTagName {

        @Test
        @DisplayName("태그명으로 게시글 필터링 성공")
        void findAllByTagName_Success() {
            // given
            springTag = em.find(Tag.class, springTag.getId());
            jpaTag = em.find(Tag.class, jpaTag.getId());

            Post post1 = Post.create("Spring 글", "내용1", blog);
            Post post2 = Post.create("JPA 글", "내용2", blog);
            em.persist(post1);
            em.persist(post2);

            TagMap tagMap1 = TagMap.create(post1, springTag);
            TagMap tagMap2 = TagMap.create(post2, jpaTag);
            em.persist(tagMap1);
            em.persist(tagMap2);
            em.flush();
            em.clear();

            // when
            Page<Post> result = postRepository.findAllByTagName("Spring", PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Spring 글");
        }

        @Test
        @DisplayName("존재하지 않는 태그로 조회 시 빈 결과")
        void findAllByTagName_Empty() {
            // when
            Page<Post> result = postRepository.findAllByTagName("NonExistent", PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("태그+블로그 복합 필터링")
    class FindAllByTagNameAndBlogId {

        @Test
        @DisplayName("태그명과 블로그ID로 복합 필터링 성공")
        void findAllByTagNameAndBlogId_Success() {
            // given
            springTag = em.find(Tag.class, springTag.getId());

            Post post1 = Post.create("Spring 글", "내용1", blog);
            em.persist(post1);

            TagMap tagMap = TagMap.create(post1, springTag);
            em.persist(tagMap);
            em.flush();
            em.clear();

            // when
            Page<Post> result = postRepository.findAllByTagNameAndBlogId("Spring", blog.getId(), PageRequest.of(0, 10));

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("Spring 글");
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
