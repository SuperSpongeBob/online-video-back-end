package com.example.onlinevideo.Mapper;

import com.example.onlinevideo.Entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleMapper {
    List<Role> getRole(@Param("roleId")Integer roleId);
}
