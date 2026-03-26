package edu.ruc.platform.auth.service;

import edu.ruc.platform.auth.dto.ChangePasswordRequest;
import edu.ruc.platform.auth.dto.ChangePasswordResponse;
import edu.ruc.platform.auth.dto.LoginRequest;
import edu.ruc.platform.auth.dto.LoginResponse;
import edu.ruc.platform.auth.dto.LogoutResponse;
import edu.ruc.platform.auth.dto.UserProfileResponse;
import edu.ruc.platform.auth.dto.WechatLoginRequest;

public interface AuthApplicationService {

    LoginResponse login(LoginRequest request);

    LoginResponse wechatLogin(WechatLoginRequest request);

    UserProfileResponse currentUser();

    ChangePasswordResponse changePassword(ChangePasswordRequest request);

    LogoutResponse logout(String token);
}
