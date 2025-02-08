package com.study.liao.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.dto.DownloadFileDto;
import com.study.liao.entity.enums.ColRuleEnum;
import com.study.liao.service.OnlineEditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.study.liao.dao.StatisticalRulesDao;
import com.study.liao.entity.StatisticalRulesEntity;
import com.study.liao.service.StatisticalRulesService;
import org.springframework.util.CollectionUtils;


@Service("statisticalRulesService")
public class StatisticalRulesServiceImpl extends ServiceImpl<StatisticalRulesDao, StatisticalRulesEntity> implements StatisticalRulesService {
    @Autowired
    OnlineEditService onlineEditService;

    @Override
    public List<String> processExcelColumnsWithRules(String fileId) {
        //1.从onlyOffice下载文件到工作区
        DownloadFileDto downloadFileDto = onlineEditService.download(fileId);
        String fileName = downloadFileDto.getFileName();
        if (!fileName.contains(".xlsx")) {
            throw new BusinessException("非xlsx格式的文件无法解析");
        }
        //2.获取该文件绑定的规则
        LambdaQueryWrapper<StatisticalRulesEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StatisticalRulesEntity::getFileId, fileId);
        List<StatisticalRulesEntity> ruleList = this.baseMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(ruleList)) {
            return null;
        }
        //按列号给规则分组
        HashMap<Integer, StatisticalRulesEntity> ruleMap = new HashMap<>();
        for (StatisticalRulesEntity entity : ruleList) {
            ruleMap.put(entity.getColIndex(), entity);
        }
        //3.解析excel数据
        InputStream downloadFile = new ByteArrayInputStream(downloadFileDto.getDownloadFile());
        List<String> resultList = new ArrayList<>();
        EasyExcel.read(downloadFile, new AnalysisEventListener<Map<Integer, String>>() {
            // 存储表头数据，key为列号，value为列名
            Map<Integer, String> headerNameMap = new HashMap<>();
            // 用于保存行记录，外层key为列号，内层key为行号，value为单元格值
            Map<Integer, Map<Integer, String>> rowDataMap = new HashMap<>();

            @Override
            public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
                // 读取到每一行时的回调
                //获取当前行号
                Integer rowIndex = context.readRowHolder().getRowIndex();
                for (Map.Entry<Integer, String> entry : rowData.entrySet()) {
                    Integer colIndex = entry.getKey();
                    String value = entry.getValue();
                    if (!rowDataMap.containsKey(colIndex)) {
                        rowDataMap.put(colIndex, new HashMap<>());
                    }
                    Map<Integer, String> cellData = rowDataMap.get(colIndex);
                    cellData.put(rowIndex, value);
                }
            }

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                headerNameMap = headMap;
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 解析完成后的回调
                for (Map.Entry<Integer, StatisticalRulesEntity> ruleEntry : ruleMap.entrySet()) {
                    Integer colIndex = ruleEntry.getKey();
                    StatisticalRulesEntity rule = ruleEntry.getValue();
                    Map<Integer, String> cellData = rowDataMap.get(colIndex);
                    ColRuleEnum type = ColRuleEnum.getEnumByCode(rule.getType());
                    String result = calculateColumnMetricsByRule(type, cellData.values(), rule.getDataRange());
                    String headerName = headerNameMap.get(colIndex);
                    resultList.add(headerName + result);
                }
            }
        }).sheet().doRead();  // 读取第一个 sheet 中的数据
        return resultList;
    }

    /**
     * 根据类型解析指定列的所有元素
     *
     * @param type     规则类型
     * @param cellData 指定列的所有元素
     * @return
     */
    private String calculateColumnMetricsByRule(ColRuleEnum type, Collection<String> cellData, String range) {
        StringBuffer result = new StringBuffer();//用于拼接统计结果
        Double sum = 0d;//用于计算总和
        int size = cellData.size();//元素个数
        Map<String, Integer> countMap = new HashMap<>();//统计每个元素的计数
        switch (type) {
            case SUM:
                // 计算总和
                result.append("总和为");
                for (String value : cellData) {
                    try {
                        double num = Double.parseDouble(value);
                        sum += num;
                    } catch (Exception e) {
                        log.error("数据:" + value + " 无法转换成数字");
                    }
                }
                result.append(sum);
                return result.toString();
            case AVG:
                // 计算平均数
                result.append("平均数为");
                if (size == 0) {
                    result.append("不存在");
                    return result.toString();
                }
                for (String value : cellData) {
                    try {
                        double num = Double.parseDouble(value);
                        sum += num;
                    } catch (Exception e) {
                        log.error("数据:" + value + " 无法转换成数字");
                    }
                }
                result.append(sum / size);
                return result.toString();
            case MODE:
                // 计算众数
                result.append("众数为");
                if (size == 0) {
                    result.append("不存在");
                    return result.toString();
                }
                Integer maxCount = 0;//用于存储最大的数量
                for (String value : cellData) {
                    if (value == null) {
                        continue;
                    }
                    if (!countMap.containsKey(value)) {
                        countMap.put(value, 0);
                    }
                    Integer count = countMap.get(value) + 1;
                    countMap.put(value, count);
                    maxCount = maxCount < count ? count : maxCount;
                }
                for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                    if (maxCount.equals(entry.getValue())) {
                        result.append(entry.getKey());
                        result.append("、");
                    }
                }
                return result.substring(0, result.length() - 1).toString();
            case MEAN:
                // 计算中位数
                result.append("中位数为 ");
                if (size == 0) {
                    result.append("不存在");
                    return result.toString();
                }
                List<Double> numList = new ArrayList<>();
                for (String value : cellData) {
                    try {
                        double num = Double.parseDouble(value);
                        numList.add(num);
                    } catch (Exception e) {
                        log.error("数据:" + value + " 无法转换成数字");
                    }
                }
                Collections.sort(numList);
                if (size % 2 == 0) {
                    result.append(numList.get(size / 2 - 1) + numList.get(size / 2));
                } else {
                    result.append(numList.get(size / 2));
                }
                return result.toString();
            case COUNT:
                //统计每个元素的计数
                result.append("每个元素的计数分别为");
                if (size == 0) {
                    result.append("不存在");
                    return result.toString();
                }
                for (String value : cellData) {
                    if (!countMap.containsKey(value)) {
                        countMap.put(value, 0);
                    }
                    Integer count = countMap.get(value) + 1;
                    countMap.put(value, count);
                }
                for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                    result.append(entry.getKey());
                    result.append(":");
                    result.append(entry.getValue());
                    result.append("、");
                }
                return result.substring(0, result.length() - 1).toString();
            case RANGE:
                // 计算数值区间,检查区间内哪些元素不存在
                result.append("未填写的数据为 ");
                String[] numRange = range.split("\\-");
                Long minNum = Long.parseLong(numRange[0]);
                Long maxNum = Long.parseLong(numRange[1]);
                HashSet<Long> numSet = new HashSet<>();
                for (long i = minNum; i <= maxNum; i++) {
                    numSet.add(i);
                }
                for (String value : cellData) {
                    try {
                        long num = Long.parseLong(value);
                        if (numSet.contains(num)) {
                            numSet.remove(num);
                        }
                    } catch (Exception e) {
                        log.error("数据:" + value + " 无法转换成数字");
                    }

                }
                if (numSet.isEmpty()) {
                    result.append("不存在");
                    return result.toString();
                }
                for (Long num : numSet) {
                    result.append(num);
                    result.append("、");
                }
                return result.substring(0, result.length() - 1).toString();
            case SET:
                // 计算数据集
                result.append("未填写的数据为 ");
                String[] dataRange = range.split("\\,");
                HashSet<String> dataSet = new HashSet<>();
                for (String data : dataRange) {
                    dataSet.add(data);
                }
                for (String value : cellData) {
                    if (dataSet.contains(value)) {
                        dataSet.remove(value);
                    }
                }
                if (dataSet.isEmpty()) {
                    result.append("不存在");
                    return result.toString();
                }
                for (String value : dataSet) {
                    result.append(value);
                    result.append("、");
                }
                return result.substring(0, result.length() - 1).toString();
        }
        return null;
    }

}