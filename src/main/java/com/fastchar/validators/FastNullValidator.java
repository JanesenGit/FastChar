package com.fastchar.validators;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastValidator;
import com.fastchar.utils.FastStringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 空值验证器，格式：@null:message
 */
public class FastNullValidator extends FastBaseValidator {


    @Override
    public String validate(String validator, String key, Object value) {
        if (checkKey(validator, key)) {
            if (validator.startsWith("@null")) {
                String[] split = validator.split(":");
                String message = null;
                if (split.length == 2) {
                    message = split[1];
                }
                if (FastStringUtils.isEmpty(message)) {
                    message = FastChar.getLocal().getInfo("Param_Error1", key);
                }
                if (value == null || FastStringUtils.isEmpty(value.toString())) {
                    return formatMessage(message, key);
                }
            }
        }
        return null;
    }
}
