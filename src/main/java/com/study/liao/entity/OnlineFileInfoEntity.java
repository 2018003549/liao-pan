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
@TableName("online_file_info")
public class OnlineFileInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 在线编辑文件id
	 */
	@TableId(type= IdType.AUTO)
	private Integer id;
	/**
	 * 文件名
	 */
	private String filename;
	/**
	 * 物理存储地址
	 */
	private String storageAddress;
	/**
	 * 创建人id
	 */
	private String createById;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 更新时间
	 */
	private Date updateTime;
	/**
	 * 状态 0-不可编辑 1-可编辑
	 */
	private Integer status;
	/**
	 * 逻辑删除 0-未删除 1-已删除
	 */
	private Integer isDeleted;

}
