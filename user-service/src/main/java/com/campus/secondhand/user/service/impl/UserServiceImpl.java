package com.campus.secondhand.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.secondhand.common.core.result.ApiResponse;
import com.campus.secondhand.common.security.context.UserContext;
import com.campus.secondhand.common.security.jwt.JwtTokenService;
import com.campus.secondhand.user.dto.LoginRequest;
import com.campus.secondhand.user.dto.RegisterRequest;
import com.campus.secondhand.user.dto.UpdatePasswordRequest;
import com.campus.secondhand.user.dto.UpdateProfileRequest;
import com.campus.secondhand.user.entity.User;
import com.campus.secondhand.user.mapper.UserMapper;
import com.campus.secondhand.user.service.CaptchaService;
import com.campus.secondhand.user.service.UserService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

// 用户业务逻辑：注册、登录、获取当前用户
@Service
public class UserServiceImpl implements UserService {

    // 操作数据库 user 表
    private final UserMapper userMapper;
    // 生成和解析 JWT token
    private final JwtTokenService jwtTokenService;
    private final CaptchaService captchaService;
    // Spring Security 提供的密码加密器
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, JwtTokenService jwtTokenService, CaptchaService captchaService) {
        this.userMapper = userMapper;
        this.jwtTokenService = jwtTokenService;
        this.captchaService = captchaService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public ApiResponse<Map<String, String>> captcha() {
        return ApiResponse.success(captchaService.generate());
    }

    // 用户注册
    @Override
    public ApiResponse<Map<String, Object>> register(RegisterRequest request) {
        if (!captchaService.verify(request.getCaptchaKey(), request.getCaptchaCode())) {
            return ApiResponse.failed(400, "验证码错误或已过期");
        }
        // 1. 查数据库，看用户名是否已经存在
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (count > 0) {
            return ApiResponse.failed(400, "用户名已存在");
        }

        // 2. 创建新用户对象，密码用 BCrypt 加密后存库
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setRole("USER");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // 3. 返回注册成功和用户信息
        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("phone", user.getPhone());
        return ApiResponse.success(result);
    }

    // 用户登录
    @Override
    public ApiResponse<Map<String, Object>> login(LoginRequest request) {
        // 1. 按用户名查数据库
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (user == null) {
            return ApiResponse.failed(400, "用户名或密码错误");
        }
        // 2. 用 BCrypt 比对密码（不直接比对原文，因为密码是加密存库的）
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.failed(400, "用户名或密码错误");
        }
        // 3. 检查账号是否被禁用（status=0 表示禁用）
        if (user.getStatus() == 0) {
            return ApiResponse.failed(400, "账号已被禁用");
        }

        // 4. 生成 JWT token，里面包含 userId、username、nickname、role
        String token = jwtTokenService.generateToken(user.getId(), user.getUsername(), user.getNickname(), user.getRole());

        // 5. 返回 token 和用户信息给前端
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("role", user.getRole());
        result.put("avatar", user.getAvatar());
        result.put("phone", user.getPhone());
        return ApiResponse.success(result);
    }

    @Override
    public ApiResponse<String> updateAvatar(String avatarUrl) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.failed(401, "未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.failed(404, "用户不存在");
        }
        user.setAvatar(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.success(avatarUrl);
    }

    // 获取当前登录用户信息
    @Override
    public ApiResponse<Map<String, Object>> currentUser() {
        // 1. 从 ThreadLocal 里拿当前登录用户的 id（由 JwtAuthenticationFilter 解析 token 后存入）
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return ApiResponse.failed(401, "未登录或登录已失效");
        }

        // 2. 按用户 id 查数据库
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ApiResponse.failed(404, "用户不存在");
        }

        // 3. 返回用户完整信息
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("phone", user.getPhone());
        result.put("avatar", user.getAvatar());
        result.put("role", user.getRole());
        result.put("status", user.getStatus());
        return ApiResponse.success(result);
    }

    @Override
    public ApiResponse<Map<String, Object>> updateProfile(UpdateProfileRequest request) {
        User user = getCurrentUserEntity();
        user.setNickname(request.getNickname().trim());
        user.setPhone(request.getPhone() == null ? "" : request.getPhone().trim());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return currentUser();
    }

    @Override
    public ApiResponse<Void> updatePassword(UpdatePasswordRequest request) {
        User user = getCurrentUserEntity();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ApiResponse.failed(400, "原密码错误");
        }
        if (request.getOldPassword().equals(request.getNewPassword())) {
            return ApiResponse.failed(400, "新密码不能和原密码相同");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return ApiResponse.success(null);
    }

    @Override
    public ApiResponse<Map<String, Object>> contact(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return ApiResponse.failed(404, "用户不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("phone", user.getPhone());
        result.put("avatar", user.getAvatar());
        return ApiResponse.success(result);
    }

    private User getCurrentUserEntity() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return user;
    }
}
