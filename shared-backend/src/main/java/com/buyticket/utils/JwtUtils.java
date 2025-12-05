package com.buyticket.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.buyticket.entity.SysUser;
import com.buyticket.entity.AdminUser;

import java.util.Date;

public class JwtUtils {

    // 过期时间 7 天
    private static final long EXPIRE = 604800;
    // 密钥
    private static final String SECRET = "BuyTicketSecretKey88888888";

    /**
     * 生成 C端用户 token
     */
    public static String generateToken(SysUser user) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000 * EXPIRE);
        return Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setSubject(String.valueOf(user.getId()))
                .claim("username", user.getUsername())
                .claim("role", "user")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    /**
     * 生成 管理员 token
     */
    public static String generateAdminToken(AdminUser admin) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 1000 * EXPIRE);
        return Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setSubject(String.valueOf(admin.getId()))
                .claim("username", admin.getUsername())
                .claim("role", "admin")
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    /**
     * 解析 token
     */
    public static Claims getClaimsByToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
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
}
