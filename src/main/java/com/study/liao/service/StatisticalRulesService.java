package com.study.liao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.liao.entity.StatisticalRulesEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2025-02-07 17:03:40
 */
public interface StatisticalRulesService extends IService<StatisticalRulesEntity> {
    /**
     * 根据配置的列规则去聚合统计列字段信息
     * @return 返回多个列的聚合结果报告
     */
    List<String> processExcelColumnsWithRules(String fileId);
}

