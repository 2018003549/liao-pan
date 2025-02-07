package com.study.liao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * 
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2025-02-04 13:57:00
 */
@Data
@TableName("user_group_detail_info")
public class UserGroupDetailInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId(type = IdType.AUTO)
	private Integer id;
	/**
	 * 组内的用户id
	 */
	private String userId;
	/**
	 * 关联的用户组id
	 */
	private Integer groupId;
	/**
	 * 逻辑删除
	 */
	private Integer isDeleted;
	/**
	 * 加入时间
	 */
	private Date joinTime;
	/**
	 * 审批状态
	 */
	private Integer approvalStatus;

}
