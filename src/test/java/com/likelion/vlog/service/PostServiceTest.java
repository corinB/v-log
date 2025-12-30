package com.likelion.vlog.service;

import com.likelion.vlog.dto.posts.PostGetRequest;
import com.likelion.vlog.dto.posts.response.PostListResponse;
import com.likelion.vlog.enums.SearchFiled;
import com.likelion.vlog.enums.SortField;
import com.likelion.vlog.enums.TagMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.*;
import java.util.List;

@SpringBootTest
class PostServiceTest {

    @Autowired
    PostService postService;


    @Test
    void getPosts() {
        var req = new PostGetRequest();
        List<String> tags = List.of("java");
        req.setTag(tags);
        req.setSort(SortField.VIEW);
        req.setTagMode(TagMode.AND);
        var page = postService.getPosts(req);

        System.out.println("-------------------------------------------------------------------------");
        System.out.println(page.getPageInfo().getPage());
        System.out.println(page.getPageInfo().getSize());
        System.out.println(page.getPageInfo().getTotalElements());
        System.out.println(page.getPageInfo().getTotalPages());
        System.out.println("-------------------------------------------------------------------------\n\n");


        for (PostListResponse p : page.getContent()){
            System.out.println(p.getTitle());
            System.out.println(p.getViewCount());
            System.out.println("\n\n");
        }

    }
}