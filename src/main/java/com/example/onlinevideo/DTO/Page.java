package com.example.onlinevideo.DTO;

import lombok.Data;

@Data
public class Page {
    protected Integer pageNum;
    protected Integer pageSize;

    public Integer getPageNum() {
        if (pageNum == null || pageSize == null) {
            return null;
        }
        return (pageNum - 1) * pageSize;
    }
}
