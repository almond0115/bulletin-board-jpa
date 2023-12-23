package com.nerocoding.springboot.config.auth.dto;

import com.nerocoding.springboot.domain.user.Role;
import com.nerocoding.springboot.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes<T> {
    private final Map<String, T> attributes;
    private final String nameAttributeKey;
    private final String name;
    private final String email;
    private final String picture;

    @Builder
    public OAuthAttributes(Map<String, T> attributes,
                           String nameAttributeKey,
                           String name,
                           String email,
                           String picture) {
        this.attributes = attributes;
        this.nameAttributeKey= nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }

    // OAuth2User 반환하는 사용자 정보는 Map 이므로 값 하나하나 변환해야 함
    public static <T> OAuthAttributes<T> of(String registrationId,
                                     String userNameAttributeName,
                                     Map<String, T> attributes) {
        if("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static <T> OAuthAttributes<T> ofGoogle(String userNameAttributeName,
                                            Map<String, T> attributes) {
        return OAuthAttributes.<T>builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static <T> OAuthAttributes<T> ofNaver(String userNameAttributeName,
                                           Map<String, T> attributes) {
        // 응답 받은 사용자의 정보를 Map 형태로 변경
        Map<String, T> response = (Map<String, T>) attributes.get("response");

        return OAuthAttributes.<T>builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image"))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    // OAuthAttribute 에서 엔티티 생성 시점은 처음 가입할 때
    // 가입 시 기본 권한을 role 빌더 통해 GUEST 설정
    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.GUEST)
                .build();
    }
}
