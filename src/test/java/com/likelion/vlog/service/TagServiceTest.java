package com.likelion.vlog.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TagServiceTest {


    @Autowired
    private TagService tagService;

    @Test
    void getTag() {
        var req = tagService.getTag("Java");
        System.out.println(req.getTitle());
        assertEquals("Java", req.getTitle());
    }
}