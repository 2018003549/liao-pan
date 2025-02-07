package com.study.liao.util;

import com.study.liao.config.AppConfig;
import com.study.liao.entity.constants.Constants;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;

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

    public static void downloadFile(HttpServletResponse response, byte[] fileData, String fileName) {
        OutputStream out = null;
        try {
            // 设置响应内容类型
            response.setContentType("application/octet-stream");
            // 设置文件下载时的文件名
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);

            // 获取输出流
            out = response.getOutputStream();

            // 写入文件数据
            out.write(fileData);
            out.flush();
        } catch (IOException e) {
            log.error("文件下载异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
        }
    }
    public static void readFile(HttpServletResponse response, byte[] fileData, String fileName) {
        OutputStream out = null;
        try {
            // 设置响应内容类型
            response.setContentType("application/octet-stream");
            // 设置文件下载时的文件名
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
            // 获取输出流
            out = response.getOutputStream();
            // 写入文件数据
            out.write(fileData);
            out.flush();
        } catch (IOException e) {
            log.error("文件下载异常", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("IO异常", e);
                }
            }
        }
    }

    // 将字节流保存为本地文件
    public static void saveFileToLocal(byte[] fileData, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileData);  // 将字节数据写入文件
        }
    }
}
