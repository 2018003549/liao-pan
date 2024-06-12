package com.study.liao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.dto.UploadResultDto;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-30 17:06:10
 */
public interface FileInfoService extends IService<FileInfoEntity> {

    /**
     * 根据条件查询列表
     */
    List<FileInfoEntity> findListByParam(FileInfoQuery param);
    PaginationResultVO<FileInfoEntity> findListByPage(FileInfoQuery param);

    UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks);
    FileInfoEntity getFileInfoByFileIdAndUserId(String fileId, String userId);

    FileInfoEntity newFolder(String filePid, String fileName, String userId);

    FileInfoEntity rename(String fileId, String userId, String fileName);

    void changeFileFolder(String fileIds, String filePid, String userId);

    /**
     * removeFile to Recycle：删除文件，且把文件移动到回收站
     * @param userId
     * @param fileIds
     */
    void removeFile2RecycleBatch(String userId, String fileIds);

    void recoverFile(String userId, String fileIds);

    void delFileBatch(String userId, String fileIds, Boolean isAdmin);

    void checkRootFilePid(String filePid, String shareUserId, String fileId);

    void saveShare(String shareRootPid, String shareFileIds, String myFolderId,
                   String shareUserId, String currentUserId);
}

