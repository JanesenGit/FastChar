package com.fastchar.local;

import com.fastchar.interfaces.IFastLocalProvider;

import java.lang.reflect.Field;
import java.text.MessageFormat;

public class FastCharLocal_CN extends FastCharBaseLocal {

    private final String FastChar_Error1 = "FastChar启动成功！耗时：{0}秒！";


    //路由相关错误
    private final String Route_Error1 = "路由{0}没有响应结果！";


    //参数相关错误
    private final String Param_Error1 = "参数{0}不可为空！";

    private final String Interceptor_Error1 = "请求前置的拦截器{0}没有执行response或invoke方法！";
    private final String Interceptor_Error2 = "请求后置的拦截器{0}没有执行response或invoke方法！";


    private final String Class_Error1 = "类{0}必须实现{1}接口！";
    private final String Class_Error2 = "类{0}实例化失败！";
    private final String Class_Error3 = "无法获取{0}类！";

    //方法相关错误
    private final String Method_Error1 = "Return标识，不会影响程序！请勿拦截Throwable级别的异常！";


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
    private final String Action_Error2 = "路由{0}已存在！";
    private final String Action_Error3 = "路由{0}已被{1}重写！";


    //Entity相关错误
    private final String Entity_Error1 = "Entity的表格名称不可为空！";
    private final String Entity_Error2 = "Entity的表格{0}不存在！";
    private final String Entity_Error3 = "未检测到被修改的数据！";

    //文件相关错误
    private final String File_Error1 = "目录{0}无法创建！";
    private final String File_Error2 = "上传文件的请求Request不可为空！";
    private final String File_Error3 = "上传文件保存到本地服务器的路径不可为空！";
    private final String File_Error4 = "上传文件最大字节数必须大于0！";
    private final String File_Error5 = "上传文件保存的本地服务器路径不是一个有效的文件夹路径！";
    private final String File_Error6 = "上传文件保存的本地服务器路径没有权限写入！";
    private final String File_Error7 = "文件{0}不存在！";
    private final String File_Error8 = "文件{0}不可为文件夹！";

    //provider 相关错误
    private final String Provider_Error1 = "无法获得{0}对象！";


    //Override 相关错误
    private final String Override_Error1 = "无法获得{0}的覆盖类对象！";

    //redis相关错误
    private final String Redis_Error1 = "Redis的服务器地址不可为空！";
    private final String Redis_Error2 = "主Redis名称[masterName]不可为空！";


}
