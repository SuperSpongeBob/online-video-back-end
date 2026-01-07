package com.example.onlinevideo.Entity;

import com.example.onlinevideo.DTO.Page;
import com.example.onlinevideo.Enum.ReportStatus;
import com.example.onlinevideo.Enum.ReportType;
import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;

/**
 * 视频举报记录实体类
 */
@Data
@Entity
public class VideoReportRecord extends Page implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reportId;
    
    /**
     * 举报人ID
     */
    private Integer reporterId;
    
    /**
     * 被举报视频ID
     */
    private Integer videoId;
    
    /**
     * 举报状态
     */
    private ReportStatus reportStatus;
    
    /**
     * 举报类型
     */
    private ReportType reportType;
    
    /**
     * 举报描述
     */
    private String reportDescription;
    
    /**
     * 举报时间（时间戳）
     */
    private Long reportTime;
    
    /**
     * 处理人ID（管理员）
     */
    private Integer handlerId;
    
    /**
     * 处理时间（时间戳）
     */
    private Long handleTime;
    
    /**
     * 处理备注
     */
    private String handleRemark;
}

