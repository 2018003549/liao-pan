package com.study.liao.controller;

import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.annotation.VerifyParam;
import com.study.liao.entity.FileInfoEntity;
import com.study.liao.entity.ShareInfoEntity;
import com.study.liao.entity.UserInfoEntity;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.SessionShareDto;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.enums.FileCategoryEnums;
import com.study.liao.entity.enums.FileDelFlagEnums;
import com.study.liao.entity.enums.ResponseCodeEnum;
import com.study.liao.entity.query.FileInfoQuery;
import com.study.liao.entity.vo.FileInfoVO;
import com.study.liao.entity.vo.PaginationResultVO;
import com.study.liao.entity.vo.ResponseVO;
import com.study.liao.service.ShareInfoService;
import com.study.liao.service.UserInfoService;
import com.study.liao.util.StringTools;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.Date;

@RestController("webShareController")
@RequestMapping("/showShare")
public class WebShareController extends ABaseController {
    @Autowired
    ShareInfoService shareInfoService;
    @Autowired
    UserInfoService userInfoService;
    /**
     * @param session 需要校验当前登录用户是否为分享用户
     * @param shareId 根据分享码获取分享基本信息
     * @return
     */
    @RequestMapping("/getShareLoginInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO getShareLoginInfo(HttpSession session, @VerifyParam(required = true) String shareId) {
        SessionShareDto sessionShareDto = getSessionShareDto(session, shareId);
        //如果session中获取不到分享信息，说明是未登录的游客打开的分享链接
        if (sessionShareDto == null) {
            return getSuccessResponseVO(null);
        }
        //1.获取基本分享信息【包括分享文件id、分享人id、分享码等】
        ShareInfoEntity shareInfo = getShareInfoCommon(shareId);
        //2.获取当前用户信息，并且判断分享人是否就是当前用户
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        if (webUserDto != null && webUserDto.getUserId().equals(shareInfo.getUserId())) {
            shareInfo.setCurrentUser(true);
        } else {
            shareInfo.setCurrentUser(false);
        }
        return getSuccessResponseVO(shareInfo);
    }

    /**
     * @param shareId 根据分享码获取分享文件
     * @return
     */
    @RequestMapping("getShareInfo")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO getShareInfo(@VerifyParam(required = true) String shareId) {
        ShareInfoEntity shareInfo = getShareInfoCommon(shareId);
        return getSuccessResponseVO(shareInfo);
    }

