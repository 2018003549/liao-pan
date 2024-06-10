package com.study.liao.component;

import com.study.liao.dao.FileInfoMapper;
import com.study.liao.entity.UserInfoEntity;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.DownloadFileDto;
import com.study.liao.entity.dto.SysSettingsDto;
import com.study.liao.entity.dto.UserSpaceDto;
import com.study.liao.service.UserInfoService;
import com.study.liao.util.RedisUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;
    @Resource
    private FileInfoMapper fileInfoMapper;
    @Resource
    private UserInfoService userInfoService;

    public SysSettingsDto getSysSettingsDto() {
        //从redis中获取系统配置
        SysSettingsDto sysSettingsDto = (SysSettingsDto) redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if (null == sysSettingsDto) {
            //如果没有存储系统配置就新建一个，并且存放到redis中
            sysSettingsDto = new SysSettingsDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
        }
        return sysSettingsDto;
    }

    public void saveUserSpaceUse(String userId, UserSpaceDto userSpaceDto) {
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE + userId, userSpaceDto, Constants.REDIS_KEY_EXPIRES_DAY);
    }

    public UserSpaceDto getUseSpace(String userId) {
        UserSpaceDto spaceDto = (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
        if (spaceDto == null) {
            //如果没有用户空间使用情况，就得刷新缓存
            spaceDto = new UserSpaceDto();
            //1.查询用户空间使用情况
            Long useSpace = fileInfoMapper.selectUseSpace(userId);
            spaceDto.setUseSpace(useSpace);
            //2.查询用户总空间
            UserInfoEntity userInfo = userInfoService.getById(userId);
            spaceDto.setTotalSpace(userInfo.getTotalSpace());
            saveUserSpaceUse(userId, spaceDto);
        }
        return spaceDto;
    }

    /**
     * 根据用户id和文件id获取到缓存中用户临时文件的大小
     *
     * @param userId
     * @param fileId
     * @return 临时文件的大小
     */
    public Long getFileTempSize(String userId, String fileId) {
        Object sizeObj = redisUtils.get(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId);
        if (sizeObj == null) {
            return 0L;
        } else if (sizeObj instanceof Integer) {
            return ((Integer) sizeObj).longValue();
        } else {
            return (Long) sizeObj;
        }
    }

    /**
     * 保存/更新用户的临时文件大小
     *
     * @param userId
     * @param fileId
     * @param fileSize 新添加的分片大小
     */
    public void saveFileTempSize(String userId, String fileId, Long fileSize) {
        //1.获取当前该临时文件的大小
        Long currentTempSize = getFileTempSize(userId, fileId);
        //2.保存/更新临时文件大小,一个小时过期时间
        redisUtils.setex(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId,
                currentTempSize + fileSize, Constants.REDIS_KEY_EXPIRES_ONE_HOUR);
    }

    /**
     * 保存下载信息，通过临时下载code标识
     *
     * @param code            临时下载code
     * @param downloadFileDto 下载基本信息，包含文件名、文件存储路径
     */
    public void saveDownloadCode(String code, DownloadFileDto downloadFileDto) {
        redisUtils.setex(Constants.REDIS_KEY_DOWNLOAD + code, downloadFileDto, Constants.REDIS_KEY_EXPIRES_FIVE_MIN);
    }

    /**
     * 根据临时下载code获取到下载信息
     *
     * @param code 临时下载code
     * @return 下载基本信息，包含文件名、文件存储路径
     */
    public DownloadFileDto getDownloadCode(String code) {
        return (DownloadFileDto) redisUtils.get(Constants.REDIS_KEY_DOWNLOAD + code);
    }

    public void saveSysSettings(SysSettingsDto sysSettingsDto) {
        redisUtils.set(Constants.REDIS_KEY_SYS_SETTING, sysSettingsDto);
    }

    public void saveUserTotalSpace(String userId,Long totalSpace) {
        UserSpaceDto userSpaceDto = new UserSpaceDto();
        Long useSpace = fileInfoMapper.selectUseSpace(userId);
        userSpaceDto.setUseSpace(useSpace);
        userSpaceDto.setTotalSpace(totalSpace);
        redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE+userId,userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
    }

    public void saveUserStatus(String userId, int status) {
        redisUtils.setex(Constants.REDIS_USER_STATUS+userId,status,Constants.REDIS_KEY_EXPIRES_ONE_HOUR);
    }
    public Integer getUserStatus(String userId) {
        return (Integer) redisUtils.get(Constants.REDIS_USER_STATUS+userId);
    }
}
