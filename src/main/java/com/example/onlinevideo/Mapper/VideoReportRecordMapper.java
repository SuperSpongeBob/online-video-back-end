package com.example.onlinevideo.Mapper;

import com.example.onlinevideo.Entity.VideoReportRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoReportRecordMapper {
    
    /**
     * 插入举报记录
     */
    int insertReportRecord(VideoReportRecord record);
    
    /**
     * 根据ID查找举报记录
     */
    VideoReportRecord findReportRecordById(Integer reportId);
    
    /**
     * 根据视频ID查找举报记录列表
     */
    List<VideoReportRecord> findReportRecordsByVideoId(Integer videoId);
    
    /**
     * 根据举报人ID查找举报记录列表
     */
    List<VideoReportRecord> findReportRecordsByReporterId(Integer reporterId);
    
    /**
     * 查找所有举报记录（分页）
     */
    List<VideoReportRecord> findReportRecords(VideoReportRecord record);
    
    /**
     * 更新举报记录
     */
    boolean updateReportRecord(VideoReportRecord record);
    
    /**
     * 删除举报记录
     */
    boolean deleteReportRecord(Integer reportId);
    
    /**
     * 根据视频ID统计举报数量
     */
    int countReportsByVideoId(Integer videoId);
    
    /**
     * 检查用户是否已举报过该视频
     */
    boolean existsReportByUserAndVideo(@Param("reporterId") Integer reporterId, @Param("videoId") Integer videoId);
}

