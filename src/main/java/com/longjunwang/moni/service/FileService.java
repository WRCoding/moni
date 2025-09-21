package com.longjunwang.moni.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    @Value("${moni.image.path}")
    private String moniImagePath;

    public String downloadFile(MultipartFile file) throws IOException {
        Path imagePath = Paths.get(moniImagePath);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        // 确保目录存在
        Files.createDirectories(imagePath);

        // 生成安全文件名（避免路径穿越，保留后缀）
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null) {
            String base = Paths.get(original).getFileName().toString();
            int dot = base.lastIndexOf('.');
            if (dot >= 0) ext = base.substring(dot);
        }
        String safeName = UUID.randomUUID().toString().replace("-", "") + ext;

        Path target = imagePath.resolve(safeName).normalize().toAbsolutePath();
        // 防路径穿越校验
        if (!target.startsWith(imagePath.toAbsolutePath())) {
            throw new SecurityException("Invalid target path");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        log.info("downloaded file : {}", target);
        return target.toString();
    }
}
