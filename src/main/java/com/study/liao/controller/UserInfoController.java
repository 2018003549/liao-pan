package com.study.liao.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.study.liao.annotation.GlobalInterceptor;
import com.study.liao.annotation.VerifyParam;
import com.study.liao.component.RedisComponent;
import com.study.liao.config.AppConfig;
import com.study.liao.entity.UserInfoEntity;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.dto.SessionWebUserDto;
import com.study.liao.entity.dto.UserSpaceDto;
import com.study.liao.entity.enums.ResponseCodeEnum;
import com.study.liao.entity.enums.VerifyRegexEnum;
import com.study.liao.entity.vo.ResponseVO;
import com.study.liao.util.CreateImageCode;
import com.study.liao.service.EmailCodeService;
import com.study.liao.util.FileUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.study.liao.service.UserInfoService;
import org.springframework.web.multipart.MultipartFile;


/**
 * @author liao
 * @email sunlightcs@gmail.com
 * @date 2024-05-03 18:10:47
 */
@Slf4j
@RestController
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private EmailCodeService emailCodeService;
    @Autowired
    AppConfig appConfig;
    @Autowired
    RedisComponent redisComponent;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";
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
     *
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

    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/sendEmailCode")
    public ResponseVO sendEmailCode(HttpSession session, @VerifyParam(required = true) String email, String checkCode, Integer type) {
        try {
            if (!checkCode.equals(session.getAttribute(Constants.CHECK_CODE_KEY_EMAIL))) {
                ResponseVO responseVO = new ResponseVO();
                responseVO.fail(500,"图像验证码不正确，请刷新验证码");
                return responseVO;
            }
            emailCodeService.sendEmailCode(email, type);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY_EMAIL);
        }
    }

    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/register")
    public ResponseVO register(HttpSession session,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                               @VerifyParam(required = true) String nickName,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                               @VerifyParam(required = true) String checkCode,
                               @VerifyParam(required = true) String emailCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图像验证码不正确");
            }
            userInfoService.register(email, nickName, password, emailCode);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/login")
    public ResponseVO login(HttpSession session,
                            @VerifyParam(required = true) String email,
                            @VerifyParam(required = true) String password,
                            @VerifyParam(required = true) String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图像验证码不正确");
            }
            SessionWebUserDto sessionWebUserDto = userInfoService.login(email, password);
            session.setAttribute(Constants.SESSION_KEY, sessionWebUserDto);
            return getSuccessResponseVO(sessionWebUserDto);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @RequestMapping("/resetPwd")
    public ResponseVO resetPwd(HttpSession session,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.EMAIL, max = 150) String email,
                               @VerifyParam(required = true, regex = VerifyRegexEnum.PASSWORD, min = 8, max = 18) String password,
                               @VerifyParam(required = true) String checkCode,
                               @VerifyParam(required = true) String emailCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图像验证码不正确");
            }
            userInfoService.resetPwd(email, password, emailCode);
            return getSuccessResponseVO(null);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }

    @GlobalInterceptor(checkParams = true,checkLogin = false)
    @GetMapping("/getAvatar/{userId}")
    public void resetPwd(HttpServletResponse response, @VerifyParam(required = true) @PathVariable("userId") String userId) throws IOException {
        //1.获取存储头像的目录
        String avatarFolderName=Constants.FILE_FOLDER_FILE+Constants.FILE_FOLDER_AVATAR_NAME;
        File folder = new File(appConfig.getProjectFolder() + avatarFolderName);
        if(!folder.exists()){
            folder.mkdirs();//mkdirs是根据绝对路径创建目录
        }
        //2.拼接完整路径
        String avatarPath=appConfig.getProjectFolder()+avatarFolderName+userId+Constants.AVATAR_SUFFIX;
        File file=new File(avatarPath);
        if(!file.exists()){
            //3.如果头像不存在就给个默认头像
            String defalutPath = appConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFUALT;
            File defaultAvatar = new File(defalutPath);
            if(!defaultAvatar.exists()){
                //4.如果默认头像都不存在就要抛异常，提醒管理员去设置
                response.sendError(500,"请设置系统默认头像");
                printNoDefaultImage(response);
            }
            avatarPath=defalutPath;
        }
        response.setContentType("image/jpg");
        FileUtils.readFile(response,avatarPath);
    }
    private void printNoDefaultImage(HttpServletResponse response) {
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        response.setStatus(HttpStatus.OK.value());
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print("请在头像目录下放置默认头像default_avatar.jpg");
        } catch (Exception e) {
            log.error("输出无默认图失败", e);
        } finally {
            if(writer!=null){
                writer.close();
            }
        }
    }
    @RequestMapping("getUseSpace")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getUseSpace(HttpSession session) throws IOException {
        //从session中获取用户信息
        SessionWebUserDto sessionWebUserDto= (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        UserSpaceDto useSpace = redisComponent.getUseSpace(sessionWebUserDto.getUserId());
        return getSuccessResponseVO(useSpace);
    }
    @RequestMapping("logout")
    public ResponseVO logout(HttpSession session){
        session.invalidate();
        return getSuccessResponseVO(null);
    }
    @RequestMapping("/updateUserAvatar")
    @GlobalInterceptor
    public ResponseVO updateUserAvatar(HttpSession session, MultipartFile avatar) {
        SessionWebUserDto webUserDto= (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
        //1.获取到头像存放目录
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        if (!targetFileFolder.exists()) {
            targetFileFolder.mkdirs();
        }
        //2.拼接用户头像存储luj
        String avatarPath=targetFileFolder.getPath() + "/" + webUserDto.getUserId() + Constants.AVATAR_SUFFIX;
        File targetFile = new File(avatarPath);
        try {
            avatar.transferTo(targetFile);
        } catch (Exception e) {
            log.error("上传头像失败", e);
        }
        UserInfoEntity userInfo = new UserInfoEntity();
        //3.让数据库中用户的头像路径失效，直接走根据用户id查头像的逻辑
        userInfo.setQqAvatar("");
        userInfoService.updateById(userInfo);
        //4.session中的头像信息也要失效
        webUserDto.setAvatar(null);
        session.setAttribute(Constants.SESSION_KEY, webUserDto);
        return getSuccessResponseVO(null);
    }
}