package com.likelion.vlog.config;

import com.likelion.vlog.enums.SearchFiled;
import com.likelion.vlog.enums.SortField;
import com.likelion.vlog.enums.TagMode;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class appConfig implements WebMvcConfigurer {
    private final EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory(){
        return new JPAQueryFactory(entityManager);
    }

    @Override //enum 설정
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, SortField.class, SortField::from);
        registry.addConverter(String.class, SearchFiled.class, SearchFiled::from);
        registry.addConverter(String.class, TagMode.class, TagMode::from);
    }
}
