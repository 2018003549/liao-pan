package com.study.liao.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)//防止redis中存储的属性和javaBean中属性不一致导致的报错
public class SysSettingsDto implements Serializable {
    private String registerEmailTitle="邮箱验证码";
    private String registerEmailContent="你好，你的邮箱验证码是,%s,15分钟内有效";
    private Integer userInitUseSpace=500;
}
