package com.study.liao.dao;

import com.study.liao.entity.EmailCodeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 
 * 
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-04 19:27:15
 */
@Mapper
public interface EmailCodeDao extends BaseMapper<EmailCodeEntity> {

    void disableEmailCode(@Param("email") String email);
}
