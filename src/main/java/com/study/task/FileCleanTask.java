package com.study.task;

import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.enums.FileDelFlagEnums;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.service.FileInfoService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FileCleanTask {
    @Autowired
    private FileInfoService fileInfoService;
    @Scheduled(fixedDelay = 1000*60*3)
    public void execute(){
        //1.查询已过期的文件
        FileInfoQuery query = new FileInfoQuery();
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        query.setQueryExpire(true);//查询已过期的文件
        List<FileInfoEntity> fileInfoList = fileInfoService.findListByParam(query);
        //2.按用户名进行分组
        Map<String,List<FileInfoEntity>> fileInfoMap=fileInfoList.stream().
                collect(Collectors.groupingBy(FileInfoEntity::getUserId));
        for (Map.Entry<String, List<FileInfoEntity>> entry : fileInfoMap.entrySet()) {
            List<String> fileIdList = entry.getValue().stream().
                    map(FileInfoEntity::getFileId).collect(Collectors.toList());
            String userId = entry.getKey();
            fileInfoService.delFileBatch(userId, StringUtils.join(fileIdList,","),false);
        }
    }
}
