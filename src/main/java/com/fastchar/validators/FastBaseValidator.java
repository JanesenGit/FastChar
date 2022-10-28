package com.fastchar.validators;

import com.fastchar.interfaces.IFastValidator;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastStringUtils;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class FastBaseValidator implements IFastValidator {

    private static final Pattern KEY_PATTERN = Pattern.compile("#(.*)");
    private boolean securityMessage;


    @Override
    public Set<String> pluckKeys(String validator) {
        Set<String> keys = new HashSet<>();
        Matcher matcher = KEY_PATTERN.matcher(validator);
        if (matcher.find()) {
            String attr = matcher.group(1).replace(" ", "");
            String[] split = FastStringUtils.splitByWholeSeparator(attr,"#");
            keys.addAll(Arrays.asList(split));
        }
        return keys;
    }


    protected boolean checkKey(String validator, String key) {
        if (validator.contains("#")) {
            return validator.contains("#" + key);
        }
        return true;
    }


    protected String formatMessage(String message, String key) {
        if (isSecurityMessage()) {
            return FastCharLocal.PARAM_ERROR3;
        }
        message = message.replaceAll("#.*", "");
        return MessageFormat.format(message, key);
    }


    public boolean isSecurityMessage() {
        return securityMessage;
    }

    public FastBaseValidator setSecurityMessage(boolean securityMessage) {
        this.securityMessage = securityMessage;
        return this;
    }
}
