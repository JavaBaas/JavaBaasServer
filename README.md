# JavaBaas
**JavaBaas** 是基于Java语言开发的后台服务框架，其核心设计目标是实现移动客户端、网页应用的后台数据存储、物理文件存储、消息推送等功能，极大的降低后台开发难度，实现快速开发。

使用 **JavaBaas** 提供的工具进行简单的配置，即可完成后台搭建。后台搭建成功后，即可使用RestAPI、iOS/Android-SDK进行数据的操作。

项目地址：

* [码云](https://gitee.com/javabaas/JavaBaas)

技术讨论群：479167886

完整文档参见：[www.javabaas.com](http://www.javabaas.com/)（原域名备案出现问题，需要重新备案，目前在备案流程中，暂时可以使用[www.zangyilin.com](http://www.zangyilin.com/)访问）

## 主要功能
* 结构化数据存储
* RestAPI接口
* iOS/Android SDK
* 物理文件存储
* ACL权限管理机制
* 用户系统
* 消息推送

## 快速上手

### 一、安装
#### 1.Docker方式

JavaBaas提供了完整的Docker演示环境(包含数据库等)，需安装[Docker](https://www.docker.com)后使用。

```
docker pull javabaas/javabaas-starter
docker run -p 8080:8080 javabaas/javabaas-starter
```

执行成功后，即可使用本地8080端口访问JavaBaas环境。

以上的执行方式每次会创建全新的执行环境，如需要持久化数据，需使用`DockerVolume`。

```
docker run -p 8080:8080 -v /usr/javabaas:/data/db javabaas/javabaas-starter
```

其中`/usr/javabaas`为本地数据存储目录，请创建该目录或修改为你所需要的目录。

#### 2.Jar包方式
也可以选择使用jar包方式启动JavaBaas。

##### JDK
JavaBaas基于JDK1.8编写，编译及运行需要安装JDK1.8环境。

提示: 在Oracle官网可以下载最新的[JDK安装包](http://www.oracle.com/technetwork/java/javase/downloads/index.html)。

##### MongoDB
JavaBaas使用MongoDB作为存储数据库，请先正确安装并启动MongoDB数据库。

提示: 在MongoDB官网可以下载最新的[MongoDB官网](http://www.mongodb.com)。

##### Redis
JavaBaas使用Redis作为缓存引擎，请先正确安装并启动Redis数据库。

提示: 在Redis官网可以下载最新的[Redis官网](http://redis.io/)。

##### 启动
Server目录下的`JavaBaas.jar`为系统启动文件，系统依赖环境配置正确后，使用以下命令启动系统：

`java -jar JavaBaas.jar`

看到以下信息，表明系统启动成功。

```
[main] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 8080 (http)
[main] c.s.b.c.l.ApplicationEventListener       : JavaBaasServer started.
[main] c.j.s.c.l.ApplicationEventListener       : Key: JavaBaas
[main] c.j.s.c.l.ApplicationEventListener       : Timeout: 600000
[main] c.s.b.c.l.ApplicationEventListener       : Host:127.0.0.1
[main] c.j.s.c.l.ApplicationEventListener       : JavaBaas status at http://127.0.0.1:8080
[main] c.j.s.c.l.ApplicationEventListener       : Browse REST API at http://127.0.0.1:8080/explorer.html
[main] com.staryet.baas.Main                    : Started Main in 2.653 seconds (JVM running for 3.232)
```

### 二、命令行工具
`JavaBaas`系统成功启动后，默认将在http 8080端口监听所有用户请求。此时首先要使用命令行工具`JBShell`创建应用。

命令行工具`JBShell`是JavaBaas的配套工具，使用`JBShell`可以完成应用的创建删除、类的创建删除、字段的创建删除、对象的增删改查等操作，以及一些便捷的辅助功能。

`JBShell`基于java编写，编译及运行需要安装JDK环境。

#### Mac&Linux安装
在命令行中执行

```
curl -s "http://get.javabaas.com/jbshell.sh" | bash
```

安装成功后将看到以下信息

```
成功安装jbshell
请打开一个新的终端，或者在当前终端执行下面命令:
    source "/Users/staryet/.javabaas/jbshell/export/jbexport"
开始造吧!!!
```

打开一个新窗口，或在当前窗口执行以上命令即可完成`JBShell`工具安装。

#### Windows安装


#### 启动
在命令行中使用以下命令启动`JBShell`工具：

```
jb
```

启动成功后显示以下信息

```
   ___                     ______
  |_  |                    | ___ \
    | |  __ _ __   __ __ _ | |_/ /  __ _   __ _  ___
    | | / _` |\ \ / // _` || ___ \ / _` | / _` |/ __|
/\__/ /| (_| | \ V /| (_| || |_/ /| (_| || (_| |\__ \
\____/  \__,_|  \_/  \__,_|\____/  \__,_| \__,_||___/
Version:1.0.0
Host:http://localhost:8080/api/
AdminKey:JavaBaas
BAAS>
```

### 三、构建数据

#### 创建应用
在`JavaBaas`中，最高层的组织结构为`应用`，各应用之间权限、用户、数据相互隔离。为了开始使用`JavaBaas`，我们首先需要创建一个应用。

首先启动命令行工具，在命令行工具中，使用命令`app add Blog`，创建一个博客应用。

```bash
BAAS>app add Blog
应用创建成功
设置当前应用为 Blog
```
现在，我们成功创建了一个名为`Blog`的应用。

提示: 一般情况下建议使用大写字母开头做为应用名称

#### 查看应用列表
在命令行工具中，使用`apps`命令可以查看当前`JavaBaas`中所有的应用。

```
BAAS>apps
Blog
```
现在我们可以看到，系统中只有我们刚刚创建的，名为`Blog`的一个应用。

#### 切换当前应用
在命令行工具中，使用`use`命令即可切换当前应用，切换应用后，即可为当前应用创建类来存储数据。

```
BAAS>use Blog
设置当前应用为 Blog
```
现在我们的应用即切换为刚刚创建的`Blog`。

#### 创建类
在`JavaBaas`中，数据使用`类`进行组织。用户可以自由创建类，类名需使用英文字母开头且名称中只能包含数字与英文字母。类创建后，需要在类中创建字段以存储数据。同时、系统初始化后会自动创建用户类、设备类、文件类等系统内建类，内建类名使用下划线`_`开头，系统内建类禁止删除或修改。

现在我们使用命令`class add Article`创建一个类用于存储博客中的文章信息。

```
Blog>class add Article
类创建成功
```
现在我们便在`Blog`应用中创建了名为`Article`的类。

提示: 一般情况下建议使用大写字母开头做为类名称

#### 查看类列表
使用`class`命令，可以查看当前应用下所有的类。

```
Blog>class
_File(0)
_Installation(0)
_PushLog(0)
_User(0)
Article(0)
```
可以看到，当前应用中存在系统内建的文件类、设备类、推送日志类、用户类，以及我们刚刚创建的用于存储文章的`Article`类。

#### 切换当前类
成功创建`Article`类后，我们需要为类创建用于存储文章标题、作者等信息的字段以存储数据。首先，我们需要将当前类切换至`Article`以便在此类下创建字段。

使用`set Article`命令，将当前类切换至`Article`

```
Blog>set Article
设置当前类为 Article
Blog Article>
```
切换完成后，光标变为`Blog Article>`，表示当前应用为`Blog`，当前类为`Article`。

#### 创建字段
使用`field add title`命令，在`Article`类中创建一个用于存储文章标题的字符型字段。

```
Blog Article>field add title
请选择FieldType 默认为STRING
1 STRING
2 NUMBER
3 BOOLEAN
4 DATE
5 FILE
6 OBJECT
7 ARRAY
8 POINTER
0 取消
>1
创建字段成功
```
创建字段时，需要选择[数据类型](/overview/object.md#数据类型)。此处我们选择1(字符型)。现在我们就在类`Article`中创建了名为`title`的字符型字段。

使用`field add read`命令，在`Article`类中创建一个用于存储文章浏览次数的数值型字段。

```
Blog Article>field add read
请选择FieldType 默认为STRING
1 STRING
2 NUMBER
3 BOOLEAN
4 DATE
5 FILE
6 OBJECT
7 ARRAY
8 POINTER
0 取消
>2
创建字段成功
```
现在我们在类`Article`中创建了名为`read`的数值型字段。

提示: 一般情况下建议使用小写字母开头做为字段名称

#### 查看字段列表
使用命令`fields`即可查看当前类中的所有字段列表。

```
Blog Article>fields
   <NUMBER>  read
   <STRING>  title
```
可以看到，当前类中存在我们刚创建的两个字段。

#### 存储数据
成功创建类并添加字段后，我们就可以开始存储数据操作了(包括使用`RestAPI`以及`iOS/Android-SDK`)。此处我们先使用命令行工具进行基本的增删改查操作。

#### 插入数据
使用命令`add`在`Article`类中插入数据。

```
Blog Article>add {"title":"StarWars","read":10}
对象创建.
```

现在我们便在`Article`类中插入了一条数据记录。

提示: 在 JavaBaas 中，所有数据以json形式存储。详情参见[数据存储](/overview/object.md)文档。

#### 查询数据
使用命令`list`查询`Article`类中的所有数据。

```
Blog Article>list
{"className":"Article", "objectId":"06904d004d2d462b888800abb9d03a8b", "updatedAt":"1509953994688", "createdAt":"1509953994688", "serverData":{"read":10,"title":"StarWars"}}
```

可以看到，现在`Article`类中只有一条刚刚创建的数据。

#### 表格打印
使用命令`table`打印`Article`类中的所有数据。

```
Blog Article>table
┌──────────────────────────────────┬────────────────────┬────────────────────┐
│ id                               │ read               │ title              │
│ <STRING>                         │ <NUMBER>           │ <STRING>           │
├──────────────────────────────────┼────────────────────┼────────────────────┤
│ 06904d004d2d462b888800abb9d03a8b │ 10                 │ StarWars           │
└──────────────────────────────────┴────────────────────┴────────────────────┘
```

#### 删除数据
使用命令`del id`，删除指定数据，其中 `id` 为刚才查询结果中的id。

```
Blog Article>del 06904d004d2d462b888800abb9d03a8b
对象删除.
```

### 四、使用RestAPI测试工具
我们已经成功创建了应用，构建了数据结构，并存储了一些数据。我们可以使用`客户端SDK`、`REST API`存取数据了。

为了方便测试API接口，`JavaBaas`内建了`RestAPI测试工具`。启动成功后，使用浏览器访问`http://127.0.0.1:8080/explorer.html`页面即可看到API测试工具。

<img src="http://oyzzvjk5k.bkt.clouddn.com/17-11-6/32511675.jpg" width="60%">

在文本框输中输入AdminKey(默认为JavaBaas)并回车，然后在下拉菜单中选择应用，此处我们选择刚刚创建的博客应用Blog。

<img src="http://oyzzvjk5k.bkt.clouddn.com/17-11-6/29899411.jpg" width="60%">

此时会显示应用中所有类的列表，此处可以看到我们已经创建的Article类，点击后，会展示对应类的数据操作接口(增删改查、计数等)。

<img src="http://oyzzvjk5k.bkt.clouddn.com/17-11-6/54361529.jpg" width="60%">

展开GET方法，点击测试按钮，即可请求查询接口。

<img src="http://oyzzvjk5k.bkt.clouddn.com/17-11-6/36465067.jpg" width="60%">

测试后，可以看到请求结果，此处看到的数据即为我们刚才使用命令行工具添加的数据。

<img src="http://oyzzvjk5k.bkt.clouddn.com/17-11-6/96312821.jpg" width="60%">

提示: API测试工具中会显示完整的请求结构，可以参照此处显示的请求构建http请求。

下面我们使用`RestAPI测试工具`向`JavaBaas`中添加一条数据。选择POST方法，并按照Article类的数据格式构建一条新数据，然后点击测试按钮。

<img src="http://oyzzvjk5k.bkt.clouddn.com/17-11-6/16742048.jpg" width="60%">

现在我们再调用GET方法，查询表中的数据，可以看到，此时表中已经有了两条记录。

### 五、使用客户端SDK
在生产环境中，我们可以使用`客户端SDK`、`REST API`存取数据。详见`客户端SDK`以及`REST API`相关文档。


## 自定义配置
### 配置文件
Server目录下的`application.properties`为配置文件。

### MongoDB配置
在application.properties中配置MongoDB数据库连接信息。

```
spring.data.mongodb.host = 127.0.0.1 //MongoDB数据库地址 默认为127.0.0.1
spring.data.mongodb.database = baas //用于存储数据的数据库名称 默认为baas
spring.data.mongodb.username = baas //用户名 不填写为无身份校验
spring.data.mongodb.password = baas //密码 不填写为无身份校验
spring.data.mongodb.authentication-database = admin //用于校验身份的数据库
```

### Redis配置
在application.properties中配置Redis数据库连接信息。

```
spring.redis.host = 127.0.0.1 //Redis数据库地址
```

### 监听端口
在`application.properties`中配置监听端口，不设置默认为8080。

```
server.port = 8080
```

### 超级密钥
超级密钥用于鉴定管理员的超级权限，系统的核心管理接口需要使用此权限进行调用，默认密钥为`JavaBaas`。超级密钥可以自行设置，建议使用32位随机字符串。如：`c3ca79cca3c24147902c1114640268a5`。

在`application.properties`中配置超级密钥，不设置默认为`JavaBaas`。

注意: 为了保障数据安全，强烈建议不要使用默认密钥！

```
baas.auth.key = c3ca79cca3c24147902c1114640268a5
```

### 服务器地址
JavaBaas需要接收外部系统回调请求，因此需要配置系统部署服务器的ip地址。（本地测试时可使用127.0.0.1代替，生产环境需配置公网ip地址。）

例如，在`application.properties`中配置当前服务器ip信息。

```
host = http://58.132.171.126/
```

### 七牛云存储
为了使用七牛云存储作为物理文件存储引擎，需要配置七牛云存储相关信息。
在`application.properties`中配置以下信息。

```
qiniu.ak = 七牛云存储的帐号ak
qiniu.sk = 七牛云存储的帐号sk
qiniu.bucket = bucket名称
qiniu.file.host = bucket的存储域名
```

## 常见问题