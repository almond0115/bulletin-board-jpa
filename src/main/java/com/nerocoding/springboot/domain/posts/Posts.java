package com.nerocoding.springboot.domain.posts;

import com.nerocoding.springboot.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 실제 DB 클래스와 매칭될 클래스
 */
@Getter
@NoArgsConstructor
@Entity
public class Posts extends BaseTimeEntity {
    @Id                                                     // 해당 테이블의 PK 필드
    @GeneratedValue(strategy = GenerationType.IDENTITY)     // PK 생성 규칙 (IDENTITY 옵션 : auto_increment)
    private Long id;

    @Column(length = 500, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    private String author;

    @Builder
    public Posts(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    public void update(String title, String content){
        this.title = title;
        this.content = content;
    }
}
