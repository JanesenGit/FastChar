## FastChar
Java语言开发的WEB+ORM开源免费的MVC框架，设计原理符合一般Web框架规则。学习成本低、代码少、极易上手、零配置，极大的提高开发效率，减少错误率。具有高并发、模块化、高灵活性、占用内存少等特点。

## Maven搭建项目

第一步：在pom.xml中加入fastchar的maven，如下代码：

```Html
<dependency>
<groupId>com.fastchar</groupId>
<artifactId>fastchar</artifactId>
<version>1.0</version>
<!--最新版本请以前往maven搜索FastChar查看-->
</dependency>
```

第二步：修改web.xml，将如下代码复制进入

```Html
<filter>
<filter-name>fastchar</filter-name>
<filter-class>com.fastchar.core.FastFilter</filter-class>
</filter>
<filter-mapping>
<filter-name>fastchar</filter-name>
<url-pattern>/*</url-pattern>
</filter-mapping>
```

第三步：创建Action并继承FastAction类，如下代码：

```java
public class TestAction extends FastAction {
/**
* 获得路由地址
* Get routing address
* @return
*/
@Override
protected String getRoute() {
return "/test";
}

public void index() {
responseText("搭建成功！");
}
}
```

接下来运行项目，访问项目地址即可，例如上述案例中访问的地址：[http://xxxx:8080/xxx/test/](http://xxxx:8080/xxx/test/)
