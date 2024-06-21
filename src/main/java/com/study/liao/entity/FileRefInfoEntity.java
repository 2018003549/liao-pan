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
 * @date 2024-06-21 13:56:14
 */
@Data
@TableName("file_ref_info")
public class FileRefInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 文件md5值
	 */
	@TableId
	private String fileMd5;
	/**
	 * 引用计数
	 */
	private Long count;
	/**
	 * 文件存储路径
	 */
	private String filePath;

}
