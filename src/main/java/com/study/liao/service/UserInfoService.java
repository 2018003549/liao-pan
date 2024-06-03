package com.study.liao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liao.common.utils.PageUtils;
import com.study.liao.entity.UserInfoEntity;
import com.study.liao.entity.dto.SessionWebUserDto;

import java.util.Map;

/**
 * 
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-03 18:10:47
 */
public interface UserInfoService extends IService<UserInfoEntity> {

    void  register(String email, String nickName, String password, String emailCode);

    PageUtils queryPage(Map<String, Object> params);

    UserInfoEntity selectByEmail(String email);
    SessionWebUserDto login(String email,String password);

    void resetPwd(String email, String password, String emailCode);

    Integer updateUseSpace(String userId, Long useSpace, Long totalSpace);
}

