# 环境搭建

## 安装ffmpeg

1. 下载地址https://wwur.lanzout.com/iORvc0tia6uj  密码:9n15

2. 配置环境变量，将bin目录所在路径配置到path下【执行相关命令时会去环境变量找可执行文件，否则只有在安装目录下才可执行】

   <img src="easypan.assets/image-20240503162614035.png" alt="image-20240503162614035" style="zoom: 67%;" />.

3. `ffmpeg -verison`查看ffmpeg 的版本，检查是否配置成功

   <img src="easypan.assets/image-20240503162828444.png" alt="image-20240503162828444" style="zoom:50%;" />.

## 后端环境搭建

### 设置配置文件的编码方式

- 【setting->File Encoding】，否则配置文件会出现中文乱码

<img src="easypan.assets/image-20240503163501830.png" alt="image-20240503163501830" style="zoom:80%;" />.

### 导入依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.6.1</version>
        <relativePath/>
    </parent>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.easypan</groupId>
    <artifactId>easypan</artifactId>
    <version>1.0</version>
    <packaging>jar</packaging>
    <name>easypan</name>
    <description>easypan</description>
    <properties>
        <java.version>1.8</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <skipTests>true</skipTests>
        <springboot.version>2.6.1</springboot.version>
        <mybatis.version>1.3.2</mybatis.version>
        <logback.version>1.2.10</logback.version>
        <mysql.version>8.0.23</mysql.version>
        <aspectjweaver.version>1.9.4</aspectjweaver.version>
        <fastjson.version>1.2.66</fastjson.version>
        <commons.lang3.version>3.4</commons.lang3.version>
        <commons.codec.version>1.9</commons.codec.version>
        <commons.io.version>2.5</commons.io.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <!--邮件发送-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
            <version>${springboot.version}</version>
        </dependency>
        <!--redis -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <version>${springboot.version}</version>
        </dependency>
        <!--mybatis-->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>${mybatis.version}</version>
        </dependency>
        <!-- 数据库-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>${mysql.version}</version>
        </dependency>
        <!--切面-->
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjweaver</artifactId>
            <version>${aspectjweaver.version}</version>
        </dependency>
        <!--fastjson-->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>${fastjson.version}</version>
        </dependency>
        <!--apache common-->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>${commons.lang3.version}</version>
        </dependency>
        <dependency>
            <groupId>commons-codec</groupId>
            <artifactId>commons-codec</artifactId>
            <version>${commons.codec.version}</version>
        </dependency>
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>${commons.io.version}</version>
        </dependency>
    </dependencies>
</project>
```

### 日志配置

- logback-spring.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration scan="true" scanPeriod="10 minutes">
    <appender name="stdot" class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <pattern>%d{yyyy-MM-dd HH:mm:ss,GMT+8} [%p][%c][%M][%L]-> %m%n</pattern>
        </layout>
    </appender>
    <springProperty scope="context" name="log.path" source="project.folder"/>
    <springProperty scope="context" name="log.root.level" source="log.root.level"/>
    <property name="LOG_FOLDER" value="logs"/>
    <property name="LOG_FILE_NAME" value="easypan.log"/>
    <appender name="file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${log.path}/${LOG_FOLDER}/${LOG_FILE_NAME}</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <FileNamePattern>${log.path}/${LOG_FOLDER}/${LOG_FILE_NAME}.%d{yyyyMMdd}.%i</FileNamePattern>
            <cleanHistoryOnStart>true</cleanHistoryOnStart>
            <TimeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <MaxFileSize>20MB</MaxFileSize>
            </TimeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <charset>utf-8</charset>
            <pattern>%d{yyyy-MM-dd HH:mm:ss,GMT+8} [%p][%c][%M][%L]-> %m%n</pattern>
        </encoder>
        <append>false</append>
        <prudent>false</prudent>
    </appender>
    <root level="${log.root.level}">
        <appender-ref ref="stdot"/>
        <appender-ref ref="file"/>
    </root>
</configuration>

```

### 邮箱配置

1. 登录QQ邮箱，点击设置->账号->POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务

   <img src="easypan.assets/image-20240503165042870.png" alt="image-20240503165042870" style="zoom:80%;" />.

2. 开启服务，生成授权码jrodoqmdprmpbdgh

   <img src="easypan.assets/image-20240503165102458.png" alt="image-20240503165102458" style="zoom: 50%;" />.

3. 配置邮箱信息

   ```properties
   #发送邮件配置相关
   # 配置邮件服务器的地址 smtp.qq.com
   spring.mail.host=smtp.qq.com
   # 配置邮件服务器的端口（465或587）
   spring.mail.port=465
   # 配置用户的账号
   spring.mail.username=2018003549@qq.com
   # 配置用户的密码
   spring.mail.password=jrodoqmdprmpbdgh
   # 配置默认编码
   spring.mail.default-encoding=UTF-8
   # SSL 连接配置
   spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
   ```

### 配置文件

```properties
# 应用服务 WEB 访问端口
server.port=7090
server.servlet.context-path=/api
#session过期时间 60M 一个小时
server.servlet.session.timeout=PT60M
#处理favicon
spring.mvc.favicon.enable=false
#异常处理
spring.mvc.throw-exception-if-no-handler-found=true
spring.web.resources.add-mappings=false
#数据库配置
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/easypan?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf8&autoReconnect=true&allowMultiQueries=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.hikari.pool-name=HikariCPDatasource
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=180000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.max-lifetime=1800000
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.connection-test-query=SELECT 1
#发送邮件配置相关
# 配置邮件服务器的地址 smtp.qq.com
spring.mail.host=smtp.qq.com
# 配置邮件服务器的端口（465或587）
spring.mail.port=465
# 配置用户的账号
spring.mail.username=2018003549@qq.com
# 配置用户的密码
spring.mail.password=jrodoqmdprmpbdgh
# 配置默认编码
spring.mail.default-encoding=UTF-8
# SSL 连接配置
spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory
#Spring redis配置
# Redis数据库索引（默认为0）
spring.data.redis.database=0
spring.data.redis.host=120.26.122.127
spring.data.redis.port=6379
# 连接池最大连接数（使用负值表示没有限制）
spring.data.redis.jedis.pool.max-active=20
# 连接池最大阻塞等待时间（使用负值表示没有限制）
spring.data.redis.jedis.pool.max-wait=-1
# 连接池中的最大空闲连接
spring.data.redis.jedis.pool.max-idle=10
# 连接池中的最小空闲连接
spring.data.redis.jedis.pool.min-idle=0
# 连接超时时间（毫秒）
spring.data.redis.timeout=2000
#项目目录【存放项目生成的一些日志文件、图片信息等】
project.folder=D:/Study/FileTest/easypan
#超级管理员id
admin.emails=2018003549@qq.com
#是否是开发环境
dev=false
##qq登陆相关：这一块需要修改##
qq.app.id=12333
qq.app.key=2222222
qq.url.authorization=https://graph.qq.com/oauth2.0/authorize?response_type=code&client_id=%s&redirect_uri=%s&state=%s
qq.url.access.token=https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s
qq.url.openid=https://graph.qq.com/oauth2.0/me?access_token=%S
qq.url.user.info=https://graph.qq.com/user/get_user_info?access_token=%s&oauth_consumer_key=%s&openid=%s
qq.url.redirect=http://easypan.wuhancoder.com/qqlogincalback
```

### 主启动类

```java
@EnableAsync
@EnableTransactionManagement
@EnableScheduling
@SpringBootApplication
public class LiaoPanApplication {
    public static void main(String[] args) {
        SpringApplication.run(LiaoPanApplication.class, args);
    }
}
```

## 部署前端环境

### 部署nginx

1. 下载nginx

2. 配置nginx的监听服务器

   ```nginx
   server {
       listen 80;
       server_name liao.easypan.com;
       charset utf-8;
        location / { 
              #打包好的前端程序的位置
              alias D:/Study/tool/easypan-nginx/EasyPan-main/myworkspace-front/easypan-front/dist/;
              try_files $uri $uri/ /index.html;
              index  index.html index.htm;
        }
         location /api { 
             proxy_pass http://localhost:7090/api;
             proxy_set_header x-forwarded-for  $remote_addr;
        }
   }
   ```

3. host文件配置域名映射

4. 部署成功【nice！！！】

   ![image-20240503212301245](easypan.assets/image-20240503212301245.png)

### 单独运行前端环境

- `crtl+·`打开vscode控制台，执行`cnpm run dev`启动前端项目

![image-20250202142212328](easypan.assets/image-20250202142212328.png).

# 工具类

## 字符串

```java
public class StringTools {
	//将密码转成md5密文
    public static String encodeByMD5(String originString) {
        return StringTools.isEmpty(originString) ? null : DigestUtils.md5Hex(originString);
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }
	
    public static String getFileSuffix(String fileName) {
        Integer index = fileName.lastIndexOf(".");
        if (index == -1) {
            return "";
        }
        String suffix = fileName.substring(index);
        return suffix;
    }


    public static String getFileNameNoSuffix(String fileName) {
        Integer index = fileName.lastIndexOf(".");
        if (index == -1) {
            return fileName;
        }
        fileName = fileName.substring(0, index);
        return fileName;
    }

    public static String rename(String fileName) {
        String fileNameReal = getFileNameNoSuffix(fileName);
        String suffix = getFileSuffix(fileName);
        return fileNameReal + "_" + getRandomString(Constants.LENGTH_5) + suffix;
    }

    public static final String getRandomString(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }

    public static final String getRandomNumber(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }


    public static String escapeTitle(String content) {
        if (isEmpty(content)) {
            return content;
        }
        content = content.replace("<", "&lt;");
        return content;
    }


    public static String escapeHtml(String content) {
        if (isEmpty(content)) {
            return content;
        }
        content = content.replace("<", "&lt;");
        content = content.replace(" ", "&nbsp;");
        content = content.replace("\n", "<br>");
        return content;
    }
	//检查文件路径是否正确,避免越级读取文件的风险
    public static boolean pathIsOk(String path) {
        if (StringTools.isEmpty(path)) {
            return true;
        }
        if (path.contains("../") || path.contains("..\\")) {
            return false;
        }
        return true;
    }
}
```

## 文件操作

```java
@Slf4j
public class FileUtils {
    //读取文件流
    protected void readFile(HttpServletResponse response, String filePath) {
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
     // 将字节流保存为本地文件 2025-1-23新加，来源gpt
    public static void saveFileToLocal(byte[] fileData, String filePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileData);  // 将字节数据写入文件
        }
    }
}
```

## FFmpeg

```java
public class ProcessUtils {
    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);
    public static String executeCommand(String cmd, Boolean outprintLog) throws BusinessException {
        if (StringTools.isEmpty(cmd)) {
            logger.error("--- 指令执行失败，因为要执行的FFmpeg指令为空！ ---");
            return null;
        }
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            // 执行ffmpeg指令
            // 取出输出流和错误流的信息
            // 注意：必须要取出ffmpeg在执行命令过程中产生的输出信息，如果不取的话当输出流信息填满jvm存储输出留信息的缓冲区时，线程就回阻塞住
            PrintStream errorStream = new PrintStream(process.getErrorStream());
            PrintStream inputStream = new PrintStream(process.getInputStream());
            errorStream.start();
            inputStream.start();
            // 等待ffmpeg命令执行完
            process.waitFor();
            // 获取执行结果字符串
            String result = errorStream.stringBuffer.append(inputStream.stringBuffer + "\n").toString();
            // 输出执行的命令信息
            if (outprintLog) {
                logger.info("执行命令:{}，已执行完毕,执行结果:{}", cmd, result);
            } else {
                logger.info("执行命令:{}，已执行完毕", cmd);
            }
            return result;
        } catch (Exception e) {
            // logger.error("执行命令失败:{} ", e.getMessage());
            e.printStackTrace();
            throw new BusinessException("视频转换失败");
        } finally {
            if (null != process) {
                ProcessKiller ffmpegKiller = new ProcessKiller(process);
                runtime.addShutdownHook(ffmpegKiller);
            }
        }
    }
    /**
     * 在程序退出前结束已有的FFmpeg进程
     */
    private static class ProcessKiller extends Thread {
        private Process process;
        public ProcessKiller(Process process) {
            this.process = process;
        }
        @Override
        public void run() {
            this.process.destroy();
        }
    }
    /**
     * 用于取出ffmpeg线程执行过程中产生的各种输出和错误流的信息
     */
    static class PrintStream extends Thread {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        public PrintStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }
        @Override
        public void run() {
            try {
                if (null == inputStream) {
                    return;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
            } catch (Exception e) {
                logger.error("读取输入流出错了！错误信息：" + e.getMessage());
            } finally {
                try {
                    if (null != bufferedReader) {
                        bufferedReader.close();
                    }
                    if (null != inputStream) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    logger.error("调用PrintStream读取输出流后，关闭流时出错！");
                }
            }
        }
    }
}
```

## 全局异常处理

```java
@RestControllerAdvice
public class AGlobalExceptionHandlerController extends ABaseController {

    private static final Logger logger = LoggerFactory.getLogger(AGlobalExceptionHandlerController.class);

    @ExceptionHandler(value = Exception.class)
    Object handleException(Exception e, HttpServletRequest request) {
        logger.error("请求错误，请求地址{},错误信息:", request.getRequestURL(), e);
        ResponseVO ajaxResponse = new ResponseVO();
        //404
        if (e instanceof NoHandlerFoundException) {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_404.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_404.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        } else if (e instanceof BusinessException) {
            //业务错误
            BusinessException biz = (BusinessException) e;
            ajaxResponse.setCode(biz.getCode() == null ? ResponseCodeEnum.CODE_600.getCode() : biz.getCode());
            ajaxResponse.setInfo(biz.getMessage());
            ajaxResponse.setStatus(STATUC_ERROR);
        } else if (e instanceof BindException|| e instanceof MethodArgumentTypeMismatchException) {
            //参数类型错误
            ajaxResponse.setCode(ResponseCodeEnum.CODE_600.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_600.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        } else if (e instanceof DuplicateKeyException) {
            //主键冲突
            ajaxResponse.setCode(ResponseCodeEnum.CODE_601.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_601.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        } else {
            ajaxResponse.setCode(ResponseCodeEnum.CODE_500.getCode());
            ajaxResponse.setInfo(ResponseCodeEnum.CODE_500.getMsg());
            ajaxResponse.setStatus(STATUC_ERROR);
        }
        return ajaxResponse;
    }
}
```

## HTTP远程调用

- 2025-1-23新加，来源gpt

```java
package com.study.liao.util;
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
}
```

# 登录注册

## 前置准备

### 数据库设计

- 字段设计

<img src="easypan.assets/image-20240503175410722.png" alt="image-20240503175410722" style="zoom:67%;" />.

- 索引设计

<img src="easypan.assets/image-20240503175422745.png" alt="image-20240503175422745" style="zoom:67%;" />.

### 快速搭建

- 这一块我是用谷粒商城之前的renrenfast
- 报错

```java
Caused by: java.lang.IllegalArgumentException: Property 'sqlSessionFactory' or 'sqlSessionTemplate' are required
```

- 原因是mybatis的版本和springboot3的版本不兼容，所以将mybatis版本号改成3.0.3即解决问题

## 获取验证码

### 请求接口

- 接口URL：GET http://localhost:9091/api/checkCode
- 请求体参数

| 参数名 | 参数值 | 是否必填 | 参数类型 | 描述说明                          |
| ------ | ------ | -------- | -------- | --------------------------------- |
| type   | 0      | 是       | String   | 0:登录注册 1:邮箱验证码发送 默认0 |

![image-20240504190929297](easypan.assets/image-20240504190929297.png)

### 生成图形验证码的工具类

```java
public class CreateImageCode {
    // 图片的宽度。
    private int width = 160;
    // 图片的高度。
    private int height = 40;
    // 验证码字符个数
    private int codeCount = 4;
    // 验证码干扰线数
    private int lineCount = 20;
    // 验证码
    private String code = null;
    // 验证码图片Buffer
    private BufferedImage buffImg = null;
    Random random = new Random();
    public CreateImageCode() {
        creatImage();
    }
    public CreateImageCode(int width, int height) {
        this.width = width;
        this.height = height;
        creatImage();
    }
    public CreateImageCode(int width, int height, int codeCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        creatImage();
    }
    public CreateImageCode(int width, int height, int codeCount, int lineCount) {
        this.width = width;
        this.height = height;
        this.codeCount = codeCount;
        this.lineCount = lineCount;
        creatImage();
    }
    // 生成图片
    private void creatImage() {
        int fontWidth = width / codeCount;// 字体的宽度
        int fontHeight = height - 5;// 字体的高度
        int codeY = height - 8;
        // 图像buffer
        buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = buffImg.getGraphics();
        //Graphics2D g = buffImg.createGraphics();
        // 设置背景色
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        // 设置字体
        //Font font1 = getFont(fontHeight);
        Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
        g.setFont(font);
        // 设置干扰线
        for (int i = 0; i < lineCount; i++) {
            int xs = random.nextInt(width);
            int ys = random.nextInt(height);
            int xe = xs + random.nextInt(width);
            int ye = ys + random.nextInt(height);
            g.setColor(getRandColor(1, 255));
            g.drawLine(xs, ys, xe, ye);
        }
        // 添加噪点
        float yawpRate = 0.01f;// 噪声率
        int area = (int) (yawpRate * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            buffImg.setRGB(x, y, random.nextInt(255));
        }
        String str1 = randomStr(codeCount);// 得到随机字符
        this.code = str1;
        for (int i = 0; i < codeCount; i++) {
            String strRand = str1.substring(i, i + 1);
            g.setColor(getRandColor(1, 255));
            // g.drawString(a,x,y);
            // a为要画出来的东西，x和y表示要画的东西最左侧字符的基线位于此图形上下文坐标系的 (x, y) 位置处
            g.drawString(strRand, i * fontWidth + 3, codeY);
        }
    }
    // 得到随机字符
    private String randomStr(int n) {
        String str1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        String str2 = "";
        int len = str1.length() - 1;
        double r;
        for (int i = 0; i < n; i++) {
            r = (Math.random()) * len;
            str2 = str2 + str1.charAt((int) r);
        }
        return str2;
    }
    // 得到随机颜色
    private Color getRandColor(int fc, int bc) {// 给定范围获得随机颜色
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
    /**
     * 产生随机字体
     */
    private Font getFont(int size) {
        Random random = new Random();
        Font font[] = new Font[5];
        font[0] = new Font("Ravie", Font.PLAIN, size);
        font[1] = new Font("Antique Olive Compact", Font.PLAIN, size);
        font[2] = new Font("Fixedsys", Font.PLAIN, size);
        font[3] = new Font("Wide Latin", Font.PLAIN, size);
        font[4] = new Font("Gill Sans Ultra Bold", Font.PLAIN, size);
        return font[random.nextInt(5)];
    }
    // 扭曲方法
    private void shear(Graphics g, int w1, int h1, Color color) {
        shearX(g, w1, h1, color);
        shearY(g, w1, h1, color);
    }
    private void shearX(Graphics g, int w1, int h1, Color color) {
        int period = random.nextInt(2);
        boolean borderGap = true;
        int frames = 1;
        int phase = random.nextInt(2);
        for (int i = 0; i < h1; i++) {
            double d = (double) (period >> 1) * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(0, i, w1, 1, (int) d, 0);
            if (borderGap) {
                g.setColor(color);
                g.drawLine((int) d, i, 0, i);
                g.drawLine((int) d + w1, i, w1, i);
            }
        }
    }
    private void shearY(Graphics g, int w1, int h1, Color color) {
        int period = random.nextInt(40) + 10; // 50;
        boolean borderGap = true;
        int frames = 20;
        int phase = 7;
        for (int i = 0; i < w1; i++) {
            double d = (double) (period >> 1) * Math.sin((double) i / (double) period + (6.2831853071795862D * (double) phase) / (double) frames);
            g.copyArea(i, 0, 1, h1, 0, (int) d);
            if (borderGap) {
                g.setColor(color);
                g.drawLine(i, (int) d, i, 0);
                g.drawLine(i, (int) d + h1, i, h1);
            }
        }
    }
    public void write(OutputStream sos) throws IOException {
        ImageIO.write(buffImg, "png", sos);
        sos.close();
    }
    public BufferedImage getBuffImg() {
        return buffImg;
    }
    public String getCode() {
        return code.toLowerCase();
    }
}
```

### 控制器方法

```java
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
```

<img src="easypan.assets/image-20240504191250281.png" alt="image-20240504191250281" style="zoom: 67%;" /><img src="easypan.assets/image-20240504191318335.png" alt="image-20240504191318335" style="zoom:80%;" />

## 发送邮箱验证码

### 数据库设计

<img src="easypan.assets/image-20240504192301390.png" alt="image-20240504192301390" style="zoom:67%;" />.

- email_code用于临时存放发送的邮箱验证码
- 然后逆向生成项目映射，之后就不赘述了

### 请求接口

- 接口URL：POST http://localhost:7090/api/sendEmailCode
- 请求体参数

| 参数名    | 参数值              | 是否必填 | 参数类型 | 描述说明          |
| --------- | ------------------- | -------- | -------- | ----------------- |
| email     | laoluo_coder@qq.com | 是       | String   | 邮箱              |
| checkCode | sqhfc               | 是       | String   | 图片验证码        |
| type      | 0                   | 是       | String   | 0:注册 1:找回密码 |

### 生成随机验证码的工具类

```java
public class StringTools {
    /**
     * 生成随机数
     * @param count
     * @return
     */
    public static final String getRandomNumber(Integer count){
        return RandomStringUtils.random(count,false,true);//第三个参数表示是否生成数字验证码
    }
}
```

### 控制层方法

```java
//封装成功响应信息
protected <T> ResponseVO getSuccessResponseVO(T t) {
    ResponseVO<T> responseVO = new ResponseVO<>();
    responseVO.setStatus("200");
    responseVO.setCode(ResponseCodeEnum.CODE_200.getCode());
    responseVO.setInfo(ResponseCodeEnum.CODE_200.getMsg());
    responseVO.setData(t);
    return responseVO;
}
@RequestMapping("/sendEmailCode")
public ResponseVO sendEmailCode(HttpSession session, String email, String checkCode, Integer type){
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
```

### 业务方法

#### 主骨架【未发送给邮箱】

```java
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
    //TODO 发送验证码
    EmailCodeEntity emailCodeEntity = new EmailCodeEntity();
    emailCodeEntity.setCode(code);
    emailCodeEntity.setEmail(email);
    emailCodeEntity.setStatus(0);//为0表示该验证码还未使用
    emailCodeEntity.setCreateTime(new Date());
    baseMapper.insert(emailCodeEntity);
}
```

#### 根据邮箱查询用户信息

- 对应user_info表

```java
public UserInfoEntity selectByEmail(String email) {
    return baseMapper.selectOne(new QueryWrapper<UserInfoEntity>().eq("email",email));
}
```

#### 重置发送过的验证码

- 发送新验证码之前，要将之前该邮箱发送过的有效验证码置为无效

```xml
<update id="disableEmailCode">
    update email_code
    set status=1
    where email = ${email} and status = 0
</update>
```

### 报错分析

1. 一开始报如下错误，说绑定异常，然后我修改了mapper映射等都无法解决

   ```java
   org.apache.ibatis.binding.BindingException: Invalid bound statement (not found): com.study.liao.dao.UserInfoDao.insert
   ```

