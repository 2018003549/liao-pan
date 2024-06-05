package com.study.liao.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.annotation.VerifyParam;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.dto.UploadResultDto;
import com.study.liao.entity.enums.FileCategoryEnums;
import com.study.liao.entity.enums.FileDelFlagEnums;
import com.study.liao.entity.enums.FileFolderTypeEnums;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.entity.vo.FileInfoVO;
import com.study.liao.entity.vo.PaginationResultVO;
import com.study.liao.entity.vo.ResponseVO;
import com.study.liao.util.CopyTools;
import com.study.liao.util.StringTools;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.liao.entity.FileInfoEntity;
import com.study.liao.service.FileInfoService;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-30 17:06:10
 */
@RestController
@RequestMapping("file")
public class FileInfoController extends ABaseController {
    @Autowired
    private FileInfoService fileInfoService;

    @RequestMapping("/loadDataList")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadDataList(HttpSession session, FileInfoQuery query, String category) {
        FileCategoryEnums categoryEnum = FileCategoryEnums.getByCode(category);
        if (null != categoryEnum) {
            query.setFileCategory(categoryEnum.getCategory());
        }
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        query.setUserId(sessionWebUserDto.getUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        PaginationResultVO result = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(convert2PaginationVO(result, FileInfoVO.class));
    }

    /**
     * @param fileId 分片对应的文件id，首个分片没有id，所以为非必填
     * @param file 本次传输的分片
     * @param fileName 文件原名
     * @param filePid 文件所在目录
     * @param fileMd5 文件的md5码，如果数据库中存在相同的md5码，就可以秒传
     * @param chunkIndex 分片索引
     * @param chunks 分片数
     */
    @RequestMapping("/uploadFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO uploadFile(HttpSession session,
                                 String fileId,
                                 MultipartFile file,
                                 @VerifyParam(required = true) String fileName,
                                 @VerifyParam(required = true) String filePid,
                                 @VerifyParam(required = true) String fileMd5,
                                 @VerifyParam(required = true) Integer chunkIndex,
                                 @VerifyParam(required = true) Integer chunks) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        //上传过程要和前端有交互，所以返回当前状态信息
        UploadResultDto uploadResultDto = fileInfoService.uploadFile(webUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
        return getSuccessResponseVO(uploadResultDto);
    }

    @RequestMapping("/getImage/{imageFolder}/{imageName}")
    @GlobalInterceptor(checkParams = true)
    public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder
            , @PathVariable("imageName") String imageName) {
        super.getImage(response, imageFolder, imageName);
    }

    @RequestMapping("ts/getVideoInfo/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public void getVideoInfo(HttpServletResponse response, HttpSession session, @PathVariable("fileId") String fileId) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        super.getFile(response, fileId, webUserDto.getUserId());
    }

    @RequestMapping("/getFile/{fileId}")
    @GlobalInterceptor(checkParams = true)
    public void getFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId") String fileId) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        super.getFile(response, fileId, webUserDto.getUserId());
    }

    @RequestMapping("/newFoloder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO newFolder(HttpSession session, @VerifyParam(required = true) String filePid,
                                @VerifyParam(required = true) String fileName) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        String userId = webUserDto.getUserId();
        FileInfoEntity fileInfo = fileInfoService.newFolder(filePid, fileName, userId);
        return getSuccessResponseVO(fileInfo);
    }

    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getFolderInfo(HttpSession session, @VerifyParam(required = true) String path) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        String userId = webUserDto.getUserId();
        return super.getFolderInfo(path, userId);
    }

    @RequestMapping("/rename")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO rename(HttpSession session, @VerifyParam(required = true) String fileId
            , @VerifyParam(required = true) String fileName) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        String userId = webUserDto.getUserId();
        FileInfoEntity fileInfo = fileInfoService.rename(fileId, userId, fileName);
        return getSuccessResponseVO(CopyTools.copy(fileInfo,FileInfoVO.class));
    }

    /**
     * @param session
     * @param filePid 当前父目录【即移动到的位置】
     * @param currentFileIds 原文件所在目录【需要排除掉】
     * @return
     */
    @RequestMapping("/loadAllFolder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadAllFolder(HttpSession session, @VerifyParam(required = true) String filePid
            ,  String currentFileIds) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        String userId = webUserDto.getUserId();
        FileInfoQuery fileInfoQuery = new FileInfoQuery();
        //1.排除掉原文件所在目录
        if(!StringTools.isEmpty(currentFileIds)){
            fileInfoQuery.setExcludeFileIdArray(currentFileIds.split(","));
        }
        //2.查询当前目录下的所有目录
        fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
        fileInfoQuery.setFilePid(filePid);
        fileInfoQuery.setUserId(userId);
        fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
        fileInfoQuery.setOrderBy("create_time desc");
        List<FileInfoEntity> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);
        return getSuccessResponseVO(CopyTools.copyList(fileInfoList,FileInfoVO.class));
    }

    /**
     * @param fileIds 批量移动的多个文件id
     * @param filePid 目标目录
     */
    @RequestMapping("/changeFileFolder")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO changeFileFolder(HttpSession session, @VerifyParam(required = true) String fileIds
            ,  String filePid){
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        String userId = webUserDto.getUserId();
        fileInfoService.changeFileFolder(fileIds,filePid,userId);
        return getSuccessResponseVO(null);
    }
}
