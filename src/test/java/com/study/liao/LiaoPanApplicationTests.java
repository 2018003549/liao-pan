package com.study.liao;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.study.liao.dao.EmailCodeDao;
import com.study.liao.dao.OnlineFileInfoDao;
import com.study.liao.dao.UserGroupDetailInfoDao;
import com.study.liao.dao.UserInfoDao;
import com.study.liao.entity.OnlineFileInfoEntity;

import com.alibaba.excel.read.listener.ReadListener;

import com.study.liao.entity.query.UserGroupQuery;
import com.study.liao.entity.vo.PaginationResultVO;
import com.study.liao.service.OnlineEditService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    void testMybatisPlus() {
        emailCodeDao.disableEmailCode("dasda");
    }

}
