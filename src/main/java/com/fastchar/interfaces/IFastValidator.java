package com.fastchar.interfaces;

import java.util.Set;

/**
 * 参数验证器
 */
public interface IFastValidator {

    /**
     * 提取验证的参数名
     * @param validator 验证表达式
     * @return 参数集合
     */
    Set<String> pluckKeys(String validator);


    /**
     * 验证参数是否正确
     * @param validator 验证表达式
     * @param key 参数名
     * @param value  参数值
     * @return 验证错误信息 为：null 验证通过
     */
    String validate(String validator, String key, Object value);

}
