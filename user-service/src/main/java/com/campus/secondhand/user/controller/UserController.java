package com.campus.secondhand.user.controller;

import com.campus.secondhand.common.core.result.ApiResponse;
import com.campus.secondhand.user.dto.LoginRequest;
import com.campus.secondhand.user.dto.RegisterRequest;
import com.campus.secondhand.user.service.FileService;
import com.campus.secondhand.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// 用户接口入口，所有 /api/users 开头的请求先进这里
@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FileService fileService;

    public UserController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Validated @RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @ApiOperation("获取图片验证码")
    @GetMapping("/captcha")
    public ApiResponse<Map<String, String>> captcha() {
        return userService.captcha();
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Validated @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    // 获取当前登录用户信息，需要请求头带 Authorization: Bearer xxx
    @ApiOperation("上传头像")
    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String url = fileService.uploadAvatar(file);
        return userService.updateAvatar(url);
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> currentUser() {
        return userService.currentUser();
    }
}
