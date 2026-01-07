package com.example.onlinevideo.TypeHandler;

import com.example.onlinevideo.Enum.VideoApprovalStatus;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * MyBatis TypeHandler for VideoApprovalStatus enum
 * 将枚举类型与数据库的tinyint类型进行转换
 */
@MappedTypes(VideoApprovalStatus.class)
@MappedJdbcTypes(JdbcType.TINYINT)
public class VideoApprovalStatusTypeHandler extends BaseTypeHandler<VideoApprovalStatus> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, VideoApprovalStatus parameter, JdbcType jdbcType) throws SQLException {
        ps.setInt(i, parameter.getCode());
    }

    @Override
    public VideoApprovalStatus getNullableResult(ResultSet rs, String columnName) throws SQLException {
        int code = rs.getInt(columnName);
        return rs.wasNull() ? null : VideoApprovalStatus.fromCode(code);
    }

    @Override
    public VideoApprovalStatus getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        int code = rs.getInt(columnIndex);
        return rs.wasNull() ? null : VideoApprovalStatus.fromCode(code);
    }

    @Override
    public VideoApprovalStatus getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        int code = cs.getInt(columnIndex);
        return cs.wasNull() ? null : VideoApprovalStatus.fromCode(code);
    }
}

