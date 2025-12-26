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
class TagMapRepositoryTest {

    @Autowired
    private TagMapRepository tagMapRepository;

    @Autowired
    private TestEntityManager em;

    private User user;
    private Blog blog;
    private Post post;
    private Tag springTag;
    private Tag jpaTag;

    @BeforeEach
    void setUp() {
        user = createTestUser("test@test.com", "테스터");
        em.persist(user);

        blog = user.getBlog();

        post = Post.create("테스트 글", "테스트 내용", blog);
        em.persist(post);

        springTag = Tag.create("Spring");
        jpaTag = Tag.create("JPA");
        em.persist(springTag);
        em.persist(jpaTag);

        em.flush();
        em.clear();

        user = em.find(User.class, user.getId());
        blog = user.getBlog();
        post = em.find(Post.class, post.getId());
        springTag = em.find(Tag.class, springTag.getId());
        jpaTag = em.find(Tag.class, jpaTag.getId());
    }

    @Nested
    @DisplayName("태그맵 조회")
    class FindTagMaps {

        @Test
        @DisplayName("게시글의 태그맵 조회")
        void findAllByPost_Success() {
            // given
            TagMap tagMap1 = TagMap.create(post, springTag);
            TagMap tagMap2 = TagMap.create(post, jpaTag);
            em.persist(tagMap1);
            em.persist(tagMap2);
            em.flush();
            em.clear();

            post = em.find(Post.class, post.getId());

            // when
            List<TagMap> result = tagMapRepository.findAllByPost(post);

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("태그맵 벌크 삭제")
    class DeleteTagMaps {

        @Test
        @DisplayName("게시글의 태그맵 벌크 삭제")
        void deleteAllByPost_Success() {
            // given
            TagMap tagMap1 = TagMap.create(post, springTag);
            TagMap tagMap2 = TagMap.create(post, jpaTag);
            em.persist(tagMap1);
            em.persist(tagMap2);
            em.flush();
            em.clear();

            post = em.find(Post.class, post.getId());

            // when
            tagMapRepository.deleteAllByPost(post);
            em.flush();
            em.clear();

            post = em.find(Post.class, post.getId());

            // then
            List<TagMap> result = tagMapRepository.findAllByPost(post);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("태그맵이 없는 게시글 삭제 시 정상 동작")
        void deleteAllByPost_Empty() {
            // when & then (예외 없이 정상 동작)
            tagMapRepository.deleteAllByPost(post);
            em.flush();
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
