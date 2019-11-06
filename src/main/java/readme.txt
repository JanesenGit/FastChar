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