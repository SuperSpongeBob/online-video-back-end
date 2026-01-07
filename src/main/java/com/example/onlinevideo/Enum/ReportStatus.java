package com.example.onlinevideo.Enum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 举报状态枚举
 * 对应数据库tinyint类型：0-待处理, 1-处理中, 2-已处理, 3-已驳回
 */
public enum ReportStatus {
    PENDING(0, "待处理"),
    PROCESSING(1, "处理中"),
    PROCESSED(2, "已处理"),
    REJECTED(3, "已驳回");

    private final int code;
    private final String description;

    ReportStatus(int code, String description) {
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
    public static ReportStatus fromCode(int code) {
        for (ReportStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReportStatus code: " + code);
    }

    /**
     * 根据description获取枚举值
     */
    @JsonCreator
    public static ReportStatus fromDescription(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        
        // 首先尝试通过description匹配
        for (ReportStatus status : values()) {
            if (status.description.equals(value)) {
                return status;
            }
        }
        
        // 然后尝试通过枚举名称匹配
        try {
            return valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown ReportStatus value: " + value);
        }
    }
}

