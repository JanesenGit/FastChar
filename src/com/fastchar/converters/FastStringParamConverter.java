package com.fastchar.converters;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;
import com.fastchar.interfaces.IFastParamConverter;

import java.lang.reflect.ParameterizedType;
import java.util.*;

@SuppressWarnings("unchecked")
public class FastStringParamConverter implements IFastParamConverter {
    @Override
    public Object convertValue(FastAction action, FastParameter parameter, int[] marker) throws Exception {
        Object value = null;
        if (parameter.getType() == String.class) {
            value = action.getParam(parameter.getName());
            marker[0] = 1;
        } else if (parameter.getType() == String[].class) {
            value = action.getParamToArray(parameter.getName());
            marker[0] = 1;
        } else if (Collection.class.isAssignableFrom(parameter.getType())) {
            if (parameter.getParameterizedType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                if (parameterizedType.getActualTypeArguments()[0] == String.class) {
                    Collection collection = FastTypeHelper.getCollectionInstance(parameter.getType());
                    if (collection != null) {
                        String[] paramToArray = action.getParamToArray(parameter.getName());
                        collection.addAll(Arrays.asList(paramToArray));
                        value = collection;
                    }
                    marker[0] = 1;
                }
            }
        }
        return value;
    }
}
