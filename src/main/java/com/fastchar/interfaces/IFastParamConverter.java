package com.fastchar.interfaces;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;

import java.lang.reflect.Type;

/**
 * 参数转换器接口
 */
public interface IFastParamConverter {

    /**
     * 转换参数
     * @param action FastAction对象
     * @param parameter 参数信息描述
     * @param marker 转换结果标注，marker[0]=0 转换成功
     * @return 参数值
     * @throws Exception 异常
     */
    Object convertValue(FastAction action, FastParameter parameter, int[] marker) throws Exception;

}
