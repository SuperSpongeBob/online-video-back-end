package com.example.onlinevideo.Service;

import com.example.onlinevideo.Constant.HttpStatusEnum;
import com.example.onlinevideo.DTO.UserDTO;
import com.example.onlinevideo.Entity.UserRole;
import com.example.onlinevideo.Entity.VideoAlbum;
import com.example.onlinevideo.Mapper.UserMapper;
import com.example.onlinevideo.Entity.User;
import com.example.onlinevideo.Utils.StringUtil;
import com.example.onlinevideo.Vo.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private VideoAlbumService videoAlbumService;
    @Autowired
    private UserRoleService userRoleService;

    //  获取用户信息
    public List<UserDTO> getUsers(User user, Long startTime, Long endTime) {
        return userMapper.getUsers(user, startTime, endTime);
    }

    //  验证手机号是否已经存在
    public boolean ExistsPhone(String Phone) {
        return userMapper.ExistsPhone(Phone);
    }

    //  管理员直接调用进行插入用户信息
    @Transactional
    public boolean insertUserWithRole(User user,Integer roleId){
        try {
            boolean existsPhone = userMapper.ExistsPhone(user.getUserPhone());
            if (!existsPhone && user.getUserPassword() != null && !user.getUserPassword().isEmpty()) {
                user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
                //  插入用户
                userMapper.insertUser(user);
                //  插入用户角色关联
                userRoleService.addUserRole(UserRole.builder().userId(user.getUserId()).roleId(roleId).build());
                return true;
            }
            return false;
        }catch (Exception e){
            //  操作失败，回滚事务
            return false;
        }
    }

    //更新用户信息
    public R updateUserInfo(User user) {
        User existingUser = userMapper.ExistPhoneByOtherId(user.getUserPhone(), user.getUserId());
        //  检查用户手机号是否存在
        if (existingUser != null) {
            return R.error(HttpStatusEnum.USER_ALREADY_EXIST);//手机号已存在,修改手机号失败
        }
        //  检查邮箱是否合法
        if (!StringUtil.checkEmail(user.getUserEmail())) {
            return R.error(HttpStatusEnum.EMAIL_FORMAT_ERROR);
        }
        boolean result = userMapper.updateUserInfo(user);

        return result ? R.ok().message("信息更新成功") : R.error(HttpStatusEnum.UNKNOWN_ERROR);//修改成功

    }

    //  admin更新用户信息，无需验证
    @Transactional
    public R updateUser(UserDTO user) {
        //  先判断用户手机号是否为null
        if (user.getUserPhone() == null || user.getUserPhone().isEmpty()) {
            return R.error().message("用户手机号不能够为null");
        }
        User existingUser = userMapper.ExistPhoneByOtherId(user.getUserPhone(), user.getUserId());
        if (existingUser != null) {
            return R.error(HttpStatusEnum.USER_ALREADY_EXIST);
        }
        if (user.getUserPassword() != null && !user.getUserPassword().isEmpty()) {
            user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
        }
        return userMapper.updateUser(user) ? R.ok().message("修改用户信息成功") : R.error(HttpStatusEnum.UNKNOWN_ERROR);
    }

    //  admin根据用户id删除用户
    public boolean deleteUserById(Integer userId) {
        videoAlbumService.deleteAlbum(VideoAlbum.builder().userId(userId).build());
        return userMapper.deleteUserById(userId);
    }

    //  用户更新密码
    public boolean updateUserPassword(String oldPwd, String newPwd, String userPhone) {
        //  通过用户名获取用户密码
        User userInfo = userMapper.getUserByUserPhone(userPhone);

        //  判断输入的旧密码是否正确
        if (passwordEncoder.matches(oldPwd, userInfo.getUserPassword())) {
            //  替换user Info中的旧密码换成新的重新插入数据库
            //  更新密码
            return userMapper.updateUserPassword(
                    UserDTO
                            .builder()
                            .userPhone(userPhone)
                            .userId(userInfo.getUserId())
                            .userNewPassword(passwordEncoder.encode(newPwd))
                            .build());
        }
        return false;
    }

    //  根据手机号获取用户信息
    public User getUserByUserPhone(String userPhone) {
        return userMapper.getUserByUserPhone(userPhone);
    }
}


