2. 然后根据[SpringBoot 3 项目 mybatis-plus报错解决：org.apache.ibatis.binding.BindingException: Invalid bound statement-腾讯云开发者社区-腾讯云 (tencent.com)](https://cloud.tencent.com/developer/article/2329732)，发现问题是springboot3不兼容mybatis-plus3.5.3之前的版本，于是把版本改成了3.5.3.2

3.  报新的错误

   ```java
   org.springframework.beans.factory.BeanNotOfRequiredTypeException: Bean named 'ddlApplicationRunner' is expected to be of type 'org.springframework.boot.Runner' but was actually of type 'org.springframework.beans.factory.support.NullBean'
   ```

4. 根据[Bean named‘ddlApplicationRunner‘is expected to be of type ‘org.springframework.boot.Runner‘_bean named 'ddlapplicationrunner' is expected to b-CSDN博客](https://blog.csdn.net/tangshiyilang/article/details/135287188)得出3.5.3.2仍然存在问题，修改成3.5.5就可以兼容springboot3.1.7，所以降低了springboot版本并且提高了mybatis-plus场景的版本

   ```xml
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>3.1.7</version>
       <relativePath/> <!-- lookup parent from repository -->
   </parent>
   <dependency>
       <groupId>com.baomidou</groupId>
       <artifactId>mybatis-plus-boot-starter</artifactId>
       <version>3.5.5</version>
   </dependency>
   ```

5. 项目启动成功，得出结论
   - springboot应用中整合了mybatis-plus，但是报无法绑定mapper映射的异常，有可能是springboot和mybatis-plus版本不兼容
   - 排除可以去官方github中找对应issue

### 初版测试

1. 初次发送

<img src="easypan.assets/image-20240504222419363.png" alt="image-20240504222419363" style="zoom:80%;" /><img src="easypan.assets/image-20240504222429933.png" alt="image-20240504222429933" style="zoom:80%;" />

2. 再次发送，检测未使用的验证码状态是否改变

<img src="easypan.assets/image-20240504222613567.png" alt="image-20240504222613567" style="zoom:80%;" />.

### 实现发送验证码功能

#### 相关依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

#### 封装系统配置

```java
@JsonIgnoreProperties(ignoreUnknown = true)//防止redis中存储的属性和javaBean中属性不一致导致的报错
public class SysSettingsDto implements Serializable {
    private String registerMailTile="邮箱验证码";
    private String registerEmailContent="你好，你的邮箱验证码是,%s,15分钟内有效";
    private Integer userInitUseSpace=5;
}
```

- 配置可以从redis中读取

```java
@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;
    public SysSettingsDto getSysSettingsDto(){
        //从redis中获取系统配置
        SysSettingsDto sysSettingsDto = (SysSettingsDto)redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        if(null==sysSettingsDto){
            //如果没有存储系统配置就新建一个，并且存放到redis中
            sysSettingsDto=new SysSettingsDto();
            redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingsDto);
        }
        return sysSettingsDto;
    }
}
```

#### 核心代码

- sendEmail方法直接调用如下方法就可以发送邮箱验证码

```java
private void sendCode(String toEmail,String code){
    try {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true);
        //1.从redis中读取系统配置
        SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();
        //2.设置邮件信息
        helper.setFrom(appConfig.getSendUserName());//设置发件人
        helper.setTo(toEmail);//设置收件邮箱
        helper.setSubject(sysSettingsDto.getRegisterMailTitle());//设置标题
        //因为sysSettingsDto中验证码内容设置了占位符，所以可以用code去替换占位符
        helper.setText(String.format(sysSettingsDto.getRegisterEmailContent(),code));
        javaMailSender.send(message);//发送邮件
    } catch (MessagingException e) {
        log.error("邮件发送失败",e);
        throw new BusinessException("邮件发送失败");
    }
}
```

- 发送成功！！！

<img src="easypan.assets/image-20240520193703725.png" alt="image-20240520193703725" style="zoom:67%;" /><img src="easypan.assets/image-20240520193714653.png" alt="image-20240520193714653" style="zoom:67%;" />

## AOP实现参数拦截【这一块老罗在论坛中详细实现了，这里只是简单带过，了解即可】

### 相关依赖

```xml
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>${aspectjweaver.version}</version>
</dependency>
```

### 业务实现

1. 定义方法校验注解

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface GlobalInterceptor {
    /**
     * 校验参数，默认不校验
     */
    boolean checkParams()default false;
}
```

2. 定义参数校验注解

```java
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface VerifyParam {
    int min() default -1;
    int max() default -1;
    boolean required() default false;
    VerifyRegexEnum regex() default VerifyRegexEnum.NO;//正则表达式校验，默认不校验
}
```

3. 定义切面

```java
@Aspect
@Component("GlobalOperationAspect")
public class GlobalOperationAspect {
    @Pointcut("@annotation(com.study.liao.annotation.GlobalInterceptor)")
    private void requestInterceptor(){

    }
    @Before("requestInterceptor()")
    public void interceptorDo(JoinPoint point)throws BusinessException{
        Object target = point.getTarget();
    }
}
```

4. 拦截成功

<img src="easypan.assets/image-20240520195832803.png" alt="image-20240520195832803" style="zoom: 67%;" />.

### 参数校验切面类

- 老罗没有带着敲，那就直接cv了

```java
@Slf4j
@Aspect
@Component("GlobalOperationAspect")
public class GlobalOperationAspect {
    private static final String TYPE_STRING = "java.lang.String";
    private static final String TYPE_INTEGER = "java.lang.Integer";
    private static final String TYPE_LONG = "java.lang.Long";
    @Pointcut("@annotation(com.study.liao.annotation.GlobalInterceptor)")
    private void requestInterceptor(){
    }
    @Before("requestInterceptor()")
    public void interceptorDo(JoinPoint point)throws BusinessException{
        try {
            Object target = point.getTarget();
            Object[] arguments = point.getArgs();
            String methodName = point.getSignature().getName();
            Class<?>[] parameterTypes = ((MethodSignature) point.getSignature()).getMethod().getParameterTypes();
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if (null == interceptor) {
                return;
            }
            /**
             * 校验参数
             */
            if (interceptor.checkParams()) {
                validateParams(method, arguments);
            }
        } catch (BusinessException e) {
            log.error("全局拦截器异常", e);
            throw e;
        } catch (Exception e) {
            log.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        } catch (Throwable e) {
            log.error("全局拦截器异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        }
    }

    private void validateParams(Method m, Object[] arguments) throws BusinessException {
        Parameter[] parameters = m.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Object value = arguments[i];
            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            if (verifyParam == null) {
                continue;
            }
            //基本数据类型
            if (TYPE_STRING.equals(parameter.getParameterizedType().getTypeName()) || TYPE_LONG.equals(parameter.getParameterizedType().getTypeName()) || TYPE_INTEGER.equals(parameter.getParameterizedType().getTypeName())) {
                checkValue(value, verifyParam);
                //如果传递的是对象
            } else {
                checkObjValue(parameter, value);
            }
        }
    }
    private void checkObjValue(Parameter parameter, Object value) {
        try {
            String typeName = parameter.getParameterizedType().getTypeName();
            Class classz = Class.forName(typeName);
            Field[] fields = classz.getDeclaredFields();
            for (Field field : fields) {
                VerifyParam fieldVerifyParam = field.getAnnotation(VerifyParam.class);
                if (fieldVerifyParam == null) {
                    continue;
                }
                field.setAccessible(true);
                Object resultValue = field.get(value);
                checkValue(resultValue, fieldVerifyParam);
            }
        } catch (BusinessException e) {
            log.error("校验参数失败", e);
            throw e;
        } catch (Exception e) {
            log.error("校验参数失败", e);
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }
    private void checkValue(Object value, VerifyParam verifyParam) throws BusinessException {
        Boolean isEmpty = value == null || StringTools.isEmpty(value.toString());
        Integer length = value == null ? 0 : value.toString().length();

        /**
         * 校验空
         */
        if (isEmpty && verifyParam.required()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        /**
         * 校验长度
         */
        if (!isEmpty && (verifyParam.max() != -1 && verifyParam.max() < length || verifyParam.min() != -1 && verifyParam.min() > length)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        /**
         * 校验正则
         */
        if (!isEmpty && !StringTools.isEmpty(verifyParam.regex().getRegex()) && !VerifyUtils.verify(verifyParam.regex(), String.valueOf(value))) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }
}
```

## 注册

### 控制层

```java
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
```

### 业务层

- 核心代码

```java
@Override
@Transactional(rollbackFor = Exception.class)
public void register(String email, String nickName, String password, String emailCode) {
    //1.查询当前邮箱是否存在
    UserInfoEntity userInfo = baseMapper.selectOne(new QueryWrapper<UserInfoEntity>()
            .eq("email", email));
    if (null != userInfo) {
        throw new BusinessException("邮箱账号已存在");
    }
    //2.查询当前用户名称是否存放
    userInfo = baseMapper.selectOne(new QueryWrapper<UserInfoEntity>()
            .eq("nick_name", nickName));
    if (null != userInfo) {
        throw new BusinessException("昵称已经存在");
    }
    //3.校验邮箱验证码
    emailCodeService.checkCode(email, emailCode);
    String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
    userInfo = new UserInfoEntity();
    userInfo.setUserId(userId);
    userInfo.setNickName(nickName);
    userInfo.setJoinTime(new Date());
    userInfo.setPassword(StringTools.encodeByMd5(password));
    userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
    userInfo.setUseSpace(0L);
    SysSettingsDto sysSettingsDto = redisComponent.getSysSettingsDto();
    userInfo.setTotalSpace(sysSettingsDto.getUserInitUseSpace()*Constants.MB);
    save(userInfo);
}
```

- emailCodeService中的校验邮箱验证码方法【校验是否存在、是否失效、是否超时】

```java
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
```

## 登录

### 封装登录信息

```java
@Data
public class SessionWebUserDto {
    private String nickName;
    private String userId;
    private Boolean isAdmin;
    private String avatar;//qq登录的头像
}
```

### 控制层

```java
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
```

### 业务层

```java
@Override
public SessionWebUserDto login(String email, String password) {
    UserInfoEntity userInfoEntity=selectByEmail(email);
    //1.登录校验【这一块password不用再转成密文，因为前端传输时就已经转化了】
    if(null==userInfoEntity||!userInfoEntity.getPassword().equals(password)){
        throw new BusinessException("账号密码错误");
    }
    if(UserStatusEnum.DISABLE.getStatus().equals(userInfoEntity.getStatus())){
        throw new BusinessException("账号已禁用");
    }
    //2.更新登录时间
    String userId = userInfoEntity.getUserId();
    UserInfoEntity updateInfo = new UserInfoEntity();
    updateInfo.setLastLoginTime(new Date());
    updateInfo.setUserId(userId);
    updateById(userInfoEntity);
    SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
    sessionWebUserDto.setNickName(userInfoEntity.getNickName());
    sessionWebUserDto.setUserId(userId);
    //3.判断是否为管理员【可能有多个管理员】
    if(ArrayUtils.contains(appConfig.getAdminUserName().split(","),email)){
        sessionWebUserDto.setAdmin(true);
    }else {
        sessionWebUserDto.setAdmin(false);
    }
    //4.更新用户空间信息
    UserSpaceDto userSpaceDto = new UserSpaceDto();
    // 实时查询文件占用情况
    Long useSpace = fileInfoMapper.selectUseSpace(userId);
    userSpaceDto.setUseSpace(useSpace);
    UserInfoEntity lastUserInfo = getById(userId);
    userSpaceDto.setTotalSpace(lastUserInfo.getTotalSpace());
    //刷新缓存中的用户空间信息
    redisComponent.saveUserSpaceUse(userId,userSpaceDto);
    return sessionWebUserDto;
}
```

### 登录拦截

- 切面类

```java
private void checkLogin(Boolean checkAdmin){
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    HttpSession session = request.getSession();
    SessionWebUserDto userDto= (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
    if(null==userDto){
        throw new BusinessException(ResponseCodeEnum.CODE_901);
    }
    if(checkAdmin&&!userDto.getIsAdmin()){
        throw new BusinessException(ResponseCodeEnum.CODE_404);
    }
}
```

- GlobalInterceptor注解添加`boolean checkLogin() default true;`属性

## 账号相关功能

### 重置密码

#### 请求接口

- 接口URL：POSThttp://localhost:7090/api/resetPwd
- Content-Type：multipart/form-data

- 请求参数

| 参数名    | 参数值              | 是否必填 | 参数类型 | 描述说明          |
| --------- | ------------------- | -------- | -------- | ----------------- |
| email     | laoluo_coder@qq.com | 是       | String   | 邮箱              |
| password  | test1234561         | 是       | String   | md5加密传输的密码 |
| checkCode | 12333               | 是       | String   | 图片验证码        |
| emailCode | 23333               | 是       | String   | 邮箱验证码        |

- 响应示例

```json
{
"status": "success", 
"code": 200, 
"info": "请求成功", 
"data": null 
}
```

#### 业务实现

- 控制层

```java
@GlobalInterceptor(checkParams = true)
@RequestMapping("/resetPwd")
public ResponseVO resetPwd(HttpSession session,
                           @VerifyParam(required = true,regex = VerifyRegexEnum.EMAIL,max=150) String email,
                           @VerifyParam(required = true,
                                        regex = VerifyRegexEnum.PASSWORD,min=8,max=18)String password,
                           @VerifyParam(required = true) String checkCode,
                           @VerifyParam(required = true) String emailCode){
    try {
        if(!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))){
            throw new BusinessException("图像验证码不正确");
        }
        userInfoService.resetPwd(email,password,emailCode);
        return getSuccessResponseVO(null);
    } finally {
        session.removeAttribute(Constants.CHECK_CODE_KEY);
    }
}
```

- 业务层

```java
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
```

### 获取用户头像

- 接口URL：GEThttp://localhost:7090/api/getAvatar/{userId}
- 业务代码

```java
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
        }
        avatarPath=defalutPath;
    }
    response.setContentType("image/jpg");
    FileUtils.readFile(response,avatarPath);
    printNoDefaultImage(response);
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
```

### 获取用户空间信息

- 接口URL：GEThttp://localhost:7090/api/getUseSpace

- 响应示例

```json
{
	"status": "success",
	"code": 200,
	"info": "请求成功",
	"data": {
		"useSpace": 0,
		"totalSpace": 5242880
	}
}
```

- 业务代码

```java
//UserInfoController
@RequestMapping("getUseSpace")
public ResponseVO getUseSpace(HttpSession session) throws IOException {
    //从session中获取用户信息
    SessionWebUserDto sessionWebUserDto= (SessionWebUserDto) session.getAttribute(Constants.SESSION_KEY);
    UserSpaceDto useSpace = redisComponent.getUseSpace(sessionWebUserDto.getUserId());
    return getSuccessResponseVO(useSpace);
}
//RedisComponent
public UserSpaceDto getUseSpace(String userId){
    UserSpaceDto spaceDto= (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE+userId);
    if(spaceDto==null){
        //如果没有用户空间使用情况，就得刷新缓存
        spaceDto=new UserSpaceDto();
        //todo 需要查询用户文件表，统计用户目前的存储空间使用情况
        spaceDto.setTotalSpace(0l);
        spaceDto.setTotalSpace(spaceDto.getTotalSpace()*Constants.MB);
        saveUserSpaceUse(userId,spaceDto);
    }
    return spaceDto;
}
```

### 上传用户头像

```java
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
    //2.拼接用户头像存储路径
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
```

## 测试

1. 如果出现了java.nio.channels.ClosedChannelException: null，大概率是redis连接不上

2. 无法重置密码，前端404，那这一块我就不管咯

3. 用户头像无法回显，而且携带的用户id为null

   - 原因是登录方法没有将用户id存到session中
   - 解决：login方法加上`sessionWebUserDto.setUserId(userInfo.getUserId());`

   <img src="easypan.assets/image-20240530192630892.png" alt="image-20240530192630892" style="zoom:80%;" />.
   
4. 时不时抛出异常

   ```java
   2024-06-11 14:16:09 [ERROR][com.study.liao.controller.UserInfoController][printNoDefaultImage][193]-> 输出无默认图失败
   java.lang.IllegalStateException: getOutputStream() has already been called for this response
   	at org.apache.catalina.connector.Response.getWriter(Response.java:549)
   ```

   - 原因推断

     - printNoDefaultImage方法应该是在**没有设置默认图时**向前端发出的提醒，所以控制层应该改为如下代码

     ```java
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
     ```

     - 其次应该是输出默认图方法执行完后，没有对response清理干净，这一块参考[解决java.lang.IllegalStateException: getOutputStream() has already been called for this response_nested exception is java.lang.illegalstateexceptio-CSDN博客](https://blog.csdn.net/cjiankai/article/details/84523764)，我的代码暂时不做改动

5. 登录时没有更新最新登录信息，原因时登录方法中更新到的是数据库查到的实体，而不是新创建的更新实体【低级错误】

   ```java
   UserInfoEntity updateInfo = new UserInfoEntity();
   updateInfo.setLastLoginTime(new Date());
   updateInfo.setUserId(userId);
   updateById(userInfoEntity);
   ```
   - 改为

   ```java
   ...
   updateById(updateInfo);
   ```

# ！！！文件上传

## 环境搭建

- 数据库

![image-20240530170039597](easypan.assets/image-20240530170039597.png)<img src="easypan.assets/image-20240530170051263.png" alt="image-20240530170051263" style="zoom:67%;" />

## 完善用户已用空间查询

### DAO层

- 接口

```java
Long selectUseSpace(@Param("userId") String userId);
```

- sql：查询当前用户所有文件大小的总和

```xml
<select id="selectUseSpace" resultType="java.lang.Long">
    select IFNULL(sum(file_size), 0)
    from file_info
    where user_id = #{userId}
</select>
```

### 业务优化

```java
Long useSpace = fileInfoMapper.selectUseSpace(userId);
userSpaceDto.setUseSpace(useSpace);
```

## ！！！文件分片上传

### 控制层

- fileId是非必填的，因为首次上传是没有fileId的

```java
/**
 * @param fileId 分片对应的文件id，首个分片没有id，所以为非必填
 * @param file 本次传输的分片
 * @param fileName 文件原名
 * @param filePid 文件所在目录
 * @param fileMd5 文件的md5码，如果数据库中存在相同的md5码，就可以秒传
 * @param chunkIndex 分片索引
 * @param chunks 分片数
 */
@RequestMapping("/uploadFile")
@GlobalInterceptor(checkParams = true)
public ResponseVO uploadFile(HttpSession session,
                             String fileId,
                             MultipartFile file,
                             @VerifyParam(required = true) String fileName,
                             @VerifyParam(required = true) String filePid,
                             @VerifyParam(required = true) String fileMd5,
                             @VerifyParam(required = true) Integer chunkIndex,
                             @VerifyParam(required = true) Integer chunks) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    //上传过程要和前端有交互，所以返回当前状态信息
    UploadResultDto uploadResultDto = fileInfoService.uploadFile(webUserDto, fileId, file, fileName, filePid, fileMd5, chunkIndex, chunks);
    return getSuccessResponseVO(uploadResultDto);
}
```

- 返回数据【分片上传需要和前端交互，因此要返回当前文件分片的传输状态】

```java
@Data
public class UploadResultDto implements Serializable {
    private String fileId;
    private String status;
}
```

### 业务层

#### 主骨架

##### 业务逻辑

1. 根据首个分片的md5码去数据库查询，如果存在相同文件，就直接将别人的文件同步到自己的文件记录，即秒传
   1. 先得判断用户剩余空间是否足够
   2. 然后将数据库查询到的文件信息同步到当前用户的传输记录中
   3. 最后更新用户的使用空间
2. 否则就是分片上传
   1. 在redis中存储临时文件的大小，然后将分片存到临时文件目录
   2. 如果不是传最后一个分片，那文件类型依旧是**正在传输**
3. 全部文件传输完成，保存到数据库，异步合并分片
   1. 最后一个分片上传完成就往数据库插入一些基本信息（大小、类型、状态等）
   2. 然后开始转码【转码要等插入操作提交之后才可以执行，所有要在事务提交之后进行】，先获取到临时目录和目标目录信息
   3. 然后进行分片合并，将临时目录中的文件读出来，写入到目标文件中
4. 全过程中需要定义一个全局变量来判断是否上传失败，如果上传失败且创建了临时目录，就要把临时目录删了

##### 首个切片判断是否秒传

```java
if (chunkIndex == 0) {
    FileInfoQuery infoQuery = new FileInfoQuery();
    infoQuery.setFileMd5(fileMd5);
    infoQuery.setSimplePage(new SimplePage(0, 1));//分页查询查出第一条数据
    infoQuery.setStatus(FileStatusEnums.USING.getStatus());
    List<FileInfoEntity> fileInfoList = fileInfoMapper.selectList(infoQuery);
    //4.如果数据库已经有这个文件就秒传即可
    if (!fileInfoList.isEmpty()) {
        FileInfoEntity dbFile = fileInfoList.get(0);
        //4.1判断用户可用空间是否可以容纳该文件
        if (dbFile.getFileSize() + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
            throw new BusinessException(ResponseCodeEnum.CODE_904);//网盘空间不足
        }
        //4.2同步秒传文件信息
        dbFile.setFileId(fileId);
        dbFile.setFilePid(filePid);
        dbFile.setUserId(userId);
        Date date = new Date();
        dbFile.setCreateTime(date);
        dbFile.setUpdateTime(date);
        dbFile.setStatus(FileStatusEnums.USING.getStatus());
        dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
        dbFile.setFileMd5(fileMd5);
        dbFile.setFileName(fileRename(filePid,userId,fileName));//如果存在同名文件，就重命名
        uploadResultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());//设置当前状态为秒传
        fileInfoMapper.insert(dbFile);//写入数据库
        //4.3更新用户使用空间
        updateUserSpace(userId,dbFile.getFileSize());
        return uploadResultDto;
    }
}
```

##### 分片上传

```java
//5.否则就是分片上传,将分片暂存到临时目录
//5.1判断磁盘空间
Long currentTempSize=redisComponent.getFileTempSize(userId,fileId);
if(file.getSize()+currentTempSize+userSpaceDto.getUseSpace()>userSpaceDto.getTotalSpace()){
    //上传分片时容量不够了，当前分片只能停止上传
    throw new BusinessException(ResponseCodeEnum.CODE_904);
}
//5.2获取临时文件目录
String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
String currentUserFolderName=userId+fileId;
File tempFileFolder = new File(tempFolderName+currentUserFolderName);
if(!tempFileFolder.exists()){
    tempFileFolder.mkdirs();
}
//5.3将当前分片存储到临时目录
File newFilePath=new File(tempFileFolder.getPath()+"/"+chunkIndex);//分片存储路径
file.transferTo(newFilePath);
//5.4保存临时大小
redisComponent.saveFileTempSize(userId, fileId, file.getSize());
if (chunkIndex < chunks - 1) {
    //非最后一个分片都是转码中的状态
    uploadResultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
    return uploadResultDto;
}
```
##### 最后一个分片上传完成

```java
//6.最后一个分片上传完成，保存到数据库，并且异步合并分片
Date date = new Date();
String yearMonth = DateUtil.format(date, DateTimePatternEnum.YYYYMM.getPattern());//获取年月
//6.1拼接真实文件名
String fileSuffix = StringTools.getFileSuffix(fileName);
String realFileName = currentUserFolderName + fileSuffix;
//6.2重命名
fileName = fileRename(filePid, userId, fileName);
//6.3保存信息
FileTypeEnums fileTypeBySuffix = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
FileInfoEntity fileInfo = new FileInfoEntity();
fileInfo.setFileId(fileId);
fileInfo.setUserId(userId);
fileInfo.setFileMd5(fileMd5);
fileInfo.setFileName(fileName);
fileInfo.setFilePath(yearMonth + "/" + realFileName);
fileInfo.setFilePid(filePid);
fileInfo.setCreateTime(date);
fileInfo.setLastUpdateTime(date);
fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
fileInfo.setFileCategory(fileTypeBySuffix.getCategory().getCategory());//获取文件分类
fileInfo.setFileType(fileTypeBySuffix.getType());//详细文件类型
fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());//目录类型
fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
fileInfoMapper.insert(fileInfo);
//6.4更新用户空间信息
Long useSize = redisComponent.getFileTempSize(userId, fileId);
updateUserSpace(userId, useSize);
//6.5设置状态为上传完成
uploadResultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
```

##### 文件转码

```java
//7.转码
//要等事务提交之后才可以转码
TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
    @Override
    public void afterCommit() {
        //交给spring管理才可以使异步生效
        fileInfoService.transferFile(fileInfo.getFileId(),userId);
    }
});
```

##### 全代码

```java
@Transactional
@Override
public UploadResultDto uploadFile(SessionWebUserDto webUserDto, String fileId, MultipartFile file, String fileName, String filePid, String fileMd5, Integer chunkIndex, Integer chunks) {
    UploadResultDto uploadResultDto = new UploadResultDto();
    String userId = webUserDto.getUserId();
    File tempFileFolder = null;
    boolean uploadSuccess = true;//用于判断全局传送过程是否有问题
    try {
        //1.设置文件id
        if (StringTools.isEmpty(fileId)) {
            //首次上传的分片是没有文件id的，所以要生成一个
            fileId = StringTools.getRandomNumber(Constants.LENGTH_10);
        }
        uploadResultDto.setFileId(fileId);
        //2.获取用户使用空间
        UserSpaceDto userSpaceDto = redisComponent.getUseSpace(userId);
        //3.处理首个分片
        if (chunkIndex == 0) {
            FileInfoQuery infoQuery = new FileInfoQuery();
            infoQuery.setFileMd5(fileMd5);
            infoQuery.setSimplePage(new SimplePage(0, 1));//分页查询查出第一条数据
            infoQuery.setStatus(FileStatusEnums.USING.getStatus());
            List<FileInfoEntity> fileInfoList = fileInfoMapper.selectList(infoQuery);
            //4.如果数据库已经有这个文件就秒传即可
            if (!fileInfoList.isEmpty()) {
                FileInfoEntity dbFile = fileInfoList.get(0);
                //4.1判断用户可用空间是否可以容纳该文件
                if (dbFile.getFileSize() + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
                    throw new BusinessException(ResponseCodeEnum.CODE_904);//网盘空间不足
                }
                //4.2同步秒传文件信息
                dbFile.setFileId(fileId);
                dbFile.setFilePid(filePid);
                dbFile.setUserId(userId);
                Date date = new Date();
                dbFile.setCreateTime(date);
                dbFile.setUpdateTime(date);
                dbFile.setStatus(FileStatusEnums.USING.getStatus());
                dbFile.setDelFlag(FileDelFlagEnums.USING.getFlag());
                dbFile.setFileMd5(fileMd5);
                dbFile.setFileName(fileRename(filePid, userId, fileName));//如果存在同名文件，就重命名
                uploadResultDto.setStatus(UploadStatusEnums.UPLOAD_SECONDS.getCode());//设置当前状态为秒传
                fileInfoMapper.insert(dbFile);//写入数据库
                //4.3更新用户使用空间
                updateUserSpace(userId, dbFile.getFileSize());
                return uploadResultDto;
            }
        }
        //5.否则就是分片上传,将分片暂存到临时目录
        //5.1判断磁盘空间
        Long currentTempSize = redisComponent.getFileTempSize(userId, fileId);
        if (file.getSize() + currentTempSize + userSpaceDto.getUseSpace() > userSpaceDto.getTotalSpace()) {
            //上传分片时容量不够了，当前分片只能停止上传
            throw new BusinessException(ResponseCodeEnum.CODE_904);
        }
        //5.2获取临时文件目录
        String tempFolderName = appConfig.getProjectFolder() + Constants.FILE_FOLDER_TEMP;
        String currentUserFolderName = userId + fileId;
        tempFileFolder = new File(tempFolderName + currentUserFolderName);
        if (!tempFileFolder.exists()) {
            tempFileFolder.mkdirs();
        }
        //5.3将当前分片存储到临时目录
        File newFilePath = new File(tempFileFolder.getPath() + "/" + chunkIndex);//分片存储路径
        file.transferTo(newFilePath);
        //5.4保存临时大小
        redisComponent.saveFileTempSize(userId, fileId, file.getSize());
        if (chunkIndex < chunks - 1) {
            //非最后一个分片都是转码中的状态
            uploadResultDto.setStatus(UploadStatusEnums.UPLOADING.getCode());
            return uploadResultDto;
        }
        //6.最后一个分片上传完成，保存到数据库，并且异步合并分片
        Date date = new Date();
        String yearMonth = DateUtil.format(date, DateTimePatternEnum.YYYYMM.getPattern());//获取年月
        //6.1拼接真实文件名
        String fileSuffix = StringTools.getFileSuffix(fileName);
        String realFileName = currentUserFolderName + fileSuffix;
        //6.2重命名
        fileName = fileRename(filePid, userId, fileName);
        //6.3保存信息
        FileTypeEnums fileTypeBySuffix = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
        FileInfoEntity fileInfo = new FileInfoEntity();
        fileInfo.setFileId(fileId);
        fileInfo.setUserId(userId);
        fileInfo.setFileMd5(fileMd5);
        fileInfo.setFileName(fileName);
        fileInfo.setFilePath(yearMonth + "/" + realFileName);
        fileInfo.setFilePid(filePid);
        fileInfo.setCreateTime(date);
        fileInfo.setLastUpdateTime(date);
        fileInfo.setStatus(FileStatusEnums.TRANSFER.getStatus());
        fileInfo.setFileCategory(fileTypeBySuffix.getCategory().getCategory());//获取文件分类
        fileInfo.setFileType(fileTypeBySuffix.getType());//详细文件类型
        fileInfo.setFolderType(FileFolderTypeEnums.FILE.getType());//目录类型
        fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
        fileInfoMapper.insert(fileInfo);
        //6.4更新用户空间信息
        Long useSize = redisComponent.getFileTempSize(userId, fileId);
        updateUserSpace(userId, useSize);
        //6.5设置状态为上传完成
        uploadResultDto.setStatus(UploadStatusEnums.UPLOAD_FINISH.getCode());
        //7.转码
        //要等事务提交之后才可以转码
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                //交给spring管理才可以使异步生效
                fileInfoService.transferFile(fileInfo.getFileId(),userId);
            }
        });
        return uploadResultDto;
    } catch (BusinessException e) {
        throw e;
    } catch (Exception e) {
        log.error("文件上传失败！！");
        e.printStackTrace();
        uploadSuccess = false;
    } finally {
        if (!uploadSuccess && tempFileFolder != null) {
            //上传失败就要删除对应临时目录
            try {
                FileUtils.deleteDirectory(tempFileFolder);
            } catch (IOException e) {
                log.error("删除临时目录失败");
                e.printStackTrace();
            }
        }
        return uploadResultDto;
    }
}
```

#### 同名文件重命名

- 如果文件名不存在，就直接返回当前文件名，否则就需要重命名防止同名

```java
private String fileRename(String filePid, String userId, String fileName) {
    FileInfoQuery fileInfoQuery = new FileInfoQuery();
    fileInfoQuery.setUserId(userId);
    fileInfoQuery.setFilePid(filePid);
    fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
    Integer count = fileInfoMapper.selectCount(fileInfoQuery);
    if(count>0){
        fileName=StringTools.rename(fileName);
    }
    return fileName;
}
```

#### 更新用户使用空间

- 调用userInfoService的更新空间功能【包含修改总空间和修改使用空间】，这里只需要修改使用空间

```java
private void updateUserSpace(String userId,Long useSpace){
    Integer count=userInfoService.updateUseSpace(userId,useSpace,null);
    if(count==0){
        //更新失败
        throw new BusinessException(ResponseCodeEnum.CODE_904);
    }
    //数据库更新成功就同步缓存
    UserSpaceDto spaceDto = redisComponent.getUseSpace(userId);
    spaceDto.setUserSpace(spaceDto.getUserSpace()+useSpace);
    redisComponent.saveUserSpaceUse(userId,spaceDto);
}
```

- 更新sql
  - XML无法识别"<="，所以需要用`<![CDATA[]]>`转义<img src="easypan.assets/image-20240603125727846.png" alt="image-20240603125727846" style="zoom:80%;" />
  - 这里传的useSpace和totalSpace都是增量
  - test中的表达式是对传参进行判断

```xml
<update id="updateUseSpace">
    update user_info
    <set>
        <if test="useSpace!=null">
            use_space=use_space+#{useSpace},
        </if>
        <if test="totalSpace!=null">
            total_space=total_space+#{totalSpace}
        </if>
    </set>
    where user_id=#{userId}
    <if test="useSpace!=null and totalSpace!=null  ">
