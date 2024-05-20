package com.study.liao.service.impl;

import com.study.liao.component.RedisComponent;
import com.study.liao.config.AppConfig;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.dto.SysSettingsDto;
import com.study.liao.entity.dto.UserSpaceDto;
import com.study.liao.entity.enums.UserStatusEnum;
import com.study.liao.service.EmailCodeService;
import com.study.liao.util.StringTools;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liao.common.utils.PageUtils;
import com.liao.common.utils.Query;

import com.study.liao.dao.UserInfoDao;
import com.study.liao.entity.UserInfoEntity;
import com.study.liao.service.UserInfoService;
import org.springframework.transaction.annotation.Transactional;


@Service("userInfoService")
public class UserInfoServiceImpl extends ServiceImpl<UserInfoDao, UserInfoEntity> implements UserInfoService {
    @Autowired
    EmailCodeService emailCodeService;
    @Autowired
    RedisComponent redisComponent;
    @Autowired
    AppConfig appConfig;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, String password, String emailCode) {
        //1.查询当前邮箱是否存在
        UserInfoEntity userInfo = baseMapper.selectOne(new QueryWrapper<UserInfoEntity>()
                .eq("email", email));
        if (null != userInfo) {
            throw new BusinessException("邮箱账号已存在");
        }
        //2.查询当前用户名称是否存放
        userInfo = baseMapper.selectOne(new QueryWrapper<UserInfoEntity>()
                .eq("nick_name", nickName));
        if (null != userInfo) {
            throw new BusinessException("昵称已经存在");
        }
        //3.校验邮箱验证码
        emailCodeService.checkCode(email, emailCode);
        String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
        userInfo = new UserInfoEntity();
        userInfo.setUserId(userId);
        userInfo.setNickName(nickName);
        userInfo.setPassword(StringTools.encodeByMd5(password));
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setUseSpace(0L);
        SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();
        userInfo.setTotalSpace(sysSettingsDto.getUserInitUseSpace()*Constants.MB);
        save(userInfo);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<UserInfoEntity> page = this.page(
                new Query<UserInfoEntity>().getPage(params),
                new QueryWrapper<UserInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public UserInfoEntity selectByEmail(String email) {
        return baseMapper.selectOne(new QueryWrapper<UserInfoEntity>().eq("email", email));
    }

    @Override
    public SessionWebUserDto login(String email, String password) {
        UserInfoEntity userInfoEntity=selectByEmail(email);
        if(null==userInfoEntity||!userInfoEntity.getPassword().equals(StringTools.encodeByMd5(password))){
            throw new BusinessException("账号密码错误");
        }
        if(UserStatusEnum.DISABLE.getStatus().equals(userInfoEntity.getStatus())){
            throw new BusinessException("账号已禁用");
        }
        //1.更新登录时间
        UserInfoEntity userInfo = new UserInfoEntity();
        userInfo.setLastLoginTime(new Date());
        userInfo.setUserId(userInfoEntity.getUserId());
        updateById(userInfoEntity);
        SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
        sessionWebUserDto.setNickName(userInfo.getNickName());
        sessionWebUserDto.setNickName(userInfo.getUserId());
        //2.判断是否为管理员【可能有多个管理员】
        if(ArrayUtils.contains(appConfig.getAdminUserName().split(","),email)){
            sessionWebUserDto.setIsAdmin(true);
        }else {
            sessionWebUserDto.setIsAdmin(false);
        }
        //3.更新用户空间信息
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        //TODO 实时查询文件占用情况userSpaceDto.setUserSpace();
        userSpaceDto.setTotalSpace(userInfoEntity.getTotalSpace());
        //刷新缓存中的用户空间信息
        redisComponent.saveUserSpaceUse(userInfo.getUserId(),userSpaceDto);
        return null;
    }

}