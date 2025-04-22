package com.example.onlinevideo.Mapper;

import com.example.onlinevideo.Entity.History;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HistoryMapper {
    boolean insertHistory(History history);

    History historyByHistoryId(History history);

    List<History> detailHistory(History history);

    boolean deleteHistory(History history);
}
