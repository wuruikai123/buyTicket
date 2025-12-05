package com.buyticket.interceptor;

import com.buyticket.utils.JsonData;
import com.buyticket.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.PrintWriter;

public class AdminLoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        String token = request.getHeader("Authorization");
        if (token == null) {
            token = request.getParameter("token");
        }

        if (token != null) {
            Claims claims = JwtUtils.getClaimsByToken(token);
            if (claims != null) {
                // 检查角色是否为 admin
                String role = (String) claims.get("role");
                if ("admin".equals(role)) {
                    request.setAttribute("admin_id", claims.getSubject());
                    request.setAttribute("username", claims.get("username"));
                    return true;
                }
            }
        }

        sendJsonMessage(response, JsonData.buildError("管理员未登录或无权限", -2));
        return false;
    }

    private void sendJsonMessage(HttpServletResponse response, Object obj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            response.setContentType("application/json; charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.print(objectMapper.writeValueAsString(obj));
            writer.close();
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
