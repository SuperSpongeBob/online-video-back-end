package com.example.onlinevideo.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public class VideoPreviewGenerator {
    private static final String FFMPEG_PATH = "ffmpeg"; // ffmpeg可执行文件路径
    private static final String SOURCE_DIR = "E:\\mysqlStorage\\videos";
    private static final String TARGET_DIR = "E:\\mysqlStorage\\previews";
    private static final int PREVIEW_DURATION = 30; // 预览时长(秒)

    public static void main(String[] args) {
        File sourceDirectory = new File(SOURCE_DIR);
        File targetDirectory = new File(TARGET_DIR);

        // 检查源目录是否存在
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            System.err.println("源目录不存在或不是目录: " + SOURCE_DIR);
            return;
        }

        // 检查并创建目标目录
        if (!targetDirectory.exists()) {
            if (!targetDirectory.mkdirs()) {
                System.err.println("无法创建目标目录: " + TARGET_DIR);
                return;
            }
        }

        // 遍历源目录中的所有文件
        for (File file : Objects.requireNonNull(sourceDirectory.listFiles())) {
            if (file.isFile() && isVideoFile(file.getName())) {
                generatePreview(file, new File(targetDirectory, "preview_" + file.getName()));
            }
        }
    }

    // 检查文件是否为视频文件
    private static boolean isVideoFile(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".mp4") || lowerCaseName.endsWith(".avi") ||
                lowerCaseName.endsWith(".mov") || lowerCaseName.endsWith(".mkv") ||
                lowerCaseName.endsWith(".wmv");
    }

    // 使用ffmpeg生成视频预览
    private static void generatePreview(File sourceFile, File targetFile) {
        System.out.println("正在处理: " + sourceFile.getName());

        try {
            // 构建ffmpeg命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                    FFMPEG_PATH,
                    "-i", sourceFile.getAbsolutePath(),
                    "-t", String.valueOf(PREVIEW_DURATION),
                    "-c:v", "copy", // 视频流直接复制不重新编码
                    "-c:a", "copy", // 音频流直接复制不重新编码
                    "-avoid_negative_ts", "1",
                    targetFile.getAbsolutePath()
            );

            // 重定向错误输出到标准输出
            processBuilder.redirectErrorStream(true);

            // 启动进程
            Process process = processBuilder.start();

            // 读取进程输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // 等待进程结束并获取退出码
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("成功生成预览: " + targetFile.getName());
            } else {
                System.err.println("生成预览失败，退出码: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("处理视频时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}