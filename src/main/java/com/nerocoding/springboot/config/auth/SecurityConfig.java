package com.nerocoding.springboot.config.auth;

import com.nerocoding.springboot.domain.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/예외처리하고 싶은 url", "/예외처리하고 싶은 url");
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // h2-console 화면을 사용하기 위해 해당 옵션들 disable
                .csrf(AbstractHttpConfigurer::disable)
                .headers((headerConfig) -> headerConfig.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // 권한 관리 대상을 지정하는 option
                // permitAll() 으로 전체 열람 권한 부여
                // "/api/v1/**" 주소 API USER 권한만 사용 가능하도록 설정
                // anyRequest 는 설정된 값 이외 URL -> authenticated() 로 나머지 URL 모두 인증된 사옹자들에게만 허용
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/", "/login", "/css/**", "/images/**", "/js/**", "/h2-console/**", "/profile").permitAll()
                        .requestMatchers("/api/v1/**").hasRole(Role.USER.name())
                        .anyRequest().authenticated()
                )
                // logout 기능에 대한 여러 설정의 진입점
                // logout 성공 시 "/" 주소로 이동
                .logout((logoutConfig) ->
                        logoutConfig.logoutSuccessUrl("/")
                )
                // oauth2Login 은 OAuth 2 로그인 기능에 대한 여러 설정의 진입점
                // userInfoEndpoint 는 OAuth2 로그인 성공 이후 사용자 정보를 가져올 때 설정들 담당
                // userService 는 소셜 로그인 성공 시 후속 조치 진행할 UserService 인터페이스 구현체를 등록
                // -> 리소스 서버(소셜 서비스)에서 사용자 정보를 가져온 상태에서 추가로 진행하고자 하는 기능 명시 가능
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                );
        return http.build();
    }
}