--         使用空间更新后不能超过总空间
        and <![CDATA[(use_space+#{useSpace}) <= total_space]]>
    </if>
    <if test="useSpace!=null and totalSpace!=null">
--         总空间更新后不能比使用空间还小
        and <![CDATA[(total_space+#{totalSpace}) >= use_space]]>
    </if>
</update>
```

#### 缓存中临时文件操作

##### 获取临时文件大小

```java
public Long getFileTempSize(String userId, String fileId) {
    Long sizeObj = (Long) redisUtils.get(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE+userId + fileId);
    if (sizeObj == null) {
        return 0L;
    }
    return sizeObj;
}
```

##### 保存临时文件

```java
public void saveFileTempSize(String userId,String fileId,String fileSize){
    //1.获取当前该临时文件的大小
    Long currentTempSize = getFileTempSize(userId, fileId);
    //2.保存/更新临时文件大小,一个小时过期时间
    redisUtils.setex(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE+userId+fileId,
            currentTempSize+fileSize,Constants.REDIS_KEY_EXPIRES_ONE_HOUR);
}
```

#### 转码

```java
@Async
public void transferFile(String fileId,String userId){
    boolean transferSuccess=true;
    String targetFilePath=null;
    String cover=null;
    FileInfoEntity fileInfo=fileInfoMapper.selectByFileIdAndUserId(fileId,userId);
    try {
        if(fileInfo==null||!FileStatusEnums.TRANSFER.getStatus().equals(fileInfo.getStatus())){
            return;
        }
        //1.获取临时目录
        String tempFolderName=appConfig.getProjectFolder()+Constants.FILE_FOLDER_TEMP;
        String currentUserFolderName=userId+fileId;
        File fileFolder=new File(tempFolderName+currentUserFolderName);
        String fileSuffix=StringTools.getFileSuffix(fileInfo.getFileName());
        String month=DateUtil.format(fileInfo.getCreateTime(),DateTimePatternEnum.YYYYMM.getPattern());
        //2.获取目标目录
        String targetFolderName=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE;
        File targetFolder = new File(targetFolderName + "/" + month);
        if(!targetFolder.exists()){
            targetFolder.mkdirs();
        }
        String realFileName=currentUserFolderName+fileSuffix;//真实文件名
        targetFilePath=targetFolder.getPath()+"/"+realFileName;
        //3.合并文件
        union(fileFolder.getPath(),targetFilePath,fileInfo.getFileName(),true);
        //4.文件切割
        FileTypeEnums fileTypeBySuffix = FileTypeEnums.getFileTypeBySuffix(fileSuffix);
        if(FileTypeEnums.VIDEO==fileTypeBySuffix){
            //4.1视频文件切割
            cutFileVideo(fileId,targetFilePath);
            //4.2生成视频文件缩略图
            cover=month+"/"+currentUserFolderName+Constants.IMAGE_PNG_SUFFIX;
            String coverPath=targetFolderName+"/"+cover;
            ScaleFilter.createCover4Video(new File(targetFilePath),Constants.LENGTH_150,new File(coverPath));
        }else if(FileTypeEnums.IMAGE==fileTypeBySuffix){
            //4.3图片就不需要切割了，直接生成缩略图
            cover=month+"/"+realFileName.replace(".","_.");
            String coverPath=targetFolderName+"/"+cover;
            Boolean isCreated= ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFilePath),
                    Constants.LENGTH_150,new File(coverPath),false);
            if(!isCreated){
                //如果没有生成缩略图，说明原图太小了，那就直接复制一份就行了
                FileUtils.copyFile(new File(targetFilePath),new File(coverPath));
            }
        }
    }catch (Exception e){
        log.error("文件转码失败，{}",e);
        e.printStackTrace();
    }finally {
        //4.更新转码状态
        FileInfoEntity updateInfo = new FileInfoEntity();
        updateInfo.setFileSize(new File(targetFilePath).length());
        updateInfo.setFileCover(cover);
        updateInfo.setStatus(transferSuccess?FileStatusEnums.USING.getStatus() : FileStatusEnums.TRANSFER_FAIL.getStatus());
        fileInfoMapper.updateFileStatusWithOldStatus(fileId,userId,
                                                     updateInfo,FileStatusEnums.TRANSFER.getStatus());
    }
}
```

#### 合并文件

```java
/**
 * @param dirPath 临时文件目录
 * @param toFilePath 目标文件路径
 * @param fileName 文件名
 * @param delSource 是否要删除源文件目录
 */
private void union(String dirPath,String toFilePath,String fileName,Boolean delSource){
    File dir=new File(dirPath);
    if(!dir.exists()){
        throw new BusinessException("目录不存在");
    }
    //1.读取临时目录中的所有文件
    File[] files = dir.listFiles();
    File targetFile = new File(toFilePath);
    RandomAccessFile writeFile=null;
    try {
        writeFile=new RandomAccessFile(targetFile,"rw");
        byte[] b=new byte[1024*10];
        for(int i=0;i<files.length;i++){
            int len=-1;
            //2.文件名就是分片号，按分片号读出数据
            File chunkFile=new File(dirPath+"/"+i);
            RandomAccessFile readFile=null;
            try {
                //3.将读取出的文件流按顺序写入到目标文件
                readFile=new RandomAccessFile(chunkFile,"r");
                while((len=readFile.read(b))!=-1){
                    writeFile.write(b,0,len);
                }
            }catch (Exception e){
                log.error("合并分片失败");
                throw new BusinessException("合并分片失败");
            }finally {
                readFile.close();
            }
        }
    }catch (Exception e){
        log.error("合并{}文件失败",fileName);
        throw new BusinessException("合并文件"+fileName+"出错了");
    }finally {
        if(writeFile!=null){
            try {
                writeFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(delSource&&dir.exists()){
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```

#### 视频文件切割

1. 先将视频文件转成.ts文件
2. 然后对.ts文件进行切割，最后再删除原本的.ts文件

```java
private void cutFileVideo(String fileId,String videoFilePath){
    //1.创建同名切片目录
    File tsFolder = new File(videoFilePath.substring(0, videoFilePath.lastIndexOf(".")));
    if (!tsFolder.exists()) {
        tsFolder.mkdirs();
    }
    //2.调用ffmpeg命令,将视频文件转成.ts文件再进行切割
    final String CMD_TRANSFER_2TS = "ffmpeg -y -i %s  -vcodec copy -acodec copy -vbsf h264_mp4toannexb %s";
    final String CMD_CUT_TS = "ffmpeg -i %s -c copy -map 0 -f segment -segment_list %s -segment_time 30 %s/%s_%%4d.ts";
    String tsPath = tsFolder + "/" + Constants.TS_NAME;//tsFolder/index.ts
    //3.生成index.ts文件
    String cmd = String.format(CMD_TRANSFER_2TS, videoFilePath, tsPath);
    ProcessUtils.executeCommand(cmd, false);
    //4.java执行cmd命令，生成索引文件.m3u8 和切片.ts
    cmd = String.format(CMD_CUT_TS, tsPath, tsFolder.getPath() + "/" + Constants.M3U8_NAME, tsFolder.getPath(), fileId);
    ProcessUtils.executeCommand(cmd, false);
    //5.删除index.ts
    new File(tsPath).delete();
}
```

#### 待优化

1. mkv文件无法成功转成index.ts文件【todo】

### 测试

#### 首个分片秒传和分片上传测试

1. 上传文件，前端报错<img src="easypan.assets/image-20240603140334988.png" alt="image-20240603140334988" style="zoom:80%;" />原因是nginx做了传输大小配置，改大点即可`client_max_body_size 50m`

2. springboot传输文件也有大小限制

   ```java
   org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException: The field file exceeds its maximum permitted size of 1048576 bytes.
   ```

   - 解决方式一：修改配置文件【来自csdn】

   ```properties
   spring.servlet.multipart.enabled=true
   spring.servlet.multipart.max-file-size=20MB
   spring.servlet.multipart.max-request-size=200MB
   ```

   - 解决方式二：前端分片切小一点【了解】

3. 修改RedisComponet中获取用户使用空间和总空间的方法，原本用户空间信息不存在时，总空间直接获取系统设置的默认值，现在改为直接去数据库中查询

   ```java
   public UserSpaceDto getUseSpace(String userId) {
       UserSpaceDto spaceDto = (UserSpaceDto) redisUtils.get(Constants.REDIS_KEY_USER_SPACE_USE + userId);
       if (spaceDto == null) {
           //如果没有用户空间使用情况，就得刷新缓存
           spaceDto = new UserSpaceDto();
           Long useSpace = fileInfoMapper.selectUseSpace(userId);
           spaceDto.setUserSpace(useSpace);
           UserInfoEntity userInfo = userInfoService.getById(userId);
           spaceDto.setTotalSpace(userInfo.getTotalSpace());
           saveUserSpaceUse(userId, spaceDto);
       }
       return spaceDto;
   }
   ```

4. 登录时，获取到用户空间信息为null，因为登录业务设置空间占用没有写完整，改为如下即可

   ```java
   Long useSpace = fileInfoMapper.selectUseSpace(userId);
   userSpaceDto.setUseSpace(useSpace);
   UserInfoEntity lastUserInfo = getById(userId);
   userSpaceDto.setTotalSpace(lastUserInfo.getTotalSpace());
   ```

<img src="easypan.assets/image-20240603153307792.png" alt="image-20240603153307792" style="zoom:80%;" />.

#### 测试全分片上传

1. 传输多片时报错

   ```java
   java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.Long (java.lang.Integer and java.lang.Long are in module java.base of loader 'bootstrap')
   ```

   - 原因是redis中存储的临时文件大小不一定是Long类型的，有可能是Integer类型，所以要进行类型推断

   ```java
   public Long getFileTempSize(String userId, String fileId) {
       Object sizeObj = redisUtils.get(Constants.REDIS_KEY_USER_FILE_TEMP_SIZE + userId + fileId);
       if (sizeObj == null) {
           return 0L;
       } else if (sizeObj instanceof Integer) {
           return ((Integer) sizeObj).longValue();
       } else {
           return (Long) sizeObj;
       }
   }
   ```

2. 上传成功<img src="easypan.assets/image-20240603154418041.png" alt="image-20240603154418041" style="zoom:80%;" /><img src="easypan.assets/image-20240603155622335.png" alt="image-20240603155622335" style="zoom:50%;" />

#### 测试转码和分片合并

1. 上传完成

   <img src="easypan.assets/image-20240603182552445.png" alt="image-20240603182552445" style="zoom: 50%;" />.

2. 转码成功

   <img src="easypan.assets/image-20240603182615252.png" alt="image-20240603182615252" style="zoom:80%;" /><img src="easypan.assets/image-20240603182631151.png" alt="image-20240603182631151" style="zoom:67%;" />

3. 目标文件生成且临时文件删除

   <img src="easypan.assets/image-20240603182707472.png" alt="image-20240603182707472" style="zoom:80%;" /><img src="easypan.assets/image-20240603182751474.png" alt="image-20240603182751474" style="zoom:80%;" />

#### 测试视频切片和缩略图

1. 测试视频切片

   <img src="easypan.assets/image-20240604172747489.png" alt="image-20240604172747489" style="zoom:80%;" />.

2. 测试缩略图：图片缩略图生成成功，**视频的缩略图没有生成**

   <img src="easypan.assets/image-20240604172814115.png" alt="image-20240604172814115" style="zoom:80%;" /><img src="easypan.assets/image-20240604172829869.png" alt="image-20240604172829869" style="zoom:80%;" />

   - 原因是封面路径拼接错了`String coverPath=targetFilePath+"/"+cover;`
   - 改为`String coverPath=targetFolderName+"/"+cover;`【上述代码也修改了】

3. 视频缩略图生成成功

   <img src="easypan.assets/image-20240604173624397.png" alt="image-20240604173624397" style="zoom:80%;" /><img src="easypan.assets/image-20240604173635528.png" alt="image-20240604173635528" style="zoom:67%;" />

# 文件其它相关操作

## 获取【预览】文件

### 获取图片

- 控制层接收图片所在目录和图片名

```java
@RequestMapping("/getImage/{imageFolder}/{imageName}")
@GlobalInterceptor(checkParams = true)
public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder
        , @PathVariable("imageName") String imageName) {
    getImage(response,imageFolder,imageName);
}
```

- 本地读取图片的方法

```java
public void getImage(HttpServletResponse response, String imageFolder, String imageName) {
    if (StringTools.isEmpty(imageFolder) || StringTools.isEmpty(imageName) ||
            !StringTools.pathIsOk(imageFolder) || !StringTools.pathIsOk(imageName)) {
        return;
    }
    String imageSuffix=StringTools.getFileSuffix(imageName);
    String filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+imageFolder+"/"+imageName;
    imageSuffix=imageSuffix.replace(".","");//去掉后缀名的“.”
    response.setContentType("image/"+imageSuffix);
    response.setHeader("Cache-Control","max-age=2592000");
    FileUtils.readFile(response,filePath);
}
```

- 测试

<img src="easypan.assets/image-20240605000148952.png" alt="image-20240605000148952" style="zoom:50%;" />.

### 获取视频/其它文件

#### 控制层

- 获取视频文件

```java
@RequestMapping("ts/getVideoInfo/{fileId}")
@GlobalInterceptor(checkParams = true)
public void getFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId") String fileId) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    super.getFile(response, fileId, webUserDto.getUserId());
}
```

- 获取其它文件

```java
@RequestMapping("/getFile/{fileId}")
@GlobalInterceptor(checkParams = true)
public void getFile(HttpServletResponse response, HttpSession session, @PathVariable("fileId") String fileId) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    super.getFile(response, fileId, webUserDto.getUserId());
}
```

#### 核心逻辑

1. 播放视频时会获取切片索引目录文件和视频切片，所以要分类讨论
2. 视频切片的fileId为文件id拼接上切片id，所以需要先解析从fileId才能查询到文件所在位置
3. 索引目录文件携带的fileId就是文件id，可以查询出文件所在位置再定位到索引目录位置
4. 如果非视频文件就直接通过名字找到存储位置

```java
protected void getFile(HttpServletResponse response,String fileId,String userId){
    String filePath=null;
    if(fileId.endsWith(".ts")){
        //1.以ts为后缀说明是读取切片，fileId是文件id拼接上切片id
        //1.1解析出fileId
        String[] tsArray = fileId.split("_");
        String realFileId=tsArray[0];
        //1.2拼接文件地址
        FileInfoEntity fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(realFileId, userId);
        if(fileInfo==null){
            return;
        }
        String fileName=StringTools.getFileNameNoSuffix(fileInfo.getFilePath())+"/"+fileId;
        filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+fileName;
    }else{
        //2.否则就是读取正常文件，fileId就是文件id
        FileInfoEntity fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
        if(fileInfo==null){
            return;
        }
        if(FileCategoryEnums.VIDEO.getCategory().equals(fileInfo.getFileCategory())){
            //2.1获取不包含后缀的文件名
            String fileNameNoSuffix = StringTools.getFileNameNoSuffix(fileInfo.getFilePath());
            //2.2获取索引文件所在路径
            filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+fileNameNoSuffix+"/"+Constants.M3U8_NAME;
        }else {
            //3.非视频索引文件就直接通过文件名读出就行了
            filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+fileInfo.getFilePath();
        }
        File file = new File(filePath);
        if(!file.exists()){
            return;
        }
    }
    FileUtils.readFile(response,filePath);
}
```

#### 测试

1. 获取到切片索引

   <img src="easypan.assets/image-20240604180655295.png" alt="image-20240604180655295" style="zoom:80%;" />.
   
2. 视频播放

   <img src="easypan.assets/image-20240604233711657.png" alt="image-20240604233711657" style="zoom:50%;" />.

3. 预览其他文件

   <img src="easypan.assets/image-20240605000042395.png" alt="image-20240605000042395" style="zoom:50%;" />.

## 创建目录

### 接口信息

- 接口URL：POSThttp://localhost:7090/api/file/newFoloder

| 参数名   | 参数值 | 是否必填 | 参数类型 | 描述说明 |
| -------- | ------ | -------- | -------- | -------- |
| filePid  |        | 是       | String   | 文件父id |
| fileName |        | 是       | String   | 目录名   |

- 成功响应

```json
{
	"status": "success",
	"code": 200,
	"info": "请求成功",
	"data": null
}
```

### 控制层

- 创建的目录只是一个逻辑实体，不在硬盘上创建

```java
@RequestMapping("/newFoloder")
@GlobalInterceptor(checkParams = true)
public ResponseVO newFolder(HttpSession session, @VerifyParam(required = true) String filePid,
                            @VerifyParam(required = true) String fileName) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    String userId = webUserDto.getUserId();
    FileInfoEntity fileInfo = fileInfoService.newFolder(filePid, fileName, userId);
    return getSuccessResponseVO(fileInfo);
}
```

### 业务层

```java
public FileInfoEntity newFolder(String filePid, String fileName, String userId) {
    //1.校验文件夹名称【不能重名】
    checkFileName(filePid, userId,fileName, FileFolderTypeEnums.FOLDER.getType());
    //2.直接新增文件夹信息
    Date date = new Date();
    FileInfoEntity fileInfo = new FileInfoEntity();
    fileInfo.setFileId(StringTools.getRandomString(Constants.LENGTH_10));
    fileInfo.setFilePid(filePid);
    fileInfo.setFileName(fileName);
    fileInfo.setUserId(userId);
    fileInfo.setFolderType(FileFolderTypeEnums.FOLDER.getType());
    fileInfo.setCreateTime(date);
    fileInfo.setLastUpdateTime(date);
    fileInfo.setStatus(FileStatusEnums.USING.getStatus());
    fileInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
    fileInfoMapper.insert(fileInfo);
    return fileInfo;
}
//校验文件名称
private void checkFileName(String filePid, String userId, String fileName, Integer folderType) {
    FileInfoQuery fileInfoQuery = new FileInfoQuery();
    fileInfoQuery.setFolderType(folderType);
    fileInfoQuery.setFileName(fileName);
    fileInfoQuery.setFilePid(filePid);
    fileInfoQuery.setUserId(userId);
    Integer count = fileInfoMapper.selectCount(fileInfoQuery);
    if (count > 0) {
        throw new BusinessException("此目录下已经存在同名文件，请修改名称");
    }
}
```

### 测试

1. 创建多级目录，结果都只在根节点创建

   <img src="easypan.assets/image-20240605134648979.png" alt="image-20240605134648979" style="zoom:80%;" /><img src="easypan.assets/image-20240605134659452.png" alt="image-20240605134659452" style="zoom:50%;" />

2. 原因是前端在创建目录方法中，没有将当前目录的id作为pid，而是直接取0，如下

   ```java
   tableData.value.list.unshift({
       showEdit: true,
       fileType: 0,
       fileId: "",
       filePid: 0,
   });
   ```

   - 解决`filePid: currentFolder.value.fileId`

   <img src="easypan.assets/image-20240605161026602.png" alt="image-20240605161026602" style="zoom: 67%;" /><img src="easypan.assets/image-20240605161044253.png" alt="image-20240605161044253" style="zoom: 67%;" /><img src="easypan.assets/image-20240605161110849.png" alt="image-20240605161110849" style="zoom: 67%;" />

## 获取目录

### 获取当前目录

#### 控制层

- path是文件层级路径

```java
@RequestMapping("/getFolderInfo")
@GlobalInterceptor(checkParams = true)
public ResponseVO getFolderInfo(HttpSession session,@VerifyParam(required = true) String path) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    String userId = webUserDto.getUserId();
    return super.getFolderInfo(path,userId);
}
```

#### 业务方法

```java
public ResponseVO getFolderInfo(String path, String userId) {
    String[] pathArray = path.split("/");
    FileInfoQuery infoQuery = new FileInfoQuery();
    infoQuery.setUserId(userId);
    //1.使用非递归的方式，直接把所有层级的目录都查出来
    infoQuery.setFileIdArray(pathArray);
    //2.根据层级顺序排序,拼接成形如 field(file_Id,"id1","id2",...)
    String orderBy="field(file_Id,\""+ StringUtils.join(pathArray,"\",\"")+"\")";
    infoQuery.setOrderBy(orderBy);
    List<FileInfoEntity> fileInfoEntityList = fileInfoService.findListByParam(infoQuery);
    return getSuccessResponseVO(fileInfoEntityList);
}
```

### 获取所有目录

- 移动时可以选择目标目录位置，所以需要记录当前所在的目录id

  <img src="easypan.assets/image-20240605170237285.png" alt="image-20240605170237285" style="zoom:50%;" />.

- 原文件所在目录需要排除掉

```java
/**
 * @param session
 * @param filePid 当前父目录【即移动到的位置】
 * @param currentFileIds 原文件所在目录【需要排除掉】
 * @return
 */
@RequestMapping("/loadAllFolder")
@GlobalInterceptor(checkParams = true)
public ResponseVO loadAllFolder(HttpSession session, @VerifyParam(required = true) String filePid
        ,  String currentFileIds) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    String userId = webUserDto.getUserId();
    FileInfoQuery fileInfoQuery = new FileInfoQuery();
    //1.排除掉原文件所在目录
    if(!StringTools.isEmpty(currentFileIds)){
        fileInfoQuery.setExcludeFileIdArray(currentFileIds.split(","));
    }
    //2.查询当前目录下的所有目录
    fileInfoQuery.setFolderType(FileFolderTypeEnums.FOLDER.getType());
    fileInfoQuery.setFilePid(filePid);
    fileInfoQuery.setUserId(userId);
    fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());
    fileInfoQuery.setOrderBy("create_time desc");
    List<FileInfoEntity> fileInfoList = fileInfoService.findListByParam(fileInfoQuery);
    return getSuccessResponseVO(CopyTools.copyList(fileInfoList,FileInfoVO.class));
}
```

## 文件（夹）重命名

### 控制层

```java
@RequestMapping("/rename")
@GlobalInterceptor(checkParams = true)
public ResponseVO rename(HttpSession session, @VerifyParam(required = true) String fileId
        , @VerifyParam(required = true) String fileName) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    String userId = webUserDto.getUserId();
    FileInfoEntity fileInfo = fileInfoService.rename(fileId, userId, fileName);
    return getSuccessResponseVO(fileInfo);
}
```

### 业务层

```java
public FileInfoEntity rename(String fileId, String userId, String fileName) {
    FileInfoEntity fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
    if(null==fileInfo){
        throw new BusinessException("文件不存在");
    }
    //1.校验文件名
    String filePid = fileInfo.getFilePid();
    checkFileName(filePid,userId,fileName,FileFolderTypeEnums.FOLDER.getType());
    //2.如果修改的是文件类型，获取文件后缀,只改名不该后缀
    if(FileFolderTypeEnums.FILE.getType().equals(fileInfo.getFolderType())){
        fileName=fileName+StringTools.getFileSuffix(fileInfo.getFileName());
    }
    Date date = new Date();
    FileInfoEntity dbFileInfo = new FileInfoEntity();
    dbFileInfo.setFileName(fileName);
    dbFileInfo.setUpdateTime(date);
    fileInfoMapper.updateByFileIdAndUserId(dbFileInfo,fileId,userId);
    //3.双检,避免其它线程同时修改
    FileInfoQuery fileInfoQuery = new FileInfoQuery();
    fileInfoQuery.setFileName(fileName);
    fileInfoQuery.setFilePid(filePid);
    fileInfoQuery.setUserId(userId);
    Integer count = fileInfoMapper.selectCount(fileInfoQuery);
    if (count > 1) {
        throw new BusinessException("此目录下已经存在同名文件，请修改名称");
    }
    fileInfo.setFileName(fileName);
    fileInfo.setLastUpdateTime(date);
    return fileInfo;
}
```

## 文件移动

### 控制层

- 只做逻辑移动

```java
/**
 * @param fileIds 批量移动的多个文件id
 * @param filePid 目标目录
 */
@RequestMapping("/changeFileFolder")
@GlobalInterceptor(checkParams = true)
public ResponseVO changeFileFolder(HttpSession session, @VerifyParam(required = true) String fileIds
        ,  String filePid){
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    String userId = webUserDto.getUserId();
    fileInfoService.changeFileFolder(fileIds,filePid,userId);
    return getSuccessResponseVO(null);
}
```

### 业务层

```java
public void changeFileFolder(String fileIds, String filePid, String userId) {
    if(fileIds.equals(filePid)){
        throw new BusinessException("目标目录不能是原文件所在目录！");
    }
    //1.不在根目录的情况
    if(!Constants.ZERO_STR.equals(filePid)){
        FileInfoEntity fileInfo = getFileInfoByFileIdAndUserId(filePid, userId);
        if(fileInfo==null||!FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())){
            //如果目标目录不存在，就直接抛异常
            throw new BusinessException("目标目录不存在！");
        }
    }
    //2.查询出目标目录中的所有文件，检查是否与当前移动的文件重名了
    String[] fileIdArray = fileIds.split(",");
    FileInfoQuery query = new FileInfoQuery();
    query.setFilePid(filePid);
    query.setUserId(userId);
    List<FileInfoEntity> dbFileInfoList = findListByParam(query);
    Map<String,FileInfoEntity> dbFileNameMap=
            dbFileInfoList.stream().collect(Collectors.toMap(FileInfoEntity::getFileName,
                    Function.identity(),//表示将FileInfoEntity对象本身作为Map的值
                    (file1,file2)->file2));//合并函数，当遇到重复的键时，选择保留第二个值
    //3.查询选中的文件
    query=new FileInfoQuery();
    query.setFileIdArray(fileIdArray);
    query.setUserId(userId);
    List<FileInfoEntity> selectFileList = findListByParam(query);
    //4.修改所选文件的父目录
    for (FileInfoEntity fileInfo : selectFileList) {
        String fileName=fileInfo.getFileName();
        FileInfoEntity dbFileInfo = dbFileNameMap.get(fileName);
        if(dbFileInfo!=null){
            //4.1目标目录存在同名文件，就将所选文件重命名
            fileName=StringTools.rename(fileName);
            fileInfo.setFileName(fileName);
        }
        //4.2修改选中文件的父目录
        fileInfo.setFilePid(filePid);
        fileInfoMapper.updateByFileIdAndUserId(fileInfo,fileInfo.getFileId(),userId);
    }
}
```

# 下载文件

## 业务分析

- 如果下载文件需要登录权限，那第三方下载工具下载文件时就需要获取用户信息

- 因此先创建下载链接，在这一步进行用户信息校验，然后生成一个临时的code，给予下载权限，之后直接通过下载链接和code即可，不需要获取用户权限

- 创建下载链接接口信息

  - 接口URL：GEThttp://localhost:7090/api/file/createDownloadUrl/{fileId}
  - 响应示例【响应一个临时的code作为下载权限标识】

  ```json
  {
  	"status": "success",
  	"code": 200,
  	"info": "请求成功",
  	"data": "UEr0bAmOoi0NLui6CvRaBFtn3SLyvIf02g4XqnyVK8G5iDxZ0i"
  }
  ```

## 创建下载链接

### 封装下载信息

```java
@Data
public class DownloadFileDto {
    private String downloadCode;
    private String fileName;
    private String filePath;
}
```

### 控制层

```java
@RequestMapping("createDownloadUrl/{fileId}")
@GlobalInterceptor(checkParams = true)
public ResponseVO createDownloadUrl(HttpSession session, @PathVariable(required = true) String fileId) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    String userId = webUserDto.getUserId();
    return super.createDownloadUrl(fileId,userId);
}
```

### 业务代码

- 下载方法

```java
protected  ResponseVO createDownloadUrl(String fileId,String userId){
    FileInfoEntity fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(fileId, userId);
    if(fileInfo==null){
        throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
    if(FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFileType())){
        //只能下载文件，不能下载目录
        throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
    //1.生成随机码
    String code=StringTools.getRandomString(Constants.LENGTH_50);
    //2.封装下载信息，并且存到redis中
    DownloadFileDto downloadFileDto = new DownloadFileDto();
    downloadFileDto.setDownloadCode(code);
    downloadFileDto.setFileName(fileInfo.getFileName());
    downloadFileDto.setFilePath(fileInfo.getFilePath());
    redisComponent.saveDownloadCode(code,downloadFileDto);//用临时码作为key，下载信息作为value
    //3.返回临时码
    return getSuccessResponseVO(code);
}
```

- redisComponent

```java
public void saveDownloadCode(String code, DownloadFileDto downloadFileDto) {
   redisUtils.setex(Constants.REDIS_KEY_DOWNLOAD+code,downloadFileDto,Constants.REDIS_KEY_EXPIRES_FIVE_MIN);
}
```

## 根据code下载文件

### 控制层

```java
@RequestMapping("/download/{code}")
@GlobalInterceptor(checkParams = true, checkLogin = false)//不需要校验登录
public void download(HttpServletRequest request, HttpServletResponse response,
                     @VerifyParam(required = true) @PathVariable("code") String code) throws UnsupportedEncodingException {
    super.download(request, response, code);
}
```

### 业务代码

- 下载代码

```java
protected void download(HttpServletRequest request, HttpServletResponse response, String code) throws UnsupportedEncodingException {
    //1.根据临时下载码获取下载信息
    DownloadFileDto downloadFileDto = redisComponent.getDownloadCode(code);
    if(downloadFileDto==null){
        throw new BusinessException("下载信息过期!!!");
    }
    //2.下载文件
    String filePath=appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE+downloadFileDto.getFilePath();
    String fileName=downloadFileDto.getFileName();
    if (request.getHeader("User-Agent").toLowerCase().indexOf("msie")>0) {
        //IE浏览
        fileName= URLEncoder.encode(fileName,"UTF-8");
    }else {
        fileName=new String(fileName.getBytes("UTF-8"),"ISO8859-1");
    }
    response.setHeader("Content-Disposition","attachment;filename=\""+fileName+"\"");
    FileUtils.readFile(response,filePath);
}
```

- redisComponent

```java
/**
 * 根据临时下载code获取到下载信息
 * @param code 临时下载code
 * @return 下载基本信息，包含文件名、文件存储路径
 */
public DownloadFileDto getDownloadCode(String code){
    return (DownloadFileDto)redisUtils.get(Constants.REDIS_KEY_DOWNLOAD+code);
}
```

## 测试

1. 下载MP4类型文件被当成是文件夹类型

   <img src="easypan.assets/image-20240611154237389.png" alt="image-20240611154237389" style="zoom:67%;" />.

   - 原因：应该是获取文件夹类型而不是获取文件类别，上述if判断改为如下【低级错误】

   ```java
   FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())
   ```

# 删除文件

## 删除到回收站

### 控制层

```java
/**
 * 批量删除文件到回收站
 * @param fileIds 所选的多个待删除文件id
 */
@RequestMapping("delFile")
@GlobalInterceptor(checkParams = true)
public ResponseVO delFile(HttpSession session,@VerifyParam(required = true)String fileIds){
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    fileInfoService.removeFile2RecycleBatch(webUserDto.getUserId(),fileIds);
    return getSuccessResponseVO(null);
}
```

### 业务层

#### 主骨架

```java
@Transactional
@Override
public void removeFile2RecycleBatch(String userId, String fileIds) {
    //1.查询所有待删文件
    String[] fileIdArray=fileIds.split(",");
    FileInfoQuery query = new FileInfoQuery();
    query.setUserId(userId);
    query.setFileIdArray(fileIdArray);
    query.setDelFlag(FileDelFlagEnums.USING.getFlag());
    List<FileInfoEntity> fileInfoEntityList = fileInfoMapper.selectList(query);
    if(fileInfoEntityList.isEmpty()){
        return;
    }
    //2.如果删除的是目录，那就得把目录下的所有子文件都修改标记，并且可能有多级目录嵌套，所以需要递归修改
    ArrayList<String> delFilePidList = new ArrayList<>();//存放需要删除的目录id
    for (FileInfoEntity fileInfo : fileInfoEntityList) {
        if(FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())){
            findAllSubFolderFileList(delFilePidList,userId,fileInfo.getFileId(),fileInfo.getDelFlag());
        }
    }
    //3.批量删除所有目录下的子文件
    FileInfoEntity updateInfo = new FileInfoEntity();
    if(!delFilePidList.isEmpty()){
        //TODO 这样做，目录中的文件就直接删除了，我个人认为得加一个状态，表示目录中的文件是因为父层级没了从而连带删除
        updateInfo.setDelFlag(FileDelFlagEnums.DEL.getFlag());
      	fileInfoMapper.updateFileDelFlagBatch
          (updateInfo,userId,delFilePidList,null,FileDelFlagEnums.USING.getFlag());
    }
    //4.批量把首层的所有文件移到回收站
    updateInfo.setRecoveryTime(new Date());//进入回收站的时间
    updateInfo.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
    List<String> list = Arrays.stream(fileIdArray).toList();
    fileInfoMapper.updateFileDelFlagBatch(updateInfo,userId,null,list,FileDelFlagEnums.USING.getFlag());
}
```

#### 递归查询子目录

```java
private void findAllSubFolderFileList(List<String> filePidList,String userId,String fileId,Integer delFlag){
    filePidList.add(fileId);
    //1.查询出本层所有目录
    FileInfoQuery query = new FileInfoQuery();
    query.setUserId(userId);
    query.setFilePid(fileId);
    query.setDelFlag(delFlag);
    query.setFolderType(FileFolderTypeEnums.FOLDER.getType());
    List<FileInfoEntity> fileInfoEntityList = fileInfoMapper.selectList(query);
    //2.继续递归
    for (FileInfoEntity fileInfo : fileInfoEntityList) {
        findAllSubFolderFileList(filePidList,userId,fileInfo.getFileId(),delFlag);
    }
}
```

#### 优化

- 添加删除状态`PARENT2RECYCLE(3,"父目录进入回收站");`，表示父目录进入回收站，所以受牵连
- 修改主骨架部分代码

```java
//3.批量删除所有目录下的子文件
FileInfoEntity updateInfo = new FileInfoEntity();
if(!delFilePidList.isEmpty()){
    //回收站只需要显示首层文件，所以嵌套文件应该单独设置一个状态，即因为父目录被移到回收站而级联更新
    updateInfo.setDelFlag(FileDelFlagEnums.PARENT2RECYCLE.getFlag());
    fileInfoMapper.updateFileDelFlagBatch
        (updateInfo,userId,delFilePidList,null,FileDelFlagEnums.USING.getFlag());
}
```

### DAO层

- Mapper接口

```java
/**
 * 根据筛选条件去批量修改指定目录的删除标识
 * @param fileInfo 修改信息
 * @param filePidList 根据父id，即按照目录来筛选，将指定目录下的所有文件修改状态
 * @param fileIdList 根据文件id，修改指定文件id的文件状态，和filePidList参数只能有一个生效
 * @param oldDelFlag 旧的删除标识，已经删除过的文件没必要删除
 */
void updateFileDelFlagBatch(@Param("bean") FileInfoEntity fileInfo, @Param("userId") String userId,
                            @Param("filePidList") List<String> filePidList, 
                            @Param("fileIdList") List<String> fileIdList,
                            @Param("oldDelFlag") Integer oldDelFlag);
```

- sql

```xml
<update id="updateFileDelFlagBatch">
    update file_info
    <set>
        <if test="bean.recoveryTime != null">
            recovery_time = #{bean.recoveryTime},
        </if>
        <if test="bean.delFlag != null">
            del_flag = #{bean.delFlag},
        </if>
    </set>
    where user_id = #{userId}
    <if test="filePidList!=null">
        and file_pid in (<foreach collection="filePidList" separator="," item="item"> #{item}</foreach> )
    </if>
    <if test="fileIdList!=null">
        and file_id in (<foreach collection="fileIdList" separator="," item="item"> #{item}</foreach> )
    </if>
    <if test="oldDelFlag!=null">
        and del_flag=#{oldDelFlag}
    </if>
</update>
```

### 测试

1. 删除图片文件夹和软考pdf

<img src="easypan.assets/image-20240607155732333.png" alt="image-20240607155732333" style="zoom:80%;" />.

2. 受牵连的文件的状态

   <img src="easypan.assets/image-20240607155802945.png" alt="image-20240607155802945" style="zoom: 50%;" />.

3. 重置所有状态，先删除`图片-》银河足球队-》修卡.png`和`图片-》aabb`，然后删除图片文件夹

   <img src="easypan.assets/image-20240607160038608.png" alt="image-20240607160038608" style="zoom:80%;" />

## 获取回收站列表

- 直接获取删除状态为回收站的数据即可

```java
@RequestMapping("/loadRecycleList")
@GlobalInterceptor
public ResponseVO loadRecycleList(HttpSession session,Integer pageNo,Integer pageSize){
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    FileInfoQuery query = new FileInfoQuery();
    query.setPageNo(pageNo);
    query.setPageSize(pageSize);
    query.setUserId(webUserDto.getUserId());
    query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
    query.setOrderBy("recovery_time desc");
    PaginationResultVO<FileInfo> listByPage = fileInfoService.findListByPage(query);
    return getSuccessResponseVO(convert2PaginationVO(listByPage, FileInfoVO.class));
}
```

## 从回收站还原文件

### 控制层

```java
/**
 * 批量将回收站文件还原
 * @param fileIds 选中的文件
 */
@RequestMapping("/recoverFile")
@GlobalInterceptor
public ResponseVO recoverFile(HttpSession session, @VerifyParam(required = true)String fileIds){
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    fileInfoService.recoverFile(webUserDto.getUserId(),fileIds);
    return getSuccessResponseVO(null);
}
```

### 业务层

#### 主骨架

- 老罗默认是把文件都还原到根目录，这样明显是有问题的，我的做法是还原回原来的目录，如果原来目录不存在再还原到根目录
- 检查还原回去的文件是否和同级目录文件重名，需要用当前文件实体的父id获取到同级目录中未被删除的文件信息

```java
@Transactional
@Override
public void recoverFile(String userId, String fileIds) {
    //1.查询出选中文件
    String[] fileIdArray = fileIds.split(",");
    FileInfoQuery query = new FileInfoQuery();
    query.setUserId(userId);
    query.setFileIdArray(fileIdArray);
    query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
    List<FileInfoEntity> fileInfoEntityList = fileInfoMapper.selectList(query);
    List<String> recoverPidList = new ArrayList<>();
    //2.如果是目录，就得把子文件都递归查出来
    for (FileInfoEntity fileInfo : fileInfoEntityList) {
        if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
            //要查出那些因为父文件被删导致自己被删的文件
            findAllSubFolderFileList(recoverPidList, userId, fileInfo.getFileId(), FileDelFlagEnums.PARENT2RECYCLE.getFlag());
        }
    }
    FileInfoEntity updateInfo=new FileInfoEntity();
    updateInfo.setDelFlag(FileDelFlagEnums.USING.getFlag());
    updateInfo.setLastUpdateTime(new Date());
    //3.根据目录还原子文件
    if (!recoverPidList.isEmpty()) {
        for (String pid : recoverPidList) {
            //3.1还原存在重名问题
            checkChildName(pid,userId,FileDelFlagEnums.PARENT2RECYCLE.getFlag());
        }
        //3.2改成使用中的状态
        fileInfoMapper.updateFileDelFlagBatch
            (updateInfo,userId,recoverPidList,null,FileDelFlagEnums.PARENT2RECYCLE.getFlag());
    }
    //4.还原首层文件
    List<String> list = Arrays.asList(fileIdArray);
    for (FileInfoEntity recoverFile : fileInfoEntityList) {
        //4.1先得查询当前文件的原目录是否存在
        String pid=recoverFile.getFilePid();
        FileInfoEntity parentInfo = fileInfoMapper.selectByFileIdAndUserId(pid, userId);
        if(parentInfo==null||!FileDelFlagEnums.USING.getFlag().equals(parentInfo.getDelFlag())){
            //原目录被删了，那就只能还原到根目录;又或者是直接在根目录中删除的
            pid=Constants.ZERO_STR;
            FileInfoEntity updatePidInfo = new FileInfoEntity();
            updatePidInfo.setFilePid(pid);
            fileInfoMapper.updateByFileIdAndUserId(updatePidInfo,recoverFile.getFileId(),userId);
        }
        //4.2检查还原回去是否会和同级文件重名,重名就自动修改
        checkChildName(pid,userId,FileDelFlagEnums.RECYCLE.getFlag());
    }
    //4.3还原状态
    fileInfoMapper.updateFileDelFlagBatch(updateInfo,userId,null,list,FileDelFlagEnums.RECYCLE.getFlag());
}
```

#### 检查重名

- 检查还原回去的文件是否会和原本的目录中的文件命名冲突，如果冲突就需要自动重命名

```java
private void checkChildName(String pid,String userId,Integer delFlag) {
    //1.查询出当前目录中未被删除的文件,并用set存储，之后用set来检测回收站中的同级所有文件
    FileInfoQuery query = new FileInfoQuery();
    query.setUserId(userId);
    query.setFilePid(pid);
    query.setDelFlag(FileDelFlagEnums.USING.getFlag());
    List<FileInfoEntity> curUsingFileList = fileInfoMapper.selectList(query);
    Set<String> fileNameSet=new HashSet<>();
    for (FileInfoEntity fileInfo : curUsingFileList) {
        fileNameSet.add(fileInfo.getFileName());
    }
    //2.查询出当前目录中需要还原的文件
    query.setDelFlag(delFlag);
    List<FileInfoEntity> recoveryFileList = fileInfoMapper.selectList(query);
    for (FileInfoEntity fileInfo : recoveryFileList) {
        if(fileNameSet.contains(fileInfo)){
            //3.重名了就得自动重命名,这一块也可以批量修改
            FileInfoEntity updateInfo=new FileInfoEntity();
            updateInfo.setFileName(StringTools.rename(fileInfo.getFileName()));
            fileInfoMapper.updateByFileIdAndUserId(updateInfo,fileInfo.getFileId(),userId);
        }
    }
}
```

+ 二版：重名还得判断文件类型，所以用set不行，而且map的value的值应该要是list，存放同名的文件夹和文件

```java
//检查还原回去的文件是否会和原本的目录中的文件命名冲突，如果冲突就需要自动重命名
private void checkChildName(String pid,String userId,Integer delFlag) {
    //1.查询出当前目录中未被删除的文件,并用map存储，key为文件名，value为文件列表【因为文件和文件夹可以重名】
    FileInfoQuery query = new FileInfoQuery();
    query.setUserId(userId);
    query.setFilePid(pid);
    query.setDelFlag(FileDelFlagEnums.USING.getFlag());
    List<FileInfoEntity> curUsingFileList = fileInfoMapper.selectList(query);
    Map<String,List<FileInfoEntity>> fileNameMap=new HashMap<>();
    for (FileInfoEntity fileInfo : curUsingFileList) {
        String fileName=fileInfo.getFileName();
        if(!fileNameMap.containsKey(fileName)){
            fileNameMap.put(fileName,new ArrayList<>());
        }
        fileNameMap.get(fileName).add(fileInfo);
    }
    //2.查询出当前目录中需要还原的文件
    query.setDelFlag(delFlag);
    List<FileInfoEntity> recoveryFileList = fileInfoMapper.selectList(query);
    for (FileInfoEntity fileInfo : recoveryFileList) {
        String fileName=fileInfo.getFileName();
        //3.没有重名的就不用管
        if(!fileNameMap.containsKey(fileName)){
            continue;
        }
        //4.重名了还得判断是否是同类型
        List<FileInfoEntity> sameNameFileList = fileNameMap.get(fileName);
        for (FileInfoEntity sameNameFile : sameNameFileList) {
            if(sameNameFile.getFolderType().equals(fileInfo.getFolderType())){
                //同类型文件重名了就得自动重命名,这一块也可以批量修改
                FileInfoEntity updateInfo=new FileInfoEntity();
                updateInfo.setFileName(StringTools.rename(fileInfo.getFileName()));
                fileInfoMapper.updateByFileIdAndUserId(updateInfo,fileInfo.getFileId(),userId);
            }
        }
    }
}
```

### 测试

1. 测试原目录被删除的情况下，还原文件夹aabb，没有出现在根目录

   <img src="easypan.assets/image-20240607173513133.png" alt="image-20240607173513133" style="zoom:80%;" />.

   - 原因判断原目录不存在逻辑有误，这样子是跑不进代码块里头的，而且删除状态要**<a>不</a>为使用中**`if(parentInfo==null&&FileDelFlagEnums.USING.getFlag().equals(parentInfo.getDelFlag()))`

   - 改为`if(parentInfo==null||!FileDelFlagEnums.USING.getFlag().equals(parentInfo.getDelFlag()))`
   - 已同步修改到业务层中的代码

   <img src="easypan.assets/image-20240607174127145.png" alt="image-20240607174127145" style="zoom:67%;" /><img src="easypan.assets/image-20240607174147031.png" alt="image-20240607174147031" style="zoom:67%;" />

2. 创建文件夹时，和回收站的名字撞了，出现以下问题

   <img src="easypan.assets/image-20240607174337678.png" alt="image-20240607174337678" style="zoom:50%;" />.

   - 描述：已删除的文件夹还会影响重名
   - 解决：修改新建文件夹调用的校验文件名的方法【对应FileInfoService中的checkFileName方法】，加上一行`fileInfoQuery.setDelFlag(FileDelFlagEnums.USING.getFlag());`

3. 测试还原时，同级目录有同名文件，没有自动重命名

   <img src="easypan.assets/image-20240607174855916.png" alt="image-20240607174855916" style="zoom:67%;" />.

   - 原因是写快了，原版没获取文件名`fileNameSet.contains(fileInfo)`
   - 改为`fileNameSet.contains(fileInfo.getFileName())`
   - 同时原本使用set给同名文件去重，但是没有考虑文件类型，所以需要用map去重，key为文件名，value为文件实体列表
   - 用列表是因为文件夹和文件名字冲突了无所谓，如果**不用列表可能会丢失同名文件夹和文件的其中一个**
   - 代码改在了业务层-》检查重名-》二版

   <img src="easypan.assets/image-20240607181115592.png" alt="image-20240607181115592" style="zoom:80%;" />.

## 从数据库彻底删除

### 控制层

```java
/**
 * 从数据库彻底删除文件
 * @param fileIds
 * @return
 */
@RequestMapping("delFile")
@GlobalInterceptor(checkParams = true)
public ResponseVO delFile(HttpSession session,@VerifyParam(required = true)String fileIds){
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    fileInfoService.delFileBatch( webUserDto.getUserId(), fileIds, webUserDto.getIsAdmin());
    return getSuccessResponseVO(null);
}
```

### 业务层

```java
@Transactional
@Override
public void delFileBatch(String userId, String fileIds, Boolean isAdmin) {
    //1.查询出待删除文件
    String[] fileArray = fileIds.split(",");
    FileInfoQuery query = new FileInfoQuery();
    query.setUserId(userId);
    query.setUserId(userId);
    query.setFileIdArray(fileArray);
    query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
    List<FileInfoEntity> fileInfoList = fileInfoMapper.selectList(query);
    //2.找到所有的待删目录
    List<String> filePidList=new ArrayList<>();
    for (FileInfoEntity fileInfo : fileInfoList) {
        if(FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())){
            findAllSubFolderFileList
                (filePidList,userId,fileInfo.getFileId(),FileDelFlagEnums.PARENT2RECYCLE.getFlag());
        }
    }
    //3.删除所有目录下的子文件
    if(!filePidList.isEmpty()){
        fileInfoMapper.delFileBatch(userId,filePidList,null,
                isAdmin?null:FileDelFlagEnums.PARENT2RECYCLE.getFlag());//超级管理员不需要过滤删除状态
    }
    //4.删除首层所选文件
    fileInfoMapper.delFileBatch(userId,null,Arrays.asList(fileArray),isAdmin?null:FileDelFlagEnums.RECYCLE.getFlag());
    //5.更新用户空间信息
    Long useSpace = fileInfoMapper.selectUseSpace(userId);
    UserInfoEntity userInfo = new UserInfoEntity();
    userInfo.setUserId(userId);
    userInfo.setUseSpace(useSpace);
    userInfoService.updateById(userInfo);
    //6.缓存中同步使用空间信息
    UserSpaceDto spaceDto = redisComponent.getUseSpace(userId);
    spaceDto.setUseSpace(useSpace);
    redisComponent.saveUserSpaceUse(userId,spaceDto);
}
```

### Dao层

- mapper接口

```java
/**
 * 根据删选条件批量从数据库删除文件记录
 * @param filePidList 根据父id，即按照目录来筛选，将指定目录下的所有文件修改状态
 * @param fileIdList  根据文件id，修改指定文件id的文件状态，和filePidList参数只能有一个生效
 * @param oldDelFlag  旧的删除标识，已经删除过的文件没必要删除
 */
