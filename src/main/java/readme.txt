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