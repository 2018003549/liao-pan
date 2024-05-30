package com.study.liao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.entity.vo.PaginationResultVO;
import org.apache.tomcat.jni.FileInfo;

import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-30 17:06:10
 */
public interface FileInfoService extends IService<FileInfoEntity> {

    /**
     * 根据条件查询列表
     */
    List<FileInfo> findListByParam(FileInfoQuery param);
    PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param);
}

