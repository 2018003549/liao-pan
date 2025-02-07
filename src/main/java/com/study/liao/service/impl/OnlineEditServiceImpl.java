package com.study.liao.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.liao.config.AppConfig;
import com.study.liao.dao.OnlineFileInfoDao;
import com.study.liao.dao.UserGroupDetailInfoDao;
import com.study.liao.dao.UserGroupInfoDao;
import com.study.liao.entity.OnlineFileInfoEntity;
import com.study.liao.entity.UserGroupDetailInfoEntity;
import com.study.liao.entity.UserGroupInfoEntity;
import com.study.liao.entity.UserInfoEntity;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.dto.ApprovalDTO;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.dto.UserGroupDetailDTO;
import com.study.liao.entity.enums.UserGroupStatusEnum;
import com.study.liao.entity.query.UserGroupQuery;
import com.study.liao.entity.vo.PaginationResultVO;
import com.study.liao.service.OnlineEditService;
import com.study.liao.service.UserInfoService;
import com.study.liao.util.FileUtils;
import com.study.liao.util.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("OnlineEditService")
public class OnlineEditServiceImpl implements OnlineEditService {
    @Autowired
    AppConfig appConfig;
    @Autowired
    OnlineFileInfoDao onlineFileInfoDao;
    @Autowired
    UserGroupInfoDao userGroupInfoDao;
    @Autowired
    UserGroupDetailInfoDao userGroupDetailInfoDao;
    @Autowired
    UserInfoService userInfoService;
    @Value("${onlyOffice.path}")
    String onlyOfficePath;

