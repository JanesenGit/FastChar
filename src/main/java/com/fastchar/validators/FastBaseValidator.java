package com.fastchar.validators;

import com.fastchar.interfaces.IFastValidator;
import com.fastchar.local.FastCharLocal;

import java.text.MessageFormat;

public abstract class FastBaseValidator implements IFastValidator {

    private boolean securityMessage;

    protected String formatMessage(String message, String key) {
        if (isSecurityMessage()) {
            return FastCharLocal.PARAM_ERROR3;
        }
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
