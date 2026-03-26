package edu.ruc.platform.auth.controller;

import edu.ruc.platform.auth.dto.ChangePasswordRequest;
import edu.ruc.platform.auth.dto.ChangePasswordResponse;
import edu.ruc.platform.auth.dto.LoginRequest;
import edu.ruc.platform.auth.dto.LoginResponse;
import edu.ruc.platform.auth.dto.LogoutResponse;
import edu.ruc.platform.auth.dto.UserProfileResponse;
import edu.ruc.platform.auth.dto.WechatLoginRequest;
import edu.ruc.platform.auth.service.AuthApplicationService;
import edu.ruc.platform.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthApplicationService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("登录成功", authService.login(request));
    }

    @PostMapping("/wechat-login")
    public ApiResponse<LoginResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return ApiResponse.success("微信登录成功", authService.wechatLogin(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.success(authService.currentUser());
    }

    @PostMapping("/change-password")
    public ApiResponse<ChangePasswordResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ApiResponse.success("密码修改成功", authService.changePassword(request));
    }

    @PostMapping("/logout")
    public ApiResponse<LogoutResponse> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String token = authorization != null && authorization.startsWith("Bearer ") ? authorization.substring(7) : null;
        return ApiResponse.success("退出登录成功", authService.logout(token));
    }
}
