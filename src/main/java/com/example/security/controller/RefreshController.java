package com.example.security.controller;

import com.example.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RefreshController {

    private final JwtService jwtService;

    @PostMapping("/refresh")
    public String validateRefreshToken(@RequestBody HashMap<String, String> bodyJson){

        log.info("refresh controller 실행");
        Map<String, String> map = jwtService.validateRefreshToken(bodyJson.get("refresh_token"));

        if(map.get("status").equals("402")){
            log.info("RefreshController - Refresh Token이 만료.");
            return map.get("status");
        }

        log.info("RefreshController - Refresh Token이 유효.");
        return map.get("accessToken");

    }
}