void delFileBatch(@Param("userId") String userId,
                  @Param("filePidList") List<String> filePidList,
                  @Param("fileIdList") List<String> fileIdList,
                  @Param("oldDelFlag") List<String> oldDelFlag);
```

- sql

```xml
<delete id="delFileBatch">
    delete from file_info where user_id = #{userId}
    <if test="filePidList!=null">
        and file_pid in(<foreach collection="filePidList" separator="," item="item">#{item}</foreach>)
    </if>
    <if test="fileIdList!=null">
        and file_id in(<foreach collection="fileIdList" separator="," item="item">#{item}</foreach>)
    </if>
    <if test="oldDelFlag!=null">
        and del_flag = #{oldDelFlag}
    </if>
</delete>
```

## 定时删除回收站过期文件

### 定时任务

```java
@Component
public class FileCleanTask {
    @Autowired
    private FileInfoService fileInfoService;
    @Scheduled(fixedDelay = 1000*60*3)
    public void execute(){
        //1.查询已过期的文件
        FileInfoQuery query = new FileInfoQuery();
        query.setDelFlag(FileDelFlagEnums.RECYCLE.getFlag());
        query.setQueryExpire(true);//查询已过期的文件
        List<FileInfoEntity> fileInfoList = fileInfoService.findListByParam(query);
        //2.按用户名进行分组
        Map<String,List<FileInfoEntity>> fileInfoMap=fileInfoList.stream().
                collect(Collectors.groupingBy(FileInfoEntity::getUserId));
        for (Map.Entry<String, List<FileInfoEntity>> entry : fileInfoMap.entrySet()) {
            List<String> fileIdList = entry.getValue().stream().
                    map(FileInfoEntity::getFileId).collect(Collectors.toList());
            String userId = entry.getKey();
            fileInfoService.delFileBatch(userId, StringUtils.join(fileIdList,","),false);
        }
    }
}
```

### SQL-过期过滤条件

- 当前时间减去十天如果大于进入回收站的时间，就需要删除

```xml
<if test="query.queryExpire!=null and query.queryExpire">
    <![CDATA[  and recovery_time< date_sub(now(),interval 10 day)]]>
</if>
```

## 从磁盘彻底删除【原创】

### 业务分析

- 从数据库中删除只是删了当前用户对删除文件的引用，但是如果直接删除磁盘中的文件，会导致其它引用该文件的用户无法打开
- 因此如果要从磁盘删除，要先用md5码检查数据库中是否还有引用，完全没引用的文件才可以从磁盘删除
- 初步想法是加一张**引用计数表**
  - 包含md5、文件的物理存储路径、引用计数
  - 在上传时要对相应的md5值的计数加一
  - 从数据库删除时就对其减一
  - 通过**定时任务**扫描数据库中md5引用次数为0的文件，找到所在的路径进行删除

### 数据库搭建

<img src="easypan.assets/image-20240621135016194.png" alt="image-20240621135016194" style="zoom:80%;" /><img src="easypan.assets/image-20240621135035686.png" alt="image-20240621135035686" style="zoom:80%;" />

### 文件上传优化

- 文件上传部分需要给相应的引用文件计数加一，如果是秒传就直接+1，否则首次上传就新增计数记录

#### 文件信息业务层

- 增加文件计数方法

```java
/**
 * 增加文件计数
 * @param fileMd5        文件md5值
 * @param filePath       文件路径，首次上传需要记录文件存储路径，如果为空就说明是秒传
 */
private void addFileRef(String fileMd5,  String filePath) {
    if (filePath==null) {
        //1.秒传就直接给相应文件计数+1,采用乐观锁修改数量
        fileRefInfoService.updateFileCount(fileMd5);
    }else {
        //2.否则就是首次上传，需要新增引用记录
        fileRefInfoService.insertFileCount(fileMd5, filePath);
    }
}
```

- 上传方法优化

  - 秒传【uploadFile方法秒传部分】

  ```java
  //4.4给该文件计数+1
  addFileRef(fileMd5, null);
  ```

  - 转码成功【transferFile方法】

  ```java
  //5.转码成功
  addFileRef(fileInfo.getFileMd5(), fileInfo.getFilePath());
  ```

#### 文件引用业务层

```java
@Override
public void updateFileCount(String fileMd5) {
    baseMapper.addFileCount(fileMd5);
}
@Override
public void insertFileCount(String fileMd5, String filePath) {
    FileRefInfoEntity fileRefInfoEntity = new FileRefInfoEntity();
    fileRefInfoEntity.setFilePath(filePath);
    fileRefInfoEntity.setFileMd5(fileMd5);
    fileRefInfoEntity.setCount(1l);
    int insertNum = baseMapper.insert(fileRefInfoEntity);
    if(insertNum==0){
        //插入失败了，有可能是引用计数为0的记录还没被清理掉，也有可能是在这期间有人已经上传了
        updateFileCount(fileMd5);
    }
}
```

- dao

```xml
<update id="addFileCount">
    update file_ref_info
    set count=count+1
    where file_md5=#{fileMd5}
</update>
```

### 数据库删除文件优化

#### 文件信息业务层优化

- 根据父id批量减少计数

```java
//3.1先批量修改子文件对应的引用计数
fileRefInfoService.decreaseFileRefBatch(userId,filePidList,null,
        isAdmin ? null : FileDelFlagEnums.PARENT2RECYCLE.getFlag());
//3.2再批量删除子文件
fileInfoMapper.delFileBatch(userId, filePidList, null,
        isAdmin ? null : FileDelFlagEnums.PARENT2RECYCLE.getFlag());//超级管理员不需要过滤删除状态
```

- 根据文件id批量减少计数

```java
List<String> list = Arrays.asList(fileArray);
//4.1批量修改首层文件的引用计数
fileRefInfoService.decreaseFileRefBatch(userId,null,list,
        isAdmin ? null : FileDelFlagEnums.RECYCLE.getFlag());
//4.2批量删除首层文件
fileInfoMapper.delFileBatch(userId, null, list,
        isAdmin ? null : FileDelFlagEnums.RECYCLE.getFlag());
```

- 减少引用方法

```java
private void decreaseFileRefBatch(String userId, List<String> filePidList, List<String> fileIdList, Integer delFlag) {
    FileInfoQuery query = new FileInfoQuery();
    List<FileInfoEntity> fileInfoList;
    if (filePidList != null) {
        //1.根据父id修改子文件的引用计数
        //查询到所有的子文件
        query.setFilePidArray(filePidList.toArray(new String[0]));
        query.setUserId(userId);
        query.setDelFlag(delFlag);
        fileInfoList = fileInfoMapper.selectList(query);
    } else {
        //2.否则就是直接根据id去修改对应文件的计数
        query = new FileInfoQuery();
        query.setFileIdArray(fileIdList.toArray(new String[0]));
        query.setUserId(userId);
        query.setDelFlag(delFlag);
        fileInfoList = fileInfoMapper.selectList(query);
    }
    //3.调用文件引用的功能修改计数
    fileRefInfoService.decreaseFileRefBatch(fileInfoList);
}
```

#### 文件引用业务层方法

```java
public void decreaseFileRefBatch(List<FileInfoEntity> fileInfoList) {
    if(fileInfoList==null){
        throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
    //1.先按照md5分组
    Map<String,Long> md5Map=new HashMap<>();
    for (FileInfoEntity fileInfo : fileInfoList) {
        String fileMd5 = fileInfo.getFileMd5();
        if(!md5Map.containsKey(fileMd5)){
            md5Map.put(fileMd5,1l);
        }else{
            Long count = md5Map.get(fileMd5);
            md5Map.put(fileMd5,count+1);
        }
    }
    //2.进行批量修改
    baseMapper.decreaseFileRefBatch(md5Map);
}
```

#### dao

```xml
<update id="decreaseFileRefBatch" parameterType="java.util.Map">
    <foreach collection="md5Map" item="value" index="key" separator=";">
        UPDATE file_ref_info
        SET count = count - #{value}
        WHERE file_md5 = #{key}
    </foreach>
</update>
```

### 测试

1. 测试文件上传和秒传

   <img src="easypan.assets/image-20240621150104115.png" alt="image-20240621150104115" style="zoom: 67%;" /><img src="easypan.assets/image-20240621150114000.png" alt="image-20240621150114000" style="zoom:80%;" />

2. 测试批量删除非目录文件【之后都用这两条sql验证】

   ```sql
   SELECT SUM(count)
   FROM file_ref_info
   
   SELECT COUNT(*)
   FROM file_info
   WHERE folder_type=0
   ```

   <img src="easypan.assets/image-20240621192839072.png" alt="image-20240621192839072" style="zoom:50%;" /><img src="easypan.assets/image-20240621193004721.png" alt="image-20240621193004721" style="zoom:80%;" /><img src="easypan.assets/image-20240621193017797.png" alt="image-20240621193017797" style="zoom:80%;" />

3. 测试删除多指向文件

   <img src="easypan.assets/image-20240621193108836.png" alt="image-20240621193108836" style="zoom: 67%;" /><img src="easypan.assets/image-20240621193136397.png" alt="image-20240621193136397" style="zoom:67%;" /><img src="easypan.assets/image-20240621193147538.png" alt="image-20240621193147538" style="zoom:67%;" />

4. 测试删除全部文件，包含目录文件

   <img src="easypan.assets/image-20240621193248969.png" alt="image-20240621193248969" style="zoom:67%;" /><img src="easypan.assets/image-20240621193337878.png" alt="image-20240621193337878" style="zoom:67%;" /><img src="easypan.assets/image-20240621193354804.png" alt="image-20240621193354804" style="zoom:67%;" /><img src="easypan.assets/image-20240621193412244.png" alt="image-20240621193412244" style="zoom:67%;" />

### 定时清除磁盘垃圾文件

- 初版基于springTask

# 文件分享

## 数据库设计

<img src="easypan.assets/image-20240609144335989.png" alt="image-20240609144335989" style="zoom:80%;" />.

## 查询文件分享列表

### 控制层

```java
@RequestMapping("/loadShareList")
@GlobalInterceptor
public ResponseVO loadShareList(HttpSession session,Integer pageNo,Integer pageSize){
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    HashMap<String, Object> map = new HashMap<>();
    map.put("currPage",pageNo);
    map.put("pageSize",pageSize);
    PageUtils pageUtils = shareInfoService.loadShareList(webUserDto.getUserId(),map);
    return getSuccessResponseVO(pageUtils);
}
```

### 业务代码

- 这次直接用谷粒商城的分页工具了，瞟了一眼，返回数据和老罗那个差不多

```java
@Override
public PageUtils loadShareList(String userId, HashMap<String, Object> map) {
    //1.查询某个用户下的分享分页数据
    IPage<ShareInfoEntity> page = this.page(
            new Query<ShareInfoEntity>().getPage(map),
            new QueryWrapper<ShareInfoEntity>().eq("user_id",userId)
    );
    //2.根据分享数据中的文件id查询到文件名并返回
    List<ShareInfoEntity> shareInfoEntityList = page.getRecords();
    if(shareInfoEntityList!=null){
        page.setTotal(shareInfoEntityList.size());
    }
    if(shareInfoEntityList==null||shareInfoEntityList.isEmpty()){
        return new PageUtils(null);
    }
    //2.1先存储文件id
    List<String> fileIds=shareInfoEntityList.stream().
            map(ShareInfoEntity::getFileId).collect(Collectors.toList());
    //2.2然后批量查询
    FileInfoQuery fileInfoQuery = new FileInfoQuery();
    fileInfoQuery.setFileIdArray(fileIds.toArray(new String[]{}));
    fileInfoQuery.setUserId(userId);
    List<FileInfoEntity> fileInfoList = fileInfoMapper.selectList(fileInfoQuery);
    //2.3用map存储文件id映射
    Map<String, FileInfoEntity> fileIdMap = fileInfoList.stream().
            collect(Collectors.toMap(FileInfoEntity::getFileId, Function.identity()
            , (file1, file2) -> file2));
    //2.4封装文件名
    for (ShareInfoEntity shareInfoEntity : shareInfoEntityList) {
        FileInfoEntity fileInfo = fileIdMap.get(shareInfoEntity.getFileId());
        shareInfoEntity.setFileName(fileInfo.getFileName());
    }
    return new PageUtils(page);
}
```

### 测试

1. 报空指针异常

   ```java
   java.lang.NullPointerException: Cannot invoke "com.study.liao.entity.FileInfoEntity.getFileName()" because "fileInfo" is null
   ```

   - 原因：当文件被删除时，分享链接的引用没有同步更新
   - 解决思路：如果分享链接对应的文件不存在了，就需要删除对应的分享链接

   ```java
   //2.4封装文件名
   List<ShareInfoEntity> delShareInfoList=new ArrayList<>();//用来存储已经失效的链接
   int index=0;
   while(index<shareInfoEntityList.size()){
       ShareInfoEntity shareInfoEntity = shareInfoEntityList.get(index);
       FileInfoEntity fileInfo = fileIdMap.get(shareInfoEntity.getFileId());
       if(fileInfo==null||!FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())){
           //分享的文件已经不存在了，需要将其删除
           delShareInfoList.add(shareInfoEntity);//存入删除列表等待批量删除
           shareInfoEntityList.remove(shareInfoEntity);//返回数据中移除
           continue;
       }
       shareInfoEntity.setFileName(fileInfo.getFileName());
       index++;
   }
   //2.5批量删除失效文件
   removeBatchByIds(delShareInfoList);
   ```

## 分享文件

### 控制层

```java
/**
 * @param fileId 分享的文件id
 * @param validType 时效类型
 * @param code 分享码
 * @return
 */
@RequestMapping("/shareFile")
@GlobalInterceptor(checkParams = true)
public ResponseVO shareFile(HttpSession session, @VerifyParam(required = true) String fileId,
                            @VerifyParam(required = true) Integer validType, String code) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    shareInfoService.shareFile(webUserDto.getUserId(), fileId, validType, code);
    return getSuccessResponseVO(null);
}
```

### 业务层

```java
@Override
public void shareFile(String userId, String fileId, Integer vaildType, String code) {
    ShareInfoEntity shareInfoEntity = new ShareInfoEntity();
    //1.先检查有效时间的类别
    ShareValidTypeEnums byType = ShareValidTypeEnums.getByType(vaildType);
    if(null==byType){
        throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
    //非永久生效就要计算失效时间
    if(ShareValidTypeEnums.FOREVER!=byType){
        shareInfoEntity.setExpireTime(DateUtil.getAfterDate(byType.getDays()));
    }
    //2.封装分享信息
    Date date = new Date();
    shareInfoEntity.setShareId(StringTools.getRandomString(Constants.LENGTH_20));
    shareInfoEntity.setShareTime(date);
    shareInfoEntity.setValidType(vaildType);
    shareInfoEntity.setFileId(fileId);
    shareInfoEntity.setUserId(userId);
    if (code==null){
        //如果用户没有自定义分享码，就得自行生成
        shareInfoEntity.setCode(StringTools.getRandomString(Constants.LENGTH_5));
    }
    save(shareInfoEntity);
}
```

## 取消分享

### 控制层

```java
/**
 * 批量取消文件分享
 * @param shareIds 选中的分享记录
 */
@RequestMapping("/cancelShare")
@GlobalInterceptor(checkParams = true)
public ResponseVO cancelShare(HttpSession session, @VerifyParam(required = true) String shareIds) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    shareInfoService.cancelShareBatch(webUserDto.getUserId(),shareIds);
    return getSuccessResponseVO(null);
}
```

### 业务层

```java
@Override
public void cancelShareBatch(String userId, String shareIds) {
    //直接删除分享记录即可
    String[] shareIdArray = shareIds.split(",");
    remove(new QueryWrapper<ShareInfoEntity>()
            .eq("user_id",userId)
            .in("share_id",Arrays.asList(shareIdArray)));
}
```

## 外部分享

### 获取分享链接信息

#### 控制层

```java
/**
 * @param shareId 根据分享码获取分享文件
 * @return 
 */
@RequestMapping("getShareInfo")
@GlobalInterceptor(checkAdmin = true, checkLogin = false)
public ResponseVO getShareInfo(@VerifyParam(required = true) String shareId) {
    ShareInfoEntity shareInfo = getShareInfoCommon(shareId);
    return getSuccessResponseVO(shareInfo);
}
```

#### 获取分享信息的公共方法

```java
protected ShareInfoEntity getShareInfoCommon(String shareId){
    ShareInfoEntity shareInfo = shareInfoService.getById(shareId);
    //1.分享链接不存在或者过期
    if (null == shareInfo || (shareInfo.getExpireTime() != null && 
                              new Date().after(shareInfo.getExpireTime()))) {
        throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
    }
    //2.查询用户的分享文件信息
    FileInfoEntity fileInfo = fileInfoService.getFileInfoByFileIdAndUserId(shareInfo.getFileId(), shareInfo.getUserId());
    if (fileInfo == null || !FileDelFlagEnums.USING.getFlag().equals(fileInfo.getDelFlag())) {
        //分享的文件被删除
        throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
    }
    shareInfo.setFileName(fileInfo.getFileName());
    //3.查询分享人信息
    UserInfoEntity userInfo = userInfoService.getById(shareInfo.getUserId());
    shareInfo.setNickName(userInfo.getNickName());
    return shareInfo;
}
```

### 获取分享人信息

#### 封装基本分享信息

```java
@Data
public class SessionShareDto {
    private String shareId;//分享链接标识
    private String shareUserId;//分享人id
    private Date expireTime;//分享链接失效时间
    private String fileId;//分享文件id
}
```

#### 获取基本分享信息

```java
protected SessionShareDto getSessionShareDto(HttpSession session,String shareId){
    return (SessionShareDto) session.getAttribute(Constants.SESSION_SHARE_KEY);
}
```

#### 控制层

```java
/**
 * @param session 需要校验当前登录用户是否为分享用户
 * @param shareId 根据分享码获取分享基本信息
 * @return
 */
@RequestMapping("/getShareLoginInfo")
@GlobalInterceptor(checkAdmin = true,checkLogin = false)
public ResponseVO getShareLoginInfo(HttpSession session, @VerifyParam(required = true) String shareId) {
    SessionShareDto sessionShareDto = getSessionShareDto(session, shareId);
    //如果session中获取不到分享信息，说明是未登录的游客打开的分享链接
    if (sessionShareDto == null) {
        return getSuccessResponseVO(null);
    }
    //1.获取基本分享信息【包括分享文件id、分享人id、分享码等】
    ShareInfoEntity shareInfo = getShareInfoCommon(shareId);
    //2.获取当前用户信息，并且判断分享人是否就是当前用户
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    if (webUserDto != null && webUserDto.getUserId().equals(shareInfo.getUserId())) {
        shareInfo.setCurrentUser(true);
    } else {
        shareInfo.setCurrentUser(false);
    }
    return getSuccessResponseVO(shareInfo);
}
```

### 校验提取码

#### 控制层

```java
@RequestMapping("/checkShareCode")
@GlobalInterceptor(checkAdmin = true, checkLogin = false)
public ResponseVO checkShareCode(HttpSession session,
                                 @VerifyParam(required = true)String shareId,
                                 @VerifyParam(required = true)String code){
    SessionShareDto shareDto=shareInfoService.checkShareCode(shareId,code);
    session.setAttribute(Constants.SESSION_SHARE_KEY+shareId,shareDto);
    return getSuccessResponseVO(null);
}
```

#### 业务层

```java
@Override
public SessionShareDto checkShareCode(String shareId, String code) {
    //1.获取分享记录
    ShareInfoEntity shareInfo = getById(shareId);
    if(null==shareInfo||(shareInfo.getExpireTime()!=null&&new Date().after(shareInfo.getExpireTime()))){
        throw new BusinessException(ResponseCodeEnum.CODE_902.getMsg());
    }
    //2.验证码错误
    if(!code.equals(shareInfo.getCode())){
        throw new BusinessException("提取码错误!!!");
    }
    //3.验证码正确，给分享次数+1,通过数据库层面的乐观锁修改分享次数，而不是先查询再修改
    shareInfoMapper.updateShareShowCount(shareId);
    SessionShareDto shareDto=new SessionShareDto();
    shareDto.setShareId(shareId);
    shareDto.setShareUserId(shareInfo.getUserId());
    shareDto.setFileId(shareInfo.getFileId());
    shareDto.setExpireTime(shareInfo.getExpireTime());
    return shareDto;
}
```

#### DAO层

```xml
<update id="updateShareShowCount">
    update file_share
    set show_count=show_count+1
    where share_id=#{shareId}
</update>
```

### ！！！获取分享文件列表

#### 控制层

```java
/**
 * 获取分享文件列表
 */
@RequestMapping("/loadFileList")
@GlobalInterceptor(checkParams = true,checkLogin = false)
public ResponseVO loadFileList(HttpSession session,
                               @VerifyParam(required = true) String shareId,
                               String filePid) {
    //1.先校验基本分享信息
    SessionShareDto shareDto = checkShareInfo(session, shareId);
    //2.防止分享越权，不能访问同级其它文件或者上级文件
    FileInfoQuery query = new FileInfoQuery();
    if(!StringTools.isEmpty(filePid)&&!Constants.ZERO_STR.equals(filePid)){
        //校验当前访问的文件是否在分享根目录下
        fileInfoService.checkRootFilePid(filePid,shareDto.getShareUserId(),shareDto.getFileId());
        query.setFilePid(filePid);
    }else {
        //说明分享的只是一个文件或者当前在系统根目录
        query.setFileId(shareDto.getFileId());
    }
    //3.查询分享文件信息
    query.setUserId(shareDto.getShareUserId());
    query.setOrderBy("last_update_time desc");
    query.setDelFlag(FileDelFlagEnums.USING.getFlag());
    PaginationResultVO<FileInfoEntity> listByPage = fileInfoService.findListByPage(query);
    return getSuccessResponseVO(listByPage);
}
```

#### 越权校验

- 当前访问的文件不能在分享根目录之外，所以需要从当前访问位置向上找父目录，看看是否能找到分享根目录

```java
/**
 * 校验当前访问的文件是否在分享根目录下
 * @param rootFilePid 分享根目录id
 * @param shareUserId 分享人id
 * @param fileId 当前浏览的文件id
 */
@Override
public void checkRootFilePid(String rootFilePid, String shareUserId, String fileId) {
    if(StringTools.isEmpty(fileId)){
        throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
    //当前访问的就是分享根目录
    if(rootFilePid.equals(fileId)){
        return;
    }
    //从当前文件向上递归查询，看看是否在分享根目录中
    checkFilePid(rootFilePid,fileId,shareUserId);
}
/**
 * 递归向上找到目标根目录，如果不存在说明当前访问的文件不在分享根目录中
 * @param rootFilePid 分享根目录
 * @param fileId 当前层级的文件id
 * @param userId 用户id
 */
private void checkFilePid(String rootFilePid,String fileId,String userId){
    //1.查询当前目录信息
    FileInfoEntity fileInfo = fileInfoMapper.selectByFileIdAndUserId(fileId, userId);
    if(fileInfo==null||Constants.ZERO_STR.equals(fileInfo.getFileId())){
        //如果目录不存在或者是系统根目录【分享不可能传系统根目录】，那就抛异常
        throw new BusinessException(ResponseCodeEnum.CODE_600);
    }
    //2.递归出口，找到目标根目录
    if(fileInfo.getFileId().equals(rootFilePid)){
        return;
    }
    //3.找上一级目录
    checkFilePid(rootFilePid,fileInfo.getFilePid(),userId);
}
```

### 保存到我的网盘

#### 控制层

- 这一步肯定是要做登录校验的

```java
/**
 * 保存选中的分享文件到我的网盘
 * @param shareId 分享链接id
 * @param shareFileIds 选中的多个分享文件id
 * @param myFolderId 保存到的目标目录id
 */
@RequestMapping("/saveShare")
@GlobalInterceptor(checkParams = true)
public ResponseVO cancelShare(HttpSession session,
                              @VerifyParam(required = true)String shareId,
                              @VerifyParam(required = true) String shareFileIds,
                              @VerifyParam(required = true)String myFolderId) {
    SessionShareDto shareDto = checkShareInfo(session, shareId);//获取分享信息
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    if(webUserDto.getUserId().equals(shareDto.getShareUserId())){
        //自己的文件保存到自己的网盘没必要
        throw new BusinessException("这是你分享的文件！！");
    }
    fileInfoService.saveShare(shareDto.getFileId(),shareFileIds,
            myFolderId,shareDto.getShareUserId(),webUserDto.getUserId());
    return getSuccessResponseVO(null);
}
```

#### 业务层

##### 主骨架

```java
/**
 * @param shareRootPid  分享根目录id
 * @param shareFileIds  选中的分享文件id
 * @param myFolderId    保存的目标目录id
 * @param shareUserId   分享人id
 * @param currentUserId 当前用户id
 */
@Override
public void saveShare(String shareRootPid, String shareFileIds, String myFolderId,
                      String shareUserId, String currentUserId) {
    //1.查询目标文件列表
    FileInfoQuery query = new FileInfoQuery();
    query.setUserId(currentUserId);
    query.setFilePid(myFolderId);
    List<FileInfoEntity> currentFileList = fileInfoMapper.selectList(query);
    Map<String, FileInfoEntity> currentFileMap = currentFileList.stream().
            collect(Collectors.toMap(FileInfoEntity::getFileName,
                    Function.identity(), (file1, file2) -> file2));
    //2.查询选择的分享文件列表
    String[] shareFileArray = shareFileIds.split(",");
    query = new FileInfoQuery();
    query.setUserId(shareUserId);
    query.setFileIdArray(shareFileArray);
    List<FileInfoEntity> shareFileList = fileInfoMapper.selectList(query);
    //3.递归复制文件结构，并且对同名文件进行重命名
    ArrayList<FileInfoEntity> copyFileList = new ArrayList<>();
    Date date = new Date();
    for (FileInfoEntity shareFile : shareFileList) {
        FileInfoEntity currentFile = currentFileMap.get(shareFile.getFileName());
        if (currentFile != null) {
            //重名了就重命名
            shareFile.setFileName(StringTools.rename(shareFile.getFileName()));
        }
        //递归复制目录内容
        copyAllSubFile(copyFileList, shareFile, shareUserId, currentUserId, myFolderId, date);
    }
    fileInfoMapper.insertBatch(copyFileList);
}
```

##### 递归复制文件结构

```java
/**
 * 递归复制文件信息
 * @param copyFileList  存放复制的文件实体
 * @param fileInfo      当前操作的源文件
 * @param sourceUserId  源文件用户id
 * @param currentUserId 当前用户id
 * @param newFilePid    新的存放目录
 */
private void copyAllSubFile(List<FileInfoEntity> copyFileList, FileInfoEntity fileInfo, String sourceUserId
        , String currentUserId, String newFilePid, Date date) {
    //1.获取原文件id，下方生成新id会覆盖fileInfo
    String sourceFileId = fileInfo.getFileId();
    //2.填充新数据
    String newFileId = StringTools.getRandomString(Constants.LENGTH_10);
    fileInfo.setCreateTime(date);
    fileInfo.setLastUpdateTime(date);
    fileInfo.setFilePid(newFilePid);
    fileInfo.setUserId(currentUserId);
    fileInfo.setFileId(newFileId);
    copyFileList.add(fileInfo);
    //3.如果是目录就递归复制
    if (FileFolderTypeEnums.FOLDER.getType().equals(fileInfo.getFolderType())) {
        FileInfoQuery query = new FileInfoQuery();
        query.setFileId(sourceFileId);//用原文件id查询
        query.setUserId(sourceUserId);
        List<FileInfoEntity> sourceFileList = fileInfoMapper.selectList(query);
        for (FileInfoEntity sourceFile : sourceFileList) {
            copyAllSubFile(copyFileList, sourceFile, sourceUserId, currentUserId, newFilePid, date);
        }
    }
}
```

##### 优化【todo】

- 保存到我的网盘的时候需要对空间大小进行校验

### 其它操作

#### 获取目录导航栏

```java
@RequestMapping("/getFolderInfo")
@GlobalInterceptor(checkParams = true,checkLogin = false)
public ResponseVO getFolderInfo(HttpSession session,
                                @VerifyParam(required = true)String shareId,
                                @VerifyParam(required = true) String path) {
    SessionShareDto shareDto = getSessionShareDto(session, shareId);
    return super.getFolderInfo(path, shareDto.getShareUserId());
}
```

#### 文件预览

- 普通文件

```java
/**
 * 根据预览某个分享记录下的某个文件
 */
@RequestMapping("/getFile/{shareId}/{fileId}")
@GlobalInterceptor(checkParams = true, checkAdmin = true)
public void getFile(HttpServletResponse response,
                    HttpSession session,
                    @PathVariable("fileId") String fileId,
                    @PathVariable("shareId") String shareId) {
    SessionShareDto shareDto = getSessionShareDto(session, shareId);
    super.getFile(response, fileId, shareDto.getShareUserId());
}
```

- 视频文件

```java
@GlobalInterceptor(checkParams = true,checkLogin = false)
public void getVideoInfo(HttpServletResponse response, HttpSession session,
                         @PathVariable("shareId")String shareId,
                         @PathVariable("fileId") String fileId) {
    SessionShareDto shareDto = getSessionShareDto(session, shareId);
    super.getFile(response, fileId, shareDto.getShareUserId());
}
```

#### 创建下载链接

```java
@RequestMapping("createDownloadUrl/{shareId}/{fileId}")
@GlobalInterceptor(checkParams = true,checkLogin = false)
public ResponseVO createDownloadUrl(
        HttpSession session,
        @PathVariable(required = true) String fileId
        , @PathVariable(required = true) String shareId) {
    SessionShareDto shareDto = getSessionShareDto(session, shareId);
    return super.createDownloadUrl(fileId, shareDto.getShareUserId());
}
```

## 测试

1. 创建分享链接后，点击复制链接及提取码的按钮失效，并且前端控制台报**shareId为null**的错误

   <img src="easypan.assets/image-20240611161743058.png" alt="image-20240611161743058" style="zoom: 67%;" /><img src="easypan.assets/image-20240611161755860.png" alt="image-20240611161755860" style="zoom: 67%;" />

   - 看了一下接口文档，后端需要返回分享实体数据

     <img src="easypan.assets/image-20240611162057340.png" alt="image-20240611162057340" style="zoom:67%;" />.

   - 那就将分享信息返回出去

   ```java
   public ResponseVO shareFile(HttpSession session, @VerifyParam(required = true) String fileId,
                               @VerifyParam(required = true) Integer validType, String code) {
       SessionWebUserDto webUserDto = getUserInfoFromSession(session);
       ShareInfoEntity shareInfoEntity=shareInfoService.
           shareFile(webUserDto.getUserId(), fileId, validType, code);
       return getSuccessResponseVO(shareInfoEntity);
   }
   ```

   <img src="easypan.assets/image-20240611162353038.png" alt="image-20240611162353038" style="zoom:50%;" />.
   
2. 外部分享输入正确分享码之后，前端没有跳转到新页面

   - 原因是从session中获取分享文件基本信息的键错了【低级错误】`session.getAttribute(Constants.SESSION_SHARE_KEY);`
   - 改为`(SessionShareDto) session.getAttribute(Constants.SESSION_SHARE_KEY+shareId);`
   - 同时外部分享相关方法不需要登录校验

3. 游客访问分享链接被登录拦截

   - 原因：外部分享相关方法加了checkAdmin的注解，导致要拦截【低级错误】
   - 解决：只用做参数校验，登录校验和管理员相关的一律不做

4. 获取视频文件缩略图被登录拦截

   - 原因：前端那个畜生又偷懒，访问的还是文件信息控制层的接口方法<img src="easypan.assets/image-20240612155745887.png" alt="image-20240612155745887" style="zoom:67%;" />
   - 解决：文件信息控制层中的获取缩略图方法不做用户校验

5. 自定义提取码没有封装成功<img src="easypan.assets/image-20240612160716129.png" alt="image-20240612160716129" style="zoom:80%;" />

   - 原因：只做了自动生成提取码的封装，没有做自定义提取码封装

     ```java
     if (code==null){
         //如果用户没有自定义分享码，就得自行生成
         shareInfoEntity.setCode(StringTools.getRandomString(Constants.LENGTH_5));
     }
     ```

   - 解决代码如下

     ```java
     if (code==null){
         //如果用户没有自定义分享码，就得自行生成
         code=StringTools.getRandomString(Constants.LENGTH_5);
     }
     shareInfoEntity.setCode(code);
     ```

# 管理员

## 环境搭建

1. 首先配置文件中需要配置管理员的邮箱号`admin.emails=2018003549@qq.com`
2. 然后SessionWebUserDto中的isAdmin参数改为admin

<img src="easypan.assets/image-20240609155745186.png" alt="image-20240609155745186" style="zoom:67%;" />.

## 系统信息相关

### 获取系统信息

```java
/**
 * 获取系统信息
 */
@RequestMapping("/getSysSettings")
@GlobalInterceptor(checkParams = true,checkAdmin = true)
public ResponseVO getSysSettings(){
    return getSuccessResponseVO(redisComponent.getSysSettingsDto());
}
```

- 测试

  - 缺失邮箱标题

  <img src="easypan.assets/image-20240609161242275.png" alt="image-20240609161242275" style="zoom:67%;" />.

  - 原因是系统字段名写错了，`registerMailTitle`改为`registerEmailTitle`

  <img src="easypan.assets/image-20240609161409449.png" alt="image-20240609161409449" style="zoom: 67%;" />.

### 更新系统信息

- 控制层

```java
@RequestMapping("/saveSysSettings")
@GlobalInterceptor(checkParams = true, checkAdmin = true)
public ResponseVO saveSysSettings(
        @VerifyParam(required = true) String registerEmailTitle,
        @VerifyParam(required = true) String registerEmailContent,
        @VerifyParam(required = true) Integer userInitUseSpace) {
    SysSettingsDto sysSettingsDto = new SysSettingsDto();
    sysSettingsDto.setRegisterEmailTitle(registerEmailTitle);
    sysSettingsDto.setRegisterEmailContent(registerEmailContent);
    sysSettingsDto.setUserInitUseSpace(userInitUseSpace);
    redisComponent.saveSysSettings(sysSettingsDto);
    return getSuccessResponseVO(redisComponent.getSysSettingsDto());
}
```

- redis保存系统参数方法

```java
public void saveSysSettings(SysSettingsDto sysSettingsDto) {
    redisUtils.set(Constants.REDIS_KEY_SYS_SETTING,sysSettingsDto);
}
```

## 用户信息相关

### 获取用户信息

- 控制层：接收用户信息和分页参数

```java
/**
 * 查询出所有用户信息
 * @param userInfoQuery 用户信息筛选条件加分页参数
 * @return 用户信息列表
 */
@RequestMapping("/loadUserList")
@GlobalInterceptor(checkParams = true, checkAdmin = true)
public ResponseVO loadUserList(UserInfoQuery userInfoQuery) {
    PageUtils pageUtils = userInfoService.loadUserList(userInfoQuery);
    return getSuccessResponseVO(pageUtils);
}
```

- 业务层

```java
@Override
public PageUtils loadUserList(UserInfoQuery userInfoQuery) {
    HashMap<String,Object>params=new HashMap<>();
    params.put("pageSize",userInfoQuery.getPageNo());
    params.put("currPage",userInfoQuery.getPageNo());
    IPage<UserInfoEntity> page = this.page(
            new Query<UserInfoEntity>().getPage(params),
            new QueryWrapper<UserInfoEntity>()
    );
    List<UserInfoEntity> records = page.getRecords();
    if(records!=null){
        page.setTotal(records.size());
    }
    return new PageUtils(page);
}
```

### 更新用户状态

#### 初版

- 老罗这一块禁用的用户会把所有存储的文件都删了，我个人感觉没必要做这一步

- 控制层

```java
/**
 * 修改用户的状态
 * @param userId 需要修改的用户
 * @param status 新状态
 */
@RequestMapping("/updateUserStatus")
@GlobalInterceptor(checkParams = true, checkAdmin = true)
public ResponseVO updateUserStatus(@VerifyParam(required = true)String userId,
                                   @VerifyParam(required = true)Integer status) {
    userInfoService.updateUserStatus(userId,status);
    return getSuccessResponseVO(null);
}
```

- 业务逻辑

```java
@Override
public void updateUserStatus(String userId, Integer status) {
    //1.先查询出修改的用户是否为管理员
    UserInfoEntity userInfo = getById(userId);
    if(userInfo==null){
        throw new BusinessException("用户不存在!!!");
    }
    if(ArrayUtils.contains(appConfig.getAdminUserName().split(","),userInfo.getEmail())){
        throw new BusinessException("无法修改管理员的状态!!!");
    }
    //2.更新用户状态
    userInfo.setStatus(status);
    updateById(userInfo);
}
```

#### 测试

- 禁用是禁用了，但是对已经登录的用户没有什么限制，依旧可以访问文件信息

<img src="easypan.assets/image-20240609180256081.png" alt="image-20240609180256081" style="zoom:80%;" /><img src="easypan.assets/image-20240609180309733.png" alt="image-20240609180309733" style="zoom:67%;" />

- 得让已登录的用户强制下线
  1. 用户登录时，在redis中存储用户状态
  2. 禁用用户时，把redis中的用户的登录状态也设置为禁用
  3. 在登录拦截校验中获取redis中的登录状态，如果为禁用状态就直接强制下线

#### 优化

1. 添加新的常量作为redis中存储用户状态的前缀

   ```java
   public static final String REDIS_USER_STATUS="easypan:user:status:";
   ```

2. 登录方法存储用户状态到redis【对应用户信息实现类的Login方法】

   ```java
   //5.redis中存储用户登录状态
   redisComponent.saveUserStatus(userId,userInfoEntity.getStatus().intValue());
   ```

   - redisComponent的用户状态相关方法

   ```java
   public void saveUserStatus(String userId, int status) {
       redisUtils.setex(Constants.REDIS_USER_STATUS+userId,status,Constants.REDIS_KEY_EXPIRES_ONE_HOUR);
   }
   public Integer getUserStatus(String userId, int status) {
       return (Integer) redisUtils.get(Constants.REDIS_USER_STATUS+userId);
   }
   ```

3. 登录校验拦截中从redis获取指定用户的登录状态【对应切面类的checkLogin方法】

   - 如果不存在就得重新去数据库中查
   - 如果用户登录状态为禁用，直接强制下线【抛异常+session下线】

   ```java
   String userId = userDto.getUserId();
   Integer userStatus = redisComponent.getUserStatus(userId);
   if(userStatus==null){
       //缓存过期就需要重新从数据库中查
       UserInfoEntity userInfo = userInfoService.getById(userId);
       userStatus=userInfo.getStatus();
   }
   if(UserStatusEnum.DISABLE.getStatus().equals(userStatus)){
       //禁用就强制下线
       session.invalidate();
       throw new BusinessException("你已被禁用!!!");
   }
   redisComponent.saveUserStatus(userId,userStatus);
   ```

4. 管理员修改用户状态方法需要同步更新redis【对应用户信息实现类的updateUserStatus方法】

   ```java
   //3.修改缓存中的用户状态
   redisComponent.saveUserStatus(userId,status);
   ```

### 给用户分配空间

- 控制层

```java
@RequestMapping("/updateUserSpace")
@GlobalInterceptor(checkParams = true, checkAdmin = true)
public ResponseVO updateUserSpace(@VerifyParam(required = true)String userId,
                                   @VerifyParam(required = true)Integer changeSpace) {
    userInfoService.updateUserSpace(userId,changeSpace);
    return getSuccessResponseVO(null);
}
```

- 业务层

```java
@Override
public void updateUserSpace(String userId, Integer changeSpace) {
    //1.数据库中更新用户总空间信息
    Long space=changeSpace*Constants.MB;
    userInfoDao.updateUseSpace(userId,null,space);
    //2.缓存相应也更新
    redisComponent.saveUserTotalSpace(userId,space);
}
```

- redisComponent

```java
public void saveUserTotalSpace(String userId,Long totalSpace) {
    UserSpaceDto userSpaceDto = new UserSpaceDto();
    Long useSpace = fileInfoMapper.selectUseSpace(userId);
    userSpaceDto.setUseSpace(useSpace);
    userSpaceDto.setTotalSpace(totalSpace);
    redisUtils.setex(Constants.REDIS_KEY_USER_SPACE_USE+userId,
                     userSpaceDto,Constants.REDIS_KEY_EXPIRES_DAY);
}
```

## 文件信息相关

### 获取所有用户的文件

#### 控制层

```java
/**
 * 查询所有文件
 * @param fileQuery 文件信息筛选条件加分页参数
 * @return 文件信息列表
 */
@RequestMapping("/loadFileList")
@GlobalInterceptor(checkParams = true, checkAdmin = true)
    public ResponseVO loadFileList(FileInfoQuery fileQuery) {
    fileQuery.setOrderBy("last_update_time desc");
    fileQuery.setQueryNickName(true);
    PaginationResultVO<FileInfo> listByPage = fileInfoService.findListByPage(fileQuery);
    return getSuccessResponseVO(listByPage);
}
```

#### 测试

1. 文件夹大小为null

   <img src="easypan.assets/image-20240610205644626.png" alt="image-20240610205644626" style="zoom:80%;" /><img src="easypan.assets/image-20240610205804604.png" alt="image-20240610205804604" style="zoom:80%;" />

   - 在上传或者是删除文件时，如果该文件所在目录不是根目录，就需要更新对应的父目录大小【todo】

### 获取文件目录【导航栏】

#### 控制层【废弃，原因见测试】

```java
/**
 * 获取文件目录导航栏
 */
@RequestMapping("/getFolderInfo")
@GlobalInterceptor(checkParams = true, checkAdmin = true)
public ResponseVO getFolderInfo(@VerifyParam(required = true)String path) {
    return getSuccessResponseVO(super.getFolderInfo(path,null));
}
```

#### 测试

1. 当预览其它用户的文件时，不会显示导航栏<img src="easypan.assets/image-20240610215931155.png" alt="image-20240610215931155" style="zoom:67%;" />

   - 原因：前端那个懒逼，压根就没调用管理员的获取文件目录方法，而是直接调用原先接口<img src="easypan.assets/image-20240610220218226.png" alt="image-20240610220218226" style="zoom:80%;" />

     

   - 解决：在原先接口多加一个管理员状态判断，并且废弃上述接口<img src="easypan.assets/image-20240610220403105.png" alt="image-20240610220403105" style="zoom:67%;" />

   ```java
   @RequestMapping("/getFolderInfo")
   @GlobalInterceptor(checkParams = true)
   public ResponseVO getFolderInfo(HttpSession session, @VerifyParam(required = true) String path) {
       SessionWebUserDto webUserDto = getUserInfoFromSession(session);
       String userId = webUserDto.getUserId();
       if(webUserDto.getAdmin()){
           userId=null;//如果是管理员，那就全查，不用筛选用户信息
       }
       return super.getFolderInfo(path, userId);
   }
   ```

### 用户文件预览

```java
/**
 * 预览某个用户的某个文件
 */
@RequestMapping("/getFile/{userId}/{fileId}")
@GlobalInterceptor(checkParams = true,checkAdmin = true)
public void getFile(HttpServletResponse response, 
                    @PathVariable("fileId") String fileId,
                    @PathVariable("userId")String userId) {
    super.getFile(response, fileId, userId);
}
```

### 用户文件下载

- 创建下载链接

```java
/**
 * 创建某个用户的某个文件的下载链接
 * @param fileId 需要下载的文件id
 * @param userId 选中的用户id
 * @return 临时下载码
 */
@RequestMapping("createDownloadUrl/{userId}/{fileId}")
@GlobalInterceptor(checkParams = true)
public ResponseVO createDownloadUrl(
        @PathVariable(required = true) String fileId
        , @PathVariable(required = true) String userId) {
    return super.createDownloadUrl(fileId, userId);
}
```

- 根据下载码下载文件信息直接复用文件信息控制层的方法即可，因为该方法不需要进行用户校验，只用校验下载码

### 用户文件批量删除

- 请求参数fileIdAndUserIds形如userId_fileId,userId_fileId...
  - 每个逗号分隔每条独立的文件记录
  - 一条文件记录需要由用户id和文件id唯一标识

<img src="easypan.assets/image-20240611143303356.png" alt="image-20240611143303356" style="zoom:80%;" />.

```java
/**
 * 删除用户文件
 * @param fileIdAndUserIds 形如 userId_fileId,userId_fileId...
 */
@RequestMapping("delFile")
@GlobalInterceptor(checkParams = true)
public ResponseVO delFile(@VerifyParam(required = true)String fileIdAndUserIds){
    //1.分隔出文件记录【每条记录包含用户id_文件id】
    String[] fileIdAndUserIdArray = fileIdAndUserIds.split(",");
    //2.封装每个用户的待删文件
    HashMap<String, List<String>> userFileIdsMap=new HashMap<>();//key为用户id，value为要删除的文件列表
    for (String fileIdAndUserId : fileIdAndUserIdArray) {
        String[] fileInfoItem = fileIdAndUserId.split("_");
        String userId=fileInfoItem[0];
        String fileId=fileInfoItem[1];
        if(!userFileIdsMap.containsKey(userId)){
            userFileIdsMap.put(userId,new ArrayList<>());
        }
        userFileIdsMap.get(userId).add(fileId);
    }
    //3.对每个用户的待删文件进行批量删除
    for (Map.Entry<String, List<String>> userFileIdsInfo : userFileIdsMap.entrySet()) {
        String userId = userFileIdsInfo.getKey();
        List<String> fileIdList = userFileIdsInfo.getValue();
        String fileIds = StringUtils.join(fileIdList, ",");
        fileInfoService.delFileBatch(userId,fileIds,true);
    }
    return getSuccessResponseVO(null);
}
```

### 测试

1. 文件下载时前端报404错误

   <img src="easypan.assets/image-20240611154629613.png" alt="image-20240611154629613" style="zoom:67%;" />.

   - 前面提到在管理员下载用户文件时执行的是**文件信息控制层的下载方法**，但是这一块前端又请求一个新接口，那就只好在管理员控制层中重写一个

   ```java
   @RequestMapping("/download/{code}")
   @GlobalInterceptor(checkParams = true, checkLogin = false)//不需要校验登录
   public void download(HttpServletRequest request, HttpServletResponse response,
                        @VerifyParam(required = true) @PathVariable("code") String code) 
       throws UnsupportedEncodingException {
       super.download(request, response, code);
   }
   ```

# 完结撒花

![image-20240612184305224](easypan.assets/image-20240612184305224.png)

# 拓展【原创】

## 环境搭建

### 技术栈采用onlyOffice，感谢黄靖淏

### onlyOffice部署

1. docker部署参照[【保姆级教程】OnlyOffice文档编辑器本地私有化部署与远程使用_onlyoffice私有化部署-CSDN博客](https://blog.csdn.net/qq_25749749/article/details/141433251)

   ![image-20250122101135277](easypan.assets/image-20250122101135277.png)

2. 参照界面提示把样例打开，并且设置自启动

![image-20250122101042326](easypan.assets/image-20250122101042326.png)

3. 样例效果

   ![image-20250122101226719](easypan.assets/image-20250122101226719.png)

4. 测试上传接口

   <img src="easypan.assets/image-20250122101316359.png" alt="image-20250122101316359" style="zoom: 80%;" /><img src="easypan.assets/image-20250122101349264.png" alt="image-20250122101349264" style="zoom:80%;" />

   - 特殊说明
     - 上传txt文件时，如果内容过小【0字节】会报文件大小异常，而且txt默认展示形式是word
     - excel文件支持传4mb【再大就没测过】，对于收集员工/学生信息绰绰有余

   <img src="easypan.assets/image-20250122101803497.png" alt="image-20250122101803497" style="zoom:67%;" /><img src="easypan.assets/image-20250122102016437.png" alt="image-20250122102016437" style="zoom: 60%;" />

5. 测试下载接口

   ![image-20250122102527622](easypan.assets/image-20250122102527622.png)

### EasyExcel整合

1. 导入依赖

   ```xml
   <dependency>
       <groupId>com.alibaba</groupId>
       <artifactId>easyexcel</artifactId>
       <version>3.3.4</version>
   </dependency>
   ```

2. 解析本地文件

   ```java
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
   ```

![image-20250207143812800](easypan.assets/image-20250207143812800.png)

### 数据库设计

- 在线文档编辑的文件要和普通文件区分开，而且需要指明创建人和访问权限组，这样子才能实现基于用户权限的合法访问，因此至少需要创建两张表——在线编辑文件表和访问权限表

#### 在线编辑文件表

- 字段设计
  - 基础字段：id、文件名、创建人、创建和更新时间、逻辑删除状态
  - 文件物理路径【在线编辑的文件是基于上传的文件生成的副本，所以要单独存在系统指定路径】
  - 状态【0-不可编辑 1-可编辑】
  - ...

![image-20250122103812272](easypan.assets/image-20250122103812272.png)

#### 访问权限表

- 在线文件编辑表不需要直接存储用户权限，在访问权限表加online_file_id来标明是哪个文件的权限规则

- 字段设计

  - 基础字段：id、逻辑删除
  - 关联的在线编辑文件id
  - 关联的用户id
  - 权限规则【用`,`分隔不同权限，1-可读 2-可写 3-可下载 】，embedded模式只能读，恰好可以控制用户的编辑权限

  ![image-20250122105654914](easypan.assets/image-20250122105654914.png).

![image-20250122112128351](easypan.assets/image-20250122112128351.png)

#### 用户组+组内人员表

- 为了方便文档上传人分配文档权限给内部人员，所以要保证上传人和待授权人在一个组里【类似于企业的概念】
- 用户组表字段设计：id、组名、逻辑删除、人数上限【之后管理员可以限制最大人数】、创建人id、创建时间

<img src="easypan.assets/image-20250204140444630.png" alt="image-20250204140444630" style="zoom:80%;" />.

- 组内人员表字段设计：id、逻辑删除、用户id、所在用户组id、加入时间

<img src="easypan.assets/image-20250203140550910.png" alt="image-20250203140550910" style="zoom:80%;" />.

- 变更：组内人员表新增审批字段，审批通过才能正式入组

<img src="easypan.assets/image-20250205141723954.png" alt="image-20250205141723954" style="zoom:80%;" />

#### 统计规则表

- 先把核心规则列一下
  - 总和、平均数、众数、中位数、计数
  - 数值区间【指定连续的数值，检查填写结果缺填了哪些值，例如检查学号211511-211565这个区间哪些人没填】
  - 数据集【类似于数值区间，检查填写结果缺了哪些数据集元素】
- 字段设计
  - id、逻辑删除、规则名、关联的在线文档id
  - 规则类型【按上面列的核心规则顺序来排】
  - 生效的列号、数据范围【用于设置数值区间和数据集的范围】

![image-20250207150928801](easypan.assets/image-20250207150928801.png)

- 2025-2-8字段修改：文件id改用下划线命名，数据范围改成data_range【因为range是mysql一个关键字】

![image-20250208140514284](easypan.assets/image-20250208140514284.png)

- 2025-2-8字段删除：一个字段只能配置一种规则，所以规则名称意义不大，删掉

#### 定时任务表

- 字段设计
  - 当前字段：id、待统计的文件id、开始收集时间、是否统计过、通知类型
  - 后续考虑优化：重复次数，重复间隔

![image-20250219171222225](easypan.assets/image-20250219171222225.png)

### ~~xxl-job部署~~【不能创建指定时间的工作流，必须去控制台创建，和我想的不太一样】

#### docker部署

- 参考博客：[Docker部署Xxl-Job分布式任务调度中心(超详细)-CSDN博客](https://blog.csdn.net/qq_35716689/article/details/135712797)

1. 创建xxl-job的数据库表【博客里面有sql】
2. 拉取并启动镜像
   - privileged=true 给予容器Root权限
   - -v 目录挂载   ：左边为宿主机目录，右边为容器内目录
   - --xxl.job.accessToken=pingzhuyan.test  这行配置指定accessToken，在程序中引入xxl-job时，需要用到accessToken
   - --spring.datasource.username=root xxl-job 数据库登录账户
   - --spring.datasource.password=pzy123 数据库登录密码
   - --spring.datasource.url Sql数据库的url

```properties
docker run -di -e PARAMS="--spring.datasource.url=jdbc:mysql://192.168.32.100:3306/xxl_job?Unicode=true&characterEncoding=UTF-8 --spring.datasource.username=root --spring.datasource.password=1212go12 --xxl.job.accessToken=liaoPan" \
-p 9001:8080 \
-v /usr/local/src/docker/xxl-job:/data/applogs \
--name xxl-job \
--privileged=true \
xuxueli/xxl-job-admin:2.4.0
```

3. 登入管理界面【默认账密：admin 123456】

![image-20250218160328306](easypan.assets/image-20250218160328306.png)

#### 后端整合

- 参考博客：[XXL-JOB安装及使用教程（保姆级教程）-CSDN博客](https://blog.csdn.net/chang_mao/article/details/135954996)

1. 导入依赖

```xml
<!-- xxl job -->
<dependency>
    <groupId>com.xuxueli</groupId>
    <artifactId>xxl-job-core</artifactId>
    <version>2.4.0</version>
</dependency>
```

2. 配置文件

```yaml
xxl:
  job:
    executor:
      appname: ${spring.application.name}
      logpath: ${spring.application.name}/xxl-job
      logretentiondays: 30
    admin:
      addresses: http://192.168.32.100:9001/xxl-job-admin  #配置xxl-job的公共访问地址
    accessToken: liaoPan
```

3. 创建配置类

```java
@Slf4j
@Configuration
public class XxlJobConfig {
    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;
    @Value("${xxl.job.accessToken}")
    private String accessToken;
    @Value("${xxl.job.executor.appname}")
    private String appname;
    @Value("${server.port}")
    private int port;
    @Value("${xxl.job.executor.logpath}")
    private String logPath;
    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setPort(port + 10000);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }
}
```

4. 创建执行器

<img src="easypan.assets/image-20250218161349186.png" alt="image-20250218161349186" style="zoom: 67%;" />.

5. 启动服务，成功注册打印日志如下，并且自动注册执行器

```java
025-02-18 16:51:04 [INFO][com.xxl.job.core.server.EmbedServer][run][82]-> >>>>>>>>>>> xxl-job remoting server start success, nettype = class com.xxl.job.core.server.EmbedServer, port = 17090
2025-02-18 16:51:04 [DEBUG][com.xxl.job.core.thread.ExecutorRegistryThread][run][51]-> >>>>>>>>>>> xxl-job registry success, registryParam:RegistryParam{registryGroup='EXECUTOR', registryKey='liao-pan', registryValue='http://192.168.32.1:17090/'}, registryResult:ReturnT [code=200, msg=null, content=null]
2025-02-18 16:51:04 [INFO][com.zaxxer.hikari.pool.HikariPool][checkFailFast][565]-> HikariCPDatasource - Added connection com.mysql.cj.jdbc.ConnectionImpl@71161e55
2025-02-18 16:
```

![image-20250218165339030](easypan.assets/image-20250218165339030.png)

### 整合钉钉webhook

1. 导入依赖

```xml
<!--        整合钉钉的webhook-->
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>alibaba-dingtalk-service-sdk</artifactId>
    <version>2.0.0</version>
</dependency>

<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
    <version>1.11</version>
</dependency>
```

2. 抽取消息发送工具类【钉钉官方文档有提供】

```java
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import org.apache.commons.codec.binary.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
public class WebhookUtil {
    public static final String CUSTOM_ROBOT_TOKEN = 
        "b3b5542e359eb1d4a103b42cab047dbdea4da9b975778d2e06d2466f03270597";
    public static final String USER_ID= "";
    public static final String SECRET = 
        "SECf0ec07af459f679fe3ff44214f564fad88f8342892c7bf0bfc8bd8f28d029c13";
    public static void sendDingDingMessage(List<String> contentList) {
        try {
            Long timestamp = System.currentTimeMillis();
            System.out.println(timestamp);
            String secret = SECRET;
            String stringToSign = timestamp + "\n" + secret;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)),"UTF-8");
            System.out.println(sign);
            //sign字段和timestamp字段必须拼接到请求URL上，否则会出现 310000 的错误信息
            DingTalkClient client = new DefaultDingTalkClient(
                "https://oapi.dingtalk.com/robot/send?sign="+sign+"&timestamp="+timestamp);
            OapiRobotSendRequest req = new OapiRobotSendRequest();
            /**
             * 发送文本消息
             */
            //定义文本内容
            OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("【测试消息】:");//关键字
            for (String content : contentList) {
                stringBuilder.append(content);
                stringBuilder.append("\n");
            }
            text.setContent(stringBuilder.toString());
            //定义 @ 对象
            OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
            at.setAtUserIds(Arrays.asList(USER_ID));
            //设置消息类型
            req.setMsgtype("text");
            req.setText(text);
            req.setAt(at);
            OapiRobotSendResponse rsp = client.execute(req, CUSTOM_ROBOT_TOKEN);
            System.out.println(rsp.getBody());
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
```

3. 测试通过

<img src="easypan.assets/image-20250220094309059.png" alt="image-20250220094309059" style="zoom:80%;" />.

## 在线文档编辑【后端】

### OnlyOffice交互

#### 上传文件到编辑器

##### 控制层

- 这一块先做简单一点，暂时不考虑分片上传，先采用全量上传的方式

```java
/**
 * 上传文件到文档编辑器
 * @param session 用于获取上传人
 * @param fileName 原文件名
 * @param file    上传文件流
 */
@PostMapping("/uploadFile")
@GlobalInterceptor(checkParams = true)
public ResponseVO uploadFile(HttpSession session, MultipartFile file,
                             @VerifyParam(required = true) String fileName) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    return getSuccessResponseVO(onlineEditService.uploadFile(webUserDto, file, fileName));
}
```

##### 业务层

```java
public Boolean uploadFile(SessionWebUserDto webUserDto, MultipartFile file, String fileName) {
    //1.上传到onlyOffice
    String json = HttpUtils.uploadFile(onlyOfficePath + "upload", file);
    log.info("上传结果===" + json);
    JSONObject jsonObject = JSON.parseObject(json);
    String storePath = jsonObject.getString("filename");
    if(StringUtils.isBlank(storePath)){
        log.error("没有返回文件名信息");
        throw new BusinessException("上传失败");
    }
    //2.保存文件信息
    OnlineFileInfoEntity fileInfoEntity = new OnlineFileInfoEntity();
    //刚上传成功不给编辑，等创建人发布在设置为1
    fileInfoEntity.setStatus(0);
    fileInfoEntity.setFilename(fileName);
    fileInfoEntity.setCreateById(webUserDto.getUserId());
    fileInfoEntity.setIsDeleted(0);
    fileInfoEntity.setStorageAddress(storePath);
    Date date = new Date();
    fileInfoEntity.setCreateTime(date);
    fileInfoEntity.setUpdateTime(date);
    onlineFileInfoDao.insert(fileInfoEntity);
    return true;
}
```

#### http工具类新增上传方法

- 其中构造参数中上传文件流的参数名要和onlyOffice的上传参数保持一致

```java
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
    ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, 
                                                                  requestEntity, String.class);
    // 返回响应结果
    return responseEntity.getBody();
}
```

#### 从编辑器下载文件

##### 控制层

- 之后要做用户权限校验就得先从request中获取用户信息

```java
@GetMapping("/download/{fileId}")
public void download(HttpServletRequest request, HttpServletResponse response,
                     @PathVariable(required = true) String fileId) {
    onlineEditService.download(request,response,fileId);
}
```

##### 业务层

- 目前是未鉴权下载版本

```java
public void download(HttpServletRequest request, HttpServletResponse response, String fileId) {
    //1.先校验文件id并且获取文件在onlyOffice的存储地址
    OnlineFileInfoEntity fileInfoEntity = onlineFileInfoDao.selectById(fileId);
    if (Objects.isNull(fileInfoEntity)) {
        throw new BusinessException("下载的文件不存在");
    }
    String storageAddress = fileInfoEntity.getStorageAddress();
    String filename = fileInfoEntity.getFilename();
    //2.通过存储地址去onlyOffice下载文件
    HashMap<String, String> params = new HashMap<>();
    params.put("fileName", storageAddress);
    byte[] downloadFile = null;
    try {
        downloadFile = HttpUtils.sendGetForBinary(onlyOfficePath + "download", params);
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {
            //IE浏览
            filename = URLEncoder.encode(filename, "UTF-8");
        } else {
            filename = new String(filename.getBytes("UTF-8"), "ISO8859-1");
        }
    } catch (IOException e) {
        log.error("远程下载失败");
        throw new RuntimeException(e);
    }
    response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
    FileUtils.readFile(response, downloadFile, filename);
}
```

### 用户组管理

#### 新建用户组

##### 控制层

- 接受当前用户登录信息、表单各个字段

```java
@PostMapping("/insertUserGroup")
public ResponseVO insertUserGroup(HttpSession session, UserGroupInfoEntity userGroupInfoEntity) {
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    return getSuccessResponseVO(onlineEditService.insertUserGroup(webUserDto.getUserId(), userGroupInfoEntity));
}
```

##### 初版

- 先做最简单的新增操作，不考虑任何限制

```java
public Boolean insertUserGroup(String userId, UserGroupInfoEntity userGroupInfoEntity) {
    String groupName = userGroupInfoEntity.getGroupName();
    Integer maxSize = userGroupInfoEntity.getMaxSize();
    String description = userGroupInfoEntity.getDescription();
    if (StringUtils.isBlank(userId)) {
        throw new BusinessException("创建人信息有误");
    }
    if (StringUtils.isBlank(groupName)) {
        throw new BusinessException("未填写组名");
    }
    if (StringUtils.isBlank(description)) {
        throw new BusinessException("未填写描述信息");
    }
    if (maxSize == null || maxSize <= 0) {
        throw new BusinessException("最大人数有误");
    }
    userGroupInfoEntity.setCreateById(userId);
    userGroupInfoEntity.setCreateTime(new Date());
    return userGroupInfoDao.insert(userGroupInfoEntity)>0;
}
```

<img src="easypan.assets/image-20250204142734585.png" alt="image-20250204142734585" style="zoom:67%;" />.

##### 优化：创建完默认加入该组

- 创建成功也往该用户组关联详情中插入一条数据，便于之后查询列表

```java
//2.然后创建人默认作为第一个用户加入该组
UserGroupDetailInfoEntity groupDetail = new UserGroupDetailInfoEntity();
groupDetail.setGroupId(userGroupInfoEntity.getId());
groupDetail.setUserId(userId);
groupDetail.setJoinTime(createTime);
return userGroupDetailInfoDao.insert(groupDetail) > 0;
```

#### 查询用户组列表

##### 控制层

- 目前不考虑搜索功能，所以请求参数只用传当前用户登录标识即可

```java
@GetMapping("/userGroupList")
public ResponseVO<List<UserGroupDetailDTO>> selectUserGroupList(HttpSession session){
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    return getSuccessResponseVO(onlineEditService.selectUserGroupList(webUserDto.getUserId()));
}
```

- 返回信息需要额外携带当前加入人数和创建人昵称

```java
public class UserGroupDetailDTO {
    /**
     * 用户组id
     */
    private Long groupId;
    /**
     * 用户组名，也可以作为企业名
     */
    private String groupName;
    /**
     * 人数上限
     */
    private Long maxSize;
    /**
     * 创建人名称
     */
    private String createByName;
    /**
     * 创建人id
     */
    private String createById;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 加入时间
     */
    private Date joinTime;
    /**
     * 简介
     */
    private String description;
    /**
     * 当前加入人数
     */
    private Long currentSize;
}
```

##### 业务层

- 调用dao接口获取用户组列表，根据用户组id去查询加入的用户人数

```java
public List<UserGroupDetailDTO> selectUserGroupList(String userId) {
    //1.先查询用户组列表详情
    List<UserGroupDetailDTO> groupDetailList = userGroupDetailInfoDao.selectUserGroupListByUserId(userId);
    Set<String> createByIdList=new HashSet<>();
    for (UserGroupDetailDTO groupDetailInfo : groupDetailList) {
        createByIdList.add(groupDetailInfo.getCreateById());
    }
    //2.设置创建人名称
    //2.1.用创建人id列表去查询创建人名称
    List<UserInfoEntity> userInfoList = userInfoService.listByIds(createByIdList);
    Map<String, String> userMap = userInfoList.stream()
            .collect(Collectors.toMap(
                    UserInfoEntity::getUserId,
                    UserInfoEntity::getNickName
            ));
    //2.2.设置创建人名称
    for (UserGroupDetailDTO groupDetailInfo : groupDetailList) {
        String createById = groupDetailInfo.getCreateById();
        if (userMap.containsKey(createById)) {
            groupDetailInfo.setCreateByName(userMap.get(createById));
        }
    }
    //3.设置每个用户组的当前人数
    for (UserGroupDetailDTO groupDetailInfo : groupDetailList) {
        Long groupId = groupDetailInfo.getGroupId();
        LambdaQueryWrapper<UserGroupDetailInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserGroupDetailInfoEntity::getGroupId,groupId);
        Long count = userGroupDetailInfoDao.selectCount(queryWrapper);
        groupDetailInfo.setCurrentSize(count);
    }
    return groupDetailList;
}
```

##### DAO层

- 关联用户组表获取用户组信息

```xml
<resultMap type="com.study.liao.entity.dto.UserGroupDetailDTO" id="userGroupDetailDTOMap">
    <result property="groupName" column="group_name"/>
    <result property="groupId" column="group_id"/>
    <result property="createById" column="create_by_id"/>
    <result property="maxSize" column="max_size"/>
    <result property="currentSize" column="currentSize"/>
    <result property="joinTime" column="join_time"/>
    <result property="createTime" column="create_time"/>
