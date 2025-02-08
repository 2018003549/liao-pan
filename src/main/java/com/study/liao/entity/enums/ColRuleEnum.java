package com.study.liao.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ColRuleEnum {
    SUM(0,"总和"),
    AVG(1,"平均数"),
    MODE(2,"众数"),
    MEAN(3,"中位数"),
    COUNT(4,"计数"),
    RANGE(5,"数值区间"),
    SET(6,"数据集");
    private Integer code;
    private String desc;
    public static ColRuleEnum getEnumByCode(Integer type) {
        for (ColRuleEnum rule : ColRuleEnum.values()) {
            if (rule.getCode().equals(type)) {
                return rule;
            }
        }
        throw new IllegalArgumentException("Invalid code: " + type);
    }

}
