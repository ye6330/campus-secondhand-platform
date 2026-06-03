package com.campus.secondhand.common.security.jwt;

import com.campus.secondhand.common.core.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// JWT token 的生成和解析工具
public class JwtTokenService {

    // 加密密钥（用于签名和验签）
    private final String secret;
    // token 过期时间（毫秒）
    private final long expirationMillis;

    public JwtTokenService(String secret, long expirationMillis) {
        this.secret = secret;
        this.expirationMillis = expirationMillis;
    }

    // 生成 JWT token（登录成功后调用）
    // token 里包含 userId、username、role，以后请求时靠 token 识别用户
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.USER_ID_CLAIM, userId);
        claims.put(SecurityConstants.USERNAME_CLAIM, username);
        claims.put(SecurityConstants.ROLE_CLAIM, role);
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(SignatureAlgorithm.HS512, secret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    // 解析 JWT token，取出里面存的信息（JwtAuthenticationFilter 中调用）
    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                .parseClaimsJws(token)
                .getBody();
    }
}
