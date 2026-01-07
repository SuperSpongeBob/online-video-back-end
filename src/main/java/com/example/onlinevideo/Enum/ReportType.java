package com.example.onlinevideo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 举报类型枚举
 * 对应数据库tinyint类型：0-侵权, 1-色情, 2-暴力, 3-其他
 */
public enum ReportType {
    INFRINGEMENT(0, "侵权"),
    PORNOGRAPHY(1, "色情"),
    VIOLENCE(2, "暴力"),
    OTHER(3, "其他");

    private final int code;
    private final String description;

    ReportType(int code, String description) {
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
    public static ReportType fromCode(int code) {
        for (ReportType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ReportType code: " + code);
    }

    /**
     * 根据description获取枚举值
     */
    @JsonCreator
    public static ReportType fromDescription(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        // 首先尝试通过description匹配
        for (ReportType type : values()) {
            if (type.description.equals(value)) {
                return type;
            }
        }

        // 然后尝试通过枚举名称匹配
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown ReportType value: " + value);
        }
    }
}
