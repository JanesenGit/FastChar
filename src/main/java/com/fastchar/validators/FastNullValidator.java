package com.fastchar.validators;

import com.fastchar.core.FastChar;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastStringUtils;

/**
 * 空值验证器，格式：@null
 */
public class FastNullValidator extends FastBaseValidator {

    @Override
    public String validate(String validator, Object[] arguments, String paramName, Object paramValue) {
        if (validator.startsWith("@null")) {
            String message = FastChar.getLocal().getInfo(FastCharLocal.PARAM_ERROR1, paramName);
            if (arguments.length > 0) {
                message = arguments[0].toString();
            }
            if (paramValue == null || FastStringUtils.isEmpty(paramValue.toString())) {
                return formatMessage(message, paramName);
            }
        }
        return null;
    }
}
