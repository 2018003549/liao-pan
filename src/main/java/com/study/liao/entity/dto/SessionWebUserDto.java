package com.study.liao.entity.dto;

import lombok.Data;

@Data
public class SessionWebUserDto {
    private String nickName;
    private String userId;
    private Boolean admin;
    private String avatar;
}
