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
 * @date 2025-02-07 17:03:40
 */
@Data
@TableName("statistical_rules")
public class StatisticalRulesEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	@TableId
	private Integer id;
	/**
	 * 规则名称
	 */
	private String ruleName;
	/**
	 * 规则类型，详情见枚举
	 */
	private Integer type;
	/**
	 * 逻辑删除
	 */
	private Integer isDeleted;
	/**
	 * 在线编辑文档id
	 */
	private Integer fileId;
	/**
	 * 生效的列号
	 */
	private Integer colIndex;
	/**
	 * 数据范围，既可以表示数值区间，也可以表示数据集
	 */
	private String dataRange;

}
