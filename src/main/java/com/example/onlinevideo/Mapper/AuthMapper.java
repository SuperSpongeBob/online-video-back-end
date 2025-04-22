package com.example.onlinevideo.Mapper;


import com.example.onlinevideo.DTO.LoginRequest;
import com.example.onlinevideo.DTO.LoginResponse;
import com.example.onlinevideo.DTO.RegisterRequest;
import com.example.onlinevideo.Vo.R;

public interface AuthMapper {
    LoginResponse login(LoginRequest loginRequest);

    R sendRegisterVerificationCode(String userPhone, String userEmail);

    R register(RegisterRequest registerRequest);
}
