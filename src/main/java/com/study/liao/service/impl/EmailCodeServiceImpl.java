package com.study.liao.service.impl;

import com.common.utils.PageUtils;
import com.common.utils.Query;
import com.study.liao.component.RedisComponent;
import com.study.liao.config.AppConfig;
import com.study.liao.entity.constants.BusinessException;
import com.study.liao.entity.constants.Constants;
import com.study.liao.entity.UserInfoEntity;
import com.study.liao.entity.dto.SysSettingsDto;
import com.study.liao.service.UserInfoService;
import com.study.liao.util.StringTools;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.study.liao.dao.EmailCodeDao;
import com.study.liao.entity.EmailCodeEntity;
import com.study.liao.service.EmailCodeService;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("emailCodeService")
public class EmailCodeServiceImpl extends ServiceImpl<EmailCodeDao, EmailCodeEntity> implements EmailCodeService {
    @Autowired
    UserInfoService userInfoService;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private AppConfig appConfig;
    @Autowired
    private RedisComponent redisComponent;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<EmailCodeEntity> page = this.page(
                new Query<EmailCodeEntity>().getPage(params),
                new QueryWrapper<EmailCodeEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailCode(String email, Integer type) {
        if (type == 0) {
            //注冊
            //去用户信息表中根据邮箱查询
            UserInfoEntity userInfo = userInfoService.selectByEmail(email);
            if (userInfo != null) {
                throw new BusinessException("邮箱已经存在");
            }
        }
        //生成五位的随机数
        String code = StringTools.getRandomNumber(Constants.LENGTH_5);
        //发送验证码之前，要将之前的该邮箱的验证码置为无效
        baseMapper.disableEmailCode(email);
        //发送验证码
        sendCode(email, code);
        EmailCodeEntity emailCodeEntity = new EmailCodeEntity();
        emailCodeEntity.setCode(code);
        emailCodeEntity.setEmail(email);
        emailCodeEntity.setStatus(0);//为0表示该验证码还未使用
        emailCodeEntity.setCreateTime(new Date());
        baseMapper.insert(emailCodeEntity);
    }

    @Override
    public void checkCode(String email, String emailCode) {
        EmailCodeEntity emailCodeEntity = baseMapper.selectOne(new QueryWrapper<EmailCodeEntity>()
                .eq("email", email).eq("code", emailCode));
        if(null==emailCodeEntity){
            throw new BusinessException("邮箱验证码不正确");
        }
        if(emailCodeEntity.getStatus()==1||
                System.currentTimeMillis()-emailCodeEntity.getCreateTime().getTime()
                        >Constants.LENGTH_15*1000*60){
            throw new BusinessException("验证码已失效");
        }
    }

    private void sendCode(String toEmail, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            //1.从redis中读取系统配置
            SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();
            //2.设置邮件信息
            helper.setFrom(appConfig.getSendUserName());//设置发件人
            helper.setTo(toEmail);//设置收件邮箱
            helper.setSubject(sysSettingsDto.getRegisterEmailTitle());//设置标题
            //因为sysSettingsDto中验证码内容设置了占位符，所以可以用code去替换占位符
            helper.setText(String.format(sysSettingsDto.getRegisterEmailContent(), code));
            javaMailSender.send(message);//发送邮件
        } catch (MessagingException e) {
            log.error("邮件发送失败", e);
            throw new BusinessException("邮件发送失败");
        }
    }
}