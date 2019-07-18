package com.fastchar.interfaces;

import java.io.File;
import java.util.Map;

/**
 * 模板渲染接口
 */
public interface IFastTemplate {

    /**
     * 渲染字符串模板
     * @param params 渲染参数
     * @param template 字符串模板
     * @return 渲染后的数据
     */
    String run(Map<String, Object> params, String template);

    /**
     * 渲染字符串模板
     * @param params 渲染参数
     * @param template 模板文件
     * @return 渲染后的数据
     */
    String run(Map<String, Object> params, File template);

}
