package com.study.liao.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Transient;

/**
 * 
 * 
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-06-09 14:05:18
 */
@Data
@TableName("share_info")
public class ShareInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分享id
	 */
	@TableId
	private String shareId;
	/**
	 * 分享文件id
	 */
	private String fileId;
	/**
	 * 分享人id
	 */
	private String userId;
	/**
	 * 分享状态
	 */
	private Integer validType;
	/**
	 * 失效时间
	 */
	private Date expireTime;
	/**
	 * 分享时间
	 */
	private Date shareTime;
	/**
	 * 提取码
	 */
	private String code;
	/**
	 * 浏览量
	 */
	private Integer showCount;
	/**
	 * 文件名，不在数据库中
	 */
	@TableField(exist = false)
	private String fileName;

}
