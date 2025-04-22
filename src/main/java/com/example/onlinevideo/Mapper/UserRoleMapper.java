package com.example.onlinevideo.Mapper;

import com.example.onlinevideo.Entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserRoleMapper {
    //  根据userId查找roleId
    UserRole getRoleIdByUserId(@Param("userId") Integer userId);

    //  用户注册，将userId与roleId关联，绑定权限
    boolean addUserRole(UserRole userRole);

    //  admin更改用户权限
    boolean updateUserRole(UserRole userRole);
}
