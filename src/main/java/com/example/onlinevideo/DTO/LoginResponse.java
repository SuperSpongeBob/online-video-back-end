package com.example.onlinevideo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private String token;
    private Integer userId;
    private String userName;
    private String userPhone;
    private String userGender;
    private String userEmail;
    private long userAddTime;
    private List<String> roles;

    public LoginResponse(boolean success,String message) {
        this.success = success;
        this.message = message;
    }
}