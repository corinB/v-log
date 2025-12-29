package com.likelion.vlog.repository;

import com.likelion.vlog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByTitle(String title);

    boolean existsByTitle(String title);
}
