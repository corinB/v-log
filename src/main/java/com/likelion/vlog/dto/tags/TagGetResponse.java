package com.likelion.vlog.dto.tags;

import com.likelion.vlog.entity.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;



@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class TagGetResponse {
    private String title;

    public static TagGetResponse from(Tag tag) {
        if (tag == null) return null;
        return new TagGetResponse(tag.getTitle());
    }
}
