package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.CheckOwnership;
import com.example.onlinevideo.Entity.History;
import com.example.onlinevideo.Service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class HistoryController {
    @Autowired
    private HistoryService historyService;

    @PostMapping("/historyByHistoryId")
    public ResponseEntity<?> historyByHistoryId(@RequestBody History history) {
        if (history.getHistoryId() == null) {
            return ResponseEntity.badRequest().body("Invalid history");
        }
        History historyData = historyService.historyByHistoryId(history);
        if (historyData!=null) {
            return ResponseEntity.ok(historyData);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/detailHistory")
    @CheckOwnership(expression = "#history.userId")
    public ResponseEntity<?> detailHistory(@RequestBody History history) {
        List<History> historyList = historyService.detailHistory(history);
        if (!historyList.isEmpty()) {
            return ResponseEntity.ok(historyList);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/insertHistory")
    @CheckOwnership(expression = "#history.userId")
    public ResponseEntity<?> insertHistory(@RequestBody History history) {
        boolean result = historyService.insertHistory(history);
        if (result) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/deleteHistory")
    @CheckOwnership(expression = "#history.userId")
    public ResponseEntity<?> deleteHistory(@RequestBody History history) {
        boolean result = historyService.deleteHistory(history);
        if (result) {
            return ResponseEntity.ok().body(result);
        }
        return ResponseEntity.notFound().build();
    }
}
