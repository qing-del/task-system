package com.jacolp.task_system.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {
    // 1. 定义加密秘钥（正常应该放到 application.yml）
    // 这里演示 采用自动生成
    //private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.ES256);

    // 上面的报错了，改进一下稳定版
    private static final String SECRET_STRING = "your-super-secret-key-life-game-system-2025-jacolp";
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // 2. Token 有效期：设置为 24 小时
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    /**
     * 生成 Token
     * @param username 用户名
     * @return 加密后的 Token 字符串
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)   // 设置主题 一般放置用户名
                .setIssuedAt(new Date())    // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // 过期时间
                .signWith(SECRET_KEY)   // 签名
                .compact();
    }

    /**
     * 从 Token 中提取用户名
     * @param token Token 字符串
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 验证 Token
     * @param token 需要验证的 Token
     * @param username 需要验证的 Token 的用户名
     * @return 是否有效
     */
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // --- 以下是内部辅助方法 ---

    /**
     * 解密 Token 中的所有字段（辅助方法）
     * @param token 需要解密的 Token
     * @return 解密后的含有所有字段 Token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 提取 Token 中的字段（辅助方法）
     * @param token 需要提取字段的 Token
     * @param claimsResolver 函数式接口
     * @return 提取的字段
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 验证 Token 是否有效
     * @param token 需要验证的 Token
     * @return 是否有效
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
