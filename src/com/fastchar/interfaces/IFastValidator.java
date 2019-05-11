package com.fastchar.interfaces;

import com.fastchar.core.FastAction;

public interface IFastValidator {
    //validator   @key:message
    String validate(String validator, String key, Object value);

}
