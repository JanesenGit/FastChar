package com.fastchar.validators;

import com.fastchar.core.FastChar;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastStringUtils;

/**
 * 空值验证器，格式：@null:message
 */
public class FastNullValidator extends FastBaseValidator {

    @Override
    public String validate(String validator, String key, Object value) {
        if (checkKey(validator, key)) {
            if (validator.startsWith("@null")) {
                String[] split = FastStringUtils.splitByWholeSeparator(validator,":");
                String message = null;
                if (split.length == 2) {
                    message = split[1];
                }
                if (FastStringUtils.isEmpty(message)) {
                    message = FastChar.getLocal().getInfo(FastCharLocal.PARAM_ERROR1, key);
                }
                if (value == null || FastStringUtils.isEmpty(value.toString())) {
                    return formatMessage(message, key);
                }
            }
        }
        return null;
    }
}
