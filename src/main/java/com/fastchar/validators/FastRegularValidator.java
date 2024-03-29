package com.fastchar.validators;

import com.fastchar.core.FastChar;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastStringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则验证器，格式：@key:message
 */
public class FastRegularValidator extends FastBaseValidator {
    public static Map<String, String> REGULARS = new HashMap<>(16);
    private static final Pattern BASE_PATTERN = Pattern.compile("@\\((.*)\\)");

    static {
        REGULARS.put("int", "[+-]?[0-9]*");
        REGULARS.put("double", "[+-]?\\d+\\.\\d+");
        REGULARS.put("boolean", "(?i)(true|false|0|1)");
        REGULARS.put("email", "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?");
        REGULARS.put("idcard", "([1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx])|([1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2})");
    }


    @Override
    public String validate(String validator, String key, Object value) {
        if (checkKey(validator, key)) {
            String[] split = FastStringUtils.splitByWholeSeparator(validator,":");
            String message = null;
            if (split.length == 2) {
                message = split[1];
            }
            if (FastStringUtils.isEmpty(message)) {
                message = FastChar.getLocal().getInfo(FastCharLocal.PARAM_ERROR2, key);
            }

            if (value != null) {
                for (Map.Entry<String, String> stringStringEntry : REGULARS.entrySet()) {
                    if (validator.toLowerCase().startsWith("@" + stringStringEntry.getKey())) {
                        String regularExp = stringStringEntry.getValue();

                        if (!String.valueOf(value).matches(regularExp)) {
                            return formatMessage(message, key);
                        }
                    }
                }
                Matcher matches = BASE_PATTERN.matcher(validator);
                if (matches.find()) {
                    String regularExp = matches.group(1);
                    if (!String.valueOf(value).matches(regularExp)) {
                        return formatMessage(message, key);
                    }
                }
            }
        }
        return null;
    }


}
