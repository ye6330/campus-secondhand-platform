package com.campus.secondhand.user.service;

import com.campus.secondhand.common.core.result.ApiResponse;
import com.campus.secondhand.user.dto.LoginRequest;
import com.campus.secondhand.user.dto.RegisterRequest;
import com.campus.secondhand.user.dto.UpdatePasswordRequest;
import com.campus.secondhand.user.dto.UpdateProfileRequest;
import java.util.Map;

public interface UserService {
    ApiResponse<Map<String, String>> captcha();
    ApiResponse<Map<String, Object>> register(RegisterRequest request);
    ApiResponse<Map<String, Object>> login(LoginRequest request);
    ApiResponse<Map<String, Object>> currentUser();
    ApiResponse<String> updateAvatar(String avatarUrl);
    ApiResponse<Map<String, Object>> updateProfile(UpdateProfileRequest request);
    ApiResponse<Void> updatePassword(UpdatePasswordRequest request);
    ApiResponse<Map<String, Object>> contact(Long id);
}
