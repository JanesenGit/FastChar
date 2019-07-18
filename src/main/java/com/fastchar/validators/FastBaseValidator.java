package com.fastchar.validators;

import com.fastchar.interfaces.IFastValidator;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FastBaseValidator implements IFastValidator {

    @Override
    public Set<String> pluckKeys(String validator) {
        Set<String> keys = new HashSet<>();
        String regStr = "#(.*)";
        Matcher matcher = Pattern.compile(regStr).matcher(validator);
        if (matcher.find()) {
            String attr = matcher.group(1).replace(" ", "");
            String[] split = attr.split("#");
            keys.addAll(Arrays.asList(split));
        }
        return keys;
    }


    protected  boolean checkKey(String validator, String key) {
        if (validator.contains("#")) {
            return validator.contains("#" + key);
        }
        return true;
    }


    protected  String formatMessage(String message, String key) {
        message = message.replaceAll("#.*", "");
        return MessageFormat.format(message, key);
    }

}
