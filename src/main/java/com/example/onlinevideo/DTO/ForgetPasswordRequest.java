package com.example.onlinevideo.DTO;

import lombok.Data;

@Data
public class ForgetPasswordRequest {
    private String userPhone;
    private String userEmail;
    private String code;
    private String newPassword;
    private String confirmPassword;
}
