package com.fastchar.interfaces;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastHandler;

/**
 * 参数转换器接口
 */
public interface IFastParamConverter {

    /**
     * 转换参数
     * @param action FastAction对象
     * @param parameter 参数信息描述
     * @param handler 句柄
     * @return 参数值
     * @throws Exception 异常
     */
    Object convertValue(FastAction action, FastParameter parameter, FastHandler handler) throws Exception;

}
