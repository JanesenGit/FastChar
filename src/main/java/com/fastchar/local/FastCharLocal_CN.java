package com.fastchar.local;

import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class FastCharLocal_CN extends FastCharBaseLocal {

    private final String FastChar_Error1 = "{0}启动成功！耗时：{1}秒！";
    private final String FastChar_Error2 = "{0}正在初始化，请稍后……";
    private final String FastChar_Error3 = "{0}已停止运行！";

    private final String Date_Error1 = "今天";
    private final String Date_Error2 = "昨天";
    private final String Date_Error3 = "前天";

    //路由相关错误
    private final String Route_Error1 = "路由 {0} 未响应！";
    private final String Route_Error2 = "路由 {0} 无效！";
    private final String Route_Error3 = "路由 {0} 不支持HTTP方法：{1} ！";
    private final String Route_Error4 = "移除被释放的路由 {0} ";

    //参数相关错误
    private final String Param_Error1 = "参数{0}不可为空！";
    private final String Param_Error2 = "参数{0}值错误！";

    private final String Interceptor_Error1 = "请求前置的拦截器{0}没有执行response或invoke方法！";
    private final String Interceptor_Error2 = "请求后置的拦截器{0}没有执行response或invoke方法！";
    private final String Interceptor_Error3 = "移除被释放的拦截器类 {0} ";

    private final String Class_Error1 = "类{0}必须实现{1}接口！";
    private final String Class_Error2 = "类{0}实例化失败！";
    private final String Class_Error3 = "无法获取{0}类！";
    private final String Class_Error4 = "无法获取{0}类！请前往 {1} 下载并引用必要的Jar包！";

    //方法相关错误
    private final String Method_Error1 = "Return标识，不会影响程序！请勿拦截Throwable级别的异常！";


    //scanner相关错误
    private final String Scanner_Error1 = "加载{0}中……";
    private final String Scanner_Error2 = "加载{0}成功!";
    private final String Scanner_Error3 = "跳过{0}";
    private final String Scanner_Error4 = "解压{0}中……";
    private final String Scanner_Error5 = "解压{0}成功!";
    private final String Scanner_Error6 = "移除被释放的扫描接收器类 {0} ";

    //数据库相关错误
    private final String Db_Error1 = "数据库名称{0}已存在！";
    private final String Db_Error2 = "数据库名称{0}不存在！";
    private final String Db_Error3 = "未配置数据库信息！";


    //数据库sql相关错误
    private final String Db_Sql_Error1 = "Sql参数错误！参数数量少于占位符(?)的数量！";
    private final String Db_Sql_Error2 = "Select语句错误！";
    private final String Db_Sql_Error3 = "Sql参数错误！字段{0}的值不可为空！";
    private final String Db_Sql_Error4 = "Sql参数错误！主键字段{0}的值不可为空！";



    //数据库表格相关错误
    private final String Db_Table_Error1 = "表格名称[name]不可为空！";
    private final String Db_Table_Info1 = "在数据库 {0} 中创建表格：{1}";
    private final String Db_Table_Info2 = "在表格 {1}@{0} 中添加列：{2}";
    private final String Db_Table_Info3 = "修改表格 {1}@{0} 中的列：{2}";
    private final String Db_Table_Info4 = "在表格 {1}@{0} 中添加列 {2} 的索引:{3}";


    //数据库表格字段相关错误
    private final String Db_Column_Error1 = "字段名称[name]不可为空！";
    private final String Db_Column_Error2 = "字段{0}的类型[type]不可为空！";

    //Action相关错误
    private final String Action_Error1 = "Action的路由不可为空！";
    private final String Action_Error2 = "路由 {0} 已存在！";
    private final String Action_Error3 = "路由 {0} 已被重写！";
    private final String Action_Error4 = "路由 {0} 的前缀 {1} 是WebRoot下的文件夹！不可与WebRoot下的文件夹重复！";
    private final String Action_Error5 = "Action {0} 已被禁用！";


    //Entity相关错误
    private final String Entity_Error1 = "Entity的关联的表格名称不可为空！";
    private final String Entity_Error2 = "Entity的所关联的表格{0}不存在！";
    private final String Entity_Error3 = "未检测到被修改的属性数据！";
    private final String Entity_Error4 = "执行replace操作时，必须指定属性名！";
    private final String Entity_Error5 = "移除被释放的实体类 {0} ";


    //文件相关错误
    private final String File_Error1 = "目录{0}无法创建！";
    private final String File_Error2 = "上传文件的请求Request不可为空！";
    private final String File_Error3 = "上传文件保存到本地服务器的路径不可为空！";
    private final String File_Error4 = "上传文件最大字节数必须大于0！";
    private final String File_Error5 = "上传文件保存的本地服务器路径不是一个有效的文件夹路径！";
    private final String File_Error6 = "上传文件保存的本地服务器路径没有权限写入！";
    private final String File_Error7 = "文件{0}不存在！";
    private final String File_Error8 = "文件{0}不可为文件夹！";
    private final String File_Error9 = "文件{0}无法创建！";
    private final String File_Error10 = "文件{0}无法没有操作权限！";

    //provider 相关错误
    private final String Provider_Error1 = "无法获得{0}对象！";


    //Override 相关错误
    private final String Override_Error1 = "无法获得{0}的代理类对象！";
    private final String Override_Error2 = "类{0}，返回{1}对象！";
    private final String Override_Error3 = "移除被释放的代理器类 {0} ";

    //redis相关错误
    private final String Redis_Error1 = "Redis的服务器地址不可为空！";
    private final String Redis_Error2 = "主Redis名称[masterName]不可为空！";
    private final String Redis_Error3 = "已启用Redis高级缓存！";


    //数据源相关消息
    private final String DataSource_Info1 = "已启用{0}数据源！";
    private final String DataSource_Info2 = "已关闭{0}数据源！";
    private final String DataSource_Info3 = "未获取到{0}数据源！";

    //cglib相关错误
    private final String CGLib_Error1 = "被代理的类不可以为空！";
    private final String CGLib_Error2 = "代理拦截器不可为空！";


    //velocity的错误
    private final String Velocity_Error1 = "文件不存在于WebRoot目录下！{0}";

    //ticket的错误
    private final String Ticket_Error1 = "必读！此文件为FastChar配置文件！请勿轻易的删除！！！";

    //观察者类
    private final String Observable_Error1 = "移除被释放的观察者类 {0} ";

    //验证器类
    private final String Validator_Error1 = "移除被释放的验证器类 {0} ";

    //转换器
    private final String Converter_Error1 = "移除被释放的转换器 {0} ";


    //IFastWeb
    private final String Web_Error1 = "移除被释放的IFastWeb类 {0} ";


    //Jar包相关错误
    private final String Jar_Error1 = "发现不同版本的JAR包 {0} ";


//    public static void main(String[] args) throws Exception {
//        Field[] declaredFields = FastCharLocal_CN.class.getDeclaredFields();
//        for (Field declaredField : declaredFields) {
//            String comment = "" +
//                    "/**" +
//                    "\n" +
//                    " * 描述模板：" +
//                    "\n" +
//                    " * " + declaredField.get(new FastCharLocal_CN()) +
//                    "\n" +
//                    " */";
//            String toUpperCase = declaredField.getName().toUpperCase();
//            toUpperCase = toUpperCase.replaceAll("FASTCHAR", "FAST_CHAR");
//
//            String declare = "public static final String " + toUpperCase + " = \"" + declaredField.getName() + "\";";
//
//            System.out.println(comment);
//            System.out.println(declare);
//            System.out.println("\n");
//        }
//    }
}
