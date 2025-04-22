package com.example.onlinevideo.Service;

import com.example.onlinevideo.Constant.HttpStatusEnum;
import com.example.onlinevideo.Constant.RedisConstant;
import com.example.onlinevideo.DTO.LoginRequest;
import com.example.onlinevideo.DTO.LoginResponse;
import com.example.onlinevideo.DTO.RegisterRequest;
import com.example.onlinevideo.Entity.User;
import com.example.onlinevideo.Entity.Role;
import com.example.onlinevideo.Mapper.AuthMapper;
import com.example.onlinevideo.Mapper.UserMapper;
import com.example.onlinevideo.Security.CustomUserDetails;
import com.example.onlinevideo.Security.JwtTokenProvider;
import com.example.onlinevideo.Utils.StringUtil;
import com.example.onlinevideo.Vo.R;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthMapperImpl implements AuthMapper {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final ThreadService threadService;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserService userService;
    private final UserRoleService userRoleService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        //  获取账号信息
        User user = userMapper.getUserByUserPhone(loginRequest.getUserPhone());

        //  判断用户是否存在
        if (user == null) {
            return new LoginResponse(false,"账号不存在");
        }

        try {
            //  用户登录认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUserPhone(),
                            loginRequest.getUserPassword()
                    )
            );

            //  getContext  获取当前线程的安全上下文实例
            //  setAuthentication将已经认证的Authentication对象设置到安全上下文中
            SecurityContextHolder.getContext().setAuthentication(authentication);

            //  生成token
            String token = jwtTokenProvider.generateToken((CustomUserDetails) authentication.getPrincipal());

            //  获取用户信息并返回给用户
            List<Role> roles = userMapper.findRolesByUserId(user.getUserId());

            //  登录成功后返回用户信息
            return new LoginResponse(
                    true,
                    "Login Success",
                    token,
                    user.getUserId(),
                    user.getUserName(),
                    user.getUserPhone(),
                    user.getUserGender(),
                    user.getUserEmail(),
                    user.getUserAddTime(),
                    roles.stream().map(Role::getRole).collect(Collectors.toList())
            );
        } catch (AuthenticationException e) {
            return new LoginResponse(false, "账号或密码错误");
        }
    }

    @Override
    public R sendRegisterVerificationCode(String userPhone, String userEmail) {
        //  邮箱格式验证
        if (!StringUtil.checkEmail(userEmail)) {
            return R.error(HttpStatusEnum.EMAIL_FORMAT_ERROR);
        }

        //  验证手机号是否已经存在
        if (userMapper.ExistsPhone(userPhone)){
            return R.error(HttpStatusEnum.USER_ALREADY_EXIST);
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
        threadService.sendSimpleMail(userEmail, "雄雄同学还小", content);

        //  将验证码存储到 Redis，设置 5 分钟后过期
        String key = RedisConstant.FORGET_PWD_CODE + userPhone;
        redisTemplate.opsForValue().set(key, code, RedisConstant.EXPIRE_FIVE_MINUTES, TimeUnit.SECONDS);

        return R.ok().message("验证码已发送");
    }

    @Override
    public R register(RegisterRequest request) {
        //  从 Redis 中获取验证码
        String key = RedisConstant.FORGET_PWD_CODE + request.getUserPhone();
        String correctCode = redisTemplate.opsForValue().get(key);

        //  验证码已过期
        if (correctCode == null) {
            return R.error(HttpStatusEnum.CODE_EXPIRED_ERROR);
        }

        //  验证码错误
        if (!correctCode.equals(request.getCode())) {
            return R.error(HttpStatusEnum.CODE_ERROR);
        }

        //  邮箱格式验证
        if (!StringUtil.checkEmail(request.getUserEmail())) {
            return R.error(HttpStatusEnum.EMAIL_FORMAT_ERROR);
        }
        //  验证手机号是否已存在
        if (userMapper.ExistsPhone(request.getUserPhone())) {
            return R.error(HttpStatusEnum.USER_ALREADY_EXIST);
        }

        //  验证密码格式
        if (!StringUtil.checkPassword(request.getConfirmPassword())) {
            return R.error(HttpStatusEnum.PASSWORD_FORMAT_ERROR);
        }

        //  验证确认密码是否相同
        if (!Objects.equals(request.getUserPassword(), request.getConfirmPassword())) {
            return R.error(HttpStatusEnum.PASSWORD_INCONSISTENT);
        }

        //  注册用户，将用户信息插入数据库
        User user = User.builder()
                .userPhone(request.getUserPhone())
                .userPassword(request.getUserPassword())
                .userEmail(request.getUserEmail())
                .userGender(request.getUserGender())
                .userName(request.getUserName())
                .userAddTime(System.currentTimeMillis())
                .build();

        boolean result = userService.insertUserWithRole(user,1);    //  注册的用户默认权限为1
        if (result){
            return R.ok().message("注册成功");
        }
        return R.error(HttpStatusEnum.REGISTRATION_FAILED);
    }
}