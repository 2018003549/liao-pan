package com.study.liao.util;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.*;
import java.util.*;

public class HttpUtils {

    // 发送GET请求
    public static String sendGet(String url, Map<String, String> params) throws IOException {
        StringBuilder urlWithParams = new StringBuilder(url);
        // 如果有参数，拼接在URL后面
        if (params != null && !params.isEmpty()) {
            urlWithParams.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlWithParams.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                        .append("&");
            }
            // 去掉最后的&符号
            urlWithParams.deleteCharAt(urlWithParams.length() - 1);
        }
        // 创建URL对象并打开连接
        URL urlObj = new URL(urlWithParams.toString());
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        // 获取响应内容
        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        if (responseCode == HttpURLConnection.HTTP_OK) { // 正常响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }
        return response.toString();
    }

    // 发送POST请求
    public static String sendPost(String url, Map<String, String> params) throws IOException {
        // 创建URL对象并打开连接
        URL urlObj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.setDoOutput(true); // 允许输出数据
        // 设置POST请求的参数
        if (params != null && !params.isEmpty()) {
            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                postData.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                        .append("&");
            }
            // 去掉最后的&符号
            postData.deleteCharAt(postData.length() - 1);
            // 发送请求参数
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.toString().getBytes("UTF-8");
                os.write(input, 0, input.length);
            }
        }
        // 获取响应内容
        int responseCode = connection.getResponseCode();
        StringBuilder response = new StringBuilder();
        if (responseCode == HttpURLConnection.HTTP_OK) { // 正常响应
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }
        return response.toString();
    }

    // 发送GET请求并返回二进制数据，用于下载文件
    public static byte[] sendGetForBinary(String url, Map<String, String> params) throws IOException {
        StringBuilder urlWithParams = new StringBuilder(url);
        // 如果有参数，拼接在URL后面
        if (params != null && !params.isEmpty()) {
            urlWithParams.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlWithParams.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                        .append("&");
            }
            // 去掉最后的&符号
            urlWithParams.deleteCharAt(urlWithParams.length() - 1);
        }
        // 创建URL对象并打开连接
        URL urlObj = new URL(urlWithParams.toString());
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        // 获取响应内容（读取二进制数据）
        int responseCode = connection.getResponseCode();
        byte[] response = null;
        if (responseCode == HttpURLConnection.HTTP_OK) { // 正常响应
            try (InputStream inputStream = connection.getInputStream()) {
                response = inputStream.readAllBytes();  // 读取所有的字节数据
            }
        }
        return response;
    }

    /**
     * 使用 RestTemplate 上传 MultipartFile
     * @param url 上传的 URL
     * @param file 文件对象
     * @return 响应信息
     */
    public static String uploadFile(String url, MultipartFile file)  {
        // 创建 RestTemplate 对象
        RestTemplate restTemplate = new RestTemplate();
        // 创建 multipart 请求体
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("uploadedFile", file.getResource());
        // 创建 HttpHeaders 设置内容类型为 multipart
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        // 构建 HTTP 请求
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        // 发送 POST 请求并获取响应
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        // 返回响应结果
        return responseEntity.getBody();
    }
}
