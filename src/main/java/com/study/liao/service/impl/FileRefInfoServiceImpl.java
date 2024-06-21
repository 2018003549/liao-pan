package com.study.liao.service.impl;

import com.common.utils.PageUtils;
import com.common.utils.Query;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.enums.ResponseCodeEnum;
import com.study.liao.entity.query.FileInfoQuery;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.liao.dao.FileRefInfoDao;
import com.study.liao.entity.FileRefInfoEntity;
import com.study.liao.service.FileRefInfoService;


@Service("fileRefInfoService")
public class FileRefInfoServiceImpl extends ServiceImpl<FileRefInfoDao, FileRefInfoEntity> implements FileRefInfoService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<FileRefInfoEntity> page = this.page(
                new Query<FileRefInfoEntity>().getPage(params),
                new QueryWrapper<FileRefInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void updateFileCount(String fileMd5) {
        baseMapper.addFileCount(fileMd5);
    }

    @Override
    public void insertFileCount(String fileMd5, String filePath) {
        FileRefInfoEntity fileRefInfoEntity = new FileRefInfoEntity();
        fileRefInfoEntity.setFilePath(filePath);
        fileRefInfoEntity.setFileMd5(fileMd5);
        fileRefInfoEntity.setCount(1l);
        int insertNum = baseMapper.insert(fileRefInfoEntity);
        if(insertNum==0){
            //插入失败了，有可能是引用计数为0的记录还没被清理掉，也有可能是在这期间有人已经上传了
            updateFileCount(fileMd5);
        }
    }

    @Override
    public void decreaseFileRefBatch(List<FileInfoEntity> fileInfoList) {
        if(fileInfoList==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //1.先按照md5分组
        Map<String,Long> md5Map=new HashMap<>();
        for (FileInfoEntity fileInfo : fileInfoList) {
            String fileMd5 = fileInfo.getFileMd5();
            if(!md5Map.containsKey(fileMd5)){
                md5Map.put(fileMd5,1l);
            }else{
                Long count = md5Map.get(fileMd5);
                md5Map.put(fileMd5,count+1);
            }
        }
        //2.进行批量修改
        baseMapper.decreaseFileRefBatch(md5Map);
    }

}