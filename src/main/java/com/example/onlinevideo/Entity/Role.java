package com.example.onlinevideo.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Role {
    @Id
    private Integer roleId;
    private String role;
}
