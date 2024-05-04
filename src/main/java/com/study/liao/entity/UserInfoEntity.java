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
 * @date 2024-05-03 18:10:47
 */
@Data
@TableName("user_info")
public class UserInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 用户id
	 */
	@TableId
	private String userId;
	/**
	 * 用户名称
	 */
	private String nickName;
	/**
	 * 用户邮箱
	 */
	private String email;
	/**
	 * QQ的OpenId
	 */
	private String qqOpenId;
	/**
	 * QQ头像
	 */
	private String qqAvatar;
	/**
	 * 密文密码
	 */
	private String password;
	/**
	 * 创建时间
	 */
	private Date joinTime;
	/**
	 * 最后一次登录的时间
	 */
	private Date lastLoginTime;
	/**
	 * 用户状态 0表示禁用 1表示启用
	 */
	private Integer status;
	/**
	 * 使用空间，单位是bit
	 */
	private Long useSpace;
	/**
	 * 总空间，单位是bit
	 */
	private Long totalSpace;

}
