package com.example.onlinevideo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 视频类型枚举
 * 对应数据库tinyint类型：0-免费, 1-收费, 2-VIP, 3-无版权, 4-独播
 */
public enum VideoType {
    FREE(0, "免费"),
    PAID(1, "收费"),
    VIP(2, "VIP"),
    NO_COPYRIGHT(3, "无版权"),
    EXCLUSIVE(4, "独播");

    private final int code;
    private final String description;

    VideoType(int code, String description) {
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
    public static VideoType fromCode(int code) {
        for (VideoType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown VideoType code: " + code);
    }

    /**
     * 根据description获取枚举值
     */
    @JsonCreator
    public static VideoType fromDescription(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        // 首先尝试通过description匹配
        for (VideoType type : values()) {
            if (type.description.equals(value)) {
                return type;
            }
        }
        
        // 然后尝试通过枚举名称匹配
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            // 兼容旧的video_is_vip字段值
            if ("0".equals(value) || "免费".equals(value)) {
                return FREE;
            } else if ("1".equals(value) || "VIP".equals(value)) {
                return VIP;
            }
            throw new IllegalArgumentException("Unknown VideoType value: " + value);
        }
    }
}

