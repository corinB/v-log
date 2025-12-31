package com.likelion.vlog.service;

import com.likelion.vlog.dto.like.LikeResponse;
import com.likelion.vlog.entity.Like;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.User;
import com.likelion.vlog.exception.DuplicateException;
import com.likelion.vlog.exception.NotFoundException;
import com.likelion.vlog.repository.LikeRepository;
import com.likelion.vlog.repository.PostRepository;
import com.likelion.vlog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 좋아요 추가
    public LikeResponse addLike(String email, Long postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> NotFoundException.user(email));

        // 중복 체크
        if (likeRepository.existsByUserIdAndPostId(user.getId(), postId)) {
            throw DuplicateException.like();
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> NotFoundException.post(postId));

        Like like = Like.from(user, post);
        likeRepository.save(like);

        // DB 원자적 연산으로 좋아요 수 증가
        postRepository.incrementLikeCount(postId);

        // 갱신된 Post 조회하여 likeCount 반환
        Post updatedPost = postRepository.findById(postId)
                .orElseThrow(() -> NotFoundException.post(postId));

        return LikeResponse.from(updatedPost.getLikeCount(),true);
    }

    // 좋아요 삭제
    public LikeResponse removeLike(String email, Long postId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> NotFoundException.user(email));

        Like like = likeRepository.findByUserIdAndPostId(user.getId(), postId)
                .orElseThrow(NotFoundException::like);

        likeRepository.delete(like);

        // DB 원자적 연산으로 좋아요 수 감소
        postRepository.decrementLikeCount(postId);

        // 갱신된 Post 조회하여 likeCount 반환
        Post updatedPost = postRepository.findById(postId)
                .orElseThrow(() -> NotFoundException.post(postId));

        return LikeResponse.from(updatedPost.getLikeCount(), false);
    }

    // 좋아요 정보 조회 (로그인 / 비로그인 모두 허용)
    @Transactional(readOnly = true)
    public LikeResponse getLikeInfo(String email, Long postId) {

        // 1. 전체 좋아요 수 (항상 조회)
        Integer count = postRepository.findById(postId)
                .map(Post::getLikeCount)
                .orElseThrow(() -> NotFoundException.post(postId));

        // 2. 비로그인 사용자
        if (email == null) {
            return LikeResponse.from(count, false);
        }

        // 3. 로그인 사용자
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> NotFoundException.user(email));


        boolean checkLike = likeRepository.existsByUserIdAndPostId(user.getId(), postId);
        return LikeResponse.from(count, checkLike);
    }
}