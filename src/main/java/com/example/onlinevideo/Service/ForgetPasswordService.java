package com.example.onlinevideo.Service;

import com.example.onlinevideo.Constant.HttpStatusEnum;
import com.example.onlinevideo.Constant.RedisConstant;
import com.example.onlinevideo.DTO.ForgetPasswordRequest;
import com.example.onlinevideo.DTO.UserDTO;
import com.example.onlinevideo.Entity.User;
import com.example.onlinevideo.Mapper.UserMapper;
import com.example.onlinevideo.Utils.StringUtil;
import com.example.onlinevideo.Vo.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class ForgetPasswordService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ThreadService threadService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public R sendVerificationCode(String userPhone, String userEmail){
        //  邮箱格式验证
        if (!StringUtil.checkEmail(userEmail)){
            return R.error(HttpStatusEnum.EMAIL_FORMAT_ERROR);
        }
        //  验证邮箱与手机号是否匹配
        User user = userMapper.getUserByUserPhone(userPhone);
        if (user == null){
            return R.error(HttpStatusEnum.USER_NOT_EXIST);
        }
        if (!userEmail.equals(user.getUserEmail())){
            return R.error(HttpStatusEnum.EMAIL_AND_PHONE_MATCH);
        }

        //  生成 6 位数验证码
        String code = StringUtil.randomSixCode();

        // 发送邮件
        String content = "亲爱的用户：\n" +
                "您此次的验证码为：\n\n" +
                code + "\n\n" +
                "此验证码5分钟内有效，请立即进行下一步操作。 如非你本人操作，请忽略此邮件。\n" +
                "感谢您的使用！";

        //  开辟线程去执行邮箱发送操作，不占用主线程运行
        threadService.sendSimpleMail(userEmail, "雄雄同学还小",content);

        //  将验证码存储到 Redis，设置 5 分钟后过期
        String key = RedisConstant.FORGET_PWD_CODE+userPhone;
        redisTemplate.opsForValue().set(key,code,RedisConstant.EXPIRE_FIVE_MINUTES, TimeUnit.SECONDS);

        return R.ok().message("验证码已发送");
    }

    /**
     * 重置验证码
     * @Param request
     */
    public R resetPassword(ForgetPasswordRequest request){
        //  验证验证码是否正确
        String key = RedisConstant.FORGET_PWD_CODE+request.getUserPhone();
        String correctCode = redisTemplate.opsForValue().get(key);

        //  验证码已过期
        if (correctCode == null){
            return R.error(HttpStatusEnum.CODE_EXPIRED_ERROR);
        }

        //  验证码错误
        if (!correctCode.equals(request.getCode())){
            return R.error(HttpStatusEnum.CODE_ERROR);
        }

        //  验证密码格式
        if (!StringUtil.checkPassword(request.getNewPassword())){
            return R.error(HttpStatusEnum.PASSWORD_FORMAT_ERROR);
        }

        //  验证确认密码是否相同
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            return R.error(HttpStatusEnum.PASSWORD_INCONSISTENT);
        }

        //  验证手机号是否存在
        User user = userMapper.getUserByUserPhone(request.getUserPhone());
        if (user == null){
            return R.error(HttpStatusEnum.USER_NOT_EXIST);
        }

        //  验证手机号与邮箱是否匹配
        if (!request.getUserEmail().equals(user.getUserEmail())){
            return R.error(HttpStatusEnum.EMAIL_AND_PHONE_MATCH);
        }

        //  更新密码
        String encodedPassword = new BCryptPasswordEncoder().encode(request.getNewPassword());
        userMapper.updateUserPassword(
                UserDTO.builder()
                .userId(user.getUserId())
                .userNewPassword(encodedPassword)
                .build());
//        userMapper.updateUserPassword(new User());

        //  删除 Redis 验证码
        redisTemplate.delete(key);

        return R.ok().message("Password reset succeeded");
    }
}
