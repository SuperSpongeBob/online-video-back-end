package com.example.onlinevideo.Mapper;

import com.example.onlinevideo.DTO.UserDTO;
import com.example.onlinevideo.Entity.Role;
import com.example.onlinevideo.Entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    //  用于admin的获取用户信息，按条件获取
//    List<User> getUsers(User user,Long startTime,Long endTime);
    List<UserDTO> getUsers(UserDTO userDTO,Long startTime,Long endTime);
    List<Role> findRolesByUserId(Integer userId);

    //  根据用户手机号获取用户信息
    User getUserByUserPhone(@Param("userPhone") String userPhone);

    //  根据视频id获取用户Id
    Integer getUserIdByVideoId(Integer videoId);

    //  根据电话号码查询该账号是否已经存在
    boolean ExistsPhone(@Param("userPhone") String userPhone);

    //  查找除本id之外的手机号是否存在
    User ExistPhoneByOtherId(@Param("userPhone") String userPhone,@Param("userId") Integer userId);

    //  账号不存在则将数据进行插入处理
    void insertUser(User user);

    //  更新用户信息，不能更新密码和用户注册时间
    boolean updateUserInfo(User user);

    //  admin更新用户信息
    boolean updateUser(UserDTO user);

    //  admin根据用户id删除用户
    boolean deleteUserById(Integer userId);

    //  更改用户密码
    boolean updateUserPassword(UserDTO user);
}
