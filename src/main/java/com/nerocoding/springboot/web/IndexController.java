package com.nerocoding.springboot.web;

import com.nerocoding.springboot.config.auth.dto.SessionUser;
import com.nerocoding.springboot.service.posts.PostsService;
import com.nerocoding.springboot.web.dto.PostsResponseDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RequiredArgsConstructor
@Controller
public class IndexController {
    private final PostsService postsService;
    private final HttpSession httpSession;

    /**
     * Model : 서버 템플릿 엔진에서 사용할 수 있는 객체를 저장할 수 있음
     * postsService.findAllDesc()로 가져온 결과를 index.mustache posts 로 전달
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("posts", postsService.findAllDesc());
        // CustomOAuth2UserService 로그인 성공 시 세션에 SessionUser 를 저장하도록 구성
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        // 세션 저장 값이 있을 때에만 model 에 userName 으로 등록
        if(user != null) {
            model.addAttribute("userName", user.getName());
        }
        return "index";
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }

    @GetMapping("/posts/update/{id}")
    public String postsUpdate(@PathVariable Long id, Model model){
        PostsResponseDto dto = postsService.findById(id);
        model.addAttribute("post", dto);

        return "posts-update";
    }
}
