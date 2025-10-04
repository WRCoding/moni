package com.longjunwang.moni.controller;

import com.longjunwang.moni.entity.MoniMsg;
import com.longjunwang.moni.service.FileService;
import com.longjunwang.moni.service.MoniService;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@Slf4j
public class MoniController {

    @Resource
    private MoniService moniService;

    @Autowired
    private FileService fileService;

    @PostMapping("/ai")
    public String ai(@RequestBody MoniMsg moniMsg) throws MessagingException {
        log.info("moniMsg={}", moniMsg);
        return moniService.handleMessage(moniMsg);
    }

    @PostMapping("/file")
    public ResponseEntity<String> file(MultipartFile file) {
        try {
            return ResponseEntity.ok(fileService.downloadFile(file));
        } catch (IOException e) {
            log.error("upload file error : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("文件上传失败" + e.getMessage());
        }
    }

    @PostMapping("/recon")
    public ResponseEntity<String> recon(MultipartFile file) {
        return ResponseEntity.ok(fileService.recon(file));
    }
}
