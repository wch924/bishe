package com.chwww924.chwwwBackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件存储服务接口
 */
public interface FileStorageService {

    /**
     * 保存文件到指定路径
     * 
     * @param file 文件
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @param fileName 文件名
     * @return 文件完整路径
     */
    String saveFile(MultipartFile file, Long userId, Long sessionId, String fileName);

    /**
     * 删除会话目录及其所有文件
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void deleteSessionFiles(Long userId, Long sessionId);

    /**
     * 验证文件
     * 
     * @param file 文件
     */
    void validateFile(MultipartFile file);
}
