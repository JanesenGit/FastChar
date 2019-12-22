package com.fastchar.converters;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastHandler;
import com.fastchar.interfaces.IFastParamConverter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FastNormalParamConverter implements IFastParamConverter {
    @Override
    public Object convertValue(FastAction action, FastParameter parameter, FastHandler handler) throws Exception {
        if (parameter.getType() == HttpServletRequest.class) {
            return action.getRequest();
        }else  if (parameter.getType() == HttpServletResponse.class) {
            return action.getResponse();
        }
        return null;
    }
}
