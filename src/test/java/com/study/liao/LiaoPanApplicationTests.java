package com.study.liao;

import com.study.liao.dao.EmailCodeDao;
import com.study.liao.dao.UserInfoDao;
import com.study.liao.entity.UserInfoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LiaoPanApplicationTests {
    @Autowired
    UserInfoDao userInfoDao;
    @Autowired
    EmailCodeDao emailCodeDao;
    @Test
    void contextLoads() {
    }
    @Test
    void testMybatisPlus(){
        emailCodeDao.disableEmailCode("dasda");
    }
}
