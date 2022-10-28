package com.fastchar.converters;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastHandler;
import com.fastchar.interfaces.IFastParamConverter;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;

public class FastNormalParamConverter implements IFastParamConverter {
    @Override
    public Object convertValue(FastAction action, FastParameter parameter, FastHandler handler) throws Exception {
        if (parameter.getType() == FastHttpServletRequest.class) {
            return action.getRequest();
        }else  if (parameter.getType() == FastHttpServletResponse.class) {
            return action.getResponse();
        }
        return null;
    }
}