    @Override
    public Boolean uploadFile(SessionWebUserDto webUserDto, MultipartFile file, String fileName) {
        //1.上传前先校验当前文件是否上传过
        String userId = webUserDto.getUserId();
        LambdaQueryWrapper<OnlineFileInfoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OnlineFileInfoEntity::getFilename, fileName)
                .eq(OnlineFileInfoEntity::getCreateById, userId);
        Long count = onlineFileInfoDao.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException("请勿重复上传");
        }
        //2.上传到onlyOffice
        String json = HttpUtils.uploadFile(onlyOfficePath + "upload", file);
        log.info("上传结果===" + json);
        JSONObject jsonObject = JSON.parseObject(json);
        String storePath = jsonObject.getString("filename");
        if (StringUtils.isBlank(storePath)) {
            log.error("没有返回文件名信息");
            throw new BusinessException("上传失败");
        }
        //3.保存文件信息
        OnlineFileInfoEntity fileInfoEntity = new OnlineFileInfoEntity();
        //刚上传成功不给编辑，等创建人发布在设置为1
        fileInfoEntity.setStatus(0);
        fileInfoEntity.setFilename(fileName);
        fileInfoEntity.setCreateById(userId);
        fileInfoEntity.setIsDeleted(0);
        fileInfoEntity.setStorageAddress(storePath);
        Date date = new Date();
        fileInfoEntity.setCreateTime(date);
        fileInfoEntity.setUpdateTime(date);
        onlineFileInfoDao.insert(fileInfoEntity);
        return true;
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, String fileId) {
        //1.先校验文件id并且获取文件在onlyOffice的存储地址
        OnlineFileInfoEntity fileInfoEntity = onlineFileInfoDao.selectById(fileId);
        if (Objects.isNull(fileInfoEntity)) {
            throw new BusinessException("下载的文件不存在");
        }
        String storageAddress = fileInfoEntity.getStorageAddress();
        String filename = fileInfoEntity.getFilename();
        //2.通过存储地址去onlyOffice下载文件
        HashMap<String, String> params = new HashMap<>();
        params.put("fileName", storageAddress);
        byte[] downloadFile = null;
        try {
            downloadFile = HttpUtils.sendGetForBinary(onlyOfficePath + "download", params);
            if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {
                //IE浏览
                filename = URLEncoder.encode(filename, "UTF-8");
            } else {
                filename = new String(filename.getBytes("UTF-8"), "ISO8859-1");
            }
        } catch (IOException e) {
            log.error("远程下载失败");
            throw new RuntimeException(e);
        }
        response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
        FileUtils.readFile(response, downloadFile, filename);
    }

    @Override
    public Boolean insertUserGroup(String userId, UserGroupInfoEntity userGroupInfoEntity) {
        String groupName = userGroupInfoEntity.getGroupName();
        Integer maxSize = userGroupInfoEntity.getMaxSize();
        String description = userGroupInfoEntity.getDescription();
        if (StringUtils.isBlank(userId)) {
            throw new BusinessException("创建人信息有误");
        }
        if (StringUtils.isBlank(groupName)) {
            throw new BusinessException("未填写组名");
        }
        if (StringUtils.isBlank(description)) {
            throw new BusinessException("未填写描述信息");
        }
        if (maxSize == null || maxSize <= 0) {
            throw new BusinessException("最大人数有误");
        }
        //1.先创建用户组
        userGroupInfoEntity.setCreateById(userId);
        Date createTime = new Date();
        userGroupInfoEntity.setCreateTime(createTime);
        if (userGroupInfoDao.insert(userGroupInfoEntity) <= 0) {
            throw new BusinessException("用户组创建异常");
        }
        //2.然后创建人默认作为第一个用户加入该组
        UserGroupDetailInfoEntity groupDetail = new UserGroupDetailInfoEntity();
        groupDetail.setGroupId(userGroupInfoEntity.getId());
        groupDetail.setUserId(userId);
        groupDetail.setJoinTime(createTime);
        groupDetail.setApprovalStatus(UserGroupStatusEnum.APPROVED.getCode());
        return userGroupDetailInfoDao.insert(groupDetail) > 0;
    }

    @Override
    public PaginationResultVO selectUserGroupList(UserGroupQuery query) {
        //1.根据条件查询用户组列表
        List<UserGroupDetailDTO> resultList = userGroupDetailInfoDao.selectUserGroupListByQuery(query);
        //2.设置用户组详情
        setUserGroupDetail(query.getUserId(), resultList);
        //3.设置是否已申请
        //3.1查询当前用户申请的所有用户组
        UserGroupQuery currentQuery = new UserGroupQuery();
        currentQuery.setUserId(query.getUserId());
        currentQuery.setGroupName(query.getGroupName());
        List<UserGroupDetailDTO> currentUserGroupList = userGroupDetailInfoDao.selectCurrentUserGroupDetailList(currentQuery);
        Set<Long> currentSet = new HashSet<>();
        for (UserGroupDetailDTO groupDetailInfo : currentUserGroupList) {
            currentSet.add(groupDetailInfo.getGroupId());
        }
        //3.2已申请的用户组就设置标识
        for (UserGroupDetailDTO groupDetailInfo : resultList) {
            Long groupId = groupDetailInfo.getGroupId();
            groupDetailInfo.setIsActive(currentSet.contains(groupId));
        }
        PaginationResultVO resultVO = new PaginationResultVO();
        resultVO.setList(resultList);
        return resultVO;
    }

    @Override
    public PaginationResultVO selectCurrentUserGroupList(UserGroupQuery query) {
        //1.查询当前用户的所有已申请的用户组
        UserGroupQuery currentQuery = new UserGroupQuery();
        currentQuery.setUserId(query.getUserId());
        currentQuery.setGroupName(query.getGroupName());
        List<UserGroupDetailDTO> resultList = userGroupDetailInfoDao.selectCurrentUserGroupDetailList(currentQuery);
        //2.设置用户组详情
        setUserGroupDetail(query.getUserId(), resultList);
        PaginationResultVO resultVO = new PaginationResultVO();
        resultVO.setList(resultList);
        return resultVO;
    }

    private void setUserGroupDetail(String userId, List<UserGroupDetailDTO> resultList) {
        //2.设置创建人名称
        Set<String> createByIdList = new HashSet<>();
        for (UserGroupDetailDTO groupDetailInfo : resultList) {
            createByIdList.add(groupDetailInfo.getCreateById());
        }
        //2.1.用创建人id列表去查询创建人名称
        List<UserInfoEntity> userInfoList = userInfoService.listByIds(createByIdList);
        Map<String, String> userMap = userInfoList.stream()
                .collect(Collectors.toMap(
                        UserInfoEntity::getUserId,
                        UserInfoEntity::getNickName
                ));
        //2.2.设置创建人名称以及是否是自己创建的
        for (UserGroupDetailDTO groupDetailInfo : resultList) {
            String createById = groupDetailInfo.getCreateById();
            if (userMap.containsKey(createById)) {
                groupDetailInfo.setCreateByName(userMap.get(createById));
            }
            if (userId != null) {
                groupDetailInfo.setIsCreated(userId.equals(createById));
            }
        }
        //3.设置每个用户组的当前人数，即审批通过的人数
        for (UserGroupDetailDTO groupDetailInfo : resultList) {
            Long groupId = groupDetailInfo.getGroupId();
            LambdaQueryWrapper<UserGroupDetailInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserGroupDetailInfoEntity::getGroupId, groupId)
                    .eq(UserGroupDetailInfoEntity::getApprovalStatus, UserGroupStatusEnum.APPROVED.getCode());
            Long count = userGroupDetailInfoDao.selectCount(queryWrapper);
            groupDetailInfo.setCurrentSize(count);
        }
    }

    @Override
    public Boolean joinGroup(String userId, Integer groupId) {
        UserGroupDetailInfoEntity groupDetail = new UserGroupDetailInfoEntity();
        groupDetail.setGroupId(groupId);
        groupDetail.setUserId(userId);
        groupDetail.setJoinTime(new Date());
        groupDetail.setApprovalStatus(UserGroupStatusEnum.PENDING.getCode());
        return userGroupDetailInfoDao.insert(groupDetail) > 0;
    }

    @Override
    public List<UserInfoEntity> selectPendingUserList(Integer groupId) {
        //1.查询当前用户组出待审批的记录
        LambdaQueryWrapper<UserGroupDetailInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserGroupDetailInfoEntity::getGroupId, groupId)
                .eq(UserGroupDetailInfoEntity::getIsDeleted, 0)
                .eq(UserGroupDetailInfoEntity::getApprovalStatus, UserGroupStatusEnum.PENDING.getCode());
        List<UserGroupDetailInfoEntity> groupDetailList = userGroupDetailInfoDao.selectList(queryWrapper);
        //2.收集待审批的用户id
        if (CollectionUtils.isEmpty(groupDetailList)) {
            return null;
        }
        List<String> userIds = groupDetailList.stream()
                .map(UserGroupDetailInfoEntity::getUserId)  // 提取每个对象的userId
                .collect(Collectors.toList());  // 收集为List
        //3.查询用户信息
        List<UserInfoEntity> userList = userInfoService.listByIds(userIds);
        return userList;
    }

    /**
     * 修改指定的申请记录的审批状态
     */
    @Override
    public Boolean approval(ApprovalDTO approvalDTO) {
        Integer groupId = approvalDTO.getGroupId();
        String userId = approvalDTO.getUserId();
        LambdaQueryWrapper<UserGroupDetailInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserGroupDetailInfoEntity::getGroupId, groupId)
                .eq(UserGroupDetailInfoEntity::getUserId, userId)
                .eq(UserGroupDetailInfoEntity::getIsDeleted, 0);
        UserGroupDetailInfoEntity updateData = new UserGroupDetailInfoEntity();
        updateData.setApprovalStatus(approvalDTO.getIsPassed() ?
                UserGroupStatusEnum.APPROVED.getCode() :
                UserGroupStatusEnum.REJECTED.getCode());
        return userGroupDetailInfoDao.update(updateData, queryWrapper) > 0;
    }

    @Override
    public PaginationResultVO selectUploadFileList(String userId) {
        LambdaQueryWrapper<OnlineFileInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OnlineFileInfoEntity::getCreateById, userId)
                .eq(OnlineFileInfoEntity::getIsDeleted, 0);
        PaginationResultVO resultVO = new PaginationResultVO<>();
        List<OnlineFileInfoEntity> resultList = onlineFileInfoDao.selectList(queryWrapper);
        resultVO.setList(resultList);
        return resultVO;
    }
}
