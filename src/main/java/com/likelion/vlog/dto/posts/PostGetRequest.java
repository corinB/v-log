package com.likelion.vlog.dto.posts;

import com.likelion.vlog.enums.SearchFiled;
import com.likelion.vlog.enums.SortField;
import com.likelion.vlog.enums.TagMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostGetRequest {

    private Integer page = 0;
    private Integer size = 10;

    private Long blogId;
    private String keyword;

    // 요청: ...&tag=c&tag=spring
    private List<String> tag;

    private SearchFiled search = SearchFiled.TITLE;
    private TagMode tagMode = TagMode.OR;
    private SortField sort = SortField.CREATED_AT;
    private boolean asc;

    public void normalize() {
        if (tag == null) tag = List.of();
    }
}