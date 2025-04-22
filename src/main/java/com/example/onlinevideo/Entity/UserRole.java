package com.example.onlinevideo.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {
    @Id
    private Integer userRoleId;
    private Integer userId;
    private Integer roleId;
}
