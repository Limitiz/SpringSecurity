package com.example.security.security;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;  ///  pom.xml에 라이브러리 추가

import com.example.security.model.RefreshToken;
import com.example.security.model.Role;
import com.example.security.model.Token;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@PropertySource("classpath:application.yml")
@RequiredArgsConstructor   // 필수 멤버변수만 갖는 생성자함수
@Component
public class JwtTokenProvider {

    private SecretKey secretKey;

    @Value("${jwt.secret}")
    private String secretMessage;
    @Value("${jwt.token.validation.access}")
    private long accessTokenValidTime;
    @Value("${jwt.token.validation.refresh}")
    private long refreshTokenValidTime;

    private UserDetailsService  userDetailsService;

    //객체 초기화 (secretKey encoding)
    @PostConstruct
    protected void init() {  // 객체 초기화
        secretKey = Keys.hmacShaKeyFor(secretMessage.getBytes());
        accessTokenValidTime *= 1000L;
        refreshTokenValidTime *= 1000L;
    }

    // JWT token create
    public Token createAccessToken(String userId, Role role) {

        Claims claims = Jwts.claims().setSubject(userId); // JWT payload에 저장되는 정보 단위
        claims.put("roles", role);  //
        Date now = new Date();

        String accessToken =  Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims) // 정보저장
                .setIssuedAt(now)  // 토큰 발행 시간
                .setExpiration(new Date(now.getTime() + accessTokenValidTime)) // 만료 시간 세팅
                .signWith(secretKey) // 암호화 알고리즘 + secretKey = signature
                .compact();

        String refreshToken = Jwts.builder()
                .setClaims(claims) // 정보저장
                .setIssuedAt(now)  // 토큰 발행 시간
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime)) // 만료 시간 세팅
                .signWith(secretKey) // 암호화 알고리즘 + secretKey = signature
                .compact();

        return Token.builder().accessToken(accessToken).refreshToken(refreshToken).key(userId).build();
    }

    public boolean validateToken(String jwtToken){
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // token 유효성 , 만료일자 확인
    public String validateRefreshToken(RefreshToken token){
        String refreshToken = token.getRefreshToken();

        try{
            //검증
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(refreshToken);

            //만료시간이 지나지 않았을 경우 access token 다시 발급
             if(!claims.getBody().getExpiration().before(new Date()))
                 return recreationAccessToken(claims.getBody().get("sub").toString(), claims.getBody().get("roles"));
        }catch (Exception e){
            //만료되었을 경우 로그인 필요
            return null;
        }
        return null;
    }

    //access token 다시 발급
    public String recreationAccessToken(String userId, Object role){
        Claims claims = Jwts.claims().setSubject(userId);
        claims.put("roles", role);
        Date now = new Date();

        String accessToken = Jwts.builder().setClaims(claims)
                .setHeaderParam("typ", "JWT")
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+accessTokenValidTime))
                .signWith(secretKey)
                .compact();

        return accessToken;
    }

    // JWT token에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserId(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // token에서 회원 정보 추출
    public String getUserId(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
    }

    // Request의 Header에서 token 값 가져오기    "Authorization" : "token value"
    public String resolveToken(HttpServletRequest request) {  // HttpServletRequest 라이브러리 추가할것
        return request.getHeader("Authorization");
    }
}