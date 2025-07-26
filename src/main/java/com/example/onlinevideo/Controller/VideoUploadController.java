package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.Entity.Video;
import com.example.onlinevideo.Entity.VideoAlbum;
import com.example.onlinevideo.Service.VideoAlbumService;
import com.example.onlinevideo.Service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * 该项目对数据的存储还是存在本地的
 * 实际开发中可以存储到OSS（对象存储服务）中
 */
@Tag(name = "上传")
@Slf4j
@RestController
@RequestMapping("/api")
public class VideoUploadController {
    @Autowired
    private VideoAlbumService videoAlbumService;
    @Autowired
    private VideoService videoService;

    private final Environment environment;

    //    @Value("${video.upload-dir}")
    private String videoUploadDir;
    //    @Value("${image.upload-dir}")
    private String imageUploadDir;

    @Autowired
    public VideoUploadController(Environment environment) {
        this.environment = environment;
        this.videoUploadDir = environment.getProperty("video.upload-dir");
        this.imageUploadDir = environment.getProperty("image.upload-dir");
    }

    private static final Logger logger = LoggerFactory.getLogger(VideoUploadController.class);

    @PostMapping("/upload")
    @RateLimit(maxRequests = 10)  // 一分钟只能上传十次，避免恶意上传
    public ResponseEntity<?> upload(
            @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            @RequestParam(value = "videoAlbum", required = false) String videoAlbum,
            @RequestParam(value = "video", required = false) String video) {

        try {
            //  仅上传专辑海报（imageFile + videoAlbum）
            if (imageFile != null && videoAlbum != null &&
                    videoFile == null && thumbnailFile == null && video == null) {

                // 检查是否确实有文件上传
                if (imageFile.isEmpty()) {
                    return ResponseEntity.badRequest().body("海报文件不能为空");
                }
                if (videoAlbum.isEmpty()) {
                    return ResponseEntity.badRequest().body("专辑信息不能为空");
                }

                VideoAlbum videoAlbumObj = new ObjectMapper().readValue(videoAlbum, VideoAlbum.class);

                //  删除海报文件
                videoAlbumService.deleteVideoPost(videoAlbumObj.getVideoAlbumId());

                // 保存海报文件
                String imageFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                File dest = new File(imageUploadDir, imageFileName);
                imageFile.transferTo(dest);
                videoAlbumObj.setVideoPostPath(imageFileName);

                // 更新专辑海报
                videoAlbumService.updateVideoAlbum(videoAlbumObj);
                return ResponseEntity.ok().body(true);
            }

            //  新增功能：仅上传缩略图（thumbnailFile + video）
            if (thumbnailFile != null && video != null &&
                    videoFile == null && imageFile == null && videoAlbum == null) {

                // 检查是否确实有文件上传
                if (thumbnailFile.isEmpty()) {
                    return ResponseEntity.badRequest().body("缩略图文件不能为空");
                }
                if (video.isEmpty()) {
                    return ResponseEntity.badRequest().body("视频信息不能为空");
                }

                Video videoObj = new ObjectMapper().readValue(video, Video.class);

                //  删除原缩略图文件
                videoService.deleteThumbnail(videoObj.getVideoId());

                // 保存缩略图文件
                String thumbnailFileName = UUID.randomUUID() + "_" + thumbnailFile.getOriginalFilename();
                File dest = new File(imageUploadDir, thumbnailFileName);
                thumbnailFile.transferTo(dest);
                videoObj.setThumbnailPath(thumbnailFileName);

                // 更新视频缩略图
                videoService.updateVideo(videoObj);
                return ResponseEntity.ok().body(true);
            }

            //  传视频（videoFile + videoAlbum + video）
            if (videoFile != null && videoAlbum != null && video != null) {
                // 原有逻辑保持不变
                Video videoObj = new ObjectMapper().readValue(video, Video.class);
                VideoAlbum videoAlbumObj = new ObjectMapper().readValue(videoAlbum, VideoAlbum.class);

                // 保存视频文件
                if (!videoFile.isEmpty()) {
                    /*String videoFileName = UUID.randomUUID() + "_" + videoFile.getOriginalFilename();
                    File dest = new File(videoUploadDir, videoFileName);
                    videoFile.transferTo(dest);*/

                    String originalFilename = videoFile.getOriginalFilename();
                    String fileExtension = getFileExtension(originalFilename);
                    boolean isMp4 = "mp4".equalsIgnoreCase(fileExtension);

                    String videoFileName;
                    File dest;

                    // 如果不是MP4格式，先保存原始文件，然后进行转码
                    if (!isMp4) {
                        // 保存原始文件
                        String originalVideoFileName = UUID.randomUUID() + "_original_" + originalFilename;
                        File originalDest = new File(videoUploadDir, originalVideoFileName);
                        videoFile.transferTo(originalDest);

                        // 生成转码后的文件名
                        String transcodedFileName = UUID.randomUUID() + "_transcoded.mp4";
                        dest = new File(videoUploadDir, transcodedFileName);

                        // 执行转码
                        transcodeToMp4(originalDest, dest);
                        videoFileName = transcodedFileName;

                        // 转码完成后可以选择删除原始文件
                        // originalDest.delete();
                    } else {
                        // 如果是MP4格式，直接保存
                        videoFileName = UUID.randomUUID() + "_" + originalFilename;
                        dest = new File(videoUploadDir, videoFileName);
                        videoFile.transferTo(dest);
                    }

                    videoObj.setVideoPath(videoFileName);
                    Integer duration = getVideoDuration(dest);      //  获取视频时长
                    videoObj.setDuration(duration);

                    //  生成预览文件
                    generateVideoPreview(dest);

                    // 处理海报（允许为 null）
                    if (imageFile != null && !imageFile.isEmpty()) {
                        String imageFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                        File dest1 = new File(imageUploadDir, imageFileName);
                        imageFile.transferTo(dest1);
                        videoAlbumObj.setVideoPostPath(imageFileName);
                    }

                    // 处理缩略图（允许为 null）
                    if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                        String thumbnailFileName = UUID.randomUUID() + "_" + thumbnailFile.getOriginalFilename();
                        File dest2 = new File(imageUploadDir, thumbnailFileName);
                        thumbnailFile.transferTo(dest2);
                        videoObj.setThumbnailPath(thumbnailFileName);
                    }

                    // 保存专辑和视频信息
                    videoAlbumService.upsertVideoAlbum(videoAlbumObj);
                    videoObj.setVideoAlbumId(videoAlbumObj.getVideoAlbumId());
                    videoObj.setVideoApprovalStatus("待审核");
                    videoService.insertVideo(videoObj);

                    return ResponseEntity.ok().body(true);
                } else {
                    return ResponseEntity.badRequest().body("视频文件不能为空");
                }
            }

            // 如果参数组合不合法，返回错误**
            return ResponseEntity.badRequest().body("无效的上传组合");

        } catch (JsonProcessingException e) {
            log.error("JSON解析失败{}", e.getMessage());
            return ResponseEntity.badRequest().body("无效的 JSON 格式");
        } catch (Exception e) {
            log.error("上传过程发生错误",e);
//            e.printStackTrace();
            return ResponseEntity.internalServerError().body("上传过程中发生错误");
        }
    }

    /**
     * 生成视频预览方法
     */
    private String generateVideoPreview(File sourceVideo) {
        try {
            // 构建预览文件名："preview_" + 原文件名
            String previewFileName = "preview_" + sourceVideo.getName();
            File previewFile = new File(sourceVideo.getParentFile(), previewFileName);

            // 构建FFmpeg命令
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-i", sourceVideo.getAbsolutePath(),
                    "-t", "30",  // 截取前30秒
                    "-c:v", "copy",  // 直接复制视频流，不重新编码
                    "-c:a", "copy",  // 直接复制音频流，不重新编码
                    "-avoid_negative_ts", "1",
                    previewFile.getAbsolutePath()
            );

            // 设置错误输出重定向
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 读取输出信息
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.debug("FFmpeg: {}", line);
            }

            // 等待进程完成
            int exitCode = process.waitFor();
            if (exitCode == 0 && previewFile.exists()) {
                log.info("成功生成预览视频: {}", previewFile.getAbsolutePath());
                return previewFileName;
            } else {
                log.error("生成预览视频失败，退出码: {}", exitCode);
                return null;
            }
        } catch (Exception e) {
            log.error("生成预览视频时出错", e);
            return null;
        }
    }


    /**
     * 获取视频的时长，单位秒
     *
     * @param videoFile
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private Integer getVideoDuration(File videoFile) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("ffprobe", "-v", "error", "-show_entries",
                "format=duration", "-of", "default=noprint_wrappers=1:nokey=1", videoFile.getAbsolutePath());
        Process process = processBuilder.start();
        process.waitFor();
        String output = new String(process.getInputStream().readAllBytes());
        double durationInSeconds = Double.parseDouble(output.trim());
        return (int) Math.round(durationInSeconds);
    }
    /**
     * 转码视频到MP4格式
     * @param sourceFile 源视频文件
     * @param targetFile 目标MP4文件
     * @throws IOException
     * @throws InterruptedException
     */
    private void transcodeToMp4(File sourceFile, File targetFile) throws IOException, InterruptedException {
        log.info("开始转码视频: {}", sourceFile.getName());

        // 使用ffmpeg进行转码，采用libx264编码和aac音频
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg", "-i", sourceFile.getAbsolutePath(),
                "-c:v", "libx264", "-preset", "medium", "-crf", "23",
                "-c:a", "aac", "-b:a", "128k",
                "-strict", "experimental",
                "-threads", "0", // 自动使用可用线程
                "-y", // 覆盖已存在文件
                targetFile.getAbsolutePath()
        );
        Process process = processBuilder.start();
        // 读取错误输出
        Thread errorReader = new Thread(() -> {
            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("ffmpeg: {}", line);
                }
            } catch (IOException e) {
                log.error("读取ffmpeg错误输出时出错", e);
            }
        });
        errorReader.start();

        // 读取标准输出
        Thread outputReader = new Thread(() -> {
            try {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("ffmpeg: {}", line);
                }
            } catch (IOException e) {
                log.error("读取ffmpeg标准输出时出错", e);
            }
        });
        outputReader.start();

        int exitCode = process.waitFor();
        errorReader.join();
        outputReader.join();

        if (exitCode != 0) {
            throw new IOException("视频转码失败，退出码: " + exitCode);
        }

        log.info("视频转码完成: {}", targetFile.getName());
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int lastIndex = fileName.lastIndexOf('.');
        return lastIndex >= 0 ? fileName.substring(lastIndex + 1) : "";
    }


