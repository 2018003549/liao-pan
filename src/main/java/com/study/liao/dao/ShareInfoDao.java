package com.study.liao.dao;

import com.study.liao.entity.ShareInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 
 * 
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-06-09 14:05:18
 */
@Mapper
public interface ShareInfoDao extends BaseMapper<ShareInfoEntity> {

    void updateShareShowCount(@Param("shareId") String shareId);
}
