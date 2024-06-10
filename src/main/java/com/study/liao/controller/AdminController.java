package com.study.liao.controller;

import com.common.utils.PageUtils;
import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.annotation.VerifyParam;
import com.study.liao.component.RedisComponent;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.dto.SysSettingsDto;
import com.study.liao.entity.query.UserInfoQuery;
import com.study.liao.entity.vo.ResponseVO;
import com.study.liao.service.FileInfoService;
import com.study.liao.service.UserInfoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping("/loadUserList")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO loadUserList(UserInfoQuery userInfoQuery) {
        PageUtils pageUtils = userInfoService.loadUserList(userInfoQuery);
        return getSuccessResponseVO(pageUtils);
    }

    /**
     * 修改用户的状态
     * @param userId 需要修改的用户
     * @param status 新状态
     */
    @RequestMapping("/updateUserStatus")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO updateUserStatus(@VerifyParam(required = true)String userId,
                                       @VerifyParam(required = true)Integer status, HttpSession session) {
        userInfoService.updateUserStatus(userId,status);
        return getSuccessResponseVO(null);
    }
    @RequestMapping("/updateUserSpace")
    @GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO updateUserSpace(@VerifyParam(required = true)String userId,
                                       @VerifyParam(required = true)Integer changeSpace) {
        userInfoService.updateUserSpace(userId,changeSpace);
        return getSuccessResponseVO(null);
    }
}
