package com.likelion.vlog.controller;

import com.likelion.vlog.dto.common.ApiResponse;
import com.likelion.vlog.dto.tags.TagGetResponse;
import com.likelion.vlog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "태그", description = "태그 조회 API")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private  final TagService tagService;

    @Operation(summary = "태그 조회", description = "태그 이름으로 태그 정보 조회")
    @GetMapping("/{title}")
    public ResponseEntity<ApiResponse<TagGetResponse>> getTag(
            @PathVariable(name = "title") String title
            )
    {
        return ResponseEntity.ok(ApiResponse.success("태그 조회 성공", tagService.getTag(title)));
    }

}
