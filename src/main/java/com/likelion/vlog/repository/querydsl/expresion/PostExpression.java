package com.likelion.vlog.repository.querydsl.expresion;

import com.likelion.vlog.dto.posts.PostGetRequest;
import com.likelion.vlog.entity.*;
import com.likelion.vlog.enums.SearchFiled;


import com.likelion.vlog.enums.SortField;
import com.likelion.vlog.enums.TagMode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.annotations.QueryDelegate;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;

import java.util.List;
import java.util.Objects;


public class PostExpression {

    //태그리스트 전처리
    private static List<String> sanitizeTags(List<String> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();
    }


     // MySQL8: 공백무시 + 대소문자 무시
    private static BooleanExpression whitespaceIgnoreCase(StringExpression field, String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        // JAVA 공백 제거
        String normalized = keyword.trim().replaceAll("\\s+", "");
        // 문자열 반환 강제(CONCAT) + 공백클래스([[:space:]])
        StringExpression normalizedField = Expressions.stringTemplate(
                "CONCAT('', REGEXP_REPLACE({0}, '[[:space:]]+', ''))",
                field
        );
        return normalizedField.containsIgnoreCase(normalized);
    }

     //tags 목록에 있는 "모든 태그"를 포함하는 게시물만 통과 (더 많아도 OK)
    @QueryDelegate(Post.class)
    public static Predicate hasAllTags(QPost post, List<String> tags) {
        List<String> sanitized = sanitizeTags(tags);
        if (sanitized.isEmpty()) return null;

        QTagMap tagMap = QTagMap.tagMap;
        QTag tag = QTag.tag;

        var subQuery = JPAExpressions
                .select(tagMap.post.id)
                .from(tagMap)
                .join(tagMap.tag, tag)
                .where(tag.title.in(sanitized))
                .groupBy(tagMap.post.id)
                .having(tag.title.countDistinct().eq((long) sanitized.size()));

        return post.id.in(subQuery);
    }

     //게시물의 태그 중 하나라도 tags 목록에 포함되면 통과
    @QueryDelegate(Post.class)
    public static Predicate oneOfTags(QPost post, List<String> tags) {
        List<String> sanitized = sanitizeTags(tags);
        if (sanitized.isEmpty()) return null;

        return post.tagMapList.any().tag.title.in(sanitized);
    }


    //게시물의 태그 중 하나라도 tags 목록에 포함되면 불통
    @QueryDelegate(Post.class)
    public static Predicate hasNoting(QPost post, List<String> tags){
        List<String> sanitized = sanitizeTags(tags);
        if (sanitized.isEmpty()) return null;
        return post.tagMapList.any().tag.title.in(sanitized).not();
    }


    @QueryDelegate(Post.class)
    public static Predicate search(QPost post, PostGetRequest request) {

        Long blogId = request.getBlogId();
        String keyword = request.getKeyword();
        List<String> tags = request.getTag();
        SearchFiled search = request.getSearch();
        TagMode tagMode = request.getTagMode();

        BooleanBuilder builder = new BooleanBuilder();

        //플로그필터
        if (blogId != null && blogId > 0) {
            builder.and(post.blog.id.eq(blogId));
        }

        //키워드필터
        if (keyword != null && !keyword.isBlank()) {
            switch (search) {
                case BLOG -> builder.and(whitespaceIgnoreCase(post.blog.title, keyword));
                case NICKNAME -> builder.and(whitespaceIgnoreCase(post.blog.user.nickname, keyword));
                case TITLE -> builder.and(whitespaceIgnoreCase(post.title, keyword));
            }
        }

        //태그필터
        if (tags!=null && !tags.isEmpty()) {
            switch (tagMode) {
                case AND -> builder.and(post.hasAllTags(tags));
                case OR -> builder.and(post.oneOfTags(tags));
                case NAND ->  builder.and(post.hasNoting(tags));
            }
        }

        return builder;
    }


    @QueryDelegate(Post.class)
    public static OrderSpecifier<?> sort(QPost post, PostGetRequest request) {
        SortField sort = request.getSort();
        boolean asc = request.isAsc();

        return switch (sort) {
            case VIEW -> asc ? post.viewCount.asc() : post.viewCount.desc();
            case CREATED_AT -> asc ? post.createdAt.asc() : post.createdAt.desc();
            case UPDATED_AT -> asc ? post.updatedAt.asc() : post.updatedAt.desc();
            case LIKE -> asc ? post.likeCount.asc() : post.likeCount.desc();
        };
    }
}