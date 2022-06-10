package com.example.security.controller;

import com.example.security.dto.LoginDto;
import com.example.security.dto.TokenDto;
import com.example.security.model.Role;
import com.example.security.model.User;
import com.example.security.model.UserRepository;
import com.example.security.security.JwtTokenProvider;
import com.example.security.service.JwtService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Slf4j
@PropertySource("classpath:application.yml")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    /*@Value("${jwt.token.validation.refresh}")
    private final int cookieAge;*/

    // 회원 가입 (테스트용)
    @PostMapping("/join")
    public String join(@RequestBody Map<String, String> user) {
        User newUser = userRepository.save(User.builder()
                        .userId(user.get("user_id"))
                        .password(passwordEncoder.encode(user.get("password")))
                        .name(user.get("name"))
                        .role(Role.valueOf(user.get("role")))
                        .build());

        return newUser.toString();
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto, HttpServletResponse response) throws JsonProcessingException {
        User user = userRepository.findByUserId(loginDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사번 입니다."));
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("잘못된 비밀번호입니다.");
        }
        TokenDto tokenDto = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRole());
        jwtService.login(tokenDto);

        Cookie cookie = new Cookie("refresh_token", tokenDto.getRefreshToken());
        cookie.setDomain("localhost:3000");
        cookie.setPath("/login");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);

        response.addCookie(cookie);

        return new ResponseEntity<>(tokenDto.getAccessToken(), HttpStatus.OK);
    }

    //Token test
    @RestController
    public class TestController {
        @PostMapping("/test")
        public HttpStatus test(){
            return HttpStatus.OK;
        }
    }

    //Role Test
    @RestController
    public class AdminTestController {
        @PostMapping("/admin/test")
        public HttpStatus test(){
            return HttpStatus.OK;
        }
    }

    @RestController
    public class UserTestController {
        @PostMapping("/user/test")
        public HttpStatus test(){
            return HttpStatus.OK;
        }
    }
}