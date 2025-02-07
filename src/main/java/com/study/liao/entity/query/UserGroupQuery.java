package com.study.liao.entity.query;

import lombok.Data;

@Data
public class UserGroupQuery extends BaseParam {
    /**
     * 检索关键字，可以模糊匹配用户组id和组名
     */
    private String keyword;
    /**
     * 审批状态 0-待审批 1-已通过 2-已拒绝
     */
    private Integer approvalStatus;
    /**
     * 当前用户id
     */
    private String userId;
    /**
     * 用户组名
     */
    private String groupName;
}
