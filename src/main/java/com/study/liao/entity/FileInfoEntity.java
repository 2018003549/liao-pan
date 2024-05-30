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
 * @date 2024-05-30 17:06:10
 */
@Data
@TableName("file_info")
public class FileInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 文件id
	 */
	@TableId
	private String fileId;
	/**
	 * 用户id
	 */
	private String userId;
	/**
	 * 用于实现秒传，如果两个文件md5值一样就不用重新上传
	 */
	private String fileMd5;
	/**
	 * 父层级id
	 */
	private String filePid;
	/**
	 * 文件大小
	 */
	private Long fileSize;
	/**
	 * 文件名
	 */
	private String fileName;
	/**
	 * 文件封面
	 */
	private String fileCover;
	/**
	 * 文件存储路径
	 */
	private String filePath;
	/**
	 * 
	 */
	private Date createTime;
	/**
	 * 
	 */
	private Date updateTime;
	/**
	 * 目录类型
	 */
	private Integer folderType;
	/**
	 * 类别：1视频 2音频 3图片 4文档 5其它
	 */
	private Integer fileCategory;
	/**
	 * 0转码中 1转码失败 2转码成功
	 */
	private Integer status;
	/**
	 * 回收站保存时间
	 */
	private Date recoveryTime;
	/**
	 * 0删除 1回收站 2正常
	 */
	private Integer delFlag;

}
