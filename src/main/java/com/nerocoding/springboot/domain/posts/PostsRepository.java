package com.nerocoding.springboot.domain.posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/***
 * JpaRepository<Entity 클래스, PK 타입> : 기본 CRUD 메소드 자동 생성
 * `@Repository` 추가할 필요 없음
 * 주의할 점은 Entity 클래스와 기본 Entity Repository 함께 위치해야 함
 * 추후 프로젝트 확장으로 도메인 별 프로젝트 분리가 필요하면 이때 Entity 클래스와 기본 Repository 는 함께 움직여야 합니다.
 */
public interface PostsRepository extends JpaRepository<Posts, Long> {
    @Query("SELECT p FROM Posts p ORDER BY p.id DESC")
    List<Posts> findAllDesc();
}
