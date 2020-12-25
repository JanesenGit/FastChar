FastChar更新日志
2019-08-15:
1、增加常量logExtract配置，布尔值！是否打印解压jar的日志。
2、解决FastSql在Java1.7时的错误。
3、优化附件上传功能。

V1.0.6:
1、新增添加验证器指定到指定索引，check(int index,String validator);
2、新增表格列加解密IFastColumnSecurity接口，可代理表格数据的加解密！

V1.0.7:
1、优化路由转发器和FastAction识别！
2、解决forward方法无法重定向到静态网页问题！
3、优化FastFile附件类！
4、优化FastRequestLog日志打印，新增打印远程接口地址和非200状态下的响应内容！
5、优化engine.getConfig方法，新增唯一标识码参数，让一个配置类可以多重配置！
6、优化FastEntity类！
7、优化FastDispatch类，增加跨域错误日志信息！
8、优化跨域配置，新增指定域名跨域！
9、新增AFastHttpMethod注解，可指定路由的HttpMethod。
10、新增IFastWebRun接口，继承自IFastWeb，可监听onRun方法，当web服务器全部初始化结束开始运行
11、优化toEntity方法，解决前缀判断问题！
12、优化Velocity引擎模板！
13、优化getParamToMapList方法，取消自主排序，默认以参数提交的顺序为准！

V1.0.8
1、优化路由器！
2、优化代理器！
3、解决windows环境下空格和编码问题！
4、优化FastChar引擎！
5、优化FastPath！
6、优化扫描器！

V1.0.9-V1.1.0
1、优化扫描器！解决部分在main环境下无法扫描到插件jar包问题！

V1.1.2
1、优化FastNumberUtils！提高性能！
2、优化FastDb！
3、新增FastMapWrap工具类！
4、整体细节功能优化提高性能！

V1.1.3-1.1.4
1、修复参数验证问题！

V1.1.5
1、新增参数转换器FastNormalParamConverter，可直接在方法形参声明中获取request或response
2、优化部分功能！

V1.1.6-1.1.8
1、解决fast-database.xml若干问题！
2、优化部分功能！

V1.1.9
1、修复数据库无法创建问题！
2、优化部分功能！

V1.2.0
1、解决FasJson转json字符串日期格式问题！
2、优化部分方法名不规范!
3、优化部分功能！

V1.2.1
1、新增模块打包后的jar动态更新与卸载功能。
2、优化FastEntity执行sql方法，增加是否缓存的参数！
3、优化部分功能。

V1.2.2
1、去除update方法设置默认值功能！
2、优化FastJson转换。
3、优化部分功能。

V1.2.3
1、优化System.out！
2、新增IFastException接口，可自己拦截通过e.printStackTrace()打印的异常信息！
3、优化FastAction，新增删除cookie方法！
4、优化FastScanner，开放加载、卸载和更新插件jar包方法，loadJar、unloadJar和updateJar
5、新增FastDatabaseXml核心类，拆分了fast-database.xml解析类，使开发者可自行随时调用加载fast-database.xml文件！
6、优化部分功能，提高框架性能！

V1.2.4
1、优化FastChar核心组件类，新增getThreadLocalAction方法，可获得当前线程下的FastAction对象。
2、优化FastUrl解析！
3、优化MySql连接url！
4、优化数据库自动创建表格问题！
5、优化FastProperties，改为继承FastMapWrap，更方便获取值！
6、优化数据库大小写问题！
7、增加[__accept]参数检测，可强制设置Out输出的Content-Type!

V1.2.5
1、优化sql日志打印，新增显示获取或处理的条数！
2、优化了错误信息提示！
3、优化动态更新Jar包，解决路由未更新的问题！
4、优化FastEntity属性，当调用remove方法时，将同时移除修改的标识！
5、优化FastSql中的appendWhere方法！


V1.5.0
1、优化核心组件功能！
2、优化FastSql类！
3、解决MySql链接问题，针对MySql8.0以上版本。
4、新增IFastScannerExtract接口，监听Jar解压文件！
5、优化扫描Jar！
6、优化FastOverrides代理器，新增newInstances和singleInstances可返回多个实现类的对象！
7、解决在Java14版本运行错误问题！
8、优化解压Jar包，解决排除必要文件异常！
9、优化异常拦截！
10、新增@AFastOverrideError注解，当无法获取到目标类的代理时候，将抛出自定义的异常信息
11、解决设置默认值在update时无效问题！
12、优化文件类别判断，新增mime-type类型检查！
13、优化数字转换问题！

V1.5.1
1、优化windows环境的问题！

V1.5.2
1、优化代理器，增加类的检测！
2、新增WebStarted常量，判断是否启动了Web服务器！
3、优化Out输出，更加方便！
4、优化ASM类，解析class和方法。
5、优化FastScanner，解决在tomcat7无法识别jar包问题。
6、优化FastPath!

V1.5.3
1、新增IFastDatabaseListener监听接口！
2、优化getParamToArray，解决不可为空的判断问题！
3、优化数据库区分大小写问题！
4、新增FastLockUtils，方便对象锁操作！
5、优化了部分功能。

V1.5.4
1、优化FastScanner
2、新增常量LogSameJar可配置打印出不同版本的Jar包
3、优化文件上传！
4、优化FastEntity，新增clearEmpty方法
5、优化FastConstant。
6、优化复制保存copySave方法并保存返回复制后的对象！

V1.5.5
1、优化了自动修改数据库。
2、优化FastAction方法。
3、优化AFastAction和AFastRoute注解，新增拦截器允许配置！
4、优化FastDateUtils类！
5、优化FastSerializeUtils类！
6、优化FastEntity类，新增getColumns方法！