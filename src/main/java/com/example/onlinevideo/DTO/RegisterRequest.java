package com.example.onlinevideo.DTO;

import lombok.Data;

@Data
public class RegisterRequest {
    private String userPhone;
    private String userEmail;
    private String userGender;
    private String userName;
    private String userPassword;
    private String confirmPassword;
    private Integer roleId;
    private String code;
}
