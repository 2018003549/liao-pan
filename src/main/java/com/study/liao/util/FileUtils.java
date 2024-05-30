package com.study.liao.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Slf4j
public class FileUtils {
    public static void readFile(HttpServletResponse response, String filePath) {
        if (!StringTools.pathIsOk(filePath)) {
            return;
        }
        OutputStream out = null;
        FileInputStream in = null;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            log.error("读取文件异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
        }
    }
}
