package com.example.onlinevideo.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer userId;
    private String userName;
    private String userPassword;
    private Integer identity;
    private String userNewPassword;
    private String userGender;
    private String userPhone;
    private String userEmail;
    private long userAddTime;
    private Integer roleId;
}
