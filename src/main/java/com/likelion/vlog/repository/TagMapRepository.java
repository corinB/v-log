package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.TagMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagMapRepository extends JpaRepository<TagMap, Long> {

    List<TagMap> findAllByPost(Post post);

    @Modifying
    @Query("DELETE FROM TagMap tm WHERE tm.post = :post")
    void deleteAllByPost(@Param("post") Post post);

    // 게시글 ID로 태그 매핑 삭제
    void deleteAllByPostId(Long postId);

    // User의 Blog에 속한 Post들의 모든 태그 매핑 삭제
    void deleteAllByPostBlogUserId(Long userId);
}