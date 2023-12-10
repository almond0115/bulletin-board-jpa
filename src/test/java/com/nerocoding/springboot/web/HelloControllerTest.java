package com.nerocoding.springboot.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HelloController.class)
public class HelloControllerTest {
    @Autowired
    private MockMvc mvc; // HTTP GET, POST 등 대한 API 웹 테스트 시작점

    @Test
    public void hello가_리턴된다() throws Exception {
        String hello = "hello";

        mvc.perform(get("/hello"))
                .andExpect(status().isOk())             // mvc.perform 결과 검증 : HTTP Header Status 검증 (여기서는 200 인지 아닌지)
                .andExpect(content().string(hello));    // mvc.perform 결과 검증 : Controller 에서 "hello" 리턴 값 맞는지 검증
    }
}
