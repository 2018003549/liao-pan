package com.study.liao.entity.dto;

import lombok.Data;

@Data
public class DownloadFileDto {
    private String downloadCode;
    private String fileName;
    private String filePath;
    /**
     * 文件流，用于服务间传递
     */
    private byte[] downloadFile;
}
