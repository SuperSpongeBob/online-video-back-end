package com.example.onlinevideo.Constant;

import lombok.Getter;

@Getter
public enum HttpStatusEnum {
    EMAIL_FORMAT_ERROR(4001, "邮箱格式不正确"),
    EMAIL_AND_PHONE_MATCH(4001,"邮箱和手机号不匹配"),
    PARAM_ERROR(4002, "参数格式不正确"),
    CODE_ERROR(4002, "验证码不正确"),
    CODE_EXPIRED_ERROR(4002,"验证码已过期"),
    PASSWORD_ERROR(4003, "密码错误"),
    PASSWORD_INCONSISTENT(4006, "密码不一致"),
    PASSWORD_FORMAT_ERROR(4003,"密码格式错误，密码必须为6-18位且包含字母"),
    USER_NOT_EXIST(4004, "用户不存在"),
    USER_ALREADY_EXIST(4004,"用户已存在"),
    REGISTRATION_FAILED(4004,"注册失败"),
    EMAIL_ALREADY_EXIST(4005, "邮箱已被注册"),


    PARAM_ILLEGAL(4007, "参数不合法"),

    INTERNAL_SERVER_ERROR(500, "服务器异常"),
    UNKNOWN_ERROR(66666, "未知异常, 联系管理员"),
    ILLEGAL_OPERATION(88888, "非法操作");

    private final int code;
    private final String msg;

    HttpStatusEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
