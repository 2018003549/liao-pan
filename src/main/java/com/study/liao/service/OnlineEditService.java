package com.study.liao.service;

import com.study.liao.entity.UserGroupInfoEntity;
import com.study.liao.entity.UserInfoEntity;
import com.study.liao.entity.dto.ApprovalDTO;
import com.study.liao.entity.dto.DownloadFileDto;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.query.UserGroupQuery;
import com.study.liao.entity.vo.PaginationResultVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface OnlineEditService {
    Boolean uploadFile(SessionWebUserDto webUserDto, MultipartFile file, String fileName);

    void download(HttpServletRequest request, HttpServletResponse response, String fileId);

    DownloadFileDto download(String fileId);

    Boolean insertUserGroup(String userId, UserGroupInfoEntity userGroupInfoEntity);

    PaginationResultVO selectUserGroupList(UserGroupQuery query);

    PaginationResultVO selectCurrentUserGroupList(UserGroupQuery query);

    Boolean joinGroup(String userId, Integer groupId);

    List<UserInfoEntity> selectPendingUserList(Integer groupId);

    Boolean approval(ApprovalDTO approvalDTO);

    PaginationResultVO selectUploadFileList(String userId);
}