</resultMap>
<select id="selectUserGroupListByUserId" resultMap="userGroupDetailDTOMap">
    SELECT ugroup.group_name,
           ugroup.create_by_id,
           ugroup.max_size,
           ugroup.create_time,
           detail.join_time,
           detail.group_id
    FROM user_group_detail_info detail
             JOIN user_group_info ugroup ON ugroup.id = detail.group_id
        AND ugroup.is_deleted = 0
    WHERE detail.is_deleted = 0
      and user_id = #{userId}
    GROUP BY ugroup.id
</select>
```

##### 接口测试

<img src="easypan.assets/image-20250204161630676.png" alt="image-20250204161630676" style="zoom: 67%;" />.

##### 2025-2-4变更

- 为了和前端其它列表接口返回接口保持一致，并且复用前端列表组件，所以返回结果多套一层

```java
public PaginationResultVO selectUserGroupList(String userId) {
    //...
    PaginationResultVO resultVO = new PaginationResultVO();
    resultVO.setList(groupDetailList);
    return resultVO;
}
```

- 返回结果如下

```json
{
    "status": "success",
    "code": 200,
    "info": "请求成功",
    "data": {
        "totalCount": null,
        "pageSize": null,
        "pageNo": null,
        "pageTotal": null,
        "list": [
            {
                "groupId": 1786798084,
                "groupName": "21",
                "maxSize": 12,
                "createByName": "里奥",
                "createById": "4564622893",
                "createTime": "2025-02-04T06:56:50.000+00:00",
                "joinTime": "2025-02-04T06:56:50.000+00:00",
                "description": "12",
                "currentSize": 2
            }
        ]
    }
}
```

#### 用户组列表检索

##### 控制层

```java
@GetMapping("/userGroupList")
@GlobalInterceptor(checkParams = true)
public ResponseVO<List<UserGroupDetailDTO>> selectUserGroupList(HttpSession session, UserGroupQuery query){
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    query.setUserId(webUserDto.getUserId());
    return getSuccessResponseVO(onlineEditService.selectUserGroupList(query));
}
```

- 请求参数

```java
public class UserGroupQuery extends BaseParam {
    /**
     * 检索关键字，可以模糊匹配用户组id和组名
     */
    private String keyword;
    /**
     * 查询类型 0-查询当前用户已申请的 1-查询所有用户组
     */
    private Integer type;
    /**
     * 审批状态 0-待审批 1-已通过 2-已拒绝
     */
    private Integer approvalStatus;
    /**
     * 当前用户id
     */
    private String userId;
    /**
     * 是否是自己创建的
     */
    private Boolean isCreated;
}
```

- 返回结果新增

```java
/**
 * 审批状态 0-待审批 1-已通过 2-已拒绝
 */
