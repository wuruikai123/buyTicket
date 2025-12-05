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

public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 这是一个简单的登录拦截，检查 Authorization 头
        String token = request.getHeader("Authorization");
        if (token == null) {
            token = request.getParameter("token");
        }

        if (token != null) {
            Claims claims = JwtUtils.getClaimsByToken(token);
            if (claims != null) {
                // 登录成功，放行
                // 可以把 userId 放到 request 中供 Controller 使用
                request.setAttribute("user_id", claims.getSubject());
                request.setAttribute("username", claims.get("username"));
                return true;
            }
        }

        // 登录失败，响应 JSON
        sendJsonMessage(response, JsonData.buildError("请登录", -2));
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
