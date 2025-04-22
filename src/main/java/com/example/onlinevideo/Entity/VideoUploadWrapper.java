package com.example.onlinevideo.Entity;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VideoUploadWrapper {
    private VideoUpload videoUpload;
    private MultipartFile videoFile;
}
