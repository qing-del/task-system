package com.jacolp.task_system.filter;

import com.jacolp.task_system.utils.CurrentUserIdHolder;
import com.jacolp.task_system.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // Spring Security 的核心接口，用来加载用户详情
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 排除登录和注册的端点

            // 1. 获取请求头 Authorization
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String username;
            final Long userId;  // 后续加入

            // 2. 检查头格式：必须以 "Bearer " 开头
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("请求头不合法导致异常！");
                filterChain.doFilter(request, response); // 不符合格式直接放行（交给后续 SecurityConfig 拦截）
                return;
            }

            // 3. 提取 Token 和 用户名
            jwt = authHeader.substring(7); // 去掉 "Bearer "
            username = jwtUtil.extractUsername(jwt);

            // 提取 Token 中的用户id
            userId = jwtUtil.extractUserId(jwt);
            log.info("用户id：{}", userId);
            CurrentUserIdHolder.setCurrentUserId(userId);   // 后续记得清除防止内存泄漏

            // 4. 如果有用户名且当前未登录（SecurityContext 为空）
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 从数据库加载用户信息（这一步需要 UserDetailsService）
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 5. 验证 Token 有效性
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    log.info("Token is valid! -- √");

                    // 创建认证令牌
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 6. 将令牌放入 Security 上下文，表示“该用户已登录”
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // 7. 继续执行下一个过滤器
            filterChain.doFilter(request, response);

        } catch (ServletException | IOException e) {
            log.error("JWT 过滤器发生异常：{}", e.getMessage());
            throw e;
        } finally {
            // 8.清除线程变量
            CurrentUserIdHolder.clear();
        }
    }
}