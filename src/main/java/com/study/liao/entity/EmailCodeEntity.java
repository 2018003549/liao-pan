package com.study.liao.entity;

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
 * @date 2024-05-04 19:27:15
 */
@Data
@TableName("email_code")
public class EmailCodeEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 邮箱
	 */
	@TableId
	private String email;
	/**
	 * 编号
	 */
	private String code;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 状态 0-未使用 1-已使用
	 */
	private Integer status;

}
