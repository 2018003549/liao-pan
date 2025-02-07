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
@TableName("user_group_info")
public class UserGroupInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	@TableId(type= IdType.AUTO)
	private Integer id;
	/**
	 * 用户组名，也可以作为企业名
	 */
	private String groupName;
	/**
	 * 人数上限
	 */
	private Integer maxSize;
	/**
	 * 逻辑删除
	 */
	private Integer isDeleted;
	/**
	 * 创建人id
	 */
	private String createById;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 简介
	 */
	private String description;
}
