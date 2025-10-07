package com.longjunwang.moni.service;

import com.alibaba.fastjson2.JSONObject;
import com.longjunwang.moni.entity.AgentContext;
import com.longjunwang.moni.entity.Recon;
import com.longjunwang.moni.mapper.ReconMapper;
import com.longjunwang.moni.util.DateUtil;
import com.longjunwang.moni.util.Encrypt;
import com.longjunwang.moni.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class FileService {

    @Autowired
    private ReconMapper reconMapper;

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

    public String recon(MultipartFile file) {
        log.info("recon start");
        List<Recon> recons = reconMapper.selectByDate(getDay(-1))
                .stream().toList();
        List<String> encryptIds = recons.stream().map(Recon::getEncryptId).filter(Objects::nonNull).toList();
        List<String> oldEncryptIds = recons.stream().filter(item -> Objects.isNull(item.getEncryptId())).map(item -> {
            AgentContext agentContext = JSONObject.parseObject(item.getRaw(), AgentContext.class);
            return Encrypt.encrypt(agentContext.getContent());
        }).toList();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] split = line.split("@");
                var encryptId = split[0];
                var content = split[1];
                if (encryptIds.contains(encryptId) || oldEncryptIds.contains(encryptId)) {
                    log.info("id: {}, content: {}, 已经存在", encryptId, content);
                    continue;
                }
                var id = IdGenerator.generateId();
                AgentContext agentContext = new AgentContext();
                agentContext.setInsertId(id);
                agentContext.setContent(content);
                Recon recon = new Recon();
                recon.setInsertId(id);
                recon.setEncryptId(encryptId);
                recon.setRaw(JSONObject.toJSONString(agentContext));
                recon.setCreated(DateUtil.getCurrentDateTime());
                recon.setUpdated(DateUtil.getCurrentDateTime());
                log.info("recon : {}", JSONObject.toJSONString(recon));
                reconMapper.insertSelective(recon);
            }
        }catch (Exception e) {
            log.error("recon error", e);
            return "recon error e:" + e.getMessage();
        }
        log.info("recon end");
        return "recon success";
    }

    private String getDay(int num){
        LocalDate localDate = LocalDate.now();
        LocalDate date = localDate.plusDays(num);
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(date);
    }
}
