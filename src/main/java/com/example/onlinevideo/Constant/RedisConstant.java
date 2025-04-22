package com.example.onlinevideo.Constant;

public interface RedisConstant {
    //  忘记密码相关的key
    String FORGET_PWD_CODE = "forget_pwd_code";     //  忘记密码验证码前缀

    //  缓存时间
    int EXPIRE_ONE_MINUTE = 60; //  1分钟
    int EXPIRE_FIVE_MINUTES = 5 * 60;
}