private Integer approvalStatus;
/**
 * 是否已加入
 */
private Boolean isActive;
```

##### 业务

- 抽取用户组详情公共方法

```java
private void setUserGroupDetail(String userId,List<UserGroupDetailDTO> resultList){
    //2.设置创建人名称
    Set<String> createByIdList = new HashSet<>();
    for (UserGroupDetailDTO groupDetailInfo : resultList) {
        createByIdList.add(groupDetailInfo.getCreateById());
    }
    //2.1.用创建人id列表去查询创建人名称
    List<UserInfoEntity> userInfoList = userInfoService.listByIds(createByIdList);
    Map<String, String> userMap = userInfoList.stream()
            .collect(Collectors.toMap(
                    UserInfoEntity::getUserId,
                    UserInfoEntity::getNickName
            ));
    //2.2.设置创建人名称以及是否是自己创建的
    for (UserGroupDetailDTO groupDetailInfo : resultList) {
        String createById = groupDetailInfo.getCreateById();
        if (userMap.containsKey(createById)) {
            groupDetailInfo.setCreateByName(userMap.get(createById));
        }
        if (userId != null) {
            groupDetailInfo.setIsCreated(userId.equals(createById));
        }
    }
    //3.设置每个用户组的当前人数，即审批通过的人数
    for (UserGroupDetailDTO groupDetailInfo : resultList) {
        Long groupId = groupDetailInfo.getGroupId();
        LambdaQueryWrapper<UserGroupDetailInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserGroupDetailInfoEntity::getGroupId, groupId)
                .eq(UserGroupDetailInfoEntity::getApprovalStatus, UserGroupStatusEnum.APPROVED.getCode());
        Long count = userGroupDetailInfoDao.selectCount(queryWrapper);
        groupDetailInfo.setCurrentSize(count);
    }
}
```

- 查询当前已申请的用户组

```java
public PaginationResultVO selectCurrentUserGroupList(UserGroupQuery query) {
    //1.查询当前用户的所有已申请的用户组
    UserGroupQuery currentQuery = new UserGroupQuery();
    currentQuery.setUserId(query.getUserId());
    currentQuery.setGroupName(query.getGroupName());
    List<UserGroupDetailDTO> resultList = 
        userGroupDetailInfoDao.selectCurrentUserGroupDetailList(currentQuery);
    //2.设置用户组详情
    setUserGroupDetail(query.getUserId(),resultList);
    PaginationResultVO resultVO = new PaginationResultVO();
    resultVO.setList(resultList);
    return resultVO;
}
```

- 根据条件搜索用户组

```java
public PaginationResultVO selectUserGroupList(UserGroupQuery query) {
    //1.根据条件查询用户组列表
    List<UserGroupDetailDTO> resultList = userGroupDetailInfoDao.selectUserGroupListByQuery(query);
    //2.设置用户组详情
    setUserGroupDetail(query.getUserId(), resultList);
    //3.设置是否已申请
    //3.1查询当前用户申请的所有用户组
    UserGroupQuery currentQuery = new UserGroupQuery();
    currentQuery.setUserId(query.getUserId());
    currentQuery.setGroupName(query.getGroupName());
    List<UserGroupDetailDTO> currentUserGroupList = 
        userGroupDetailInfoDao.selectCurrentUserGroupDetailList(currentQuery);
    Set<Long> currentSet = new HashSet<>();
    for (UserGroupDetailDTO groupDetailInfo : currentUserGroupList) {
        currentSet.add(groupDetailInfo.getGroupId());
    }
    //3.2已申请的用户组就设置标识
    for (UserGroupDetailDTO groupDetailInfo : resultList) {
        Long groupId = groupDetailInfo.getGroupId();
        groupDetailInfo.setIsActive(currentSet.contains(groupId));
    }
    PaginationResultVO resultVO = new PaginationResultVO();
    resultVO.setList(resultList);
    return resultVO;
}
```

##### DAO层

- selectCurrentUserGroupDetailList用于搜索当前用户申请的用户组
- selectUserGroupListByQuery根据检索条件查询所有用户组

```xml
<select id="selectCurrentUserGroupDetailList" resultMap="userGroupDetailDTOMap">
    SELECT ugroup.group_name,
    ugroup.create_by_id,
    ugroup.max_size,
    ugroup.create_time,
    ugroup.description,
    detail.join_time,
    detail.group_id,
    detail.approval_status
    FROM user_group_detail_info detail
    JOIN user_group_info ugroup ON ugroup.id = detail.group_id
    AND ugroup.is_deleted = 0
    WHERE detail.is_deleted = 0
    <if test="query.userId != null">
        and user_id = #{query.userId}
    </if>
    <if test="query.groupName !=null and query.groupName!=''">
        and ugroup.group_name like concat('%', #{query.groupName}, '%')
    </if>
    <if test="query.approvalStatus != null">
        and approval_status = #{query.approvalStatus}
    </if>
</select>
<select id="selectUserGroupListByQuery" resultMap="userGroupDetailDTOMap">
    SELECT ugroup.group_name,
           ugroup.create_by_id,
           ugroup.max_size,
           ugroup.create_time,
           ugroup.description,
           ugroup.id as group_id
    FROM  user_group_info ugroup
    WHERE ugroup.is_deleted = 0
    <if test="query.keyword !=null and query.keyword!=''">
        and (ugroup.group_id like concat('%', #{query.keyword}, '%')
        or ugroup.group_name like concat('%', #{query.keyword}, '%'))
    </if>
</select>
```

##### 关联业务

- 新建用户组时创建人的审批状态设置为已通过`insertUserGroup`

```java
groupDetail.setApprovalStatus(UserGroupStatusEnum.APPROVED.getCode());
```

#### 加入用户组

##### 控制台

```java
@PostMapping("/joinGroup/{groupId}")
@GlobalInterceptor(checkParams = true)
public ResponseVO joinGroup(HttpSession session, @PathVariable("groupId")Integer groupId){
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    return getSuccessResponseVO(onlineEditService.joinGroup(webUserDto.getUserId(),groupId));
}
```

##### 业务层

- 就是简单的插入数据

```java
public Boolean joinGroup(String userId, Integer groupId) {
    UserGroupDetailInfoEntity groupDetail = new UserGroupDetailInfoEntity();
    groupDetail.setGroupId(groupId);
    groupDetail.setUserId(userId);
    groupDetail.setJoinTime(new Date());
    groupDetail.setApprovalStatus(UserGroupStatusEnum.PENDING.getCode());
    return userGroupDetailInfoDao.insert(groupDetail) > 0;
}
```

#### 查询待审批用户

##### 控制层

```java
@GetMapping("/pending/{groupId}")
@GlobalInterceptor(checkParams = true)
public ResponseVO<List<UserInfoEntity>> selectPendingUserList(@PathVariable("groupId")Integer groupId){
    return getSuccessResponseVO(onlineEditService.selectPendingUserList(groupId));
}
```

##### 业务层

```java
public List<UserInfoEntity> selectPendingUserList(Integer groupId) {
    //1.查询当前用户组出待审批的记录
    LambdaQueryWrapper<UserGroupDetailInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserGroupDetailInfoEntity::getGroupId, groupId)
            .eq(UserGroupDetailInfoEntity::getIsDeleted, 0)
            .eq(UserGroupDetailInfoEntity::getApprovalStatus,UserGroupStatusEnum.PENDING.getCode());
    List<UserGroupDetailInfoEntity> groupDetailList = userGroupDetailInfoDao.selectList(queryWrapper);
    //2.收集待审批的用户id
    if(CollectionUtils.isEmpty(groupDetailList)){
        return null;
    }
    List<String> userIds = groupDetailList.stream()
            .map(UserGroupDetailInfoEntity::getUserId)  // 提取每个对象的userId
            .collect(Collectors.toList());  // 收集为List
    //3.查询用户信息
    List<UserInfoEntity> userList = userInfoService.listByIds(userIds);
    return userList;
}
```

##### 接口测试

<img src="easypan.assets/image-20250206100954649.png" alt="image-20250206100954649" style="zoom:80%;" />.

#### 审批操作

##### 控制层

```java
@PostMapping("/approval")
@GlobalInterceptor(checkParams = true)
public ResponseVO approval(@RequestBody ApprovalDTO approvalDTO) {
    return getSuccessResponseVO(onlineEditService.approval(approvalDTO));
}
public class ApprovalDTO {
    String userId;
    Integer groupId;
    Boolean isPassed;
}
```

##### 业务层

```java
/**
 * 修改指定的申请记录的审批状态
 */
public Boolean approval(ApprovalDTO approvalDTO) {
    Integer groupId = approvalDTO.getGroupId();
    String userId = approvalDTO.getUserId();
    LambdaQueryWrapper<UserGroupDetailInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(UserGroupDetailInfoEntity::getGroupId, groupId)
            .eq(UserGroupDetailInfoEntity::getUserId, userId)
            .eq(UserGroupDetailInfoEntity::getIsDeleted, 0);
    UserGroupDetailInfoEntity updateData = new UserGroupDetailInfoEntity();
    updateData.setApprovalStatus(approvalDTO.getIsPassed() ?
            UserGroupStatusEnum.APPROVED.getCode() :
            UserGroupStatusEnum.REJECTED.getCode());
    return userGroupDetailInfoDao.update(updateData, queryWrapper) > 0;
}
```

##### 接口测试

<img src="easypan.assets/image-20250206140531508.png" alt="image-20250206140531508" style="zoom: 80%;" />.

### 文档管理

#### 查询上传的文档列表

##### 控制层

```java
/**
 * 查询当前用户上传的在线编辑文档信息
 */
@GetMapping("/uploadFileList")
@GlobalInterceptor(checkParams = true)
public ResponseVO<List<OnlineFileInfoEntity>> selectUploadFileList(HttpSession session){
    SessionWebUserDto webUserDto = getUserInfoFromSession(session);
    return getSuccessResponseVO(onlineEditService.selectUploadFileList(webUserDto.getUserId()));
}
```

##### 业务层

```java
public PaginationResultVO selectUploadFileList(String userId) {
    LambdaQueryWrapper<OnlineFileInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(OnlineFileInfoEntity::getCreateById, userId)
            .eq(OnlineFileInfoEntity::getIsDeleted, 0);
    PaginationResultVO resultVO = new PaginationResultVO<>();
    List<OnlineFileInfoEntity> resultList = onlineFileInfoDao.selectList(queryWrapper);
    resultVO.setList(resultList);
    return resultVO;
}
```

#### 配置用户规则

##### 2025-3-3功能影响

- OnlineEditController的接口过多，所以把配置用户规则放在StatisticalRulesController中，原控制层路径colRules改为rules

##### 控制层

- 枚举类

```java
public enum UserFilePermissionsEnum {
    READ(1, "read", "可读"),
    WRITE(2, "write", "可写"),
    DOWNLOAD(3, "download", "可下载");
    Integer code;
    String value;
    String desc;
    public static Integer getCodeByValue(String value) {
        for (UserFilePermissionsEnum rule : UserFilePermissionsEnum.values()) {
            if (rule.getValue().equals(value)) {
                return rule.getCode();
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
```

### ！！！Excel聚合统计

#### 规则类型

```java
public enum ColRuleEnum {
    SUM(0,"总和"),
    AVG(1,"平均数"),
    MODE(2,"众数"),
    MEAN(3,"中位数"),
    COUNT(4,"计数"),
    RANGE(5,"数值区间"),
    SET(6,"数据集");
    private Integer code;
    private String desc;
}
```

#### 将在线编辑的Excel文件下载到本地工作区

##### 下载Excel文件

- DownloadFileDto新增文件流属性，便于服务间传递

```java
public class DownloadFileDto {
    private String downloadCode;
    private String fileName;
    private String filePath;
    /**
     * 文件流，用于服务间传递
     */
    private byte[] downloadFile;
}
```

- 修改onlyOffice的下载方法，并声明返回文件对象方法，供本地工作区操作解析

```java
@Override
public void download(HttpServletRequest request, HttpServletResponse response, String fileId) {
    DownloadFileDto downloadFileDto = this.download(fileId);
    String filename = downloadFileDto.getFileName();
    try {
        if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {
            //IE浏览
            filename = URLEncoder.encode(filename, "UTF-8");
        } else {
            filename = new String(filename.getBytes("UTF-8"), "ISO8859-1");
        }
    } catch (UnsupportedEncodingException e) {
        log.error("文件名转码异常", e);
        throw new RuntimeException(e);
    }
    response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
    FileUtils.readFile(response, downloadFileDto.getDownloadFile(), filename);
}
@Override
public DownloadFileDto download(String fileId) {
    //1.先校验文件id并且获取文件在onlyOffice的存储地址
    OnlineFileInfoEntity fileInfoEntity = onlineFileInfoDao.selectById(fileId);
    if (Objects.isNull(fileInfoEntity)) {
        throw new BusinessException("下载的文件不存在");
    }
    String storageAddress = fileInfoEntity.getStorageAddress();
    String filename = fileInfoEntity.getFilename();
    //2.通过存储地址去onlyOffice下载文件
    HashMap<String, String> params = new HashMap<>();
    params.put("fileName", storageAddress);
    byte[] downloadFile = null;
    try {
        downloadFile = HttpUtils.sendGetForBinary(onlyOfficePath + "download", params);
    } catch (IOException e) {
        log.error("远程下载失败");
        throw new RuntimeException(e);
    }
    DownloadFileDto downloadFileDto = new DownloadFileDto();
    downloadFileDto.setDownloadFile(downloadFile);
    downloadFileDto.setFileName(filename);
    return downloadFileDto;
}
```

##### 解析Excel文件

- 控制层

```java
/**
 * @param fileId 在线编辑的excel文件id
 * 根据配置的规则解析在线编辑的excel文件
 */
@GetMapping("/processExcelColumnsWithRules/{fileId}")
@GlobalInterceptor(checkParams = true)
public ResponseVO  processExcelColumnsWithRules(@RequestParam("fileId")String fileId){
    return getSuccessResponseVO(statisticalRulesService.processExcelColumnsWithRules(fileId));
}
```

- 业务层

```java
public List<String> processExcelColumnsWithRules(String fileId) {
    //1.从onlyOffice下载文件到工作区
    DownloadFileDto downloadFileDto = onlineEditService.download(fileId);
    //2.解析excel数据
    InputStream downloadFile = new ByteArrayInputStream(downloadFileDto.getDownloadFile());
    EasyExcel.read(downloadFile, new AnalysisEventListener<Map<Integer, String>>() {
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
    return null;
}
```

- 单元测试【解析csv会报错，那就限制excel的类型】

<img src="easypan.assets/image-20250208101014845.png" alt="image-20250208101014845" style="zoom:67%;" />.

#### ！！！规则统计

#### 控制层

```java
public List<String> processExcelColumnsWithRules(String fileId) {
    //1.从onlyOffice下载文件到工作区
    DownloadFileDto downloadFileDto = onlineEditService.download(fileId);
    String fileName = downloadFileDto.getFileName();
    if (!fileName.contains(".xlsx")) {
        throw new BusinessException("非xlsx格式的文件无法解析");
    }
    //2.获取该文件绑定的规则
    LambdaQueryWrapper<StatisticalRulesEntity> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(StatisticalRulesEntity::getFileId, fileId)
            .eq(StatisticalRulesEntity::getIsDeleted, 0);
    List<StatisticalRulesEntity> ruleList = this.baseMapper.selectList(queryWrapper);
    if (CollectionUtils.isEmpty(ruleList)) {
        return null;
    }
    //按列号给规则分组,暂时不考虑一列配置多个规则
    HashMap<Integer, StatisticalRulesEntity> ruleMap = new HashMap<>();
    for (StatisticalRulesEntity entity : ruleList) {
        ruleMap.put(entity.getColIndex(), entity);
    }
    //3.解析excel数据
    InputStream downloadFile = new ByteArrayInputStream(downloadFileDto.getDownloadFile());
    List<String> resultList = new ArrayList<>();
    EasyExcel.read(downloadFile, new AnalysisEventListener<Map<Integer, String>>() {
        // 存储表头数据，key为列号，value为列名
        Map<Integer, String> headerNameMap = new HashMap<>();
        // 用于保存行记录，外层key为列号，内层key为行号，value为单元格值
        Map<Integer, Map<Integer, String>> rowDataMap = new HashMap<>();

        @Override
        public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
            // 读取到每一行时的回调
            //获取当前行号
            Integer rowIndex = context.readRowHolder().getRowIndex();
            for (Map.Entry<Integer, String> entry : rowData.entrySet()) {
                Integer colIndex = entry.getKey();
                String value = entry.getValue();
                if (!rowDataMap.containsKey(colIndex)) {
                    rowDataMap.put(colIndex, new HashMap<>());
                }
                Map<Integer, String> cellData = rowDataMap.get(colIndex);
                cellData.put(rowIndex, value);
            }
        }
        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            headerNameMap = headMap;
        }

        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
            // 解析完成后的回调
            for (Map.Entry<Integer, StatisticalRulesEntity> ruleEntry : ruleMap.entrySet()) {
                Integer colIndex = ruleEntry.getKey();
                StatisticalRulesEntity rule = ruleEntry.getValue();
                Map<Integer, String> cellData = rowDataMap.get(colIndex);
                ColRuleEnum type = ColRuleEnum.getEnumByCode(rule.getType());
                String result = calculateColumnMetricsByRule(type, cellData.values(), rule.getDataRange());
                String headerName = headerNameMap.get(colIndex);
                resultList.add(headerName + result);
            }
        }
    }).sheet().doRead();  // 读取第一个 sheet 中的数据
    return resultList;
}
/**
 * 根据类型解析指定列的所有元素
 * @param type     规则类型
 * @param cellData 指定列的所有元素
 * @return
 */
private String calculateColumnMetricsByRule(ColRuleEnum type, Collection<String> cellData, String range) {
    StringBuffer result = new StringBuffer();//用于拼接统计结果
    Double sum = 0d;//用于计算总和
    int size = cellData.size();//元素个数
    Map<String, Integer> countMap = new HashMap<>();//统计每个元素的计数
    switch (type) {
        case SUM:
            // 计算总和
            result.append("总和为");
            for (String value : cellData) {
                try {
                    double num = Double.parseDouble(value);
                    sum += num;
                } catch (Exception e) {
                    log.error("数据:" + value + " 无法转换成数字");
                }
            }
            result.append(sum);
            return result.toString();
        case AVG:
            // 计算平均数
            result.append("平均数为");
            if (size == 0) {
                result.append("不存在");
                return result.toString();
            }
            for (String value : cellData) {
                try {
                    double num = Double.parseDouble(value);
                    sum += num;
                } catch (Exception e) {
                    log.error("数据:" + value + " 无法转换成数字");
                }
            }
            result.append(sum / size);
            return result.toString();
        case MODE:
            // 计算众数
            result.append("众数为");
            if (size == 0) {
                result.append("不存在");
                return result.toString();
            }
            Integer maxCount = 0;//用于存储最大的数量
            for (String value : cellData) {
                if (value == null) {
                    continue;
                }
                if (!countMap.containsKey(value)) {
                    countMap.put(value, 0);
                }
                Integer count = countMap.get(value) + 1;
                countMap.put(value, count);
                maxCount = maxCount < count ? count : maxCount;
            }
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                if (maxCount.equals(entry.getValue())) {
                    result.append(entry.getKey());
                    result.append("、");
                }
            }
            return result.substring(0, result.length() - 1).toString();
        case MEAN:
            // 计算中位数
            result.append("中位数为 ");
            if (size == 0) {
                result.append("不存在");
                return result.toString();
            }
            List<Double> numList = new ArrayList<>();
            for (String value : cellData) {
                try {
                    double num = Double.parseDouble(value);
                    numList.add(num);
                } catch (Exception e) {
                    log.error("数据:" + value + " 无法转换成数字");
                }
            }
            Collections.sort(numList);
            if (size % 2 == 0) {
                result.append(numList.get(size / 2 - 1) + numList.get(size / 2));
            } else {
                result.append(numList.get(size / 2));
            }
            return result.toString();
        case COUNT:
            //统计每个元素的计数
            result.append("每个元素的计数分别为");
            if (size == 0) {
                result.append("不存在");
                return result.toString();
            }
            for (String value : cellData) {
                if (!countMap.containsKey(value)) {
                    countMap.put(value, 0);
                }
                Integer count = countMap.get(value) + 1;
                countMap.put(value, count);
            }
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                result.append(entry.getKey());
                result.append(":");
                result.append(entry.getValue());
                result.append("、");
            }
            return result.substring(0, result.length() - 1).toString();
        case RANGE:
            // 计算数值区间,检查区间内哪些元素不存在
            result.append("未填写的数据为 ");
            String[] numRange = range.split("\\-");
            Long minNum = Long.parseLong(numRange[0]);
            Long maxNum = Long.parseLong(numRange[1]);
            HashSet<Long> numSet = new HashSet<>();
            for (long i = minNum; i <= maxNum; i++) {
                numSet.add(i);
            }
            for (String value : cellData) {
                try {
                    long num = Long.parseLong(value);
                    if (numSet.contains(num)) {
                        numSet.remove(num);
                    }
                } catch (Exception e) {
                    log.error("数据:" + value + " 无法转换成数字");
                }
            }
            if (numSet.isEmpty()) {
                result.append("不存在");
                return result.toString();
            }
            for (Long num : numSet) {
                result.append(num);
                result.append("、");
            }
            return result.substring(0, result.length() - 1).toString();
        case SET:
            // 计算数据集
            result.append("未填写的数据为 ");
            String[] dataRange = range.split("\\,");
            HashSet<String> dataSet = new HashSet<>();
            for (String data : dataRange) {
                dataSet.add(data);
            }
            for (String value : cellData) {
                if (dataSet.contains(value)) {
                    dataSet.remove(value);
                }
            }
            if (dataSet.isEmpty()) {
                result.append("不存在");
                return result.toString();
            }
            for (String value : dataSet) {
                result.append(value);
                result.append("、");
            }
            return result.substring(0, result.length() - 1).toString();
    }
    return null;
}
```

#### 获取Excel文件配置规则

##### 控制层

```java
/**
 * 获取Excel表的列信息
 * @param fileId 在线编辑的excel文件id
 */
@GetMapping("/getExcelMetaData/{fileId}")
@GlobalInterceptor(checkParams = true)
public ResponseVO<List<StatisticalRulesEntity>>  getExcelMetaData(@RequestParam("fileId")String fileId){
    return getSuccessResponseVO(statisticalRulesService.getExcelMetaData(fileId));
}
```

- StatisticalRulesEntity新增`字段名`字段

```java
/**
 * 字段名
 */
@TableField(exist = false)
private String colName;
```

##### 业务层

1. 配置统计规则前得让用户知道哪些列可以配，所以要先获取元数据
2. 然后将配置好的规则绑定到指定的元数据

```java
public List<StatisticalRulesEntity> getExcelMetaData(String fileId) {
    //1.从onlyOffice下载文件到工作区
    DownloadFileDto downloadFileDto = onlineEditService.download(fileId);
    String fileName = downloadFileDto.getFileName();
    if (!fileName.contains(".xlsx")) {
        throw new BusinessException("非xlsx格式的文件无法解析");
    }
    //2.获取已配置的规则
    LambdaQueryWrapper<StatisticalRulesEntity> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(StatisticalRulesEntity::getFileId, fileId)
            .eq(StatisticalRulesEntity::getIsDeleted, 0);
    List<StatisticalRulesEntity> ruleList = this.baseMapper.selectList(queryWrapper);
    HashMap<Integer, StatisticalRulesEntity> ruleMap = new HashMap<>();
    for (StatisticalRulesEntity rulesEntity : ruleList) {
        ruleMap.put(rulesEntity.getColIndex(), rulesEntity);
    }
    //2.解析excel数据
    InputStream downloadFile = new ByteArrayInputStream(downloadFileDto.getDownloadFile());
    // 存储表头数据，key为列号，value为列名
    List<StatisticalRulesEntity> columnDataDTOList = new ArrayList<>();
    EasyExcel.read(downloadFile, new AnalysisEventListener<Map<Integer, String>>() {
        @Override
        public void invoke(Map<Integer, String> rowData, AnalysisContext context) {
        }

        @Override
        public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
            for (Map.Entry<Integer, String> entry : headMap.entrySet()) {
                StatisticalRulesEntity columnDataDTO = new StatisticalRulesEntity();
                Integer colIndex = entry.getKey();
                columnDataDTO.setColIndex(colIndex);
                columnDataDTO.setColName(entry.getValue());
                columnDataDTOList.add(columnDataDTO);
                if (!ruleMap.containsKey(colIndex)) {
                    continue;
                }
                //同步统计规则
                StatisticalRulesEntity rule = ruleMap.get(colIndex);
                BeanUtils.copyProperties(rule, columnDataDTO);
            }
        }
        @Override
        public void doAfterAllAnalysed(AnalysisContext context) {
        }
    }).sheet().doRead();
    return columnDataDTOList;
}
```

#### 配置多个字段统计规则

##### 控制层

```java
/**
 * 保存配置规则
 */
@PostMapping("saveRuleList/{fileId}")
@GlobalInterceptor(checkParams = true)
public ResponseVO saveRuleList(@PathVariable("fileId") String fileId, @RequestBody List<StatisticalRulesEntity> rulesEntityList) {
    return getSuccessResponseVO(statisticalRulesService.saveRuleList(fileId, rulesEntityList));
}
```

##### 业务层

```java
public Boolean saveRuleList(String fileId, List<StatisticalRulesEntity> rulesEntityList) {
    //1.先通过是否有规则id来区分新增和修改操作
    List<StatisticalRulesEntity> insertList = new ArrayList<>();
    List<StatisticalRulesEntity> updateList = new ArrayList<>();
    for (StatisticalRulesEntity statisticalRulesEntity : rulesEntityList) {
        if (statisticalRulesEntity.getId() == null) {
            statisticalRulesEntity.setFileId(Integer.valueOf(fileId));
            insertList.add(statisticalRulesEntity);
        } else {
            updateList.add(statisticalRulesEntity);
        }
    }
    //2.批量更新
    saveBatch(insertList);
    updateBatchById(updateList);
    return true;
}
```

##### 2025-2-14变更

- 如果类型为数值区间和数据集，要检查数据范围的格式

```java
    //校验数值区间的格式是否正确
    if (ColRuleEnum.RANGE.getCode().equals(statisticalRulesEntity.getType())) {
        String dataRange = statisticalRulesEntity.getDataRange();
        if (dataRange == null) {
            log.error("未填写数值区间");
            throw new BusinessException("数值区间的填写格式错误");
        }
        String[] split = dataRange.split("\\-");
        if (split.length != 2) {
            //必须截取出最大值和最小值
            log.error("截取元素个数有误");
            throw new BusinessException("数值区间的填写格式错误");
        }
        Integer min = null;
        Integer max = null;
        try {
            min = Integer.valueOf(split[0]);
            max = Integer.valueOf(split[1]);
        } catch (Exception e) {
            log.error("数据转换异常");
            throw new BusinessException("数值区间的填写格式错误");
        }
        if (min > max) {
            log.error("最大值和最小值颠倒");
            throw new BusinessException("数值区间的填写格式错误");
        }
    }
    //校验数据集的格式是否正确
    if (ColRuleEnum.SET.getCode().equals(statisticalRulesEntity.getType())) {
        String dataRange = statisticalRulesEntity.getDataRange();
        if (dataRange == null) {
            log.error("未填写数据集");
            throw new BusinessException("数据集的填写格式错误");
        }
        String[] split = dataRange.split("\\,");
        if (split.length == 0) {
            log.error("未填写数据集");
            throw new BusinessException("数据集的填写格式错误");
        }
    }
}
```

#### 指定截止时间进行统计

##### 定义延时任务

- 由于之前调用onlyOffice的下载接口把fileId类型写成String，所以导致后续操作都需要兼容，现在统一把所有文件id类型都改为long

```java
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
@Data
public class DelayedTask implements Delayed {
    private final long startTime;  // 目标执行时间的时间戳
    private final long fileId;

    // 修改构造函数，传入延迟时间并计算目标时间
    public DelayedTask(long delayInMillis, long fileId) {
        this.startTime = System.currentTimeMillis() + delayInMillis;  // 当前时间 + 延迟时间
        this.fileId = fileId;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = startTime - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.startTime, ((DelayedTask) o).startTime);
    }
}
```

##### 定义任务处理器

- 原本使用@Schedule参数，但是两个方法都共用一个线程，参考[Spring多定时任务@Scheduled执行阻塞问题_spring boot schedule 定时任务 多个同一个时间执行 阻塞了-CSDN博客](https://blog.csdn.net/LYM0721/article/details/89499588)，加了@Async实现异步，但是控制台报错有重复的taskExcutor的bean
- 最后还是采用两个线程的方式，分别监控入队和取数两个方法，以下代码的多线程逻辑来源deepseek

```java
@Slf4j
@Component
public class DelayedTaskExecutor {
    @Autowired
    private StatisticalRulesService ruleService;  // 注入 RuleService
    @Autowired
    private TaskInfoDao taskInfoDao;
    private final static DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();
    // 创建两个独立的线程池
    private final ExecutorService addTaskExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService executeTaskExecutor = Executors.newSingleThreadExecutor();
    // 初始化方法，在 Spring 容器启动时调用
    @PostConstruct
    public void init() {
        // 启动 addTaskToQueue 的监控线程
        addTaskExecutor.submit(this::addTaskToQueue);
        // 启动 executeDelayedTasks 的监控线程
        executeTaskExecutor.submit(this::executeDelayedTasks);
    }
    // 销毁方法，在 Spring 容器关闭时调用
    @PreDestroy
    public void destroy() {
        // 关闭线程池
        addTaskExecutor.shutdown();
        executeTaskExecutor.shutdown();
    }
    // 定期检查队列并执行任务
    public void executeDelayedTasks() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 从队列中获取已经到期的任务
                DelayedTask task = delayQueue.take();
                List<String> result = ruleService.processExcelColumnsWithRules(task.getFileId());
                System.out.println(result);
//                WebhookUtil.sendDingDingMessage(result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("executeDelayedTasks 被中断", e);
            }
        }
    }
    // 定时添加任务到队列
    public void addTaskToQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                log.info("===============开始批量加入任务到延时队列============");
                //1.先查询出未加入延时队列的任务信息
                LambdaQueryWrapper<TaskInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(TaskInfoEntity::getFlag, 0);
                List<TaskInfoEntity> taskList = taskInfoDao.selectList(queryWrapper);
                if (CollectionUtils.isEmpty(taskList)) {
                    // 如果没有任务，休眠 5 秒后继续检查
                    Thread.sleep(60 * 1000);
                    continue;
                }
                HashSet<Long> fileIdList = new HashSet<>();
                //2.加入延时队列
                for (TaskInfoEntity taskInfoEntity : taskList) {
                    // 解析开始统计时间
                    Date startTime = taskInfoEntity.getStartTime();
                    // 将 Date 转换为 LocalDateTime
                    LocalDateTime targetDateTime = startTime.toInstant()          // 将 Date 转换为 Instant
                            .atZone(java.time.ZoneId.systemDefault())             // 转换为系统时区
                            .toLocalDateTime();                                   // 转换为 LocalDateTime
                    // 获取当前时间
                    LocalDateTime currentDateTime = LocalDateTime.now();
                    // 计算延迟时间（单位：毫秒）
                    long delayInMillis = Duration.between(currentDateTime, targetDateTime).toMillis();
                    // 如果目标时间已经过去，延迟时间设置为0
                    if (delayInMillis < 0) {
                        delayInMillis = 0;
                    }
                    Long fileId = taskInfoEntity.getFileId();
                    // 创建一个新的延迟任务，并传入计算出来的延迟时间和 fileId
                    DelayedTask delayedTask = new DelayedTask(delayInMillis, fileId);
                    delayQueue.offer(delayedTask);
                    fileIdList.add(fileId);
                }
                //3.批量修改任务状态
                LambdaQueryWrapper<TaskInfoEntity> updateWrapper = new LambdaQueryWrapper<>();
                updateWrapper.in(TaskInfoEntity::getFileId, fileIdList);
                TaskInfoEntity updateInfo = new TaskInfoEntity();
                updateInfo.setFlag(1);
                taskInfoDao.update(updateInfo, updateWrapper);
                // 休眠60秒后继续检查
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("addTaskToQueue 被中断", e);
            } catch (Exception e) {
                log.error("addTaskToQueue 发生异常", e);
            }
        }
    }
}
```

##### 修改保存配置接口

- 保存规则接口干脆实现两个功能，一个是保存定时时间，另一个是保存统计规则
- 控制层修改

```java
@PostMapping("/saveRuleList/{fileId}")
@GlobalInterceptor(checkParams = true)
public ResponseVO saveRuleList(@PathVariable("fileId") String fileId, @RequestBody RuleDataDTO ruleDataDTO) {
    return getSuccessResponseVO(
        statisticalRulesService.saveRuleList(fileId, ruleDataDTO.getRulesEntityList()));
}
public class RuleDataDTO {
    List<StatisticalRulesEntity> rulesEntityList;
    String scheduleDateTime;
}
```

- 业务逻辑修改：保存为配置之后再保存调度时间

```java
//3.保存定时时间
String scheduleDateTime = ruleDataDTO.getScheduleDateTime();
if(!StringUtils.hasLength(scheduleDateTime)){
    return true;
}
try {
    //3.1日期格式化
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Date date = sdf.parse(scheduleDateTime);
    TaskInfoEntity taskInfoEntity = new TaskInfoEntity();
    taskInfoEntity.setStartTime(date);
    taskInfoEntity.setFileId(fileId);
    taskInfoEntity.setFlag(0);
    //3.2判断是更新还是新增
    LambdaQueryWrapper<TaskInfoEntity> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(TaskInfoEntity::getFileId, fileId);
    Long count = taskInfoDao.selectCount(queryWrapper);
    if (count > 0) {
        //走更新逻辑逻辑
        taskInfoDao.update(taskInfoEntity, queryWrapper);
    } else {
        taskInfoDao.insert(taskInfoEntity);
    }
} catch (ParseException e) {
    log.error("时间格式转换异常");
    throw new RuntimeException(e);
}
```

##### 回显已配置的调度时间【TODO】

## 在线文档编辑【前端】

### 新增在线文档编辑菜单

#### 原型

- 由于其它菜单项的字数都不多，所以文案采用四个字的**多人协作**，logo和样式之后再补充
- 子级目录包含
  - 文档管理：负责文档上传、授权
  - 文档列表：展示有权限的**文档列表**，包含查看、下载等操作

<img src="easypan.assets/image-20250202145229200.png" alt="image-20250202145229200" style="zoom: 67%;" />.

#### 代码分析

##### 菜单定义

- 菜单等框架信息都放在`Framework.vue`中，其中菜单代码段如下

```css
const menus = [
    {
        icon: "cloude",
        name: "首页",
        menuCode: "main",
        path: "/main/all",
        allShow: true,
        children: [
            {
                icon: "all",
                name: "全部",
                category: "all",
                path: "/main/all",
            },
            {
                icon: "video",
                name: "视频",
                category: "video",
                path: "/main/video",
            },
            {
                icon: "music",
                name: "音频",
                category: "music",
                path: "/main/music",
            },
            {
                icon: "image",
                name: "图片",
                category: "image",
                path: "/main/image",
            },
            {
                icon: "doc",
                name: "文档",
                category: "doc",
                path: "/main/doc",
            },
            {
                icon: "more",
                name: "其他",
                category: "others",
                path: "/main/others",
            },
        ],
    },
    {
        path: "/myshare",
        icon: "share",
        name: "分享",
        menuCode: "share",
        allShow: true,
        children: [
            {
                name: "分享记录",
                path: "/myshare",
            },
        ],
    },
    {
        path: "/recycle",
        icon: "del",
        name: "回收站",
        menuCode: "recycle",
        tips: "回收站为你保存10天内删除的文件",
        allShow: true,
        children: [
            {
                name: "删除的文件",
                path: "/recycle",
            },
        ],
    },
    {
        path: "/settings/fileList",
        icon: "settings",
        name: "设置",
        menuCode: "settings",
        allShow: false,
        children: [
            {
                name: "用户文件",
                path: "/settings/fileList",
            },
            {
                name: "用户管理",
                path: "/settings/userList",
            },
            {
                name: "系统设置",
                path: "/settings/sysSetting",
            },
        ],
    },
];
```

##### 菜单和二级菜单的跳转

```html
<div class="menu-list">
    <!-- 使用 v-for 遍历 menus 数组，每个菜单项用 item 表示 -->
    <template v-for="item in menus">
        <!-- v-if 用于判断菜单项是否显示：
             如果 item.allShow 为 true，直接显示；如果 item.allShow 为 false，且 userInfo.admin 为 true（用户是管理员），则显示 -->
        <div
            v-if="item.allShow || (!item.allShow && userInfo.admin)"
            @click="jump(item)" <!-- 点击菜单项时触发 jump(item) 方法 -->
            :class="[
                'menu-item',  <!-- 总是为该元素加上 'menu-item' 类 -->
                <!-- 如果当前菜单项的 menuCode 和当前选中的菜单相同，则加上 'active' 类，高亮显示 -->
                item.menuCode == currentMenu.menuCode ? 'active' : '', 
            ]"
        >
            <!-- 显示菜单项的图标，动态绑定类名 'iconfont' 和 'icon-' + item.icon -->
            <div :class="['iconfont', 'icon-' + item.icon]"></div>
            <!-- 显示菜单项的名称，通过插值语法插入 item.name -->
            <div class="text">{{ item.name }}</div>
        </div>
    </template>
