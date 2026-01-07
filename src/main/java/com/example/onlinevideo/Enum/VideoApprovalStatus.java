package com.example.onlinevideo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 视频审核状态枚举
 * 对应数据库tinyint类型：0-待审核, 1-审核中, 2-审核通过, 3-禁播
 */
public enum VideoApprovalStatus {
    PENDING_REVIEW(0, "待审核"),
    UNDER_REVIEW(1, "审核中"),
    REVIEW_PASSED(2, "审核通过"),
    REVIEW_REJECTED(3, "禁播");

    private final int code;
    private final String description;

    VideoApprovalStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    /**
     * JSON序列化时返回中文描述
     */
    @JsonValue
    public String getDescription() {
        return description;
    }

    /**
     * 根据code获取枚举值
     */
    public static VideoApprovalStatus fromCode(int code) {
        for (VideoApprovalStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown VideoApprovalStatus code: " + code);
    }

    /**
     * 根据description获取枚举值（用于兼容旧代码）
     * JsonCreator注解用于JSON反序列化时，从字符串转换为枚举
     * 支持从description（中文）或枚举名称反序列化
     */
    @JsonCreator
    public static VideoApprovalStatus fromDescription(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        // 首先尝试通过description匹配
        for (VideoApprovalStatus status : values()) {
            if (status.description.equals(value)) {
                return status;
            }
        }
        
        // 然后尝试通过枚举名称匹配（兼容前端可能发送枚举名称的情况）
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            // 如果都不匹配，尝试兼容旧的中文值
            switch (value) {
                case "待审核":
                    return PENDING_REVIEW;
                case "审核通过":
                    return REVIEW_PASSED;
                case "禁播":
                    return REVIEW_REJECTED;
                default:
                    throw new IllegalArgumentException("Unknown VideoApprovalStatus value: " + value);
            }
        }
    }
}
