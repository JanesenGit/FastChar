package com.fastchar.validators;

import com.fastchar.interfaces.IFastValidator;
import com.fastchar.utils.FastStringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastNullValidator implements IFastValidator {


    @Override
    public String validate(String validator, String key, Object value) {
        if (validator.startsWith("@null")) {
            String message = validator.replace("@null:", "");
            if (value == null || FastStringUtils.isEmpty(value.toString())) {
                return MessageFormat.format(message, key);
            }
        }
        return null;
    }
}