</div>
```

- 跳转方法

```js
 <!-- 点击菜单触发跳转 -->
const jump = (data) => {
    // 如果 data.path 不存在或当前菜单项的 menuCode 与选中的菜单 menuCode 相同，则不执行任何操作
    if (!data.path || data.menuCode == currentMenu.value.menuCode) {
        return;
    }
    // 如果条件满足（即路径存在且当前菜单项的 menuCode 与选中的菜单不同），则跳转到指定路径
    router.push(data.path);
};
// setMenu 函数用于根据传入的 menuCode 和 path 更新当前菜单和当前路径
const setMenu = (menuCode, path) => {
    // 在 menus 数组中找到对应 menuCode 的菜单项
    const menu = menus.find((item) => {
        return item.menuCode === menuCode; // 比较每个菜单项的 menuCode 是否与传入的 menuCode 匹配
    });
    // 将找到的菜单项设置为当前菜单
    currentMenu.value = menu;
    // 设置当前路径
    currentPath.value = path;
};
// 监听 route 变化
watch(
    // 监听 route 对象的变化，当 route 发生变化时触发回调
    () => route,  
    // 回调函数，处理 route 变化后的操作
    (newVal, oldVal) => {
        // 判断新 route 对象的 meta 是否包含 menuCode
        if (newVal.meta.menuCode) {
            // 如果有 menuCode，则调用 setMenu 函数，传入新的 menuCode 和 path
            setMenu(newVal.meta.menuCode, newVal.path);
        }
    },
    // 监听选项
    { 
        immediate: true,  // 立即执行一次回调，在初始化时会执行一次
        deep: true        // 深度监听 route 对象，确保对象内部的变化也能触发回调
    }
);
```

##### 路由绑定

- index.js中存储了路由和指定页面的绑定关系

```js
// 创建 Vue Router 实例
const router = createRouter({
  // 使用 HTML5 history 模式
  history: createWebHistory(import.meta.env.BASE_URL), // 使用当前环境变量的基本路径
  
  routes: [
    {
      // 登录页面路由
      path: '/login',
      name: 'Login', // 路由名称
      component: () => import("@/views/Login.vue") // 动态导入 Login 组件
    },
    {
      // QQ 登录回调页面
      path: '/qqlogincallback',
      name: 'qq登录回调', // 路由名称
      component: () => import("@/views/QqLoginCallback.vue") // 动态导入 QQ 登录回调组件
    },
    {
      // Framework 主框架页面，作为嵌套路由的父路由
      path: '/',
      name: 'Framework', // 路由名称
      component: () => import("@/views/Framework.vue"), // 动态导入 Framework 组件
      children: [
        {
          // 默认重定向到 /main/all
          path: '/',
          redirect: "/main/all" // 重定向路径
        },
        {
          // 首页，动态路由根据分类展示不同内容
          path: '/main/:category',
          name: '首页', // 路由名称
          meta: {
            needLogin: true, // 需要登录才能访问
            menuCode: "main" // 用于标识菜单项
          },
          component: () => import("@/views/main/Main.vue") // 动态导入 Main 组件
        },
        {
          // 我的分享页面
          path: '/myshare',
          name: '我的分享', // 路由名称
          meta: {
            needLogin: true, // 需要登录才能访问
            menuCode: "share" // 用于标识菜单项
          },
          component: () => import("@/views/share/Share.vue") // 动态导入 Share 组件
        },
        {
          // 回收站页面
          path: '/recycle',
          name: '回收站', // 路由名称
          meta: {
            needLogin: true, // 需要登录才能访问
            menuCode: "recycle" // 用于标识菜单项
          },
          component: () => import("@/views/recycle/Recycle.vue") // 动态导入 Recycle 组件
        },
        {
          // 系统设置页面
          path: '/settings/sysSetting',
          name: '系统设置', // 路由名称
          meta: {
            needLogin: true, // 需要登录才能访问
            menuCode: "settings" // 用于标识菜单项
          },
          component: () => import("@/views/admin/SysSettings.vue") // 动态导入 SysSettings 组件
        },
        {
          // 用户管理页面
          path: '/settings/userList',
          name: '用户管理', // 路由名称
          meta: {
            needLogin: true, // 需要登录才能访问
            menuCode: "settings" // 用于标识菜单项
          },
          component: () => import("@/views/admin/UserList.vue") // 动态导入 UserList 组件
        },
        {
          // 用户文件页面
          path: '/settings/fileList',
          name: '用户文件', // 路由名称
          meta: {
            needLogin: true, // 需要登录才能访问
            menuCode: "settings" // 用于标识菜单项
          },
          component: () => import("@/views/admin/FileList.vue") // 动态导入 FileList 组件
        },
      ]
    },
    {
      // 分享校验页面，带有 shareId 参数
      path: '/shareCheck/:shareId',
      name: '分享校验', // 路由名称
      component: () => import("@/views/webshare/ShareCheck.vue") // 动态导入 ShareCheck 组件
    },
    {
      // 分享页面，带有 shareId 参数
      path: '/share/:shareId',
      name: '分享', // 路由名称
      component: () => import("@/views/webshare/Share.vue") // 动态导入 Share 组件
    }
  ]
})
```

##### 图标定义

- 图标采用 Unicode 字符（如 `\e673`）在实际的网页中会通过对应的字体文件渲染为图标

```css
@font-face {
  font-family: "iconfont"; /* Project id 3946741 */
  src: url('iconfont.woff2?t=1680443314592') format('woff2'),
       url('iconfont.woff?t=1680443314592') format('woff'),
       url('iconfont.ttf?t=1680443314592') format('truetype');
}

.iconfont {
  font-family: "iconfont" !important;
  font-size: 16px;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.icon-settings:before {
  content: "\e673";
}

.icon-del:before {
  content: "\e636";
}
//...
```

#### 代码实现

##### 1.menus数组新增一个元素

- 参照`设置`菜单项，点击多人协作**默认跳到文档管理**

```css
{
    path: "/collaboration/documentManagement",
    icon: "team",
    name: "多人协作",
    menuCode: "collaboration",
    allShow: true,
    children: [
        {
            name: "文档管理",
            path: "/collaboration/documentManagement",
        },
        {
            name: "文档列表",
            path: "/collaboration/documentList",
        },
    ],
},
```

<img src="easypan.assets/image-20250202151406426.png" alt="image-20250202151406426" style="zoom:50%;" />.

##### 2.index.js新增多人协作路由

- 参照`/main/:category`动态根据分类展示

```js
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import("@/views/Login.vue")
    },
    {
      path: '/qqlogincallback',
      name: 'qq登录回调',
      component: () => import("@/views/QqLoginCallback.vue")
    },
    {
      path: '/',
      name: 'Framework',
      component: () => import("@/views/Framework.vue"),
      children: [
        {
          path: '/',
          redirect: "/main/all"
        },
        {
          path: '/main/:category',
          name: '首页',
          meta: {
            needLogin: true,
            menuCode: "main"
          },
          component: () => import("@/views/main/Main.vue")
        },
		//...
        // 管理在线编辑文档
        {
          path: '/collaboration/documentManagement',
          name: '文档管理',
          meta: {
            needLogin: true,
            menuCode: "collaboration" 
          },
          component: () => import("@/views/collaboration/DocumentManagement.vue") 
        },
        // 展示在线编辑的文档列表
        {
          path: '/collaboration/documentList',
          name: '文档列表',
          meta: {
            needLogin: true,
            menuCode: "collaboration"
          },
          component: () => import("@/views/collaboration/DocumentList.vue") 
        }
      ]
    },
    {
      path: '/shareCheck/:shareId',
      name: '分享校验',
      component: () => import("@/views/webshare/ShareCheck.vue")
    },
    {
      path: '/share/:shareId',
      name: '分享',
      component: () => import("@/views/webshare/Share.vue")
    }
  ]
})
```

<img src="easypan.assets/image-20250202164027977.png" alt="image-20250202164027977" style="zoom: 80%;" /><img src="easypan.assets/image-20250202163618640.png" alt="image-20250202163618640" style="zoom:50%;" />

##### 3.添加图标

- 虽然不是灰色，但黑紫色也差不多，懒得改了，形状挺符合的

```css
.icon-team:before {
  content: "\1F465";
}
```

<img src="easypan.assets/image-20250202171104657.png" alt="image-20250202171104657" style="zoom:50%;" />.

#### 变更

- 2025-2-3：考虑到多人协作需要有一个公共的平台，所以新增一个用户组（可以理解成企业）的概念，在多人协作板块新增一个用户组管理子目录

### 用户组管理

#### 原型

- 多人协作优先把人聚集起来，用户组管理界面应该包含用户组创建、已加入/创建的用户组列表
- 列表项展示用户组名、当前人数【点击当前人数可以展示组内的所有用户详情】、最大人数
  - 如果是创建人，列表项要有分享按钮，可以生成分享链接，同时还要有解散
  - 如果是普通用户，可以选择退群

<img src="easypan.assets/image-20250203141138520.png" alt="image-20250203141138520" style="zoom:80%;" />.

- 创建用户组申请表单信息包含用户组名、用户组描述、总人数

#### 代码分析

##### 文件管理界面

- 这一块涉及列表、创建等操作，那就基于文件管理界面改写

```html
<template>
  <div>
      <div class="top">
          <div class="top-op">
              <div class="btn">
                <el-upload
                  :show-file-list="false"
                  :with-credentials="true"
                  :multiple="true"
                  :http-request="addFile"
                  :accept="fileAccept"
                >
                    <el-button type="primary">
                      <span class="iconfont icon-upload"></span>
                      上传
                    </el-button>
                </el-upload>
              </div>
              <el-button type="success" @click="newFolder">
                <span class="iconfont icon-folder-add"></span>
                新建文件夹
              </el-button>
              <el-button
                type="danger"
                :disabled="selectFileIdList.length == 0"
                @click="delFileBatch"
              >
                <span class="iconfont icon-del"></span>
                批量删除
              </el-button>
              <el-button
                type="warning"
                :disabled="selectFileIdList.length == 0"
                @click="moveFolderBatch"
              >
                <span class="iconfont icon-move"></span>
                批量移动
              </el-button>
              <div class="search-panel">
                <!-- 搜索文件 -->
                <el-input
                  clearable
                  placeholder="请输入文件名搜索"
                  v-model="fileNameFuzzy"
                  @keyup.enter="search"
                >
                    <template #suffix>
                        <i class="iconfont icon-search" @click="search"></i>
                    </template>
                </el-input>
              </div>
              <div class="iconfont icon-refresh" @click="loadDataList"></div>
          </div>
          <!-- 导航 -->
          <Navigation ref="navigationRef" @navChange="navChange"></Navigation>
      </div>
      <!-- 遍历文件列表 -->
      <div class="file-list" v-if="tableData.list && tableData.list.length > 0">
        <Table
          ref="dataTableRef"
          :columns="columns"
          :dataSource="tableData"
          :fetch="loadDataList"
          :initFetch="false"
          :options="tableOptions"
          @rowSelected="rowSelected"
        >
        <template #fileName="{ index, row }">
            <div
              class="file-item"
              @mouseenter="showOp(row)"
              @mouseleave="cancelShowOp(row)"
            >
              <template
                v-if="(row.fileType == 3 || row.fileType == 1) && row.status == 2"
              >
                <Icon :cover="row.fileCover" :width="32"></Icon>
              </template>
              <template v-else>
                <Icon v-if="row.folderType == 0" :fileType="row.fileType"></Icon>
                <Icon v-if="row.folderType == 1" :fileType="0"></Icon>
              </template>
              <span class="file-name" v-if="!row.showEdit" :title="row.fileName">
                <span @click="preview(row)">{{ row.fileName }}</span>
                <span v-if="row.status == 0" class="transfer-status">转码中</span>
                <span v-if="row.status == 1" class="transfer-status transfer-fail"
                  >转码失败</span
                >
              </span>
              <div class="edit-panel" v-if="row.showEdit">
                <el-input
                  v-model.trim="row.fileNameReal"
                  ref="editNameRef"
                  :maxLength="190"
                  @keyup.enter="saveNameEdit(index)"
                >
                  <template #suffix>{{ row.fileSuffix }}</template>
                </el-input>
                <span
                  :class="[
                    'iconfont icon-right1',
                    row.fileNameReal ? '' : 'not-allow',
                  ]"
                  @click="saveNameEdit(index)"
                ></span>
                <span
                  class="iconfont icon-error"
                  @click="cancelNameEdit(index)"
                ></span>
              </div>
              <span class="op">
                <template v-if="row.showOp && row.fileId && row.status == 2">
                    <span class="iconfont icon-share1" @click="share(row)"
                      >分享</span
                    >
                    <span
                      class="iconfont icon-download"
                      v-if="row.folderType == 0"
                      @click="download(row)"
                      >下载</span
                    >
                    <span class="iconfont icon-del" @click="delFile(row)"
                      >删除</span
                    >
                    <span class="iconfont icon-edit" @click="editFileName(index)"
                      >重命名</span
                    >
                    <span class="iconfont icon-move" @click="moveFolder">移动</span>
                </template>
              </span>
            </div>
        </template>
        <template #fileSize="{index, row}">
            <span v-if="row.fileSize">{{
              proxy.Utils.size2Str(row.fileSize)
            }}</span>
        </template>
        </Table>
      </div>
      <!-- 没有任何文件就展示文件上传的标识-->
      <div class="no-data" v-else>
        <div class="no-data-inner">
          <Icon iconName="no_data" :width="120" fit="fill"></Icon>
          <div class="tips">当前目录为空, 上传你的第一个文件吧</div>
          <div class="op-list">
            <el-upload
              :show-file-list="false"
              :with-credentials="true"
              :multiple="true"
              :http-request="addFile"
              :accept="fileAccept"
            >
              <div class="op-item">
                <Icon iconName="file" :width="60"></Icon>
                <div>上传文件</div>
              </div>
            </el-upload>
            <div class="op-item" v-if="category == 'all'" @click="newFolder">
              <Icon iconName="folder" :width="60"></Icon>
              <div>新建目录</div>
            </div>
          </div>
        </div>
      </div>
      <FolderSelect
        ref="folderSelectRef"
        @folderSelect="moveFolderDone"
      ></FolderSelect>
      <!-- 预览 -->
      <Preview ref="previewRef"></Preview>
      <!-- 使用分享组件 -->
      <ShareFile ref="shareRef"></ShareFile>
  </div>
</template>
<script setup>
import CategoryInfo from "@/js/CategoryInfo.js";
//导入组件
import ShareFile from "./ShareFile.vue";
import { ref, reactive, getCurrentInstance, nextTick, computed } from "vue";
const { proxy } = getCurrentInstance();
const emit = defineEmits(["addFile"]);
const addFile = (fileData) => {
  emit("addFile", { file: fileData.file, filePid: currentFolder.value.fileId });
};
// 添加文件回调
const reload = () => {
  showLoading.value = false;
  loadDataList();
};
defineExpose({
  reload,
});
// 当前目录
const currentFolder = ref({ fileId: "0" });
const api = {
    loadDataList: "/file/loadDataList",
    rename: "/file/rename",
    newFoloder: "/file/newFoloder",
    getFolderInfo: "/file/getFolderInfo",
    delFile: "/file/delFile",
    changeFileFolder: "/file/changeFileFolder",
    createDownloadUrl: "/file/createDownloadUrl",
    download: "/api/file/download",
};
const fileAccept = computed( () => {
  const categoryItem = CategoryInfo[category.value];
  return categoryItem ? categoryItem.accept : "*";
});

const columns = [
    {
        label: "文件名",
        prop: "fileName",
        //定义一个插槽
        scopedSlots: "fileName",
    },
    {
        label: "修改时间",
        prop: "lastUpdateTime",
        width: 200,
    },
    {
        label: "大小",
        prop: "fileSize",
        scopedSlots: "fileSize",
        width: 200,
    },
];
// 搜索
const search = () => {
  showLoading.value = true;
  loadDataList();
};
const tableData = ref({});
const tableOptions = ref({
    extHeight: 50,
    selectType: "checkbox",
});
const fileNameFuzzy = ref();
const showLoading = ref(true);
const category = ref();
const loadDataList = async () => {
    let params = {
        pageNo: tableData.value.pageNo,
        pageSize: tableData.value.pageSize,
        fileNameFuzzy: fileNameFuzzy.value,
        filePid: currentFolder.value.fileId,
        category: category.value,
    };
    if (params.category !== "all") {
        delete params.filePid;
    }
    let result = await proxy.Request({
        url: api.loadDataList,
        showLoading: showLoading.value,
        params: params,
    });
    if (!result) {
        return;
    }
    tableData.value = result.data;
};
// 展示操作按钮
const showOp = (row) => {
  tableData.value.list.forEach((element) => {
    element.showOp = false;
  });
  row.showOp = true;
};
const cancelShowOp = (row) => {
    row.showOp = false;
};
// 编辑行
const editing = ref(false);
const editNameRef = ref();
// 新建文件夹
const newFolder = () => {
    if (editing.value) {
        return;
    }
    tableData.value.list.forEach(element => {
        element.showEdit = false;
    });
    editing.value = true;
    tableData.value.list.unshift({
        showEdit: true,
        fileType: 0,
        fileId: "",
        filePid: currentFolder.value.fileId,
    });
    console.log("打印当前父目录"+currentFolder.value.fileId);
    nextTick(() => {
        editNameRef.value.focus();
    });
};
const cancelNameEdit = (index) => {
    const fileData = tableData.value.list[index];
    if (fileData.fileId) {
        fileData.showEdit = false;
    } else {
        tableData.value.list.splice(index, 1);
    }
    editing.value = false;
};

const saveNameEdit = async (index) => {
    const { fileId, filePid, fileNameReal } = tableData.value.list[index];
    if (fileNameReal == "" || fileNameReal.indexOf("/") != -1) {
        proxy.Message.warning("文件名不能为空且不能含有斜杠");
        return;
    }
    let url = api.rename;
    if (fileId == "") {
        url = api.newFoloder;
    }
    let result = await proxy.Request({
        url: url,
        params: {
            fileId: fileId,
            filePid: filePid,
            fileName: fileNameReal,
        },
    });
    if (!result) {
        return;
    }
    tableData.value.list[index] = result.data;
    editing.value = false;
};

const editFileName = (index) => {
    if (tableData.value.list[0].fileId == "") {
        tableData.value.list.splice(0, 1);
        index = index - 1;
    }
    tableData.value.list.forEach((element) => {
        element.showEdit = false;
    });
    let currentData = tableData.value.list[index];
    currentData.showEdit = true;
    // 编辑文件
    if (currentData.folderType == 0) {
        currentData.fileNameReal = currentData.fileName.substring(
          0,
          currentData.fileName.indexOf(".")
        );
        currentData.fileSuffix = currentData.fileName.substring(
          currentData.fileName.indexOf(".")
        );
    } else {
        currentData.fileNameReal = currentData.fileName;
        currentData.fileSuffix = "";
    }
    editing.value = true;
    nextTick(() => {
        editNameRef.value.focus();
    });
};

// 多选
const selectFileIdList = ref([]);
const rowSelected = (rows) => {
  selectFileIdList.value = [];
  rows.forEach((item) => {
    selectFileIdList.value.push(item.fileId);
  });
};
// 删除
const delFile = (row) => {
  proxy.Confirm(
    `你确定要删除【${row.fileName}】吗? 删除的文件可在10天内通过回收站还原`,
    async () => {
      let result = await proxy.Request({
        url: api.delFile,
        params: {
          fileIds: row.fileId,
        },
      });
      if (!result) {
        return;
      }
      loadDataList();
    }
  );
};

const delFileBatch = () => {
  if (selectFileIdList.value.length == 0) {
    return;
  }
  proxy.Confirm(
    `你确定要删除这些文件吗? 删除的文件可在10天内通过回收站还原`,
    async () => {
      let result = await proxy.Request({
        url: api.delFile,
        params: {
          fileIds: selectFileIdList.value.join(","),
        },
      });
      if (!result) {
        return;
      }
      loadDataList();
    }
  );
};

const folderSelectRef = ref();
const currentMoveFile = ref({});

const moveFolder = (data) => {
  currentMoveFile.value = data;
  folderSelectRef.value.showFolderDialog(currentFolder.value.fileId);
};

const moveFolderBatch = () => {
  currentMoveFile.value = {};
  folderSelectRef.value.showFolderDialog(currentFolder.value.fileId);
};

const moveFolderDone  = async (folderId) => {
  if (currentFolder.value.fileId == folderId) {
    proxy.Message.warning("文件正在当前目录, 无需移动");
    return;
  }
  let fileIdsArray = [];
  if (currentMoveFile.value.fileId) {
    fileIdsArray.push(currentMoveFile.value.fileId);
  } else {
    fileIdsArray = fileIdsArray.concat(selectFileIdList.value);
  }
  let result = await proxy.Request({
    url: api.changeFileFolder,
    params: {
      fileIds: fileIdsArray.join(","),
      filePid: folderId,
    },
  });
  if (!result) {
    return;
  }
  folderSelectRef.value.close();
  loadDataList();
};
// 预览
const navigationRef = ref();
const previewRef = ref();
const preview = (data) => {
  // 目录
  if (data.folderType == 1) {
    navigationRef.value.openFolder(data);
    return;
  }
  // 文件
  if (data.status != 2) {
    proxy.Message.warning("文件未完成转码, 无法预览");
    return;
  }
  previewRef.value.showPreview(data, 0);
};
const navChange = (data) => {
  const { categoryId, curFolder } = data;
  currentFolder.value = curFolder;
  category.value = categoryId;
  loadDataList();
};
// 下载文件
const download = async (row) => {
  let result = await proxy.Request({
      url: api.createDownloadUrl + "/" + row.fileId,
  });
  if (!result) {
      return;
  }
  window.location.href = api.download + "/" + result.data;
};
// 分享
const shareRef = ref();
const share = (row) => {
  shareRef.value.show(row);
};
</script>
<style lang="scss" scoped>
@import "@/assets/file.list.scss";
</style>
```

##### 列表组件

```html
<template>
    <div>
        <!-- 表格组件 -->
        <el-table
          ref="dataTable"
          :data="dataSource.list || []"  <!-- 数据源 -->
          :height="tableHeight"  <!-- 表格高度 -->
          :stripe="options.stripe"  <!-- 是否启用斑马纹 -->
          :border="options.border"  <!-- 是否显示边框 -->
          header-row-class-name="table-header-row"  <!-- 表头行样式 -->
          highlight-current-row  <!-- 高亮当前选中的行 -->
          @row-click="handleRowClick"  <!-- 行点击事件 -->
          @selection-change="handleSelectionChange"  <!-- 选择变化事件 -->
        >
          <!-- 选择框列（多选） -->
          <el-table-column
            v-if="options.selectType && options.selectType == 'checkbox'"  <!-- 判断是否显示选择框 -->
            type="selection"
            width="50"
            align="center"
          ></el-table-column>
          <!-- 序号列 -->
          <el-table-column
            v-if="options.showIndex"  <!-- 判断是否显示序号 -->
            label="序号"
            type="index"
            width="60"
            align="center"
          ></el-table-column>
          <!-- 数据列动态渲染 -->
          <template v-for="(column, index) in columns">
            <!-- 判断列是否有自定义插槽 -->
            <template v-if="column.scopedSlots">
              <el-table-column
                :key="index"
                :prop="column.prop"  <!-- 对应字段 -->
                :label="column.label"  <!-- 列标题 -->
                :align="column.align || 'left'"  <!-- 列对齐方式 -->
                :width="column.width"  <!-- 列宽 -->
              >
                <template #default="scope">
                  <!-- 如果列有自定义插槽，则使用 -->
                  <slot
                    :name="column.scopedSlots"
                    :index="scope.$index"  <!-- 当前索引 -->
                    :row="scope.row"  <!-- 当前行数据 -->
                  ></slot>
                </template>
              </el-table-column>
            </template>
            <!-- 如果没有自定义插槽，直接渲染普通列 -->
            <template v-else>
              <el-table-column
                :key="index"
                :prop="column.prop"
                :label="column.label"
                :align="column.align || 'left'"
                :width="column.width"
                :fixed="column.fixed"  <!-- 固定列 -->
              >
              </el-table-column>
            </template>
          </template>
        </el-table>
        <!-- 分页 -->
        <div class="pagination" v-if="showPagination">
          <el-pagination
            v-if="dataSource.totalCount"  <!-- 判断是否有总数 -->
            background
            :total="dataSource.totalCount"  <!-- 总条数 -->
            :page-sizes="[15, 30, 50, 100]"  <!-- 可选择的每页显示条数 -->
            :page-size="dataSource.pageSize"  <!-- 当前每页显示的条数 -->
            :current-page.sync="dataSource.pageNo"  <!-- 当前页码 -->
            :layout="layout"  <!-- 分页布局 -->
            @size-change="handlePageSizeChange"  <!-- 每页大小变化 -->
            @current-change="handlePageNoChange"  <!-- 页码变化 -->
            style="text-align: right"
          ></el-pagination>
        </div>
    </div>
</template>
<script setup>
// 引入必要的vue功能
import { ref, computed } from "vue";
// 触发父组件事件
const emit = defineEmits(["rowSelected", "rowClick"]);
// 定义父组件传入的参数
const props = defineProps({
    dataSource: Object,  // 数据源
    showPagination: {
        type: Boolean,
        default: true,  // 是否显示分页
    },
    showPageSize: {
        type: Boolean,
        default: true,  // 是否显示每页大小选择
    },
    options: {
        type: Object,
        default: {
            extHeight: 0,
            showIndex: false,  // 是否显示序号列
        },
    },
    columns: Array,  // 表格的列配置
    fetch: Function,  // 获取数据的函数
    initFetch: {
        type: Boolean,
        default: true,  // 初始化时是否自动请求数据
    },
});
// 分页控件的布局设置
const layout = computed(() => {
    return `total, ${
        props.showPageSize ? "sizes" : ""
    }, prev, pager, next, jumper`;
});
// 计算表格的高度
const topHeight = 60 + 20 + 30 + 46;  // 顶部高度 + 内边距 + 分页高度
const tableHeight = ref(
    props.options.tableHeight
    ? props.options.tableHeight
    : window.innerHeight - topHeight - props.options.extHeight
);
// 初始化方法，判断是否需要自动加载数据
const init = () => {
    if (props.initFetch && props.fetch) {
        props.fetch();
    }
};
init();
// 引用表格实例
const dataTable = ref();
// 清除选中项
const clearSelection = () => {
    dataTable.value.clearSelection();
};
// 设置当前选中的行
const setCurrentRow = (rowKey, rowValue) => {
    let row = props.dataSource.list.find((item) => {
        return item[rowKey] === rowValue;
    });
    dataTable.value.setCurrentRow(row);
};
// 将方法暴露给父组件
defineExpose({ setCurrentRow, clearSelection });
// 行点击事件
const handleRowClick = (row) => {
    emit("rowClick", row);  // 触发父组件的行点击事件
};
// 选择变化（用于多选）
const handleSelectionChange = (row) => {
    emit("rowSelected", row);  // 触发父组件的选择变化事件
};
// 处理每页大小变化
const handlePageSizeChange = (size) => {
    props.dataSource.pageSize = size;  // 更新每页大小
    props.dataSource.pageNo = 1;  // 重置页码为第一页
    props.fetch();  // 重新请求数据
};
// 处理页码变化
const handlePageNoChange = (pageNo) => {
    props.dataSource.pageNo = pageNo;  // 更新当前页码
    props.fetch();  // 重新请求数据
};
</script>
<style lang="scss" scoped>
/* 分页区域样式 */
.pagination {
    padding-top: 10px;
    padding-right: 10px;
}

