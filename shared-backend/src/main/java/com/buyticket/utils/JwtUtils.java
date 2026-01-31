package com.buyticket.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.buyticket.entity.SysUser;
import com.buyticket.entity.AdminUser;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT工具类 - 使用JJWT 0.12.x
 */
public class JwtUtils {

    // 过期时间 7 天（秒）
    private static final long EXPIRE = 604800;
    
    // 密钥字符串（至少32字节用于HS256）
    private static final String SECRET_STRING = "BuyTicketSecretKey88888888BuyTicketSecretKey88888888";
    
    // 生成密钥对象（使用HMAC-SHA算法）
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    /**
     * 生成 C端用户 token
     */
    public static String generateToken(SysUser user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000 * EXPIRE);
        
        return Jwts.builder()
                .header()
                    .add("typ", "JWT")
                    .and()
                .subject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", "user")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 生成 管理员 token
     */
    public static String generateAdminToken(AdminUser admin) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000 * EXPIRE);
        
        return Jwts.builder()
                .header()
                    .add("typ", "JWT")
                    .and()
                .subject(String.valueOf(admin.getId()))
                .claim("username", admin.getUsername())
                .claim("role", "admin")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 解析 token
     */
    public static Claims getClaimsByToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            // Token无效、过期或签名错误
            return null;
        }
    }
    
    /**
     * 从 Token 获取用户 ID
     */
    public static Long getUserIdByToken(String token) {
        Claims claims = getClaimsByToken(token);
        if (claims != null && "user".equals(claims.get("role"))) {
            return Long.parseLong(claims.getSubject());
        }
        return null;
    }
    
    /**
     * 从 Token 获取 Admin ID
     */
    public static Long getAdminIdByToken(String token) {
        Claims claims = getClaimsByToken(token);
        if (claims != null && "admin".equals(claims.get("role"))) {
            return Long.parseLong(claims.getSubject());
        }
        return null;
    }
    
    /**
     * 验证 token 是否有效
     */
    public static boolean isTokenValid(String token) {
        Claims claims = getClaimsByToken(token);
        if (claims == null) {
            return false;
        }
        // 检查是否过期
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.after(new Date());
    }
    
    /**
     * 从 token 获取角色
     */
    public static String getRoleByToken(String token) {
        Claims claims = getClaimsByToken(token);
        if (claims != null) {
            return (String) claims.get("role");
        }
        return null;
    }
}
