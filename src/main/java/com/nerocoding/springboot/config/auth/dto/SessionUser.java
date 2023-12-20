package com.nerocoding.springboot.config.auth.dto;

import com.nerocoding.springboot.domain.user.User;
import lombok.Getter;

import java.io.Serializable;

/**
 * 세션 사용자 정보를 저장하기 위한 DTO 클래스
 * User 클래스를 쓰지 않고 새로 만들어서 사용하는 이유는?
 */
@Getter
public class SessionUser implements Serializable {
    private final String name;
    private final String email;
    private final String picture;

    public SessionUser(User user){
        this.name = user.getName();
        this.email = user.getEmail();
        this.picture = user.getPicture();
    }
}
