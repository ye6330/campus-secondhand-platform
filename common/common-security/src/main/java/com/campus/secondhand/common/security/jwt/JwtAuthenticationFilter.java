package com.campus.secondhand.common.security.jwt;

import com.campus.secondhand.common.core.constants.SecurityConstants;
import com.campus.secondhand.common.security.context.UserContext;
import io.jsonwebtoken.Claims;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

// JWT 过滤器：每次请求都经过这里，解析 token 并识别当前用户
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. 从请求头里取 Authorization
            String authorization = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
            // 2. 如果 Authorization 不为空，且以 "Bearer " 开头，说明带了 token
            if (StringUtils.hasText(authorization) && authorization.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                // 3. 去掉 "Bearer " 前缀，拿到真正的 token
                String token = authorization.substring(SecurityConstants.TOKEN_PREFIX.length());
                // 4. 解析 token，取出里面存的 userId、username、role
                Claims claims = jwtTokenService.parseToken(token);
                Long userId = claims.get(SecurityConstants.USER_ID_CLAIM, Long.class);
                String username = claims.get(SecurityConstants.USERNAME_CLAIM, String.class);
                String nickname = claims.get(SecurityConstants.NICKNAME_CLAIM, String.class);
                String role = claims.get(SecurityConstants.ROLE_CLAIM, String.class);

                // 5. 存入 UserContext（内部用 ThreadLocal），后续业务代码可以直接拿当前用户
                UserContext.setUser(userId, username, nickname, role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 6. 继续执行后面的过滤器或业务代码
            filterChain.doFilter(request, response);
        } finally {
            // 7. 请求结束后清理 ThreadLocal，防止内存泄漏或用户信息串到下一个请求
            UserContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
