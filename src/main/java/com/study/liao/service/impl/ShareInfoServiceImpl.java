package com.study.liao.service.impl;

import com.common.utils.PageUtils;
import com.common.utils.Query;
import com.study.liao.dao.FileInfoMapper;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.SessionShareDto;
import com.study.liao.entity.enums.FileDelFlagEnums;
import com.study.liao.entity.enums.ResponseCodeEnum;
import com.study.liao.entity.enums.ShareValidTypeEnums;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.service.FileInfoService;
import com.study.liao.util.DateUtil;
import com.study.liao.util.StringTools;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.study.liao.dao.ShareInfoDao;
import com.study.liao.entity.ShareInfoEntity;
import com.study.liao.service.ShareInfoService;


@Service("shareInfoService")
public class ShareInfoServiceImpl extends ServiceImpl<ShareInfoDao, ShareInfoEntity> implements ShareInfoService {
    @Autowired
    FileInfoService fileInfoService;
    @Autowired
    ShareInfoDao shareInfoMapper;
    @Resource
    private FileInfoMapper<FileInfoEntity, FileInfoQuery> fileInfoMapper;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ShareInfoEntity> page = this.page(
                new Query<ShareInfoEntity>().getPage(params),
                new QueryWrapper<ShareInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils loadShareList(String userId, HashMap<String, Object> map) {
        //1.查询某个用户下的分享分页数据
        IPage<ShareInfoEntity> page = this.page(
                new Query<ShareInfoEntity>().getPage(map),
                new QueryWrapper<ShareInfoEntity>().eq("user_id",userId)
        );
        //2.根据分享数据中的文件id查询到文件名并返回
        List<ShareInfoEntity> shareInfoEntityList = page.getRecords();
        if(shareInfoEntityList!=null){
            page.setTotal(shareInfoEntityList.size());
        }
        if(shareInfoEntityList==null||shareInfoEntityList.isEmpty()){
            return new PageUtils(page);
        }
        //2.1先存储文件id
        List<String> fileIds=shareInfoEntityList.stream().
                map(ShareInfoEntity::getFileId).collect(Collectors.toList());
        //2.2然后批量查询
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFileIdArray(fileIds.toArray(new String[]{}));
        fileInfoQuery.setUserId(userId);
        List<FileInfoEntity> fileInfoList = fileInfoMapper.selectList(fileInfoQuery);
        //2.3用map存储文件id映射
        Map<String, FileInfoEntity> fileIdMap = fileInfoList.stream().
                collect(Collectors.toMap(FileInfoEntity::getFileId, Function.identity()
                , (file1, file2) -> file2));
        //2.4封装文件名
        List<ShareInfoEntity> delShareInfoList=new ArrayList<>();//用来存储已经失效的链接
        int index=0;
        while(index<shareInfoEntityList.size()){
            ShareInfoEntity shareInfoEntity = shareInfoEntityList.get(index);
            FileInfoEntity fileInfo = fileIdMap.get(shareInfoEntity.getFileId());
            if(fileInfo==null||!FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())){
                //分享的文件已经不存在了，需要将其删除
                delShareInfoList.add(shareInfoEntity);//存入删除列表等待批量删除
                shareInfoEntityList.remove(shareInfoEntity);//返回数据中移除
                continue;
            }
            shareInfoEntity.setFileName(fileInfo.getFileName());
            index++;
        }
        //2.5批量删除失效文件
        removeBatchByIds(delShareInfoList);
        return new PageUtils(page);
    }

    @Override
    public ShareInfoEntity shareFile(String userId, String fileId, Integer vaildType, String code) {
        ShareInfoEntity shareInfoEntity = new ShareInfoEntity();
        //1.先检查有效时间的类别
        ShareValidTypeEnums byType = ShareValidTypeEnums.getByType(vaildType);
        if(null==byType){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //非永久生效就要计算失效时间
        if(ShareValidTypeEnums.FOREVER!=byType){
            shareInfoEntity.setExpireTime(DateUtil.getAfterDate(byType.getDays()));
        }
        //2.封装分享信息
        Date date = new Date();
        shareInfoEntity.setShareId(StringTools.getRandomString(Constants.LENGTH_20));
        shareInfoEntity.setShareTime(date);
        shareInfoEntity.setValidType(vaildType);
        shareInfoEntity.setFileId(fileId);
        shareInfoEntity.setUserId(userId);
        if (code==null){
            //如果用户没有自定义分享码，就得自行生成
            code=StringTools.getRandomString(Constants.LENGTH_5);
        }
        shareInfoEntity.setCode(code);
        save(shareInfoEntity);
        return shareInfoEntity;
    }

    @Override
    public void cancelShareBatch(String userId, String shareIds) {
        //直接删除分享记录即可
        String[] shareIdArray = shareIds.split(",");
        remove(new QueryWrapper<ShareInfoEntity>()
                .eq("user_id",userId)
                .in("share_id",Arrays.asList(shareIdArray)));
    }

    @Override
    public SessionShareDto checkShareCode(String shareId, String code) {
        //1.获取分享记录
        ShareInfoEntity shareInfo = getById(shareId);
        if(null==shareInfo||(shareInfo.getExpireTime()!=null&&new Date().after(shareInfo.getExpireTime()))){
            throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
        }
        //2.验证码错误
        if(!code.equals(shareInfo.getCode())){
            throw new BusinessException("提取码错误!!!");
        }
        //3.验证码正确，给分享次数+1,通过数据库层面的乐观锁修改分享次数，而不是先查询再修改
        shareInfoMapper.updateShareShowCount(shareId);
        SessionShareDto shareDto=new SessionShareDto();
        shareDto.setShareId(shareId);
        shareDto.setShareUserId(shareInfo.getUserId());
        shareDto.setFileId(shareInfo.getFileId());
        shareDto.setExpireTime(shareInfo.getExpireTime());
        return shareDto;
    }
}