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
 * @date 2025-01-23 11:29:32
 */
@Data
@TableName("user_online_file_permissions")
public class UserOnlineFilePermissionsEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 权限id
	 */
	@TableId(type= IdType.AUTO)
	private Integer id;
	/**
	 * 关联的在线编辑文件id
	 */
	private Integer onlyFileId;
	/**
	 * 生效的用户id，这个类型和user_info保持一致
	 */
	private String userId;
	/**
	 * 权限 1-可读 2-可写 3-可下载
	 */
	private String permissions;
	/**
	 * 逻辑删除 1-已删除
	 */
	private Integer isDeleted;

}
