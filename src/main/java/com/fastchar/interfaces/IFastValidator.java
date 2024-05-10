package com.fastchar.interfaces;

/**
 * 参数验证器
 */
public interface IFastValidator {

    /**
     * 验证参数是否正确
     *
     * @param validator  验证器名称
     * @param arguments 传入的验证器参数
     * @param paramName  参数名
     * @param paramValue 参数值
     * @return 验证错误信息 为：null 验证通过
     */
    String validate(String validator, Object[] arguments, String paramName, Object paramValue);

}
