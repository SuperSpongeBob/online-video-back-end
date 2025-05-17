package com.example.onlinevideo.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileService {
    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    @Autowired
    private Environment environment;


    public boolean deleteImagesFile(String fileName) {
        // 获取配置的静态资源路径
        String staticLocations = environment.getProperty("spring.web.resources.static-locations");
        // 去除 "file:///" 前缀
        String rootPath = staticLocations.replace("file:///", "");

        // 使用系统属性获取文件分隔符，确保跨平台兼容
        String fileSeparator = System.getProperty("file.separator");

        // 拼接完整的文件路径（使用文件分隔符变量）
        String fullPath = fileSeparator + rootPath + "images" + fileSeparator + fileName;

        /*// 拼接完整的文件路径
        String fullPath = rootPath + "images\\" + fileName;*/

        File file = new File(fullPath);
        log.info("Deleting image file: " + fullPath);
        return file.delete();
        /*// 检查文件是否存在
        if (file.exists()) {
            // 删除文件
            log.info("Deleting images" + fullPath);
            return file.delete();
        }
        return false;*/
    }

    public boolean deleteVideoFile(String fileName) {
        log.info("Deleting video" + fileName);
        // 获取配置的静态资源路径
        String staticLocations = environment.getProperty("spring.web.resources.static-locations");
        // 去除 "file:///" 前缀
        String rootPath = staticLocations.replace("file:///", "");

        // 使用系统属性获取文件分隔符，确保跨平台兼容
        String fileSeparator = System.getProperty("file.separator");

        // 拼接完整的文件路径（使用文件分隔符变量）
        String fullPath = fileSeparator + rootPath + "videos" + fileSeparator + fileName;

        /*// 拼接完整的文件路径
        String fullPath = rootPath + "videos\\" + fileName;*/

        File file = new File(fullPath);
        log.info("Deleting video file: " + fullPath);
        return file.delete();
        /*// 检查文件是否存在
        if (file.exists()) {
            // 删除文件
            log.error("Deleting video" + fullPath);
            return file.delete();
        }
        return false;*/
    }
}
