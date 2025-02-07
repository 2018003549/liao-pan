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
    @Autowired
    OnlineFileInfoDao onlineFileInfoDao;
    @Autowired
    OnlineEditService onlineEditService;

    @Test
    void contextLoads() {
    }

    @Test
    void testMybatisPlus() {
        emailCodeDao.disableEmailCode("dasda");
    }

    @Test
    void testOnlineFileSelect() {
        Long l = onlineFileInfoDao.selectCount(new QueryWrapper<OnlineFileInfoEntity>()
                .eq("filename", "777"));
        System.out.println(l);
    }

    @Test
    void testUserGroupList(){
        UserGroupQuery query = new UserGroupQuery();
        query.setUserId("4564622893");
        query.setGroupName("21");
        PaginationResultVO userGroupDetailDTOS = onlineEditService.selectUserGroupList(query);
        System.out.println(userGroupDetailDTOS);
    }

    @Test
    public void testEasyExcel() {
        String fileName = "D:\\DeskTop\\excel_test.xlsx";  // Excel 文件路径
        // 读取 Excel 文件并使用 Map 存储每一行数据
        EasyExcel.read(fileName, new AnalysisEventListener<Map<Integer, String>>() {
            // 存储表头数据，key为列号，value为列名
            Map<Integer, String> headerNameMap = new HashMap<>();
            // 用于保存行记录，外层key为行号，内层key为列号，value为单元格值
            Map<Integer, Map<Integer, String>> rowDataMap = new HashMap<>();

            @Override
            public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
                // 读取到每一行时的回调
                rowDataMap.put(context.readRowHolder().getRowIndex(), rowData);
            }
            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                headerNameMap = headMap;
            }
            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                // 解析完成后的回调
                System.out.println(headerNameMap);
                System.out.println(rowDataMap);
            }
        }).sheet().doRead();  // 读取第一个 sheet 中的数据
    }
}