/*@PostMapping("/uploadVideo")
    @RateLimit(maxRequests = 1)     //  一分钟只能上传一次，避免恶意上传
    public ResponseEntity<?> uploadVideo(@RequestParam("videoFile") MultipartFile videoFile,
                                         @RequestParam(value = "imageFile",required = false) MultipartFile imageFile,
                                         @RequestParam(value = "thumbnailFile",required = false) MultipartFile thumbnailFile,
                                         @RequestParam("videoAlbum") String videoAlbum,
                                         @RequestParam("video") String video) {
        try {
            //  将字符串转换成对象
            Video videoObj = new ObjectMapper().readValue(video, Video.class);
            VideoAlbum videoAlbumObj = new ObjectMapper().readValue(videoAlbum, VideoAlbum.class);

            // 保存视频文件
            if (videoFile != null &&!videoFile.isEmpty()) {
                //  获取上传文件的原始名
                String videoFileName = videoFile.getOriginalFilename();
                //  生成唯一的文件名
                videoFileName = UUID.randomUUID() + "_" + videoFileName;
                //  保存视频
                File dest = new File(videoUploadDir, videoFileName);
                videoFile.transferTo(dest);
                //  该文件名将是查找视频的路径
                videoObj.setVideoPath(videoFileName);
                System.out.println("文件已保存: " + dest.getAbsolutePath());
                //  获取视频时长
                Integer duration = getVideoDuration(dest);
                videoObj.setDuration(duration);
                System.out.println("视频时长：: " + duration+"秒");

                //  处理海报
                if (imageFile != null &&!imageFile.isEmpty()) {
                    String imageFileName = imageFile.getOriginalFilename();
                    imageFileName = UUID.randomUUID() + "_" + imageFileName;
                    File dest1 = new File(imageUploadDir, imageFileName);
                    imageFile.transferTo(dest1);
                    videoAlbumObj.setVideoPostPath(imageFileName);
                    System.out.println("image:" + dest1.getAbsolutePath());
                    System.out.println(imageFileName);
                }

                //  处理缩略图
                if (thumbnailFile != null &&!thumbnailFile.isEmpty()) {
                    String thumbnailFileName = thumbnailFile.getOriginalFilename();
                    thumbnailFileName = UUID.randomUUID() + "_" + thumbnailFileName;
                    File dest2 = new File(imageUploadDir, thumbnailFileName);
                    thumbnailFile.transferTo(dest2);
                    videoObj.setThumbnailPath(thumbnailFileName);
                }

                //  保存视频专辑信息
                videoAlbumService.addVideoAlbum(videoAlbumObj);
                System.out.println("已插入专辑");

                //  返回专辑id保存到video表中关联
                videoObj.setVideoAlbumId(videoAlbumObj.getVideoAlbumId());
                System.out.println(videoAlbumObj.getVideoAlbumId());

                //  保存视频信息
                videoObj.setVideoApprovalStatus("待审核");
                videoService.insertVideo(videoObj);
                System.out.println("已插入video:   "+video);
            } else {
                return ResponseEntity.noContent().build();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok().body(true);
    }*/

/*    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(@RequestParam("videoFile")MultipartFile videoFile,@RequestBody VideoUpload videoUpload) {
        Video video=videoUpload.getVideo();
        VideoAlbum videoAlbum=videoUpload.getVideoAlbum();
        System.out.println(video);
        System.out.println(videoAlbum);
        System.out.println(videoFile);
        return ResponseEntity.ok("视频上传成功");
    }*

/*    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            Video video = videoService.saveVideo(file);
            return ResponseEntity.ok("Video uploaded successfully with ID: " + video.getId());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading video");
        }
    }*/

/*    @PostMapping("/api/upload")
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("Please select a video file to upload.", HttpStatus.BAD_REQUEST);
        }

        try {
            // 设置文件保存的路径
            String uploadDir = "C:\\Users\\adminstrator\\OneDrive\\Desktop\\test\\";
            Path path = Paths.get(uploadDir + file.getOriginalFilename());

            // 保存文件
            file.transferTo(path);
            return new ResponseEntity<>("Video uploaded successfully: " + file.getOriginalFilename(), HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to upload video.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
}
