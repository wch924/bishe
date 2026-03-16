package com.yupi.yuaiagent.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
@Slf4j
@CrossOrigin
public class FileUploadController {

    private final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "tmp" + File.separator + "uploads";

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("error", "文件为空");
            return result;
        }

        try {
            // 获取原始文件名
            String originalFilename = file.getOriginalFilename();
            // 生成唯一标识作为文件名
            String suffix = FileUtil.getSuffix(originalFilename);
            String newFilename = IdUtil.simpleUUID() + "." + suffix;

            // 构建完整绝对路径
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File destFile = new File(uploadDir, newFilename);
            // 必须使用绝对路径写入，或者改用流，以防 Tomcat 临时目录相对路径解析异常
            FileUtil.writeFromStream(file.getInputStream(), destFile);

            log.info("文件上传成功: {}", destFile.getAbsolutePath());
            result.put("url", destFile.getAbsolutePath());
            result.put("name", originalFilename);

            return result;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            result.put("error", "文件上传失败：" + e.getMessage());
            return result;
        }
    }
}
