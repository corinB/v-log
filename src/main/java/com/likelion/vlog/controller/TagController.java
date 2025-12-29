package com.likelion.vlog.controller;

import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.tags.TagGetResponse;
import com.likelion.vlog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * tag API 컨트롤러
 * - Base URL: /api/v1/tags
 */
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private  final TagService tagService;

    @GetMapping("/{title}")
    public ResponseEntity<ApiResponse<TagGetResponse>> getTag(
            @PathVariable(name = "title") String title
            )
    {
        return ResponseEntity.ok(ApiResponse.success("태그 조회 성공", tagService.getTag(title)));
    }

}
