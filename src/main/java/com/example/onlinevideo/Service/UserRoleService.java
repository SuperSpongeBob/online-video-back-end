package com.example.onlinevideo.Service;

import com.example.onlinevideo.Mapper.UserRoleMapper;
import com.example.onlinevideo.Entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserRoleService {
    @Autowired
    private UserRoleMapper userRoleMapper;

    //  根据userId查找roleId
    public UserRole getRoleIdByUserId(Integer userId) {
        return userRoleMapper.getRoleIdByUserId(userId);
    }

    //  用户注册时根据userId与roleId关联权限
    public boolean addUserRole(UserRole userRole) {
        return userRoleMapper.addUserRole(userRole);
    }

    //  admin更改用户权限
    public boolean updateUserRole(UserRole userRole) {
        return userRoleMapper.updateUserRole(userRole);
    }
}
