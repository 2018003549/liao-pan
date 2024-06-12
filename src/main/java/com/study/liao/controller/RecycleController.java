package com.study.liao.controller;

import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.annotation.VerifyParam;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.enums.FileDelFlagEnums;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.entity.vo.FileInfoVO;
import com.study.liao.entity.vo.PaginationResultVO;
import com.study.liao.entity.vo.ResponseVO;
import com.study.liao.service.FileInfoService;
import jakarta.servlet.http.HttpSession;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("recycleController")
@RequestMapping("recycle")
public class RecycleController extends ABaseController{
    @Autowired
    private FileInfoService fileInfoService;
    @RequestMapping("/loadRecycleList")
    @GlobalInterceptor
    public ResponseVO loadRecycleList(HttpSession session,Integer pageNo,Integer pageSize){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        FileInfoQuery query = new FileInfoQuery();
        query.setPageNo(pageNo);
        query.setPageSize(pageSize);
        query.setUserId(webUserDto.getUserId());
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        query.setOrderBy("recovery_time desc");
        PaginationResultVO<FileInfoEntity> listByPage = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(listByPage, FileInfoVO.class));
    }

    /**
     * 批量将回收站文件还原
     * @param fileIds 选中的文件
     */
    @RequestMapping("/recoverFile")
    @GlobalInterceptor
    public ResponseVO recoverFile(HttpSession session, @VerifyParam(required = true)String fileIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.recoverFile(webUserDto.getUserId(),fileIds);
        return getSuccessResponseVO(null);
    }
    /**
     * 从数据库彻底删除文件
     * @param fileIds
     * @return
     */
    @RequestMapping("delFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delFile(HttpSession session,@VerifyParam(required = true)String fileIds){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        fileInfoService.delFileBatch( webUserDto.getUserId(), fileIds, webUserDto.getAdmin());
        return getSuccessResponseVO(null);
    }
}
