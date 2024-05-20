package com.study.liao.controller;

import java.io.IOException;

import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.annotation.VerifyParam;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.enums.ResponseCodeEnum;
import com.study.liao.entity.enums.VerifyRegexEnum;
import com.study.liao.entity.vo.ResponseVO;
import com.study.liao.util.CreateImageCode;
import com.study.liao.service.EmailCodeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.liao.service.UserInfoService;




/**
 *
 *
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-03 18:10:47
 */
@RestController
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private EmailCodeService emailCodeService;
    protected <T> ResponseVO getSuccessResponseVO(T t) {
        ResponseVO<T> responseVO = new ResponseVO<>();
        responseVO.setStatus("200");
        responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
        responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
        responseVO.setData(t);
        return responseVO;
    }
    /**
     * 生成图像验证码
     * @param response
     * @param session
     * @param type
     * @throws IOException
     */
    @RequestMapping(value = "/checkCode")
    public void checkCode(HttpServletResponse response, HttpSession session, Integer type) throws
            IOException {
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        String code = vCode.getCode();
        if (type == null || type == 0) {
            session.setAttribute(Constants.CHECK_CODE_KEY, code);
        } else {
            session.setAttribute(Constants.CHECK_CODE_KEY_EMAIL, code);
        }
        vCode.write(response.getOutputStream());
    }
    @GlobalInterceptor(checkParams = true)
    @RequestMapping("/sendEmailCode")
    public ResponseVO sendEmailCode(HttpSession session, @VerifyParam(required = true) String email, String checkCode, Integer type){
        try {
            if(!checkCode.equals(session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))){
                throw new BusinessException("图像验证码不正确");
            }
            emailCodeService.sendEmailCode(email,type);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }
    @GlobalInterceptor(checkParams = true)
    @RequestMapping("/register")
    public ResponseVO register(HttpSession session,
                               @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL,max=150) String email,
                               @VerifyParam(required = true)String nickName,
                               @VerifyParam(required = true,regex = VerifyRegexEnum.PASSWORD,min=8,max=18)String password,
                               @VerifyParam(required = true) String checkCode,
                               @VerifyParam(required = true) String emailCode){
        try {
            if(!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))){
                throw new BusinessException("图像验证码不正确");
            }
            userInfoService.register(email,nickName,password,emailCode);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }
    @GlobalInterceptor(checkParams = true)
    @RequestMapping("/login")
    public ResponseVO login(HttpSession session,
                               @VerifyParam(required = true) String email,
                               @VerifyParam(required = true)String password,
                               @VerifyParam(required = true) String checkCode){
        try {
            if(!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))){
                throw new BusinessException("图像验证码不正确");
            }
            SessionWebUserDto sessionWebUserDto = userInfoService.login(email, password);
            session.setAttribute(Constants.SESSION_KEY,sessionWebUserDto);
            return getSuccessResponseVO(sessionWebUserDto);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }
}