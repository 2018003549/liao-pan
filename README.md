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

