package com.study.liao.controller;

import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.annotation.VerifyParam;
import com.study.liao.entity.OnlineFileInfoEntity;
import com.study.liao.entity.UserGroupInfoEntity;
import com.study.liao.entity.UserInfoEntity;
import com.study.liao.entity.dto.ApprovalDTO;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.dto.UserGroupDetailDTO;
import com.study.liao.entity.query.UserGroupQuery;
import com.study.liao.entity.vo.PaginationResultVO;
import com.study.liao.entity.vo.ResponseVO;
import com.study.liao.service.OnlineEditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController("OnlineEditController")
@RequestMapping("/onlineEdit")
public class OnlineEditController extends ABaseController {
    @Autowired
    OnlineEditService onlineEditService;

    /**
     * 上传文件到文档编辑器
     * @param session  用于获取上传人
     * @param fileName 原文件名
     * @param file     上传文件流
     */
    @PostMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO uploadFile(HttpSession session, MultipartFile file,
                                 @VerifyParam(required = true) String fileName) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        return getSuccessResponseVO(onlineEditService.uploadFile(webUserDto, file, fileName));
    }

    @GetMapping("/download/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public void download(HttpServletRequest request, HttpServletResponse response,
                         @PathVariable(required = true) String fileId) {
        onlineEditService.download(request,response,fileId);
    }

    /**
     * 创建用户组
     * @param session 创建人信息
     * @param userGroupInfoEntity 用户组基本信息
     * @return
     */
    @PostMapping("/insertUserGroup")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO insertUserGroup(HttpSession session, UserGroupInfoEntity userGroupInfoEntity) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        return getSuccessResponseVO(onlineEditService.insertUserGroup(webUserDto.getUserId(), userGroupInfoEntity));
    }

    @GetMapping("/current/userGroupList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<List<UserGroupDetailDTO>> selectCurrentUserGroupList(HttpSession session, UserGroupQuery query){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        query.setUserId(webUserDto.getUserId());
        return getSuccessResponseVO(onlineEditService.selectCurrentUserGroupList(query));
    }

    @GetMapping("/pending/{groupId}")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<List<UserInfoEntity>> selectPendingUserList(@PathVariable("groupId")Integer groupId){
        return getSuccessResponseVO(onlineEditService.selectPendingUserList(groupId));
    }

    @GetMapping("/userGroupList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<List<UserGroupDetailDTO>> selectUserGroupList(HttpSession session, UserGroupQuery query){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        query.setUserId(webUserDto.getUserId());
        return getSuccessResponseVO(onlineEditService.selectUserGroupList(query));
    }

    @PostMapping("/joinGroup/{groupId}")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO joinGroup(HttpSession session, @PathVariable("groupId")Integer groupId){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        return getSuccessResponseVO(onlineEditService.joinGroup(webUserDto.getUserId(),groupId));
    }

    @PostMapping("/approval")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO approval(ApprovalDTO approvalDTO) {
        return getSuccessResponseVO(onlineEditService.approval(approvalDTO));
    }

    /**
     * 查询当前用户上传的在线编辑文档信息
     */
    @GetMapping("/uploadFileList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO<List<OnlineFileInfoEntity>> selectUploadFileList(HttpSession session){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        return getSuccessResponseVO(onlineEditService.selectUploadFileList(webUserDto.getUserId()));
    }
}
