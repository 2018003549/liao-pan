package com.study.liao.entity.dto;

import lombok.Data;

@Data
public class UserSpaceDto {
    /**
     * 用户可用空间
     */
    private Long useSpace;
    /**
     * 用户总空间
     */
    private Long totalSpace;
}
