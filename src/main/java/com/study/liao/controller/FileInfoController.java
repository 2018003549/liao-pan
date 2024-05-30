package com.study.liao.controller;

import java.util.Arrays;
import java.util.Map;

import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.enums.FileCategoryEnums;
import com.study.liao.entity.enums.FileDelFlagEnums;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.entity.vo.FileInfoVO;
import com.study.liao.entity.vo.PaginationResultVO;
import com.study.liao.entity.vo.ResponseVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.liao.entity.FileInfoEntity;
import com.study.liao.service.FileInfoService;




/**
 * 
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-30 17:06:10
 */
@RestController
@RequestMapping("file")
public class FileInfoController extends ABaseController{
    @Autowired
    private FileInfoService fileInfoService;
    @RequestMapping("/loadDataList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadDataList(HttpSession session, FileInfoQuery query, String category) {
        FileCategoryEnums categoryEnum = FileCategoryEnums.getByCode(category);
        if (null != categoryEnum) {
            query.setFileCategory(categoryEnum.getCategory());
        }
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto)session.getAttribute(Constants.SESSION_KEY);
        query.setUserId(sessionWebUserDto.getUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        PaginationResultVO result = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }
}
