package com.study.liao.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.liao.component.RedisComponent;
import com.study.liao.config.AppConfig;
import com.study.liao.dao.FileInfoMapper;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.UserInfoEntity;
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
import com.study.liao.util.ProcessUtils;
import com.study.liao.util.ScaleFilter;
import com.study.liao.util.StringTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public PaginationResultVO<FileInfoEntity> findListByPage(FileInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<FileInfoEntity> list = this.findListByParam(param);
        PaginationResultVO<FileInfoEntity> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
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
            //5.4保存临时大小
            redisComponent.saveFileTempSize(userId, fileId, file.getSize());
            if (chunkIndex < chunks - 1) {
                //非最后一个分片都是转码中的状态
                uploadResultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
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
            //6.5设置状态为上传完成
            uploadResultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
            //7.转码
            //要等事务提交之后才可以转码
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    //交给spring管理才可以使异步生效
                    fileInfoService.transferFile(fileInfo.getFileId(), userId);
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
    public void transferFile(String fileId, String userId) {
        boolean transferSuccess = true;
        String targetFilePath = null;
        String cover = null;
        FileInfoEntity fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        try {
            if (fileInfo == null || !FileStatusEnums.TRANSFER.getStatus().equals(fileInfo.getStatus())) {
                return;
            }
            //1.获取临时目录
            String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
            String currentUserFolderName = userId + fileId;
            File fileFolder = new File(tempFolderName + currentUserFolderName);
            String fileSuffix = StringTools.getFileSuffix(fileInfo.getFileName());
            String month = DateUtil.format(fileInfo.getCreateTime(), DateTimePatternEnum.YYYYMM.getPattern());
            //2.获取目标目录
            String targetFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFolder = new File(targetFolderName + "/" + month);
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            }
            String realFileName = currentUserFolderName + fileSuffix;//真实文件名
            targetFilePath = targetFolder.getPath() + "/" + realFileName;
            //3.合并文件
            union(fileFolder.getPath(), targetFilePath, fileInfo.getFileName(), true);
            //4.文件切割
            FileTypeEnums fileTypeBySuffix = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
            if (FileTypeEnums.VIDEO == fileTypeBySuffix) {
                //4.1视频文件切割
                cutFileVideo(fileId, targetFilePath);
                //4.2生成视频文件缩略图
                cover = month + "/" + currentUserFolderName + Constants.IMAGE_PNG_SUFFIX;
                String coverPath = targetFolderName + "/" + cover;
                ScaleFilter.createCover4Video(new File(targetFilePath), Constants.LENGTH_150, new File(coverPath));
            } else if (FileTypeEnums.IMAGE == fileTypeBySuffix) {
                //4.3图片就不需要切割了，直接生成缩略图
                cover = month + "/" + realFileName.replace(".", "_.");
                String coverPath = targetFolderName + "/" + cover;
                Boolean isCreated = ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath),
                        Constants.LENGTH_150, new File(coverPath), false);
                if (!isCreated) {
                    //如果没有生成缩略图，说明原图太小了，那就直接复制一份就行了
                    FileUtils.copyFile(new File(targetFilePath), new File(coverPath));
                }
            }
        } catch (Exception e) {
            log.error("文件转码失败，{}", e);
            e.printStackTrace();
        } finally {
            //4.更新转码状态
            FileInfoEntity updateInfo = new FileInfoEntity();
            updateInfo.setFileSize(new File(targetFilePath).length());
            updateInfo.setFileCover(cover);
            updateInfo.setStatus(transferSuccess ? FileStatusEnums.USING.getStatus() : FileStatusEnums.TRANSFER_FAIL.getStatus());
            fileInfoMapper.updateFileStatusWithOldStatus(fileId, userId, updateInfo, FileStatusEnums.TRANSFER.getStatus());
        }
    }

    /**
     * @param dirPath    临时文件目录
     * @param toFilePath 目标文件路径
     * @param fileName   文件名
     * @param delSource  是否要删除源文件目录
     */
    private void union(String dirPath, String toFilePath, String fileName, Boolean delSource) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            throw new BusinessException("目录不存在");
        }
        //1.读取临时目录中的所有文件
        File[] files = dir.listFiles();
        File targetFile = new File(toFilePath);
        RandomAccessFile writeFile = null;
        try {
            writeFile = new RandomAccessFile(targetFile, "rw");
            byte[] b = new byte[1024 * 10];
            for (int i = 0; i < files.length; i++) {
                int len = -1;
                //2.文件名就是分片号，按分片号读出数据
                File chunkFile = new File(dirPath + "/" + i);
                RandomAccessFile readFile = null;
                try {
                    //3.将读取出的文件流按顺序写入到目标文件
                    readFile = new RandomAccessFile(chunkFile, "r");
                    while ((len = readFile.read(b)) != -1) {
                        writeFile.write(b, 0, len);
                    }
                } catch (Exception e) {
                    log.error("合并分片失败");
                    throw new BusinessException("合并分片失败");
                } finally {
                    readFile.close();
                }
            }
        } catch (Exception e) {
            log.error("合并{}文件失败", fileName);
            throw new BusinessException("合并文件" + fileName + "出错了");
        } finally {
            if (writeFile != null) {
                try {
                    writeFile.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (delSource && dir.exists()) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void cutFileVideo(String fileId, String videoFilePath) {
        //1.创建同名切片目录
        File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
        if (!tsFolder.exists()) {
            tsFolder.mkdirs();
        }
        //2.调用ffmpeg命令,将视频文件转成.ts文件再进行切割
        final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
        final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
        String tsPath = tsFolder + "/" + Constants.TS_NAME;
        //3.生成index.ts文件
        String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
        ProcessUtils.executeCommand(cmd, false);
        //4.java执行cmd命令，生成索引文件.m3u8 和切片.ts
        cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath(), fileId);
        ProcessUtils.executeCommand(cmd, false);
        //5.删除index.ts
        new File(tsPath).delete();
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

    @Override
    public FileInfoEntity getFileInfoByFileIdAndUserId(String fileId, String userId) {
        return fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
    }

    @Override
    public FileInfoEntity newFolder(String filePid, String fileName, String userId) {
        //1.校验文件夹名称【不能重名】
        checkFileName(filePid, userId, fileName, FileFolderTypeEnums.FOLDER.getType());
        //2.直接新增文件夹信息
        Date date = new Date();
        FileInfoEntity fileInfo = new FileInfoEntity();
        fileInfo.setFileId(StringTools.getRandomString(Constants.LENGTH_10));
        fileInfo.setFilePid(filePid);
        fileInfo.setFileName(fileName);
        fileInfo.setUserId(userId);
        fileInfo.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfo.setCreateTime(date);
        fileInfo.setLastUpdateTime(date);
        fileInfo.setStatus(FileStatusEnums.USING.getStatus());
        fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
        fileInfoMapper.insert(fileInfo);
        return fileInfo;
    }

    @Override
    public FileInfoEntity rename(String fileId, String userId, String fileName) {
        FileInfoEntity fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if (null == fileInfo) {
            throw new BusinessException("文件不存在");
        }
        //1.校验文件名
        String filePid = fileInfo.getFilePid();
        checkFileName(filePid, userId, fileName, FileFolderTypeEnums.FOLDER.getType());
        //2.如果修改的是文件类型，获取文件后缀,只改名不该后缀
        if (FileFolderTypeEnums.FILE.getType().equals(fileInfo.getFolderType())) {
            fileName = fileName + StringTools.getFileSuffix(fileInfo.getFileName());
        }
        Date date = new Date();
        FileInfoEntity dbFileInfo = new FileInfoEntity();
        dbFileInfo.setFileName(fileName);
        dbFileInfo.setUpdateTime(date);
        fileInfoMapper.updateByFileIdAndUserId(dbFileInfo, fileId, userId);
        //3.双检,避免其它线程同时修改
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFileName(fileName);
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setUserId(userId);
        Integer count = fileInfoMapper.selectCount(fileInfoQuery);
        if (count > 1) {
            throw new BusinessException("此目录下已经存在同名文件，请修改名称");
        }
        fileInfo.setFileName(fileName);
        fileInfo.setLastUpdateTime(date);
        return fileInfo;
    }

    @Override
    public void changeFileFolder(String fileIds, String filePid, String userId) {
        if (fileIds.equals(filePid)) {
            throw new BusinessException("目标目录不能是原文件所在目录！");
        }
        //1.不在根目录的情况
        if (!Constants.ZERO_STR.equals(filePid)) {
            FileInfoEntity fileInfo = getFileInfoByFileIdAndUserId(filePid, userId);
            if (fileInfo == null || !FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())) {
                //如果目标目录不存在，就直接抛异常
                throw new BusinessException("目标目录不存在！");
            }
        }
        //2.查询出目标目录中的所有文件，检查是否与当前移动的文件重名了
        String[] fileIdArray = fileIds.split(",");
        FileInfoQuery query = new FileInfoQuery();
        query.setFilePid(filePid);
        query.setUserId(userId);
        List<FileInfoEntity> dbFileInfoList = findListByParam(query);
        Map<String, FileInfoEntity> dbFileNameMap =
                dbFileInfoList.stream().collect(Collectors.toMap(FileInfoEntity::getFileName,
                        Function.identity(),//表示将FileInfoEntity对象本身作为Map的值
                        (file1, file2) -> file2));//合并函数，当遇到重复的键时，选择保留第二个值
        //3.查询选中的文件
        query = new FileInfoQuery();
        query.setFileIdArray(fileIdArray);
        query.setUserId(userId);
        List<FileInfoEntity> selectFileList = findListByParam(query);
        //4.修改所选文件的父目录
        for (FileInfoEntity fileInfo : selectFileList) {
            String fileName = fileInfo.getFileName();
            FileInfoEntity dbFileInfo = dbFileNameMap.get(fileName);
            if (dbFileInfo != null) {
                //4.1目标目录存在同名文件，就将所选文件重命名
                fileName = StringTools.rename(fileName);
                fileInfo.setFileName(fileName);
            }
            //4.2修改选中文件的父目录
            fileInfo.setFilePid(filePid);
            fileInfoMapper.updateByFileIdAndUserId(fileInfo, fileInfo.getFileId(), userId);
        }
    }

    @Transactional
    @Override
    public void removeFile2RecycleBatch(String userId, String fileIds) {
        //1.查询所有待删文件
        String[] fileIdArray = fileIds.split(",");
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFileIdArray(fileIdArray);
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        List<FileInfoEntity> fileInfoEntityList = fileInfoMapper.selectList(query);
        if (fileInfoEntityList.isEmpty()) {
            return;
        }
        //2.如果删除的是目录，那就得把目录下的所有子文件都修改标记，并且可能有多级目录嵌套，所以需要递归修改
        ArrayList<String> delFilePidList = new ArrayList<>();//存放需要删除的目录id
        for (FileInfoEntity fileInfo : fileInfoEntityList) {
            if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
                findAllSubFolderFileList(delFilePidList, userId, fileInfo.getFileId(), fileInfo.getDelFlag());
            }
        }
        //3.批量删除所有目录下的子文件
        FileInfoEntity updateInfo = new FileInfoEntity();
        if (!delFilePidList.isEmpty()) {
            //回收站只需要显示首层文件，所以嵌套文件应该单独设置一个状态，即因为父目录被移到回收站而级联更新
            updateInfo.setDelFlag(FileDelFlagEnums.PARENT2RECYCLE.getFlag());
            fileInfoMapper.updateFileDelFlagBatch(updateInfo, userId, delFilePidList, null, FileDelFlagEnums.USING.getFlag());
        }
        //4.批量把首层的所有文件移到回收站
        updateInfo.setRecoveryTime(new Date());//进入回收站的时间
        updateInfo.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        List<String> list = Arrays.stream(fileIdArray).toList();
        fileInfoMapper.updateFileDelFlagBatch(updateInfo, userId, null, list, FileDelFlagEnums.USING.getFlag());
    }

    @Transactional
    @Override
    public void recoverFile(String userId, String fileIds) {
        //1.查询出选中文件
        String[] fileIdArray = fileIds.split(",");
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFileIdArray(fileIdArray);
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        List<FileInfoEntity> fileInfoEntityList = fileInfoMapper.selectList(query);
        List<String> recoverPidList = new ArrayList<>();
        //2.如果是目录，就得把子文件都递归查出来
        for (FileInfoEntity fileInfo : fileInfoEntityList) {
            if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
                //要查出那些因为父文件被删导致自己被删的文件
                findAllSubFolderFileList(recoverPidList, userId, fileInfo.getFileId(), FileDelFlagEnums.PARENT2RECYCLE.getFlag());
            }
        }
        FileInfoEntity updateInfo = new FileInfoEntity();
        updateInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
        updateInfo.setLastUpdateTime(new Date());
        //3.根据目录还原子文件
        if (!recoverPidList.isEmpty()) {
            for (String pid : recoverPidList) {
                //3.1还原存在重名问题
                checkChildName(pid, userId, FileDelFlagEnums.PARENT2RECYCLE.getFlag());
            }
            //3.2改成使用中的状态
            fileInfoMapper.updateFileDelFlagBatch(updateInfo, userId, recoverPidList, null, FileDelFlagEnums.PARENT2RECYCLE.getFlag());
        }
        //4.还原首层文件
        List<String> list = Arrays.asList(fileIdArray);
        for (FileInfoEntity recoverFile : fileInfoEntityList) {
            //4.1先得查询当前文件的原目录是否存在
            String pid = recoverFile.getFilePid();
            FileInfoEntity parentInfo = fileInfoMapper.selectByFileIdAndUserId(pid, userId);
            if (parentInfo == null || !FileDelFlagEnums.USING.getFlag().equals(parentInfo.getDelFlag())) {
                //原目录被删了，那就只能还原到根目录;又或者是直接在根目录中删除的
                pid = Constants.ZERO_STR;
                FileInfoEntity updatePidInfo = new FileInfoEntity();
                updatePidInfo.setFilePid(pid);
                fileInfoMapper.updateByFileIdAndUserId(updatePidInfo, recoverFile.getFileId(), userId);
            }
            //4.2检查还原回去是否会和同级文件重名,重名就自动修改
            checkChildName(pid, userId, FileDelFlagEnums.RECYCLE.getFlag());
        }
        //4.3还原状态
        fileInfoMapper.updateFileDelFlagBatch(updateInfo, userId, null, list, FileDelFlagEnums.RECYCLE.getFlag());
    }

    @Transactional
    @Override
    public void delFileBatch(String userId, String fileIds, Boolean isAdmin) {
        //1.查询出待删除文件
        String[] fileArray = fileIds.split(",");
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setUserId(userId);
        query.setFileIdArray(fileArray);
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        List<FileInfoEntity> fileInfoList = fileInfoMapper.selectList(query);
        //2.找到所有的待删目录
        List<String> filePidList = new ArrayList<>();
        for (FileInfoEntity fileInfo : fileInfoList) {
            if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
                findAllSubFolderFileList(filePidList, userId, fileInfo.getFileId(), FileDelFlagEnums.PARENT2RECYCLE.getFlag());
            }
        }
        //3.删除所有目录下的子文件
        if (!filePidList.isEmpty()) {
            fileInfoMapper.delFileBatch(userId, filePidList, null,
                    isAdmin ? null : FileDelFlagEnums.PARENT2RECYCLE.getFlag());//超级管理员不需要过滤删除状态
        }
        //4.删除首层所选文件
        fileInfoMapper.delFileBatch(userId, null, Arrays.asList(fileArray), isAdmin ? null : FileDelFlagEnums.RECYCLE.getFlag());
        //5.更新用户空间信息
        Long useSpace = fileInfoMapper.selectUseSpace(userId);
        UserInfoEntity userInfo = new UserInfoEntity();
        userInfo.setUserId(userId);
        userInfo.setUseSpace(useSpace);
        userInfoService.updateById(userInfo);
        //6.缓存中同步使用空间信息
        UserSpaceDto spaceDto = redisComponent.getUseSpace(userId);
        spaceDto.setUseSpace(useSpace);
        redisComponent.saveUserSpaceUse(userId, spaceDto);
    }

    /**
     * 校验当前访问的文件是否在分享根目录下
     *
     * @param rootFilePid 分享根目录id
     * @param shareUserId 分享人id
     * @param fileId      当前浏览的文件id
     */
    @Override
    public void checkRootFilePid(String rootFilePid, String shareUserId, String fileId) {
        if (StringTools.isEmpty(fileId)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //当前访问的就是分享根目录
        if (rootFilePid.equals(fileId)) {
            return;
        }
        //从当前文件向上递归查询，看看是否在分享根目录中
        checkFilePid(rootFilePid, fileId, shareUserId);
    }

    /**
     * @param shareRootPid  分享根目录id
     * @param shareFileIds  选中的分享文件id
     * @param myFolderId    保存的目标目录id
     * @param shareUserId   分享人id
     * @param currentUserId 当前用户id
     */
    @Override
    public void saveShare(String shareRootPid, String shareFileIds, String myFolderId,
                          String shareUserId, String currentUserId) {
        //1.查询目标文件列表
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(currentUserId);
        query.setFilePid(myFolderId);
        List<FileInfoEntity> currentFileList = fileInfoMapper.selectList(query);
        Map<String, FileInfoEntity> currentFileMap = currentFileList.stream().
                collect(Collectors.toMap(FileInfoEntity::getFileName,
                        Function.identity(), (file1, file2) -> file2));
        //2.查询选择的分享文件列表
        String[] shareFileArray = shareFileIds.split(",");
        query = new FileInfoQuery();
        query.setUserId(shareUserId);
        query.setFileIdArray(shareFileArray);
        List<FileInfoEntity> shareFileList = fileInfoMapper.selectList(query);
        //3.递归复制文件结构，并且对同名文件进行重命名
        ArrayList<FileInfoEntity> copyFileList = new ArrayList<>();
        Date date = new Date();
        for (FileInfoEntity shareFile : shareFileList) {
            FileInfoEntity currentFile = currentFileMap.get(shareFile.getFileName());
            if (currentFile != null) {
                //重名了就重命名
                shareFile.setFileName(StringTools.rename(shareFile.getFileName()));
            }
            //递归复制目录内容
            copyAllSubFile(copyFileList, shareFile, shareUserId, currentUserId, myFolderId, date);
        }
        fileInfoMapper.insertBatch(copyFileList);
    }

    /**
     * 递归复制文件信息
     * @param copyFileList  存放复制的文件实体
     * @param fileInfo      当前操作的源文件
     * @param sourceUserId  源文件用户id
     * @param currentUserId 当前用户id
     * @param newFilePid    新的存放目录
     */
    private void copyAllSubFile(List<FileInfoEntity> copyFileList, FileInfoEntity fileInfo, String sourceUserId
            , String currentUserId, String newFilePid, Date date) {
        //1.获取原文件id，下方生成新id会覆盖fileInfo
        String sourceFileId = fileInfo.getFileId();
        //2.填充新数据
        String newFileId = StringTools.getRandomString(Constants.LENGTH_10);
        fileInfo.setCreateTime(date);
        fileInfo.setLastUpdateTime(date);
        fileInfo.setFilePid(newFilePid);
        fileInfo.setUserId(currentUserId);
        fileInfo.setFileId(newFileId);
        copyFileList.add(fileInfo);
        //3.如果是目录就递归复制
        if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
            FileInfoQuery query = new FileInfoQuery();
            query.setFileId(sourceFileId);//用原文件id查询
            query.setUserId(sourceUserId);
            List<FileInfoEntity> sourceFileList = fileInfoMapper.selectList(query);
            for (FileInfoEntity sourceFile : sourceFileList) {
                copyAllSubFile(copyFileList, sourceFile, sourceUserId, currentUserId, newFilePid, date);
            }
        }
    }

    /**
     * 递归向上找到目标根目录，如果不存在说明当前访问的文件不在分享根目录中
     *
     * @param rootFilePid 分享根目录
     * @param fileId      当前层级的文件id
     * @param userId      用户id
     */
    private void checkFilePid(String rootFilePid, String fileId, String userId) {
        //1.查询当前目录信息
        FileInfoEntity fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
        if (fileInfo == null || Constants.ZERO_STR.equals(fileInfo.getFileId())) {
            //如果目录不存在或者是系统根目录【分享不可能传系统根目录】，那就抛异常
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //2.递归出口，找到目标根目录
        if (fileInfo.getFileId().equals(rootFilePid)) {
            return;
        }
        //3.找上一级目录
        checkFilePid(rootFilePid, fileInfo.getFilePid(), userId);
    }

    //检查还原回去的文件是否会和原本的目录中的文件命名冲突，如果冲突就需要自动重命名
    private void checkChildName(String pid, String userId, Integer delFlag) {
        //1.查询出当前目录中未被删除的文件,并用map存储，key为文件名，value为文件列表【因为文件和文件夹可以重名】
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFilePid(pid);
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        List<FileInfoEntity> curUsingFileList = fileInfoMapper.selectList(query);
        Map<String, List<FileInfoEntity>> fileNameMap = new HashMap<>();
        for (FileInfoEntity fileInfo : curUsingFileList) {
            String fileName = fileInfo.getFileName();
            if (!fileNameMap.containsKey(fileName)) {
                fileNameMap.put(fileName, new ArrayList<>());
            }
            fileNameMap.get(fileName).add(fileInfo);
        }
        //2.查询出当前目录中需要还原的文件
        query.setDelFlag(delFlag);
        List<FileInfoEntity> recoveryFileList = fileInfoMapper.selectList(query);
        for (FileInfoEntity fileInfo : recoveryFileList) {
            String fileName = fileInfo.getFileName();
            //3.没有重名的就不用管
            if (!fileNameMap.containsKey(fileName)) {
                continue;
            }
            //4.重名了还得判断是否是同类型
            List<FileInfoEntity> sameNameFileList = fileNameMap.get(fileName);
            for (FileInfoEntity sameNameFile : sameNameFileList) {
                if (sameNameFile.getFolderType().equals(fileInfo.getFolderType())) {
                    //同类型文件重名了就得自动重命名,这一块也可以批量修改
                    FileInfoEntity updateInfo = new FileInfoEntity();
                    updateInfo.setFileName(StringTools.rename(fileInfo.getFileName()));
                    fileInfoMapper.updateByFileIdAndUserId(updateInfo, fileInfo.getFileId(), userId);
                }
            }
        }
    }

    //递归查询当前目录下的所有目录
    private void findAllSubFolderFileList(List<String> filePidList, String userId, String fileId, Integer delFlag) {
        filePidList.add(fileId);
        //1.查询出本层所有目录
        FileInfoQuery query = new FileInfoQuery();
        query.setUserId(userId);
        query.setFilePid(fileId);
        query.setDelFlag(delFlag);
        query.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        List<FileInfoEntity> fileInfoEntityList = fileInfoMapper.selectList(query);
        //2.继续递归
        for (FileInfoEntity fileInfo : fileInfoEntityList) {
            findAllSubFolderFileList(filePidList, userId, fileInfo.getFileId(), delFlag);
        }
    }

    private void checkFileName(String filePid, String userId, String fileName, Integer folderType) {
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        fileInfoQuery.setFolderType(folderType);
        fileInfoQuery.setFileName(fileName);
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        Integer count = fileInfoMapper.selectCount(fileInfoQuery);
        if (count > 0) {
            throw new BusinessException("此目录下已经存在同名文件，请修改名称");
        }
    }
}