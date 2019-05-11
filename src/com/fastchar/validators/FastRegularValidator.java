package com.fastchar.validators;

import com.fastchar.interfaces.IFastValidator;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FastRegularValidator implements IFastValidator {
    public static Map<String, String> REGULARS = new HashMap<>();
    static{
        REGULARS.put("int", "[+-]?[0-9]*");
        REGULARS.put("double", "[+-]?\\d+\\.\\d+");
        REGULARS.put("boolean", "(?i)(true|false|0|1)");
        REGULARS.put("email", "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?");
        REGULARS.put("idcard", "([1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx])|([1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2})");
    }


    @Override
    public String validate(String validator, String key, Object value) {
        if (value != null) {
            for (String regular : REGULARS.keySet()) {
                if (validator.toLowerCase().startsWith("@" + regular)) {
                    String regularExp = REGULARS.get(regular);
                    String message = validator.replace("@" + regular + ":", "");
                    if (!String.valueOf(value).matches(regularExp)) {
                        return MessageFormat.format(message, key);
                    }
                }
            }
            String reg = "@\\((.*)\\):";
            Matcher matches = Pattern.compile(reg).matcher(validator);
            if (matches.find()) {
                String regularExp = matches.group(1);
                String message = validator.replace("@(" + regularExp + "):", "");
                if (!String.valueOf(value).matches(regularExp)) {
                    return MessageFormat.format(message, key);
                }
            }
        }
        return null;
    }
}
