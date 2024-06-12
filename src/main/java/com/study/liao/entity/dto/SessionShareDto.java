package com.study.liao.entity.dto;

import lombok.Data;

import java.util.Date;
@Data
public class SessionShareDto {
    private String shareId;//分享链接标识
    private String shareUserId;//分享人id
    private Date expireTime;//分享链接失效时间
    private String fileId;//分享文件id
}