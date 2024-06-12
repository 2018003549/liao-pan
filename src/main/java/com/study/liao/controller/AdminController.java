package com.study.liao.controller;

import com.common.utils.PageUtils;
import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.annotation.VerifyParam;
import com.study.liao.component.RedisComponent;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.dto.SysSettingsDto;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.entity.query.UserInfoQuery;
import com.study.liao.entity.vo.PaginationResultVO;
import com.study.liao.entity.vo.ResponseVO;
import com.study.liao.service.FileInfoService;
import com.study.liao.service.UserInfoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController("adminController")
@RequestMapping("/admin")
public class AdminController extends ABaseController {
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    RedisComponent redisComponent;
    @Autowired
    UserInfoService userInfoService;

    /**
     * 获取系统信息
     */
    @RequestMapping("/getSysSettings")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSettings() {
        return getSuccessResponseVO(redisComponent.getSysSettingsDto());
    }

    /**
     * 更新系统信息
     */
    @RequestMapping("/saveSysSettings")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO saveSysSettings(
            @VerifyParam(required = true) String registerEmailTitle,
            @VerifyParam(required = true) String registerEmailContent,
            @VerifyParam(required = true) Integer userInitUseSpace) {
        SysSettingsDto sysSettingsDto = new SysSettingsDto();
        sysSettingsDto.setRegisterEmailTitle(registerEmailTitle);
        sysSettingsDto.setRegisterEmailContent(registerEmailContent);
        sysSettingsDto.setUserInitUseSpace(userInitUseSpace);
        redisComponent.saveSysSettings(sysSettingsDto);
        return getSuccessResponseVO(redisComponent.getSysSettingsDto());
    }

    /**
     * 查询出所有用户信息
     *
     * @param userInfoQuery 用户信息筛选条件加分页参数
     * @return 用户信息列表
     */
    @RequestMapping("/loadUserList")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO loadUserList(UserInfoQuery userInfoQuery) {
        PageUtils pageUtils = userInfoService.loadUserList(userInfoQuery);
        return getSuccessResponseVO(pageUtils);
    }

    /**
     * 修改用户的状态
     *
     * @param userId 需要修改的用户
     * @param status 新状态
     */
    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO updateUserStatus(@VerifyParam(required = true) String userId,
                                       @VerifyParam(required = true) Integer status, HttpSession session) {
        userInfoService.updateUserStatus(userId, status);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateUserSpace")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO updateUserSpace(@VerifyParam(required = true) String userId,
                                      @VerifyParam(required = true) Integer changeSpace) {
        userInfoService.updateUserSpace(userId, changeSpace);
        return getSuccessResponseVO(null);
    }

    /**
     * 查询所有文件
     *
     * @param fileQuery 文件信息筛选条件加分页参数
     * @return 文件信息列表
     */
    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO loadFileList(FileInfoQuery fileQuery) {
        fileQuery.setOrderBy("last_update_time desc");
        fileQuery.setQueryNickName(true);
        PaginationResultVO<FileInfoEntity> listByPage = fileInfoService.findListByPage(fileQuery);
        return getSuccessResponseVO(listByPage);
    }

    /**
     * 预览某个用户的某个文件
     */
    @RequestMapping("/getFile/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public void getFile(HttpServletResponse response,
                        @PathVariable("fileId") String fileId,
                        @PathVariable("userId") String userId) {
        super.getFile(response, fileId, userId);
    }
    /**
     * 删除用户文件
     * @param fileIdAndUserIds 形如 userId_fileId,userId_fileId...
     */
    @RequestMapping("delFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delFile(@VerifyParam(required = true)String fileIdAndUserIds){
        //1.分隔出文件记录【每条记录包含用户id_文件id】
        String[] fileIdAndUserIdArray = fileIdAndUserIds.split(",");
        //2.封装每个用户的待删文件
        HashMap<String, List<String>> userFileIdsMap=new HashMap<>();//key为用户id，value为要删除的文件列表
        for (String fileIdAndUserId : fileIdAndUserIdArray) {
            String[] fileInfoItem = fileIdAndUserId.split("_");
            String userId=fileInfoItem[0];
            String fileId=fileInfoItem[1];
            if(!userFileIdsMap.containsKey(userId)){
                userFileIdsMap.put(userId,new ArrayList<>());
            }
            userFileIdsMap.get(userId).add(fileId);
        }
        //3.对每个用户的待删文件进行批量删除
        for (Map.Entry<String, List<String>> userFileIdsInfo : userFileIdsMap.entrySet()) {
            String userId = userFileIdsInfo.getKey();
            List<String> fileIdList = userFileIdsInfo.getValue();
            String fileIds = StringUtils.join(fileIdList, ",");
            fileInfoService.delFileBatch(userId,fileIds,true);
        }
        return getSuccessResponseVO(null);
    }
    /**
     * 创建某个用户的某个文件的下载链接
     * @param fileId 需要下载的文件id
     * @param userId 选中的用户id
     * @return 临时下载码
     */
    @RequestMapping("createDownloadUrl/{userId}/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO createDownloadUrl(
            @PathVariable(required = true) String fileId
            , @PathVariable(required = true) String userId) {
        return super.createDownloadUrl(fileId, userId);
    }
    @RequestMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)//不需要校验登录
    public void download(HttpServletRequest request, HttpServletResponse response,
                         @VerifyParam(required = true) @PathVariable("code") String code) throws UnsupportedEncodingException {
        super.download(request, response, code);
    }
}
