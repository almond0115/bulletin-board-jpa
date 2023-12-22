package com.nerocoding.springboot.service.posts;

import com.nerocoding.springboot.domain.posts.Posts;
import com.nerocoding.springboot.domain.posts.PostsRepository;
import com.nerocoding.springboot.web.dto.PostsListResponseDto;
import com.nerocoding.springboot.web.dto.PostsResponseDto;
import com.nerocoding.springboot.web.dto.PostsSaveRequestDto;
import com.nerocoding.springboot.web.dto.PostsUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostsService {
    private final PostsRepository postsRepository;

    @Transactional
    public Long save(PostsSaveRequestDto requestDto){
        return postsRepository.save(requestDto.toEntity()).getId();
    }

    /**
     * 신기한 부분은 update 기능에 DB에 쿼리를 날리는 부분이 없습니다.
     * 이게 가능한 이유는 JPA 영속성 컨텍스트 때문입니다.
     * 영속성 컨텍스트란 엔티티를 영구 저장하는 환경입니다.
     * JPA 핵심 내용은 엔티티가 연속성 컨텍스트에 포함되어 있냐 아니냐로 갈립니다.
     * JPA 엔티티 매니저가 활성화 된 상태로 트랜잭션 안에서 DB 에서 데이터를 가져오면 이 데이터는 영속성 컨텍스트가 유지된 상태입니다.
     * 이 상태에서 데이터 값을 변경하면 트랜잭션 끝나는 시점에 해당 테이블에 변경분을 반영합니다.
     * 즉, Entity 객체의 값만 변경하면 별도로 Update 쿼리를 날릴 필요가 없습니다.
     * 이 개념을 더티 체킹이라고 합니다.
     */
    @Transactional
    public Long update(Long id, PostsUpdateRequestDto requestDto) {
        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id = " + id));

        posts.update(requestDto.getTitle(), requestDto.getContent());

        return id;
    }

    @Transactional(readOnly=true)
    public PostsResponseDto findById (Long id) {
        Posts entity = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id = " + id));

        return new PostsResponseDto(entity);
    }

    @Transactional(readOnly=true)
    public List<PostsListResponseDto> findAllDesc() {
        return postsRepository.findAllDesc().stream()
                .map(PostsListResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete (Long id){
        Posts posts = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 없습니다. id = " + id));

        // JpaRepository 에서 지원하는 delete 메소드를 활용
        postsRepository.delete(posts);
    }
}
