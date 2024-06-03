package com.study.liao.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.liao.component.RedisComponent;
import com.study.liao.config.AppConfig;
import com.study.liao.dao.FileInfoMapper;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.dto.UploadResultDto;
import com.study.liao.entity.dto.UserSpaceDto;
import com.study.liao.entity.enums.*;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.entity.query.SimplePage;
import com.study.liao.entity.vo.PaginationResultVO;
import com.study.liao.service.UserInfoService;
import com.study.liao.util.DateUtil;
import com.study.liao.util.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.jni.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Function;

import com.study.liao.service.FileInfoService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@Service("fileInfoService")
public class FileInfoServiceImpl implements FileInfoService {
    @Resource
    private FileInfoMapper<FileInfoEntity, FileInfoQuery> fileInfoMapper;
    @Autowired
    private RedisComponent redisComponent;
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    AppConfig appConfig;
    @Lazy   //防止循环依赖
    @Autowired
    FileInfoServiceImpl fileInfoService;

    @Override
    public List<FileInfoEntity> findListByParam(FileInfoQuery param) {
        return fileInfoMapper.selectList(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<FileInfo> findListByPage(FileInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<FileInfoEntity> list = this.findListByParam(param);
        PaginationResultVO<FileInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    @Transactional
    @Override
    public UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks) {
        UploadResultDto uploadResultDto = new UploadResultDto();
        String userId = webUserDto.getUserId();
        File tempFileFolder = null;
        boolean uploadSuccess = true;//用于判断全局传送过程是否有问题
        try {
            //1.设置文件id
            if (StringTools.isEmpty(fileId)) {
                //首次上传的分片是没有文件id的，所以要生成一个
                fileId = StringTools.getRandomNumber(Constants.LENGTH_10);
            }
            uploadResultDto.setFileId(fileId);
            //2.获取用户使用空间
            UserSpaceDto userSpaceDto = redisComponent.getUseSpace(userId);
            //3.处理首个分片
            if (chunkIndex == 0) {
                FileInfoQuery infoQuery = new FileInfoQuery();
                infoQuery.setFileMd5(fileMd5);
                infoQuery.setSimplePage(new SimplePage(0, 1));//分页查询查出第一条数据
                infoQuery.setStatus(FileStatusEnums.USING.getStatus());
                List<FileInfoEntity> fileInfoList = fileInfoMapper.selectList(infoQuery);
                //4.如果数据库已经有这个文件就秒传即可
                if (!fileInfoList.isEmpty()) {
                    FileInfoEntity dbFile = fileInfoList.get(0);
                    //4.1判断用户可用空间是否可以容纳该文件
                    if (dbFile.getFileSize() + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
                        throw new BusinessException(ResponseCodeEnum.CODE_904);//网盘空间不足
                    }
                    //4.2同步秒传文件信息
                    dbFile.setFileId(fileId);
                    dbFile.setFilePid(filePid);
                    dbFile.setUserId(userId);
                    Date date = new Date();
                    dbFile.setCreateTime(date);
                    dbFile.setUpdateTime(date);
                    dbFile.setStatus(FileStatusEnums.USING.getStatus());
                    dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
                    dbFile.setFileMd5(fileMd5);
                    dbFile.setFileName(fileRename(filePid, userId, fileName));//如果存在同名文件，就重命名
                    uploadResultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());//设置当前状态为秒传
                    fileInfoMapper.insert(dbFile);//写入数据库
                    //4.3更新用户使用空间
                    updateUserSpace(userId, dbFile.getFileSize());
                    return uploadResultDto;
                }
            }
            //5.否则就是分片上传,将分片暂存到临时目录
            //5.1判断磁盘空间
            Long currentTempSize = redisComponent.getFileTempSize(userId, fileId);
            if (file.getSize() + currentTempSize + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
                //上传分片时容量不够了，当前分片只能停止上传
                throw new BusinessException(ResponseCodeEnum.CODE_904);
            }
            //5.2获取临时文件目录
            String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
            String currentUserFolderName = userId + fileId;
            tempFileFolder = new File(tempFolderName + currentUserFolderName);
            if (!tempFileFolder.exists()) {
                tempFileFolder.mkdirs();
            }
            //5.3将当前分片存储到临时目录
            File newFilePath = new File(tempFileFolder.getPath() + "/" + chunkIndex);//分片存储路径
            file.transferTo(newFilePath);
            if (chunkIndex < chunks - 1) {
                uploadResultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
                //5.4保存临时大小
                redisComponent.saveFileTempSize(userId, fileId, file.getSize());
                return uploadResultDto;
            }
            //6.最后一个分片上传完成，保存到数据库，并且异步合并分片
            Date date = new Date();
            String yearMonth = DateUtil.format(date, DateTimePatternEnum.YYYYMM.getPattern());//获取年月
            //6.1拼接真实文件名
            String fileSuffix = StringTools.getFileSuffix(fileName);
            String realFileName = currentUserFolderName + fileSuffix;
            //6.2重命名
            fileName = fileRename(filePid, userId, fileName);
            //6.3保存信息
            FileTypeEnums fileTypeBySuffix = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
            FileInfoEntity fileInfo = new FileInfoEntity();
            fileInfo.setFileId(fileId);
            fileInfo.setUserId(userId);
            fileInfo.setFileMd5(fileMd5);
            fileInfo.setFileName(fileName);
            fileInfo.setFilePath(yearMonth + "/" + realFileName);
            fileInfo.setFilePid(filePid);
            fileInfo.setCreateTime(date);
            fileInfo.setLastUpdateTime(date);
            fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
            fileInfo.setFileCategory(fileTypeBySuffix.getCategory().getCategory());//获取文件分类
            fileInfo.setFileType(fileTypeBySuffix.getType());//详细文件类型
            fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());//目录类型
            fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
            fileInfoMapper.insert(fileInfo);
            //6.4更新用户空间信息
            Long useSize = redisComponent.getFileTempSize(userId, fileId);
            updateUserSpace(userId, useSize);
            //6.5设置状态为转码完成
            uploadResultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
            //7.转码
            //要等事务提交之后才可以转码
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    //交给spring管理才可以使异步生效
                    fileInfoService.transferFile(fileInfo.getFileId(),userId);
                }
            });
            return uploadResultDto;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败！！");
            e.printStackTrace();
            uploadSuccess = false;
        } finally {
            if (!uploadSuccess && tempFileFolder != null) {
                //上传失败就要删除对应临时目录
                try {
                    FileUtils.deleteDirectory(tempFileFolder);
                } catch (IOException e) {
                    log.error("删除临时目录失败");
                    e.printStackTrace();
                }
            }
            return uploadResultDto;
        }
    }

    private String fileRename(String filePid, String userId, String fileName) {
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        Integer count = fileInfoMapper.selectCount(fileInfoQuery);
        if (count > 0) {
            fileName = StringTools.rename(fileName);
        }
        return fileName;
    }

    private void updateUserSpace(String userId, Long useSpace) {
        Integer count = userInfoService.updateUseSpace(userId, useSpace, null);
        if (count == 0) {
            //更新失败
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        //数据库更新成功就同步缓存
        UserSpaceDto spaceDto = redisComponent.getUseSpace(userId);
        spaceDto.setUseSpace(spaceDto.getUseSpace() + useSpace);
        redisComponent.saveUserSpaceUse(userId, spaceDto);
    }
    @Async
    public void transferFile(String fileId,String userId){
        boolean transferSuccess=true;
        String targetFilePath=null;
        String cover=null;
        FileTypeEnums fileTypeEnums=null;
        FileInfoEntity fileInfo=fileInfoMapper.selectByFileIdAndUserId(fileId,userId);
        try {
            if(fileInfo==null||!FileStatusEnums.TRANSFER.getStatus().equals(fileInfo.getStatus())){
                return;
            }
            //1.获取临时目录
            String tempFolderName=appConfig.getProjectFolder()+Constants.FILE_FOLDER_TEMP;
            String currentUserFolderName=userId+fileId;
            File fileFolder=new File(tempFolderName+currentUserFolderName);
            String fileSuffix=StringTools.getFileSuffix(fileInfo.getFileName());
            String month=DateUtil.format(fileInfo.getCreateTime(),DateTimePatternEnum.YYYYMM.getPattern());
            //2.获取目标目录
            String targetFolderName=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE;
            File targetFolder = new File(targetFolderName + "/" + month);
            if(!targetFolder.exists()){
                targetFolder.mkdirs();
            }
            String realFileName=currentUserFolderName+fileSuffix;//真实文件名
            targetFilePath=targetFolder.getPath()+"/"+realFileName;
            //3.合并文件
            union(fileFolder.getPath(),targetFilePath,fileInfo.getFileName(),true);
            //todo 视频文件切割
        }catch (Exception e){
            log.error("文件转码失败，{}",e);
            e.printStackTrace();
        }finally {
            //4.更新转码状态
            FileInfoEntity updateInfo = new FileInfoEntity();
            updateInfo.setFileSize(new File(targetFilePath).length());
            updateInfo.setFileCover(cover);
            updateInfo.setStatus(transferSuccess?FileStatusEnums.USING.getStatus() : FileStatusEnums.TRANSFER_FAIL.getStatus());
            fileInfoMapper.updateFileStatusWithOldStatus(fileId,userId,updateInfo,FileStatusEnums.TRANSFER.getStatus());
        }
    }

    /**
     * @param dirPath 临时文件目录
     * @param toFilePath 目标文件路径
     * @param fileName 文件名
     * @param delSource 是否要删除源文件目录
     */
    private void union(String dirPath,String toFilePath,String fileName,Boolean delSource){
        File dir=new File(dirPath);
        if(!dir.exists()){
            throw new BusinessException("目录不存在");
        }
        //1.读取临时目录中的所有文件
        File[] files = dir.listFiles();
        File targetFile = new File(toFilePath);
        RandomAccessFile writeFile=null;
        try {
            writeFile=new RandomAccessFile(targetFile,"rw");
            byte[] b=new byte[1024*10];
            for(int i=0;i<files.length;i++){
                int len=-1;
                //2.文件名就是分片号，按分片号读出数据
                File chunkFile=new File(dirPath+"/"+i);
                RandomAccessFile readFile=null;
                try {
                    //3.将读取出的文件流按顺序写入到目标文件
                    readFile=new RandomAccessFile(chunkFile,"r");
                    while((len=readFile.read(b))!=-1){
                        writeFile.write(b,0,len);
                    }
                }catch (Exception e){
                    log.error("合并分片失败");
                    throw new BusinessException("合并分片失败");
                }finally {
                    readFile.close();
                }
            }
        }catch (Exception e){
            log.error("合并{}文件失败",fileName);
            throw new BusinessException("合并文件"+fileName+"出错了");
        }finally {
            if(writeFile!=null){
                try {
                    writeFile.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(delSource&&dir.exists()){
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    /**
     * 根据条件查询列表
     */
    public Integer findCountByParam(FileInfoQuery param) {
        return this.fileInfoMapper.selectCount(param);
    }

    @Override
    public boolean saveBatch(Collection<FileInfoEntity> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdateBatch(Collection<FileInfoEntity> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean updateBatchById(Collection<FileInfoEntity> entityList, int batchSize) {
        return false;
    }

    @Override
    public boolean saveOrUpdate(FileInfoEntity entity) {
        return false;
    }

    @Override
    public FileInfoEntity getOne(Wrapper<FileInfoEntity> queryWrapper, boolean throwEx) {
        return null;
    }

    @Override
    public Optional<FileInfoEntity> getOneOpt(Wrapper<FileInfoEntity> queryWrapper, boolean throwEx) {
        return Optional.empty();
    }

    @Override
    public Map<String, Object> getMap(Wrapper<FileInfoEntity> queryWrapper) {
        return null;
    }

    @Override
    public <V> V getObj(Wrapper<FileInfoEntity> queryWrapper, Function<? super Object, V> mapper) {
        return null;
    }

    @Override
    public BaseMapper<FileInfoEntity> getBaseMapper() {
        return null;
    }

    @Override
    public Class<FileInfoEntity> getEntityClass() {
        return null;
    }
}