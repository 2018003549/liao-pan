package com.study.liao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liao.common.utils.PageUtils;
import com.study.liao.entity.EmailCodeEntity;

import java.util.Map;

/**
 * 
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-04 19:27:15
 */
public interface EmailCodeService extends IService<EmailCodeEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void sendEmailCode(String email, Integer type);

    void checkCode(String email, String emailCode);
}

