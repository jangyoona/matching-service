package com.boyug.security.jwt;

import com.boyug.dto.RoleDto;
import com.boyug.security.WebUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtUtil {

    private final Key key;
    private final Key refreshKey;
    private final long accessTokenExpTime;
    private final long refreshTokenExpTime;


    public JwtUtil(@Value("${jwt.secret}") String secretKey,
                   @Value("${jwt.refresh.secret}") String refreshSecretKey,
                   @Value("${jwt.expiration_time}") long accessTokenExpTime,
                   @Value("${jwt.refresh.expiration_time}") long refreshTokenExpTime) {
        byte[] keyByte = Decoders.BASE64.decode(secretKey);
        byte[] refreshKeyByte = Decoders.BASE64.decode(refreshSecretKey);
        this.key = Keys.hmacShaKeyFor(keyByte);
        this.refreshKey = Keys.hmacShaKeyFor(refreshKeyByte);

        this.accessTokenExpTime = accessTokenExpTime;
        this.refreshTokenExpTime = refreshTokenExpTime;
    }

    /*
     * Access Token 생성
     */
//    public String createAccessToken(WebUserDetails user) {
//        return createToken(user, accessTokenExpTime);
//    }

    public JwtInfoDto createToken(WebUserDetails user, String rememberMe) {

        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user, rememberMe);

        return JwtInfoDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .accessTokenExpireTime(accessTokenExpTime)
                .refreshToken(refreshToken)
                .refreshTokenExpireTime(refreshTokenExpTime)
                .build();
    }


    /*
     * Jwt Token 생성
     */
    protected String createAccessToken(WebUserDetails user) {
        Claims claims = Jwts.claims();
        claims.put("userId", user.getUser().getUserId());
        claims.put("userPhone", user.getUser().getUserPhone());
        claims.put("socialId", user.getUser().getSocialId());
        claims.put("userCategory", user.getUser().getUserCategory());
        claims.put("role", user.getAuthorities());

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(accessTokenExpTime);

        log.info("Token 발급일 ------> {}", now);
        log.info("Token 만료일 ------> {}", tokenValidity);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject("AccessToken")
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /*
     * Jwt Refresh Token 생성
     */
    protected String createRefreshToken(WebUserDetails user, String rememberMe) {

        // 자동로그인 체크시 Refresh Token 기간 30일로 설정
        long expTime = refreshTokenExpTime;
        if (rememberMe != null) {
            expTime = 60 * 60 * 24 * 30L;
        }

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(expTime);

        Claims claims = Jwts.claims();
        claims.put("userId", user.getUser().getUserId());
        claims.put("userPhone", user.getUser().getUserPhone());
        claims.put("socialId", user.getUser().getSocialId());
        claims.put("role", user.getAuthorities());
        claims.put("expiration", String.valueOf(tokenValidity));


        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject("RefreshToken")
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(refreshKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /*
     *  Token 안에서 User 정보 추출
     */
    public int getUserId(String token, String type) {
        return parseClaims(token, type).get("userId", Integer.class);
    }

//    public String getUserPhone(String token, String type) {
//        return parseClaims(token, type).get("userPhone", String.class);
//    }

    public String getSocialId(String token, String type) {
        return parseClaims(token, type).get("socialId", String.class);
    }

    public List<RoleDto> getUserRole(String token, String type) {
        return parseClaims(token, type).get("role", List.class);
    }

    public String getExpiration(String token, String type) {
        return parseClaims(token, type).get("expiration", String.class);
    }

    /*
     * JWT 검증
     */
    public boolean validateToken(String token, String type) {
        Key resultKey = type.equals("access") ? key : refreshKey;
        try {
            Jwts.parserBuilder().setSigningKey(resultKey).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
//            log.info("Expired JWT Token", e);
            log.info("Expired JWT Token");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    /*
     * JWT Claims 추출
     */
    public Claims parseClaims(String token, String type) {
        Key resultKey = type.equals("access") ? key : refreshKey;
        try {
            return Jwts.parserBuilder().setSigningKey(resultKey).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

}
