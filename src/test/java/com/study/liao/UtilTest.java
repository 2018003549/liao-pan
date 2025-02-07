package com.study.liao;

import com.study.liao.util.FileUtils;
import com.study.liao.util.HttpUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 专门用于测试各个工具类
 */
public class UtilTest {
    @Test
    public void testHttpUtil() {
        Map<String, String> params = new HashMap<>();
        params.put("fileName", "new.docxf");
        try {
            byte[] bytes = HttpUtils.sendGetForBinary("http://192.168.32.100:9000/example/download?fileName=excel_test.xlsx", params);
            FileUtils.saveFileToLocal(bytes,"D:\\Study\\FileTest\\easypan\\excel_test.xlsx");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
