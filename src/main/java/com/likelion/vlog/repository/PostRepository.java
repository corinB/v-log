package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Post;
import com.likelion.vlog.repository.querydsl.custom.PostRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    Page<Post> findAllByBlogId(Long blogId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tagMapList tm JOIN tm.tag t WHERE t.title = :tagName")
    Page<Post> findAllByTagName(@Param("tagName") String tagName, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p JOIN p.tagMapList tm JOIN tm.tag t WHERE t.title = :tagName AND p.blog.id = :blogId")
    Page<Post> findAllByTagNameAndBlogId(@Param("tagName") String tagName, @Param("blogId") Long blogId, Pageable pageable);

    // 좋아요 수 원자적 증가
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = COALESCE(p.likeCount, 0) + 1 WHERE p.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    // 좋아요 수 원자적 감소
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    // User의 Blog에 속한 모든 Post 삭제
    void deleteAllByBlogUserId(Long userId);
}