package com.example.onlinevideo.Entity;

import com.example.onlinevideo.DTO.Page;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@Builder
@Component
@Entity(name = "user")
@NoArgsConstructor  // 无参构造器
@AllArgsConstructor  // 全参构造器
public class User extends Page implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "userId")
//    @Indexed
    private Integer userId;
    private String userName;
    private String userPassword;
//    private Integer userIsVip;  //  即将弃用，identity代替
    private Integer identity;
    private String userNewPassword;
//    private String userNickname;
    private String userGender;
    private String userPhone;
//    private String userIdcard;
    private String userEmail;
//    private Integer userCredibility;
    private long userAddTime;

//    private String userLoginIp;
//    private String userLoginStatu;
//    private Integer userMoney;

}
