package com.example.onlinevideo.Service;

import com.example.onlinevideo.Mapper.HistoryMapper;
import com.example.onlinevideo.Entity.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {
    @Autowired
    private HistoryMapper historyMapper;

    //  当数据库存在数据时执行更新操作，不存在时执行插入操作
    public boolean insertHistory(History history) {
        return historyMapper.insertHistory(history);
    }

    public History historyByHistoryId(History history){
        return historyMapper.historyByHistoryId(history);
    }

    public List<History> detailHistory(History history){
        return historyMapper.detailHistory(history);
    }

    public boolean deleteHistory(History history){
        return historyMapper.deleteHistory(history);
    }
}