.el-pagination {
    justify-content: right;
}
/* 表格单元格的内边距调整 */
:deep .el-table__cell {
    padding: 4px 0px;
}
</style>
```

#### 代码实现

##### 创建用户组新增组件

```vue
<template>
  <div>
    <Dialog
      :show="dialogConfig.show"
      :title="dialogConfig.title"
      :buttons="dialogConfig.buttons"
      width="600px"
      :showCancel="showCancel"
      @close="dialogConfig.show = false"
    >
      <el-form
        :model="formData"
        :rules="rules"
        ref="formDataRef"
        label-width="100px"
        @submit.prevent
      >
        <!-- 用户组名称 -->
        <el-form-item label="用户组名称" prop="groupName">
          <el-input
            v-model="formData.groupName"
            placeholder="请输入用户组名称"
          ></el-input>
        </el-form-item>
        <!-- 用户组描述 -->
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="formData.description"
            placeholder="请输入用户组描述"
          ></el-input>
        </el-form-item>
        <!-- 总人数 -->
        <el-form-item label="总人数" prop="totalMembers">
          <el-input
            v-model="formData.totalMembers"
            placeholder="请输入总人数"
            type="number"
          ></el-input>
        </el-form-item>
        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submitForm">提交</el-button>
          <el-button @click="cancelForm">取消</el-button>
        </el-form-item>
      </el-form>
    </Dialog>
  </div>
</template>
<script setup>
import { ref, reactive, getCurrentInstance } from "vue";
const { proxy } = getCurrentInstance();

const dialogConfig = ref({
  show: false,
  title: "新建用户组",
  buttons: [
    {
      type: "primary",
      text: "确定",
      click: () => {
        submitForm();
      },
    },
  ],
});
const showCancel = ref(true);
const formData = ref({
  groupName: "",
  description: "",
  totalMembers: "",  // 修改为总人数字段
});
const rules = {
  groupName: [{ required: true, message: "请输入用户组名称", trigger: "blur" }],
  description: [{ required: true, message: "请输入描述", trigger: "blur" }],
  totalMembers: [{ required: true, message: "请输入总人数", trigger: "blur" }],
};
const formDataRef = ref();
const submitForm = async () => {
  formDataRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    let params = { ...formData.value };
    // 模拟请求
    let result = await proxy.Request({
      url: "/user/createGroup", // 假设的 API 地址
      params: params,
    });

    if (result) {
      // 成功后关闭弹窗
      dialogConfig.value.show = false;
      proxy.Message.success("用户组创建成功");
    }
  });
};
const cancelForm = () => {
  dialogConfig.value.show = false;
};
const show = () => {
  dialogConfig.value.show = true;
  showCancel.value = true;
  formData.value = { groupName: "", description: "", totalMembers: "" }; // 清空表单
};
defineExpose({ show });
</script>
<style lang="scss" scoped>
</style>
```

##### 查询用户组列表

```html
<template>
    <div>
        <div class="top">
            <div class="top-op">
                <div class="btn"></div>
                <el-button type="success" @click="showDialog">
                     <span class="iconfont icon-folder-add"></span>
                        新建用户组
                </el-button>
                <div class="iconfont icon-refresh" @click="loadDataList"></div>
            </div>
        </div>
        <!-- 动态显示新建用户组表单 -->
        <NewUserGroup ref="newUserGroup"></NewUserGroup>
      <!-- 遍历文件列表 -->
      <div class="group-list" v-if="tableData.list && tableData.list.length > 0">
        <Table
          ref="dataTableRef"
          :columns="columns"
          :dataSource="tableData"
          :fetch="loadDataList"
          :initFetch="false"
          :options="tableOptions"
          @rowSelected="rowSelected"
        >
        <template #groupSize="{ row }">
              {{row.currentSize }}/{{row.maxSize}}
        </template>
        </Table>
      </div>
        <div class="no-data" v-else>
          <div class="no-data-inner">
            <Icon iconName="no_data" :width="120" fit="fill"></Icon>
            <div class="tips">没有用户组，点击创建一个自己的用户组</div>
            <div class="op-list">
              <el-button type="success" @click="showDialog">
                  <span class="iconfont icon-folder-add"></span>
                  新建用户组
                </el-button>
            </div>
          </div>
        </div>
        <FolderSelect
          ref="folderSelectRef"
          @folderSelect="moveFolderDone"
        ></FolderSelect>
    </div>
  </template>
<script setup>
import NewUserGroup from "./NewUserGroup.vue";
import { ref, reactive, getCurrentInstance, nextTick, computed,onMounted   } from "vue";
  const { proxy } = getCurrentInstance();
  const emit = defineEmits(["addFile"]);
  const api = {
      loadDataList: "/api/onlineEdit/userGroupList"
  };

  const columns = [
      {
          label: "用户组名",
          prop: "groupName",
          width: 200,
      },
      {
          label: "创建人名称",
          prop: "createByName",
          width: 200,
      },
      {
          label: "描述",
          prop: "description",
          width: 200,
      },
      {
        label: "人数信息",
        prop: "groupSize",
        scopedSlots: "groupSize",
        width: 200,
    },
  ];
  
  onMounted(() => {
  // 在页面加载后调用 loadDataList 方法
    loadDataList();
  });
  const tableData = ref({});
  const tableOptions = ref({
      extHeight: 50,
      selectType: "checkbox",
  });
  
  const showLoading = ref(true);
  const category = ref();
  const loadDataList = async () => {
    try {
      let response = await fetch(api.loadDataList, 
      {
        method: 'GET'
      });
      if (!response.ok) {
        throw new Error('请求失败');
      }
      let result = await response.json();
      tableData.value = result.data;  // 把接口返回的数据设置到 tableData
    } catch (error) {
      console.error("请求失败", error);  // 错误处理
    }
  };

  // 展示操作按钮
  const showOp = (row) => {
    tableData.value.list.forEach((element) => {
      element.showOp = false;
    });
    row.showOp = true;
  };
  
  const cancelShowOp = (row) => {
      row.showOp = false;
  };
  
  // 编辑行
  const editing = ref(false);
  const editNameRef = ref();

  const cancelNameEdit = (index) => {
      const fileData = tableData.value.list[index];
      if (fileData.fileId) {
          fileData.showEdit = false;
      } else {
          tableData.value.list.splice(index, 1);
      }
      editing.value = false;
  };
  
  const saveNameEdit = async (index) => {
      const { fileId, filePid, fileNameReal } = tableData.value.list[index];
      if (fileNameReal == "" || fileNameReal.indexOf("/") != -1) {
          proxy.Message.warning("文件名不能为空且不能含有斜杠");
          return;
      }
      let url = api.rename;
      if (fileId == "") {
          url = api.newFoloder;
      }
      let result = await proxy.Request({
          url: url,
          params: {
              fileId: fileId,
              filePid: filePid,
              fileName: fileNameReal,
          },
      });
      if (!result) {
          return;
      }
      tableData.value.list[index] = result.data;
      editing.value = false;
  };
  
  const editFileName = (index) => {
      if (tableData.value.list[0].fileId == "") {
          tableData.value.list.splice(0, 1);
          index = index - 1;
      }
      tableData.value.list.forEach((element) => {
          element.showEdit = false;
      });
      let currentData = tableData.value.list[index];
      currentData.showEdit = true;
      // 编辑文件
      if (currentData.folderType == 0) {
          currentData.fileNameReal = currentData.fileName.substring(
            0,
            currentData.fileName.indexOf(".")
          );
          currentData.fileSuffix = currentData.fileName.substring(
            currentData.fileName.indexOf(".")
          );
      } else {
          currentData.fileNameReal = currentData.fileName;
          currentData.fileSuffix = "";
      }
      editing.value = true;
      nextTick(() => {
          editNameRef.value.focus();
      });
  };
  
  // 多选
  const selectFileIdList = ref([]);
  const rowSelected = (rows) => {
    selectFileIdList.value = [];
    rows.forEach((item) => {
      selectFileIdList.value.push(item.fileId);
    });
  };
  // 删除
  const delFile = (row) => {
    proxy.Confirm(
      `你确定要删除【${row.fileName}】吗? 删除的文件可在10天内通过回收站还原`,
      async () => {
        let result = await proxy.Request({
          url: api.delFile,
          params: {
            fileIds: row.fileId,
          },
        });
        if (!result) {
          return;
        }
        loadDataList();
      }
    );
  };
  
  const delFileBatch = () => {
    if (selectFileIdList.value.length == 0) {
      return;
    }
    proxy.Confirm(
      `你确定要删除这些文件吗? 删除的文件可在10天内通过回收站还原`,
      async () => {
        let result = await proxy.Request({
          url: api.delFile,
          params: {
            fileIds: selectFileIdList.value.join(","),
          },
        });
        if (!result) {
          return;
        }
        loadDataList();
      }
    );
  };
  const folderSelectRef = ref();
  const currentMoveFile = ref({});
  const moveFolder = (data) => {
    currentMoveFile.value = data;
    folderSelectRef.value.showFolderDialog(currentFolder.value.fileId);
  };
  
  const moveFolderBatch = () => {
    currentMoveFile.value = {};
    folderSelectRef.value.showFolderDialog(currentFolder.value.fileId);
  };
  
  const moveFolderDone  = async (folderId) => {
    if (currentFolder.value.fileId == folderId) {
      proxy.Message.warning("文件正在当前目录, 无需移动");
      return;
    }
    let fileIdsArray = [];
    if (currentMoveFile.value.fileId) {
      fileIdsArray.push(currentMoveFile.value.fileId);
    } else {
      fileIdsArray = fileIdsArray.concat(selectFileIdList.value);
    }
    let result = await proxy.Request({
      url: api.changeFileFolder,
      params: {
        fileIds: fileIdsArray.join(","),
        filePid: folderId,
      },
    });
    if (!result) {
      return;
    }
    folderSelectRef.value.close();
    loadDataList();
  };
  
  // 预览
  const navigationRef = ref();
  const previewRef = ref();
  const preview = (data) => {
    // 目录
    if (data.folderType == 1) {
      navigationRef.value.openFolder(data);
      return;
    }
    // 文件
    if (data.status != 2) {
      proxy.Message.warning("文件未完成转码, 无法预览");
      return;
    }
    previewRef.value.showPreview(data, 0);
  };
  const newUserGroup = ref(); // 通过 ref 获取子组件
  const showDialog = () => {
  // 调用子组件的 show 方法
  newUserGroup.value.show();
  };
  </script>
  <style lang="scss" scoped>
  @import "@/assets/file.list.scss";
  </style>
```

##### 待审批用户查询

- 待审批用户列表组件

```html
<template>
  <div>
    <!-- 弹窗显示待审批用户 -->
    <Dialog
      :show="dialogConfig.show"
      :title="'待审批用户'"
      width="400px"
      :showCancel="true"
      @close="dialogConfig.show = false"
    >
      <div v-if="pendingUsers != null && pendingUsers.length > 0">
        <ul class="user-list">
          <li v-for="user in pendingUsers" :key="user.userId" class="user-item">
            <span class="user-name">{{ user.nickName }}</span>
            <div class="action-buttons">
              <el-button type="success" size="small" @click="approveUser(user)">通过</el-button>
              <el-button type="danger" size="small" @click="rejectUser(user)">拒绝</el-button>
            </div>
          </li>
        </ul>
      </div>
      <div v-else>
        <p class="no-users">当前没有待审批的用户。</p>
      </div>
    </Dialog>
  </div>
</template>
<script setup>
import { ref, defineProps, defineExpose } from 'vue';
const api = {
      approval: "/approval"
    };
const props = defineProps({
  pendingUsers: {
    type: Array,
    required: true
  },
  groupId:{
    type: Number,
    required : true
  }
});

const dialogConfig = ref({
  show: false,
});
// 显示弹窗
const show = () => {
  dialogConfig.value.show = true;
};
// 关闭弹窗
const cancelForm = () => {
  dialogConfig.value.show = false;
};
// 通过用户
const approveUser = (user) => {
  // 这里可以加入通过用户的逻辑，例如调用 API 或更新状态
  console.log(`用户 ${user.nickName} 已通过`);
  // 关闭弹窗
  cancelForm();
};
// 拒绝用户
const rejectUser = (user) => {
  // 这里可以加入拒绝用户的逻辑，例如调用 API 或更新状态
  console.log(`用户 ${user.nickName} 被拒绝`);
  // 关闭弹窗
  cancelForm();
};
defineExpose({ show });
</script>
<style lang="scss" scoped>
/* 弹窗样式 */
.dialog-content {
  padding: 20px;
  font-size: 14px;
}
/* 用户列表样式 */
.user-list {
  list-style-type: none;
  padding: 0;
  margin: 0;
}
.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.user-name {
  font-weight: bold;
  color: #333;
  flex-grow: 1;
}
.action-buttons {
  display: flex;
  gap: 10px;
}
.no-users {
  text-align: center;
  font-size: 16px;
  color: #888;
}
</style>
```

- 父组件调用方法和传值

```html
<!-- 待审批的用户组列表 绑定子组件的属性-->
<PendingUserList ref="pendingUserList" :pending-users="pendingUsers" :groupId="rowGroupId"/>
<!-- 导入组件-->
import PendingUserList from "./PendingUserList.vue";
  const rowGroupId =ref();//存储当前行的组id
  const pendingUsers = ref({});  // 存储待审批用户列表
  const pendingUserList = ref(); // 通过 ref 获取子组件
  const getPendingUser = async (row) => {
    try {
      let groupId = row.groupId; // 获取当前用户组的ID
      rowGroupId.value = groupId;
      // 拼接到API接口地址
      let url = `${api.getPendingUser}/${groupId}`;
      let response = await fetch(url, { method: 'GET' });
      
      if (!response.ok) {
        throw new Error('请求失败');
      }
      // 获取待审批的用户列表
      let result = await response.json();
      // 将待审批用户存入 pendingUsers 中
      pendingUsers.value = result.data;  // 假设接口返回的数据包含待审批的用户列表
      console.log(pendingUsers.value);
      // 更新弹窗的显示状态
      pendingUserList.value.show();
    } catch (error) {
      console.error('请求失败', error);  // 错误处理
    }
  }
```

##### 审批操作

- 子组件发送审批请求，调用成功后抛出事件让父组件感知

```js
const emit = defineEmits(['update-pending-users']);
// 通过用户
const approveUser = async (user) => {
  // 这里可以加入通过用户的逻辑，例如调用 API 或更新状态
  console.log(`用户 ${user.nickName} 已通过`);
  let result = await proxy.Request({
      url: api.approval,
      params: {
        userId: user.userId,
        groupId: props.groupId,
        isPassed: true
      },
    });
  if (!result) {
    return;
  }
  // 触发父组件更新列表的事件
  emit('update-pending-users');
  // 关闭弹窗
  cancelForm();
};
```

- 父组件感知审批操作

```html
<PendingUserList ref="pendingUserList" :pending-users="pendingUsers" :groupId="rowGroupId"
@update-pending-users="fetchPendingUsers"/>  
//监听待审批列表组件的审批操作
const fetchPendingUsers = async () => {
    loadDataList();
};
```

### 用户组搜索

#### 初版【包含用户组列表、检索、加入用户组操作】

- 2025-2-5：用户加入用户组采用搜索指定组后自行加入，然后由创建人审批【后续再考虑管理员】，原型变更如下
  - 要多一个搜索栏
  - 用户组管理列表要展示用户加入的用户组
    - 自己创建的组需要有审批按钮
    - 非自己创建的组要展示审批状态字段
  - 新增子目录——用户组检索，展示满足搜索条件的列表，根据用户是否已经加入动态显示加入按钮

- 新增用户组检索子目录【加目录的代码就不重复了，这里就展示组件内容】

```html
<template>
    <div>
        <div class="top">
            <div class="top-op">
                <div class="btn"></div>
                <div class="iconfont icon-refresh" @click="loadDataList"></div>
                <div class="search-panel">
                <!-- 搜索文件 -->
                <el-input
                  clearable
                  placeholder="请输入用户组名或用户组id"
                  v-model="keyword"
                  @keyup.enter="loadDataList"
                >
                    <template #suffix>
                        <i class="iconfont icon-search" @click="search"></i>
                    </template>
                </el-input>
              </div>
            </div>

        </div>
        <!-- 动态显示新建用户组表单 -->
        <NewUserGroup ref="newUserGroup"></NewUserGroup>
      <!-- 遍历文件列表 -->
      <div class="group-list" v-if="tableData.list && tableData.list.length > 0">
        <Table
          ref="dataTableRef"
          :columns="columns"
          :dataSource="tableData"
          :fetch="loadDataList"
          :initFetch="false"
          :options="tableOptions"
          @rowSelected="rowSelected"
        >
          <template #groupSize="{ row }">
            <span class="user-group-size" @click="showUserNames(row)">
              {{ row.currentSize }}/{{ row.maxSize }}
            </span>
          </template>
          <template #description="{ row }">
            <span 
              v-if="row.description.length > 10" 
              class="ellipsis" 
              :title="row.description"
              @mouseenter="showFullDescription(row)"
              @mouseleave="hideFullDescription(row)"
            >
              {{ row.description.slice(0, 10) }}...
            </span>
            <span v-else>{{ row.description }}</span>
          </template>
          <template #options="{ row }">
            <el-button type="primary" v-if="!row.isActive">
              <span @click="joinGroup(row)">申请加入</span>
            </el-button>
            <el-button type="warning" v-else>
              <span>申请成功</span>
            </el-button>          
          </template>
        </Table>
      </div>
    </div>
  </template>
<script setup>
import NewUserGroup from "./NewUserGroup.vue";
import { ref, reactive, getCurrentInstance, nextTick, computed, onMounted   } from "vue";
  const { proxy } = getCurrentInstance();
  const emit = defineEmits(["addFile"]);
  const api = {
      loadDataList: "/api/onlineEdit/userGroupList",
      joinGroup : "/onlineEdit/joinGroup"
  };

  const columns = [
      {
          label: "用户组号",
          prop: "groupId",
          width: 180,
      },
      {
          label: "用户组名",
          prop: "groupName",
          width: 180,
      },
      {
          label: "创建人名称",
          prop: "createByName",
          width: 180,
      },
      {
          label: "描述",
          prop: "description",
          scopedSlots:"description",
          width: 180,      
      },
      {
        label: "人数信息",
        prop: "groupSize",
        scopedSlots: "groupSize",
        width: 180,
      },
      {
          label: "操作",
          prop: "options",
          scopedSlots: "options",
      },
  ];
  
  onMounted(() => {
  // 在页面加载后调用 loadDataList 方法
    loadDataList();
  });
  const tableData = ref({});
  const tableOptions = ref({
      extHeight: 50,
      selectType: "checkbox",
  });
  
  const groupName = ref('');
  const loadDataList = async () => {
    try {
      // 构建查询参数
      let params = new URLSearchParams({
        groupName: groupName.value,
        type: 1
      }).toString();
      // 拼接到API接口地址
      let url = `${api.loadDataList}?${params}`;
      let response = await fetch(url, 
      {
        method: 'GET',
      });
      if (!response.ok) {
        throw new Error('请求失败');
      }
      let result = await response.json();
      tableData.value = result.data;  // 把接口返回的数据设置到 tableData
    } catch (error) {
      console.error("请求失败", error);  // 错误处理
    }
  };
  const showFullDescription = (row) => {
    row.isFullDescription = true; // 标记为显示完整描述
  };

  const hideFullDescription = (row) => {
    row.isFullDescription = false; // 恢复为简短描述
  };

  const joinGroup = async (row) => {
    const groupId = row.groupId; // 获取当前行的用户组ID
    try {
      let result = await proxy.Request({
        url: api.joinGroup+'/'+groupId,
      });

      if (result.success) {
        // 成功加入用户组后，可以更新界面显示状态
        row.isActive = true; // 修改当前行的 `isActive` 状态，标记用户已申请成功
        proxy.Message.success("申请加入成功");
      } else {
        // 如果后端返回失败
        proxy.Message.error("申请加入失败");
      }
    } catch (error) {
      console.error("请求失败", error); // 错误处理
      proxy.Message.error("请求失败，请稍后再试");
    }
  };
  </script>
  <style lang="scss" scoped>
  @import "@/assets/file.list.scss";
  </style>
```

### 文档管理

#### 原型

<img src="easypan.assets/image-20250206154623291.png" alt="image-20250206154623291" style="zoom:67%;" />.

- 上传文件【支持本地上传和云盘上传】
- 授权
  - 权限由多选框配置，用户组设置权限则组内所有人员的相应权限也设置
  - 点击保存就传对象数组，属性为用户id以及用户权限

#### 初版

- 实现了列表查询、文件跳转

```html
<template>
    <div>
        <div class="top">
            <div class="top-op">
                <div class="btn">
                <el-upload
                  :show-file-list="false"
                  :with-credentials="true"
                  :multiple="true"
                  :http-request="addFile"
                  :accept="fileAccept"
                >
                    <el-button type="primary">
                      <span class="iconfont icon-upload"></span>
                      上传
                    </el-button>
                </el-upload>
            </div>
              <el-button type="success" @click="newFolder">
                选择文件
              </el-button>
                <div class="iconfont icon-refresh" @click="loadDataList"></div>
                <div class="search-panel">
              </div>
            </div>
        </div>
      <div class="group-list" v-if="tableData.list && tableData.list.length > 0">
        <Table
          ref="dataTableRef"
          :columns="columns"
          :dataSource="tableData"
          :fetch="loadDataList"
          :initFetch="false"
          :options="tableOptions"
          @rowSelected="rowSelected"
        >
        <template #filename="{ row }">
            <span @click="goToFile(row.storageAddress)" class="filename-link">{{ row.filename }}</span>
        </template>
        <template #options="{ row }">
          <el-button type="success" @click="getPendingUser(row)">
                授权
          </el-button>
          <el-button  type="danger" @click="newFolder">
                禁用
          </el-button>
        </template>
        </Table>
      </div>
    </div>
  </template>
<script setup>
// 导入组件
import { ref, reactive, getCurrentInstance, nextTick, computed, onMounted   } from "vue";
  const { proxy } = getCurrentInstance();
  const emit = defineEmits(["addFile"]);
  const api = {
      loadDataList: "/api/onlineEdit/uploadFileList",
  };
  const columns = [
      {
          label: "文件名",
          prop: "filename",
          scopedSlots: "filename",
          width: 300,
      },
      {
          label: "状态",
          prop: "status",
          width: 300,
      },
      {
          label: "操作",
          prop: "options",
          scopedSlots: "options",
      },
  ];
  onMounted(() => {
  // 在页面加载后调用 loadDataList 方法
    loadDataList();
  });
  const tableData = ref({});
  const tableOptions = ref({
      extHeight: 50,
      selectType: "checkbox",
  });
    const loadDataList = async () => {
    try {
      let response = await fetch(api.loadDataList, 
      {
        method: 'GET',
      });
      if (!response.ok) {
        throw new Error('请求失败');
      }
      let result = await response.json();
      tableData.value = result.data;  // 把接口返回的数据设置到 tableData
    } catch (error) {
      console.error("请求失败", error);  // 错误处理
    }
  };

  // 多选
  const selectFileIdList = ref([]);
  const rowSelected = (rows) => {
    selectFileIdList.value = [];
    rows.forEach((item) => {
      selectFileIdList.value.push(item.fileId);
    });
  };
  
    // 文件名点击事件
    const goToFile = (filename) => {
        const url = `http://192.168.32.100:9000/example/editor?fileName=${encodeURIComponent(filename)}`;
        console.log(url);
        window.location.href = url; // 跳转到新页面
    };
  </script>
  <style lang="scss" scoped>
  @import "@/assets/file.list.scss";  
  </style>

```

#### 本地文件上传

- 上传采用全量上传

```js
<el-upload
  :show-file-list="false"
  :with-credentials="true"
  :multiple="true"
  :http-request="addFile"
  :accept="fileAccept"
>
    <el-button type="primary">
      <span class="iconfont icon-upload"></span>
      上传
    </el-button>
</el-upload>
const fileAccept = ".xlsx, .doc, .docx, .pdf,.csv";  // 允许上传的文件类型
const addFile = async (file) => {
  const formData = new FormData();
  let fileObject=file.file;
  formData.append("file", fileObject);
  formData.append("fileName", fileObject.name);
  try {
    const response = await fetch(api.uploadFile, {
      method: 'POST',
      body: formData,
    });
    if (response.ok) {
      const result = await response.json();
      console.log('上传成功', result);
      loadDataList();
    } else {
      throw new Error('上传失败');
    }
  } catch (error) {
    console.error('文件上传出错', error);
  }
};
```

### 统计规则配置

#### 原型

<img src="easypan.assets/image-20250210091612165.png" alt="image-20250210091612165" style="zoom:80%;" />.

#### 表单组件

```html
<template>
  <div>
    <Dialog
      :show="dialogConfig.show"
      :title="'统计规则配置'"
      width="800px"
      :showCancel="false"
      @close="dialogConfig.show = false"
    >
      <div class="table-container" style="height: 300px;">
        <Table
          ref="dataTableRef"
          :columns="columns"
          :dataSource="ruleData"
          :fetch="getColMetaData"
          :initFetch="false"
          :options="tableOptions"
        >
          <template #type="{ row }">
            <el-select v-model="row.type" placeholder="请选择配置规则" style="width: 100%;">
              <el-option
                v-for="(option, key) in typeEnum"
                :key="key"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </template>

          <!-- 数值区间的插槽 -->
          <template #range="{ row }">
            <div v-if="row.type === typeEnum.RANGE.value">
              <el-tooltip class="item" effect="dark" content="给定数据最大最小值，形如‘最小值-最大值’" placement="top">
                <el-input
                  v-model="row.dataRange"
                  placeholder="请输入数值区间"
                  style="width: 100%;"
                />
              </el-tooltip>
            </div>
            <div v-if="row.type === typeEnum.SET.value">
              <el-tooltip class="item" effect="dark" content="以英文逗号分隔，形如‘元素1,元素2,元素3’" placement="top">
                <el-input
                  v-model="row.dataRange"
                  placeholder="请输入数据集"
                  style="width: 100%;"
                />
              </el-tooltip>
            </div>
          </template>
        </Table>
        <!-- 保存按钮 -->
        <el-button type="primary" @click="saveConfig" style="margin-top: 20px;">保存配置</el-button>
      </div>
    </Dialog>
  </div>
</template>


<script setup>
import { ref, defineProps, defineExpose } from 'vue';
import { ElMessage } from 'element-plus'; // 引入 Element Plus 的 Message 组件

const props = defineProps({
  ruleData: {
    type: Array,
    required: true,
  },
});

// 枚举数据
const typeEnum = {
  SUM: { value: 0, label: '总和' },
  AVG: { value: 1, label: '平均数' },
  MODE: { value: 2, label: '众数' },
  MEAN: { value: 3, label: '中位数' },
  COUNT: { value: 4, label: '计数' },
  RANGE: { value: 5, label: '数值区间' },
  SET: { value: 6, label: '数据集' },
};
const columns = [
  {
    label: "字段名",
    prop: "colName",
    width: 200,
  },
  {
    label: "配置规则",
    prop: "type",
    scopedSlots: "type",  // 使用自定义插槽
  },
  {
    label: "数据范围(检查区间内哪些元素未填写)",
    prop: "range",
    scopedSlots: "range",  // 使用自定义插槽
  },
];

const dialogConfig = ref({
  show: false,
});

// 显示弹窗
const show = () => {
  dialogConfig.value.show = true;
  console.log(ruleData);
};

// 关闭弹窗
const cancelForm = () => {
  dialogConfig.value.show = false;
};

// 保存配置
const saveConfig = async () => {
  try {
    // 构造保存时需要提交的数据
    const saveData = ruleData.value.map(row => ({
      colName: row.colName,
      type: row.type,
      dataRange: row.dataRange || null, // 如果没有数据范围则传 null
    }));
    // 发送请求保存配置
    const response = await fetch(api.saveConfigUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(saveData),
    });

    if (!response.ok) {
      throw new Error('保存失败');
    }
    const result = await response.json();
    if (result.status === "error") {
      throw new Error(result.info);
    }
    // 提示保存成功
    ElMessage.success('配置已保存！');
    cancelForm(); // 保存成功后关闭弹窗
  } catch (error) {
    console.error('保存配置失败', error);
    ElMessage.error('保存配置失败！');
  }
};

defineExpose({ show });
</script>

<style lang="scss" scoped>
.user-list {
  list-style-type: none;
  padding: 0;
  margin: 0;
}

.user-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.user-name {
  font-weight: bold;
  color: #333;
  flex-grow: 1;
}

.action-buttons {
  display: flex;
  gap: 10px;
}

.no-users {
  text-align: center;
  font-size: 16px;
  color: #888;
}
</style>

```

<img src="easypan.assets/image-20250210133238239.png" alt="image-20250210133238239" style="zoom: 67%;" />.

#### 保存配置方法

```js
const props = defineProps({
  ruleData: {
    type: Array,
    required: true,
  },
  fileId :{
    type: Number,
    required: true,
  }
});
const api = {
      saveConfigUrl: "/api/colRules/saveRuleList"
};
// 保存配置
const saveConfig = async () => {
  try {
    // 构造保存时需要提交的数据
    const saveData = props.ruleData.list.map(row => ({
      id: row.id,
      colIndex:row.colIndex,
      type: row.type,
      dataRange: row.dataRange || null, // 如果没有数据范围则传 null
    }));
    // 发送请求保存配置
    const response = await fetch(api.saveConfigUrl +"/" +props.fileId, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(saveData),
    });
    if (!response.ok) {
      throw new Error('保存失败');
    }
    const result = await response.json();
    if (result.status === "error") {
      throw new Error(result.info);
    }
    // 提示保存成功
    ElMessage.success('配置已保存！');
    cancelForm(); // 保存成功后关闭弹窗
  } catch (error) {
    console.error('保存配置失败', error);
    ElMessage.error('保存配置失败！');
  }
};
```

#### 2025-2-14变更

- 希望配置的规则可以叉掉

```html
<!-- 添加“清除”按钮 -->
<template #actions="{ row }">
	<el-button type="danger" @click="clearRule(row)" size="small">清除</el-button>
</template>
// 清除指定规则
const clearRule = (row) => {
  row.type = null;
  row.dataRange = null;
  ElMessage.info('规则已清除！');
};
```

- 提示后端的异常信息

```js
catch (error) {
    console.error('保存配置失败', error);
    ElMessage.error(error.message);
}
```

#### 2025-2-24变更

- 配置规则支持设置通知时间

```html
<!-- 日期选择器和保存按钮放在同一行 -->
<div class="actions-container">
  <el-date-picker
    v-model="selectedDateTime"
    type="datetime"
    placeholder="选择日期和时间"
    format="YYYY/MM/DD HH:mm"
    value-format="YYYY-MM-DD HH:mm:ss"
    class="inline-datetime-picker"
  />

  <el-button type="primary" @click="saveConfig">保存配置</el-button>
</div>
/* 设置按钮容器居右 */
.actions-container {
  display: flex;
  justify-content: space-between; /* 日期选择器和按钮在同一行 */
  align-items: center; /* 垂直居中对齐 */
  margin-top: 20px; /* 添加适当的顶部间距 */
}

.inline-datetime-picker {
  margin-right: 20px; /* 日期选择器和保存按钮之间有适当的间距 */
}
```

- 保存的时候需要携带两部分数据

```js
// 保存配置
const saveConfig = async () => {
  try {
    // 验证日期时间是否已选择
    if (!selectedDateTime.value) {
      ElMessage.warning('请选择日期和时间');
      return;
    }
    // 组合保存数据
    const saveData = {
      rulesEntityList: props.ruleData.list.map(row => ({
        id: row.id,
        colIndex: row.colIndex,
        type: row.type,
        dataRange: row.dataRange || null,
      })),
      scheduleDateTime: selectedDateTime.value // 添加日期时间配置
    };
    // 调用保存接口
    const response = await fetch(`${api.saveConfigUrl}/${props.fileId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(saveData),
    });
    const result = await response.json();
    if (result.status === "error") {
      throw new Error(result.info);
    }
    ElMessage.success('配置保存成功');
    dialogConfig.value.show = false;
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error(error.message || '保存配置失败');
  }
};
```

- 弹出表单的时候需要回显配置的时间【TODO】

## 遗留事项

### 用户组管理

#### 最大人数限制未生效，退群、解散功能不支持

![image-20250206151115498](easypan.assets/image-20250206151115498.png)

#### 审批状态没有区分创建人，自己创建的也展示

![image-20250206151157108](easypan.assets/image-20250206151157108.png)

#### 分页操作不支持【低优先级】

### 文档管理

#### 配置规则弹窗留白【已解决】

- 配置权限规则前端弹窗有大片空白，之后看看能不能优化

<img src="easypan.assets/image-20250214162600285.png" alt="image-20250214162600285" style="zoom:50%;" />.

- 2025-2-20已解决

1. 表格组件有这个设置高度的属性，可以显式指定组件的options来调整

```js
const tableHeight = ref(
    props.options.tableHeight
    ? props.options.tableHeight
    : window.innerHeight - topHeight - props.options.extHeight
);
```

2. 父组件配置高度，这个值越大，离表头的距离就越短，这个值有可能是距离页面底框的高度

```js
  <Table
    ref="dataTableRef"
    :columns="columns"
    :dataSource="ruleData"
    :fetch="getColMetaData"
    :initFetch="false"
    :options="tableOptions"
    style="flex: 1;"
  >
const tableOptions = ref({
  extHeight: 350
});
```

