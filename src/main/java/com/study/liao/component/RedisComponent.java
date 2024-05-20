package com.study.liao.component;

import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.SysSettingsDto;
import com.study.liao.entity.dto.UserSpaceDto;
import com.study.liao.util.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;



@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;
    public SysSettingsDto getSysSettingsDto(){
        //从redis中获取系统配置
        SysSettingsDto sysSettingsDto = (SysSettingsDto)redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if(null==sysSettingsDto){
            //如果没有存储系统配置就新建一个，并且存放到redis中
            sysSettingsDto=new SysSettingsDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingsDto);
        }
        return sysSettingsDto;
    }
    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto){
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE+userId,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
    }
}
