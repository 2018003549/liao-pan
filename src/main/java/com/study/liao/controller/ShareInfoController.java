package com.study.liao.controller;

import java.util.HashMap;

import com.common.utils.PageUtils;
import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.annotation.VerifyParam;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.vo.ResponseVO;
import com.study.liao.service.ShareInfoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-06-09 14:05:18
 */
@RestController
@RequestMapping("/share")
public class ShareInfoController extends ABaseController {
    @Autowired
    private ShareInfoService shareInfoService;

    @RequestMapping("/loadShareList")
    @GlobalInterceptor
    public ResponseVO loadShareList(HttpSession session, Integer pageNo, Integer pageSize) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        HashMap<String, Object> map = new HashMap<>();
        map.put("currPage", pageNo);
        map.put("pageSize", pageSize);
        PageUtils pageUtils = shareInfoService.loadShareList(webUserDto.getUserId(), map);
        return getSuccessResponseVO(pageUtils);
    }

    /**
     * @param fileId 分享的文件id
     * @param validType 时效类型
     * @param code 分享码
     * @return
     */
    @RequestMapping("/shareFile")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO shareFile(HttpSession session, @VerifyParam(required = true) String fileId,
                                @VerifyParam(required = true) Integer validType, String code) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        shareInfoService.shareFile(webUserDto.getUserId(), fileId, validType, code);
        return getSuccessResponseVO(null);
    }

    /**
     * 批量取消文件分享
     * @param shareIds 选中的分享记录
     */
    @RequestMapping("/cancelShare")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO cancelShare(HttpSession session, @VerifyParam(required = true) String shareIds) {
        SessionWebUserDto webUserDto = getUserInfoFromSession(session);
        shareInfoService.cancelShareBatch(webUserDto.getUserId(),shareIds);
        return getSuccessResponseVO(null);
    }
}
