package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.CheckOwnership;
import com.example.onlinevideo.Entity.Danmaku;
import com.example.onlinevideo.Service.DanmakuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class DanmakuController {
    @Autowired
    private DanmakuService danmakuService;

    @PostMapping("/addDanmaku")
//    @CheckOwnership(expression = "#danmaku.userId")
    public ResponseEntity<?> addDanmaku(@RequestBody Danmaku danmaku) {
        boolean result = danmakuService.addDanmaku(danmaku);
        if (result) {
            return ResponseEntity.ok().body(true);
        }else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/getDanmaku")
    public ResponseEntity<?> getDanmaku(@RequestBody Danmaku danmaku) {
        List<Danmaku> danmakuList = danmakuService.getDanmakus(danmaku);
        return ResponseEntity.ok().body(danmakuList);
    }

    @PostMapping("/deleteDanmaku")
//    @CheckOwnership(expression = "#danmaku.userId")
    public ResponseEntity<?> deleteDanmaku(@RequestBody Danmaku danmaku) {
        boolean result = danmakuService.deleteDanmaku(danmaku);
        if (result) {
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.badRequest().build();
    }
}
