package com.buyticket.interceptor;

import com.buyticket.context.UserContext;
import com.buyticket.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT拦截器
 * 验证请求中的JWT token，并设置用户上下文
 */
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 允许OPTIONS请求通过（CORS预检）
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token
        if (token == null || !JwtUtils.isTokenValid(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":-2,\"msg\":\"未登录或token已过期\"}");
            return false;
        }

        // 解析token，设置用户上下文
        Claims claims = JwtUtils.getClaimsByToken(token);
        if (claims != null) {
            Long userId = Long.parseLong(claims.getSubject());
            String role = (String) claims.get("role");
            UserContext.setUserId(userId);
            UserContext.setRole(role);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户上下文
        UserContext.clear();
    }
}
