package com.example.security.security;

import com.example.security.dto.TokenDto;
import com.example.security.model.RefreshToken;
import com.example.security.model.Role;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;

public interface TokenProvider {

    public TokenDto createAccessToken(String userId, Role role);

    public boolean validateToken(String jwtToken);

    public String validateRefreshToken(RefreshToken token);

    public String recreationAccessToken(String userId, Object role);

    public Authentication getAuthentication(String token);

    public String getUserId(String token);

    public String resolveToken(HttpServletRequest request);

}
