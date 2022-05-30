package com.example.security.controller;

import com.example.security.dto.Token;
import com.example.security.model.User;
import com.example.security.model.UserRepository;
import com.example.security.security.JwtTokenProvider;
import com.example.security.service.JwtService;
import com.example.security.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.example.security.model.Role.ROLE_USER;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserService userService;

    // 회원 가입 (테스트용)
    @PostMapping("/join")
    public String join(@RequestBody Map<String, String> user) {
        User newUser = userRepository.save(User.builder()
                        .userId(user.get("user_id"))
                        .password(passwordEncoder.encode(user.get("password")))
                        .name(user.get("name"))
                        .role(ROLE_USER) // 최초 가입시 USER 로 설정
                        .build());

        return newUser.toString();
    }

    // 로그인
    @PostMapping("/login")
    public Token login(@RequestBody Map<String, String> user) {
        log.info("user id = {}", user.get("id"));
        User member = userRepository.findByUserId(user.get("user_id"))
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사번 입니다."));
        if (!passwordEncoder.matches(user.get("password"), member.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }

        Token tokenDto = jwtTokenProvider.createAccessToken(member.getUsername(), member.getRole());
        jwtService.login(tokenDto);

        return tokenDto;
    }

    //Token test
    @RestController
    public class TestController {

        @PostMapping("/test")
        public String test(){
            return "<h1>test 통과</h1>";
        }
    }
}