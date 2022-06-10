package com.example.security.service;

import com.example.security.model.RefreshToken;
import com.example.security.dto.TokenDto;
import com.example.security.model.RefreshTokenRepository;
import com.example.security.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void login(TokenDto tokenDto){

        RefreshToken refreshToken = RefreshToken.builder().keyId(tokenDto.getKey()).refreshToken(tokenDto.getRefreshToken()).build();
        String userId = refreshToken.getKeyId();
        if(refreshTokenRepository.existsByKeyId(userId)){
            log.info("기존의 존재하는 refresh 토큰 삭제");
            refreshTokenRepository.deleteByKeyId(userId);
        }
        refreshTokenRepository.save(refreshToken);

    }

    public String validateRefreshToken(String refreshToken){
        RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken).get();
        if(token == null) return null;

        String createdAccessToken = jwtTokenProvider.validateRefreshToken(token);
        return createdAccessToken;
    }
}