    protected ShareInfoEntity getShareInfoCommon(String shareId) {
        ShareInfoEntity shareInfo = shareInfoService.getById(shareId);
        //1.分享链接不存在或者过期
        if (null == shareInfo || (shareInfo.getExpireTime() != null && new Date().after(shareInfo.getExpireTime()))) {
            throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
        }
        //2.查询用户的分享文件信息
        FileInfoEntity fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(shareInfo.getFileId(), shareInfo.getUserId());
        if (fileInfo == null || !FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())) {
            //分享的文件被删除
            throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
        }
        shareInfo.setFileName(fileInfo.getFileName());
        //3.查询分享人信息
        UserInfoEntity userInfo = userInfoService.getById(shareInfo.getUserId());
        shareInfo.setNickName(userInfo.getNickName());
        return shareInfo;
    }
    @RequestMapping("/checkShareCode")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public ResponseVO checkShareCode(HttpSession session,
                                     @VerifyParam(required = true)String shareId,
                                     @VerifyParam(required = true)String code){
        SessionShareDto shareDto=shareInfoService.checkShareCode(shareId,code);
        session.setAttribute(Constants.SESSION_SHARE_KEY+shareId,shareDto);
        return getSuccessResponseVO(null);
    }

    /**
     * 获取分享文件列表
     */
    @RequestMapping("/loadFileList")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO loadFileList(HttpSession session,
                                   @VerifyParam(required = true) String shareId,
                                   String filePid) {
        //1.先校验基本分享信息
        SessionShareDto shareDto = checkShareInfo(session, shareId);
        //2.防止分享越权，不能访问同级其它文件或者上级文件
        FileInfoQuery query = new FileInfoQuery();
        if(!StringTools.isEmpty(filePid)&&!Constants.ZERO_STR.equals(filePid)){
            //校验当前访问的文件是否在分享根目录下
            fileInfoService.checkRootFilePid(filePid,shareDto.getShareUserId(),shareDto.getFileId());
            query.setFilePid(filePid);
        }else {
            //说明分享的只是一个文件或者当前在系统根目录
            query.setFileId(shareDto.getFileId());
        }
        //3.查询分享文件信息
        query.setUserId(shareDto.getShareUserId());
        query.setOrderBy("last_update_time desc");
        query.setDelFlag(FileDelFlagEnums.USING.getFlag());
        PaginationResultVO<FileInfoEntity> listByPage = fileInfoService.findListByPage(query);
        return getSuccessResponseVO(listByPage);
    }
    private SessionShareDto checkShareInfo(HttpSession session,String shareId){
        SessionShareDto shareDto = getSessionShareDto(session, shareId);
        if(null==shareDto){
            //session中没有分享信息，说明是分享验证状态过期
            throw new BusinessException(ResponseCodeEnum.CODE_903);
        }
        if(shareDto.getExpireTime()!=null&&new Date().after(shareDto.getExpireTime())){
            //分享链接失效
            throw new BusinessException(ResponseCodeEnum.CODE_902);
        }
        return shareDto;
    }
    @RequestMapping("/getFolderInfo")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO getFolderInfo(HttpSession session,
                                    @VerifyParam(required = true)String shareId,
                                    @VerifyParam(required = true) String path) {
        SessionShareDto shareDto = getSessionShareDto(session, shareId);
        return super.getFolderInfo(path, shareDto.getShareUserId());
    }
    /**
     * 根据预览某个分享记录下的某个文件
     */
    @RequestMapping("/getFile/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)
    public void getFile(HttpServletResponse response,
                        HttpSession session,
                        @PathVariable("fileId") String fileId,
                        @PathVariable("shareId") String shareId) {
        SessionShareDto shareDto = getSessionShareDto(session, shareId);
        super.getFile(response, fileId, shareDto.getShareUserId());
    }
    @RequestMapping("ts/getVideoInfo/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public void getVideoInfo(HttpServletResponse response, HttpSession session,
                             @PathVariable("shareId")String shareId,
                             @PathVariable("fileId") String fileId) {
        SessionShareDto shareDto = getSessionShareDto(session, shareId);
        super.getFile(response, fileId, shareDto.getShareUserId());
    }

    @RequestMapping("createDownloadUrl/{shareId}/{fileId}")
    @GlobalInterceptor(checkParams = true,checkLogin = false)
    public ResponseVO createDownloadUrl(
            HttpSession session,
            @PathVariable(required = true) String fileId
            , @PathVariable(required = true) String shareId) {
        SessionShareDto shareDto = getSessionShareDto(session, shareId);
        return super.createDownloadUrl(fileId, shareDto.getShareUserId());
    }
    @RequestMapping("/download/{code}")
    @GlobalInterceptor(checkParams = true, checkLogin = false)//不需要校验登录
    public void download(HttpServletRequest request, HttpServletResponse response,
                         @VerifyParam(required = true) @PathVariable("code") String code) throws UnsupportedEncodingException {
        super.download(request, response, code);
    }

    /**
     * 保存选中的分享文件到我的网盘
     * @param shareId 分享链接id
     * @param shareFileIds 选中的多个分享文件id
     * @param myFolderId 保存到的目标目录id
     */
    @RequestMapping("/saveShare")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO cancelShare(HttpSession session,
                                  @VerifyParam(required = true)String shareId,
                                  @VerifyParam(required = true) String shareFileIds,
                                  @VerifyParam(required = true)String myFolderId) {
        SessionShareDto shareDto = checkShareInfo(session, shareId);//获取分享信息
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        if(webUserDto.getUserId().equals(shareDto.getShareUserId())){
            //自己的文件保存到自己的网盘没必要
            throw new BusinessException("这是你分享的文件！！");
        }
        fileInfoService.saveShare(shareDto.getFileId(),shareFileIds,
                myFolderId,shareDto.getShareUserId(),webUserDto.getUserId());
        return getSuccessResponseVO(null);
    }
}
