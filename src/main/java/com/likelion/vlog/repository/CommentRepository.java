package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Comment;
import com.likelion.vlog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPostAndParentIsNull(Post post);

    int countByPost(Post post);

    void deleteAllByPostId(Long postId);

    // User가 작성한 모든 댓글 삭제
    void deleteAllByUserId(Long userId);

    // User의 Blog에 속한 Post들의 모든 댓글 삭제
    void deleteAllByPostBlogUserId(Long userId);

    // N+1 해결: 여러 Post의 댓글 수를 한번에 조회
    @Query("SELECT c.post.id, COUNT(c) FROM Comment c WHERE c.post IN :posts GROUP BY c.post.id")
    List<Object[]> countByPosts(@Param("posts") List<Post> posts);

    // 게시글의 댓글과 대댓글을 함께 조회 (N+1 해결)
    @Query("SELECT DISTINCT c FROM Comment c " +
            "LEFT JOIN FETCH c.user " +
            "LEFT JOIN FETCH c.children ch " +
            "LEFT JOIN FETCH ch.user " +
            "WHERE c.post = :post AND c.parent IS NULL " +
            "ORDER BY c.createdAt ASC")
    List<Comment> findAllByPostWithChildren(@Param("post") Post post);
}