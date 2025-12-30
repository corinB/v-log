package com.likelion.vlog.repository.querydsl.custom;

import com.likelion.vlog.dto.posts.PostGetRequest;
import com.likelion.vlog.entity.Post;
import com.likelion.vlog.entity.QPost;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Post> search(PostGetRequest request) {
        QPost post = QPost.post;

        int page = request.getPage();
        int size = request.getSize();

        //검색
        var query = jpaQueryFactory
                .selectFrom(post)
                .distinct()
                .where(post.search(request));

        //정렬
        OrderSpecifier<?> order = post.sort(request);
        query.orderBy(order, post.id.desc());

        //결과
        List<Post> content = query
                .offset((long) page * size)
                .limit(size)
                .fetch();

        // 페이징을 위해 총 조회건수
        Long total = jpaQueryFactory
                .select(post.id.countDistinct())
                .from(post)
                .where(post.search(request))
                .fetchOne();

        total = total == null ? 0L : total;

        return new PageImpl<>(content, PageRequest.of(page, size), total);
    }
}