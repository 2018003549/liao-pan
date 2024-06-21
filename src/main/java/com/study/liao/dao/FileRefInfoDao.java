package com.study.liao.dao;

import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.FileRefInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-06-21 13:56:14
 */
@Mapper
public interface FileRefInfoDao extends BaseMapper<FileRefInfoEntity> {

    void addFileCount(@Param("fileMd5") String fileMd5);

    void decreaseFileRefBatch(@Param("md5Map") Map<String, Long> md5Map);
}
