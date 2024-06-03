package com.study.liao.service.impl;

import com.study.liao.component.RedisComponent;
import com.study.liao.config.AppConfig;
import com.study.liao.dao.FileInfoMapper;
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
    @Autowired
    FileInfoMapper fileInfoMapper;
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
        userInfo.setEmail(email);
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
        //1.登录校验【这一块password不用再转成密文，因为前端传输时就已经转化了】
        if(null==userInfoEntity||!userInfoEntity.getPassword().equals(password)){
            throw new BusinessException("账号密码错误");
        }
        if(UserStatusEnum.DISABLE.getStatus().equals(userInfoEntity.getStatus())){
            throw new BusinessException("账号已禁用");
        }
        //2.更新登录时间
        String userId = userInfoEntity.getUserId();
        UserInfoEntity userInfo = new UserInfoEntity();
        userInfo.setLastLoginTime(new Date());
        userInfo.setUserId(userId);
        updateById(userInfoEntity);
        SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
        sessionWebUserDto.setNickName(userInfo.getNickName());
        sessionWebUserDto.setNickName(userInfo.getUserId());
        sessionWebUserDto.setUserId(userInfo.getUserId());
        //3.判断是否为管理员【可能有多个管理员】
        if(ArrayUtils.contains(appConfig.getAdminUserName().split(","),email)){
            sessionWebUserDto.setIsAdmin(true);
        }else {
            sessionWebUserDto.setIsAdmin(false);
        }
        //4.更新用户空间信息
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        // 实时查询文件占用情况
        Long useSpace = fileInfoMapper.selectUseSpace(userId);
        userSpaceDto.setUseSpace(useSpace);
        UserInfoEntity lastUserInfo = getById(userId);
        userSpaceDto.setTotalSpace(lastUserInfo.getTotalSpace());
        //刷新缓存中的用户空间信息
        redisComponent.saveUserSpaceUse(userInfo.getUserId(),userSpaceDto);
        return sessionWebUserDto;
    }

    @Override
    @Transactional
    public void resetPwd(String email, String password, String emailCode) {
        UserInfoEntity userInfoEntity=selectByEmail(email);
        //1.登录校验
        if(null==userInfoEntity){
            throw new BusinessException("邮箱账号不存在");
        }
        if(UserStatusEnum.DISABLE.getStatus().equals(userInfoEntity.getStatus())){
            throw new BusinessException("账号已禁用");
        }
        emailCodeService.checkCode(email,emailCode);
        userInfoEntity.setPassword(password);
        baseMapper.updateById(userInfoEntity);
    }

    @Override
    public Integer updateUseSpace(String userId, Long useSpace, Long totalSpace) {
        return baseMapper.updateUseSpace(userId,useSpace,totalSpace);
    }
}