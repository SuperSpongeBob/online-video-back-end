package com.example.onlinevideo.Service;

import com.example.onlinevideo.Mapper.RoleMapper;
import com.example.onlinevideo.Entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {
    @Autowired
    private RoleMapper roleMapper;

    public List<Role> getRole(Integer roleId) {
        return roleMapper.getRole(roleId);
    }
}
