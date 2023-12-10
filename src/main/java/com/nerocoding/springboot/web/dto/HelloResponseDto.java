package com.nerocoding.springboot.web.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor        // 선언된 모든 final 필드가 포함된 생성자 생성 (final 없는 필드는 생성자 포함 X)
public class HelloResponseDto {
    private final String name;
    private final int amount;
}
