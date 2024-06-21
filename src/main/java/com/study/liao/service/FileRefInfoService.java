package com.study.liao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.utils.PageUtils;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.FileRefInfoEntity;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-06-21 13:56:14
 */
public interface FileRefInfoService extends IService<FileRefInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateFileCount(String fileMd5);

    void insertFileCount(String fileMd5, String filePath);

    void decreaseFileRefBatch(List<FileInfoEntity> fileInfoList);
}

