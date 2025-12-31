package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Like;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    // 좋아요 존재 여부 확인
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    // 게시글 좋아요 수 조회
    Long countByPostId(Long postId);

    // 좋아요 엔티티 찾기
    Optional<Like> findByUserIdAndPostId(Long userId, Long postId);

    // 게시글의 모든 좋아요 삭제
    void deleteAllByPostId(Long postId);

    // User가 누른 모든 좋아요 삭제
    void deleteAllByUserId(Long userId);

    // User의 Blog에 속한 Post들의 모든 좋아요 삭제
    void deleteAllByPostBlogUserId(Long userId);

    // N+1 해결: 여러 Post의 좋아요 수를 한번에 조회
    @Query("SELECT l.post.id, COUNT(l) FROM Like l WHERE l.post IN :posts GROUP BY l.post.id")
    List<Object[]> countByPosts(@Param("posts") List<Post> posts);

}