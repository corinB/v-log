package com.likelion.vlog.service;

import com.likelion.vlog.dto.tags.TagGetResponse;
import com.likelion.vlog.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public TagGetResponse getTag (String tagName){
        return TagGetResponse.from(tagRepository.findByTitle(tagName).orElse(null));
    }
}
