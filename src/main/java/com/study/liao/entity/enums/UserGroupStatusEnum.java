package com.study.liao.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserGroupStatusEnum {
    PENDING(0, "待审批"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝");
    private final int code;
    private final String description;
}
