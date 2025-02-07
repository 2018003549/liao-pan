package com.study.liao.entity.dto;

import lombok.Data;

import java.util.Date;

@Data
public class UserGroupDetailDTO {
    /**
     * 用户组id
     */
    private Long groupId;
    /**
     * 用户组名，也可以作为企业名
     */
    private String groupName;
    /**
     * 人数上限
     */
    private Long maxSize;
    /**
     * 创建人名称
     */
    private String createByName;
    /**
     * 创建人id
     */
    private String createById;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 加入时间
     */
    private Date joinTime;
    /**
     * 简介
     */
    private String description;
    /**
     * 当前加入人数
     */
    private Long currentSize;
    /**
     * 审批状态 0-待审批 1-已通过 2-已拒绝
     */
    private Integer approvalStatus;
    /**
     * 是否已加入
     */
    private Boolean isActive;
    /**
     * 是否是自己创建的
     */
    private Boolean isCreated;
}
