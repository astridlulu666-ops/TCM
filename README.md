# 智汇千方-基于多源异构数据融合的数据库分析与管理系统- 运行指南
文件夹说明：
1.itcmdas_web.war文件为项目可部署在tomcat工具中的文件
2.bin文件夹中存放的是已编译文件
3.classes文件夹中存放的是所创建的类
4.lib文件夹中存放需要调用的包
5.src文件夹中存放项目后端源代码
6.web文件夹中存放项目前端源代码
7.temp_war文件夹中存放用于tomcat服务器部署时需要的文件
8.测试数据文件：存放可用于测试系统分析功能的实例数据文件

## 环境准备

在开始之前，请确保您已经安装了以下软件：

1. **JDK (Java Development Kit) 8或更高版本**
   - 下载地址：https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html

2. **Apache Tomcat 8.0或更高版本**
   - 下载地址：https://tomcat.apache.org/download-80.cgi
   - 选择 "Binary Distributions" -> "Core" -> "zip" 格式下载

## 环境变量配置

### Windows系统

1. 右键点击"此电脑" -> "属性" -> "高级系统设置" -> "环境变量"

2. 新建系统变量：
   - **变量名**: `JAVA_HOME`
   - **变量值**: `C:\Program Files\Java\jdk1.8.0_291` (根据您的JDK安装路径调整)

3. 新建系统变量：
   - **变量名**: `CATALINA_HOME`
   - **变量值**: `C:\apache-tomcat-8.5.70` (根据您的Tomcat安装路径调整)

4. 编辑系统变量 `Path`，添加以下内容：
   - `%JAVA_HOME%\bin`
   - `%CATALINA_HOME%\bin`

### 验证配置

打开命令提示符，输入以下命令验证配置是否正确：

```bash
java -version
javac -version
catalina version
```

如果命令都能正常执行并显示版本信息，则配置成功。

## 编译项目

由于项目没有使用Maven或Gradle等构建工具，我们需要手动编译Java类。

### 1. 收集所有依赖JAR文件

项目的依赖JAR文件已存放在 `itcmdas_web/web/WEB-INF/lib/` 目录中，包括：
- MySQL驱动 (mysql-connector-java-8.0.13.jar)
- FastJSON (fastjson-1.2.60.jar)
- HanLP (hanlp-1.7.8.jar)
- Commons FileUpload (commons-fileupload-1.3.3.jar)
- Commons IO (commons-io-2.8.0.jar)
- Servlet API (javax.servlet-api-4.0.1.jar)

此外，还需要Tomcat的servlet-api.jar（已在JAR列表中）。

### 2. 创建编译目录

```bash
mkdir -p e:\lulu_homework\JAVA\system_develop\Medicine-master\itcmdas_web\build\classes
```

### 3. 编译Java类

```bash
cd e:\lulu_homework\JAVA\system_develop\Medicine-master\itcmdas_web

javac -cp "web\WEB-INF\lib\*" -d build/classes src/com/itcmdas/dao/*.java src/com/itcmdas/hanlp/*.java src/com/itcmdas/service/*.java src/com/itcmdas/servlet/*.java src/com/itcmdas/test/*.java src/com/itcmdas/util/*.java src/com/itcmdas/vo/*.java
```

## 部署到Tomcat

### 1. 创建Web应用目录结构

```bash
mkdir -p %CATALINA_HOME%\webapps\itcmdas_web\WEB-INF\classes
mkdir -p %CATALINA_HOME%\webapps\itcmdas_web\WEB-INF\lib
```

### 2. 复制JAR依赖文件

```bash
xcopy /E e:\lulu_homework\JAVA\system_develop\Medicine-master\itcmdas_web\web\WEB-INF\lib\* %CATALINA_HOME%\webapps\itcmdas_web\WEB-INF\lib\
```

### 3. 复制Web资源文件

```bash
xcopy /E e:\lulu_homework\JAVA\system_develop\Medicine-master\itcmdas_web\web\* %CATALINA_HOME%\webapps\itcmdas_web\
```

### 4. 复制编译后的class文件

```bash
xcopy /E e:\lulu_homework\JAVA\system_develop\Medicine-master\itcmdas_web\build\classes\* %CATALINA_HOME%\webapps\itcmdas_web\WEB-INF\classes\
```

### 5. 创建必要的目录

```bash
mkdir -p %CATALINA_HOME%\webapps\itcmdas_web\upload
mkdir -p %CATALINA_HOME%\webapps\itcmdas_web\temp
mkdir -p %CATALINA_HOME%\webapps\itcmdas_web\hanlp-data
```

## 启动Tomcat服务器

### 1. 启动Tomcat

```bash
%CATALINA_HOME%\bin\startup.bat
```

### 2. 验证Tomcat是否启动成功

打开浏览器，访问：http://localhost:8080

如果看到Tomcat的欢迎页面，则启动成功。

## 访问应用

在浏览器中输入以下地址访问智能中医药数据分析系统：

http://localhost:8080/itcmdas_web/

## 停止Tomcat服务器

当您完成使用后，可以停止Tomcat服务器：

```bash
%CATALINA_HOME%\bin\shutdown.bat
```

## 常见问题与解决方案

### 1. 编译错误：找不到符号

**原因**：缺少依赖的JAR文件或类路径配置错误。

**解决方案**：
- 确保所有必需的JAR文件都已包含在classpath中
- 检查JAR文件路径是否正确

### 2. 启动Tomcat后无法访问应用

**原因**：
- 应用部署路径错误
- Tomcat端口被占用
- 应用内部错误

**解决方案**：
- 检查应用是否正确部署到%CATALINA_HOME%\webapps\itcmdas_web目录
- 检查Tomcat的日志文件 (%CATALINA_HOME%\logs\catalina.out) 查看具体错误信息
- 尝试更改Tomcat的端口号 (在%CATALINA_HOME%\conf\server.xml中修改)

### 3. 文件上传功能失败

**原因**：
- upload或temp目录不存在
- 目录没有写入权限

**解决方案**：
- 确保%CATALINA_HOME%\webapps\itcmdas_web\upload和%CATALINA_HOME%\webapps\itcmdas_web\temp目录存在
- 检查目录的写入权限

### 4. HanLP相关功能失败

**原因**：
- hanlp-data目录不存在或缺少必要的数据文件

**解决方案**：
- 确保%CATALINA_HOME%\webapps\itcmdas_web\hanlp-data目录存在
- 首次运行时，HanLP会自动下载所需的数据文件，请确保网络连接正常

## 项目结构说明

- `src/`: Java源代码目录
- `web/`: Web资源目录 (JSP、HTML、CSS、JS等)
- `lib/`: 依赖JAR文件目录
- `build/`: 编译输出目录
- `%CATALINA_HOME%\webapps\itcmdas_web/`: Tomcat部署目录

## 功能说明

1. **频次分析**：统计处方中单味药、药对、三元组的出现频次
2. **关联分析**：基于Apriori算法的药物关联规则分析
3. **聚类分析**：基于复杂系统熵聚类的药物组合分析
4. **数据库管理**：基于团队收集的百万级中药复方-成分-靶点数据库进行的数据库管理服务

## 联系方式

如果在运行过程中遇到任何问题，请参考项目文档或联系系统管理员。
