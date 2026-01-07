package com.example.onlinevideo.Service;

import com.example.onlinevideo.Entity.VideoReportRecord;
import com.example.onlinevideo.Enum.ReportStatus;
import com.example.onlinevideo.Enum.ReportType;
import com.example.onlinevideo.Mapper.VideoReportRecordMapper;
import com.example.onlinevideo.Mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VideoReportRecordService {
    
    @Autowired
    private VideoReportRecordMapper videoReportRecordMapper;
    
    @Autowired
    private VideoMapper videoMapper;
    
    /**
     * 提交举报
     */
    @Transactional
    public boolean submitReport(VideoReportRecord record) {
        // 检查用户是否已经举报过该视频
        if (videoReportRecordMapper.existsReportByUserAndVideo(record.getReporterId(), record.getVideoId())) {
            throw new RuntimeException("您已经举报过该视频，请勿重复举报");
        }
        
        // 设置默认状态为待处理
        record.setReportStatus(ReportStatus.PENDING);
        record.setReportTime(System.currentTimeMillis());
        
        int result = videoReportRecordMapper.insertReportRecord(record);
        
        // 如果举报类型为侵权，更新视频表添加source_video_url字段（如果还没有）
        if (result > 0 && record.getReportType() == ReportType.INFRINGEMENT) {
            // 这里可以添加逻辑，如果视频还没有source_video_url，可以提示管理员添加
            // 或者自动设置为空，等待管理员处理时填写
        }
        
        return result > 0;
    }
    
    /**
     * 根据ID查找举报记录
     */
    public VideoReportRecord findReportRecordById(Integer reportId) {
        return videoReportRecordMapper.findReportRecordById(reportId);
    }
    
    /**
     * 根据视频ID查找举报记录列表
     */
    public List<VideoReportRecord> findReportRecordsByVideoId(Integer videoId) {
        return videoReportRecordMapper.findReportRecordsByVideoId(videoId);
    }
    
    /**
     * 根据举报人ID查找举报记录列表
     */
    public List<VideoReportRecord> findReportRecordsByReporterId(Integer reporterId) {
        return videoReportRecordMapper.findReportRecordsByReporterId(reporterId);
    }
    
    /**
     * 查找所有举报记录（分页）
     */
    public List<VideoReportRecord> findReportRecords(VideoReportRecord record) {
        return videoReportRecordMapper.findReportRecords(record);
    }
    
    /**
     * 处理举报（管理员操作）
     */
    @Transactional
    public boolean handleReport(Integer reportId, Integer handlerId, ReportStatus status, String handleRemark) {
        VideoReportRecord record = videoReportRecordMapper.findReportRecordById(reportId);
        if (record == null) {
            return false;
        }
        
        record.setHandlerId(handlerId);
        record.setHandleTime(System.currentTimeMillis());
        record.setReportStatus(status);
        record.setHandleRemark(handleRemark);
        
        return videoReportRecordMapper.updateReportRecord(record);
    }
    
    /**
     * 更新举报记录
     */
    public boolean updateReportRecord(VideoReportRecord record) {
        return videoReportRecordMapper.updateReportRecord(record);
    }
    
    /**
     * 删除举报记录
     */
    public boolean deleteReportRecord(Integer reportId) {
        return videoReportRecordMapper.deleteReportRecord(reportId);
    }
    
    /**
     * 根据视频ID统计举报数量
     */
    public int countReportsByVideoId(Integer videoId) {
        return videoReportRecordMapper.countReportsByVideoId(videoId);
    }
    
    /**
     * 检查用户是否已举报过该视频
     */
    public boolean hasUserReported(Integer reporterId, Integer videoId) {
        return videoReportRecordMapper.existsReportByUserAndVideo(reporterId, videoId);
    }
}

