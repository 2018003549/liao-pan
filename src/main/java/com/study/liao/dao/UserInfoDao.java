package com.study.liao.dao;

import com.study.liao.entity.UserInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 
 * 
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-03 18:10:47
 */
@Mapper
public interface UserInfoDao extends BaseMapper<UserInfoEntity> {

    Integer updateUseSpace(@Param("userId") String userId, @Param("useSpace") Long useSpace, @Param("totalSpace") Long totalSpace);
}
