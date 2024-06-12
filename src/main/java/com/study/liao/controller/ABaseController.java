package com.study.liao.controller;

import com.study.liao.component.RedisComponent;
import com.study.liao.config.AppConfig;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.DownloadFileDto;
import com.study.liao.entity.dto.SessionShareDto;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.enums.FileCategoryEnums;
import com.study.liao.entity.enums.FileFolderTypeEnums;
import com.study.liao.entity.enums.ResponseCodeEnum;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.entity.vo.PaginationResultVO;
import com.study.liao.entity.vo.ResponseVO;
import com.study.liao.service.FileInfoService;
import com.study.liao.util.CopyTools;
import com.study.liao.util.FileUtils;
import com.study.liao.util.StringTools;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.jni.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.URLEncoder;
import java.util.List;


public class ABaseController {
    @Autowired
    AppConfig appConfig;
    @Autowired
    FileInfoService fileInfoService;
    @Autowired
    RedisComponent redisComponent;

    protected static final String STATUC_SUCCESS = "success";

    protected static final String STATUC_ERROR = "error";

    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus(STATUC_SUCCESS);
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }

    protected <S, T> PaginationResultVO<T> convert2PaginationVO(PaginationResultVO<S> result, Class<T> classz) {
        PaginationResultVO<T> resultVO = new PaginationResultVO<>();
        resultVO.setList(CopyTools.copyList(result.getList(), classz));
        resultVO.setPageNo(result.getPageNo());
        resultVO.setPageSize(result.getPageSize());
        resultVO.setPageTotal(result.getPageTotal());
        resultVO.setTotalCount(result.getTotalCount());
        return resultVO;
    }

    protected SessionWebUserDto getUserInfoFromSession(HttpSession session) {
        SessionWebUserDto sessionWebUserDto = (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        return sessionWebUserDto;
    }

    protected void getImage(HttpServletResponse response, String imageFolder, String imageName) {
        if (StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName) ||
                !StringTools.pathIsOk(imageFolder) || !StringTools.pathIsOk(imageName)) {
            return;
        }
        String imageSuffix = StringTools.getFileSuffix(imageName);
        String filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
        imageSuffix = imageSuffix.replace(".", "");//去掉后缀名的“.”
        response.setContentType("image/" + imageSuffix);
        response.setHeader("Cache-Control", "max-age=2592000");
        FileUtils.readFile(response, filePath);
    }

    protected void getFile(HttpServletResponse response, String fileId, String userId) {
        String filePath = null;
        if (fileId.endsWith(".ts")) {
            //1.以ts为后缀说明是读取切片，fileId是文件id拼接上切片id
            //1.1解析出fileId
            String[] tsArray = fileId.split("_");
            String realFileId = tsArray[0];
            //1.2拼接文件地址
            FileInfoEntity fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(realFileId, userId);
            if (fileInfo == null) {
                return;
            }
            String fileName = StringTools.getFileNameNoSuffix(fileInfo.getFilePath()) + "/" + fileId;
            filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileName;
        } else {
            //2.否则就是读取正常文件，fileId就是文件id
            FileInfoEntity fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
            if (fileInfo == null) {
                return;
            }
            if (FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategory())) {
                //2.1获取不包含后缀的文件名
                String fileNameNoSuffix = StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
                //2.2获取索引文件所在路径
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileNameNoSuffix + "/" + Constants.M3U8_NAME;
            } else {
                //3.非视频索引文件就直接通过文件名读出就行了
                filePath = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + fileInfo.getFilePath();
            }
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
        }
        FileUtils.readFile(response, filePath);
    }

    protected ResponseVO getFolderInfo(String path, String userId) {
        String[] pathArray = path.split("/");
        FileInfoQuery infoQuery = new FileInfoQuery();
        infoQuery.setUserId(userId);
        //1.使用非递归的方式，直接把所有层级的目录都查出来
        infoQuery.setFileIdArray(pathArray);
        //2.根据层级顺序排序,拼接成形如 field(file_Id,"id1","id2",...)
        String orderBy="field(file_Id,\""+ StringUtils.join(pathArray,"\",\"")+"\")";
        infoQuery.setOrderBy(orderBy);
        List<FileInfoEntity> fileInfoEntityList = fileInfoService.findListByParam(infoQuery);
        return getSuccessResponseVO(fileInfoEntityList);
    }
    protected  ResponseVO createDownloadUrl(String fileId,String userId){
        FileInfoEntity fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
        if(fileInfo==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())){
            //只能下载文件，不能下载目录
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //1.生成随机码
        String code=StringTools.getRandomString(Constants.LENGTH_50);
        //2.封装下载信息，并且存到redis中
        DownloadFileDto downloadFileDto = new DownloadFileDto();
        downloadFileDto.setDownloadCode(code);
        downloadFileDto.setFileName(fileInfo.getFileName());
        downloadFileDto.setFilePath(fileInfo.getFilePath());
        redisComponent.saveDownloadCode(code,downloadFileDto);//用临时码作为key，下载信息作为value
        //3.返回临时码
        return getSuccessResponseVO(code);
    }

    protected void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException {
        //1.根据临时下载码获取下载信息
        DownloadFileDto downloadFileDto = redisComponent.getDownloadCode(code);
        if(downloadFileDto==null){
            throw new BusinessException("下载信息过期!!!");
        }
        //2.下载文件
        String filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+downloadFileDto.getFilePath();
        String fileName=downloadFileDto.getFileName();
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie")>0) {
            //IE浏览
            fileName= URLEncoder.encode(fileName,"UTF-8");
        }else {
            fileName=new String(fileName.getBytes("UTF-8"),"ISO8859-1");
        }
        response.setHeader("Content-Disposition","attachment;filename=\""+fileName+"\"");
        FileUtils.readFile(response,filePath);
    }
    protected SessionShareDto getSessionShareDto(HttpSession session,String shareId){
        return (SessionShareDto) session.getAttribute(Constants.SESSION_SHARE_KEY+shareId);
    }
}
