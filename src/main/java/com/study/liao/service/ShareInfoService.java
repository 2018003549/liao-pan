package com.study.liao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.utils.PageUtils;
import com.study.liao.entity.ShareInfoEntity;
import com.study.liao.entity.dto.SessionShareDto;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-06-09 14:05:18
 */
public interface ShareInfoService extends IService<ShareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils loadShareList(String userId, HashMap<String, Object> map);

    ShareInfoEntity shareFile(String userId, String fileId, Integer vaildType, String code);

    void cancelShareBatch(String userId, String shareIds);

    SessionShareDto checkShareCode(String shareId, String code);
}

