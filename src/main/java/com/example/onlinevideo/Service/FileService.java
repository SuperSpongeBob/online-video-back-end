package com.example.onlinevideo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileService {
    @Autowired
    private Environment environment;


    public boolean deleteImagesFile(String fileName) {
        // 获取配置的静态资源路径
        String staticLocations = environment.getProperty("spring.web.resources.static-locations");
        // 去除 "file:///" 前缀
        String rootPath = staticLocations.replace("file:///", "");
        // 拼接完整的文件路径
        String fullPath = rootPath + "images\\" + fileName;

        File file = new File(fullPath);
        // 检查文件是否存在
        if (file.exists()) {
            // 删除文件
            return file.delete();
        }
        return false;
    }

    public boolean deleteVideoFile(String fileName) {
        // 获取配置的静态资源路径
        String staticLocations = environment.getProperty("spring.web.resources.static-locations");
        // 去除 "file:///" 前缀
        String rootPath = staticLocations.replace("file:///", "");
        // 拼接完整的文件路径
        String fullPath = rootPath + "videos\\" + fileName;

        File file = new File(fullPath);
        // 检查文件是否存在
        if (file.exists()) {
            // 删除文件
            return file.delete();
        }
        return false;
    }
}
