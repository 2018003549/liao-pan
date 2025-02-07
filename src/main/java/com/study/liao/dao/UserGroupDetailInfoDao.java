package com.study.liao.dao;

import com.study.liao.entity.UserGroupDetailInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.liao.entity.dto.UserGroupDetailDTO;
import com.study.liao.entity.query.UserGroupQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 
 * 
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2025-02-04 13:57:00
 */
@Mapper
public interface UserGroupDetailInfoDao extends BaseMapper<UserGroupDetailInfoEntity> {

    List<UserGroupDetailDTO> selectCurrentUserGroupDetailList(@Param("query") UserGroupQuery query);

    List<UserGroupDetailDTO> selectUserGroupListByQuery(@Param("query")UserGroupQuery query);
}
