package com.likelion.vlog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "tag_maps",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_post_tag",
                        columnNames = {"post_id", "tag_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagMap extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_map_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    // TagMap 생성 메서드
    public static TagMap create(Post post, Tag tag) {
        TagMap tagMap = new TagMap();
        tagMap.post = post;
        tagMap.tag = tag;
        return tagMap;
    }
}
