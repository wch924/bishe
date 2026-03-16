package com.chwww924.chwwwBackend.service.impl;

import cn.hutool.core.io.FileUtil;
import com.chwww924.chwwwBackend.common.ErrorCode;
import com.chwww924.chwwwBackend.exception.BusinessException;
import com.chwww924.chwwwBackend.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 文件存储服务实现
 */
@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.storage.location}")
    private String storageLocation;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {"csv"};

    @Override
    public String saveFile(MultipartFile file, Long userId, Long sessionId, String fileName) {
        try {
            // 构建目录路径: {storageRoot}/datafiles/{userId}/{sessionId}/
            String directoryPath = String.format("%s/datafiles/%d/%d", storageLocation, userId, sessionId);
            File directory = new File(directoryPath);
            
            // 创建目录（如果不存在）
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建存储目录失败");
                }
            }

            // 构建完整文件路径
            String filePath = directoryPath + File.separator + fileName;
            File destFile = new File(filePath);

            // 保存文件
            file.transferTo(destFile);
            
            log.info("文件保存成功: {}", filePath);
            return filePath;
            
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件保存失败");
        }
    }

    @Override
    public void deleteSessionFiles(Long userId, Long sessionId) {
        try {
            String directoryPath = String.format("%s/datafiles/%d/%d", storageLocation, userId, sessionId);
            Path path = Paths.get(directoryPath);
            
            if (Files.exists(path)) {
                // 递归删除目录及其所有文件
                Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
                
                log.info("会话文件删除成功: {}", directoryPath);
            }
        } catch (IOException e) {
            log.error("删除会话文件失败: userId={}, sessionId={}", userId, sessionId, e);
            // 不抛出异常，避免影响事务回滚
        }
    }

    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过10MB");
        }

        // 检查文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名无效");
        }

        String extension = FileUtil.getSuffix(originalFilename);
        if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(extension.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持CSV格式文件");
        }
    }
}
