package com.likelion.vlog.repository.querydsl.custom;

import com.likelion.vlog.dto.posts.PostGetRequest;
import com.likelion.vlog.entity.Post;
import org.springframework.data.domain.Page;

public interface PostRepositoryCustom {
    Page<Post> search(PostGetRequest request);
}
