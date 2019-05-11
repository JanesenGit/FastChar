package com.fastchar.converters;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;
import com.fastchar.interfaces.IFastParamConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

@SuppressWarnings("unchecked")
public class FastBooleanParamConverter implements IFastParamConverter {
    @Override
    public Object convertValue(FastAction action, FastParameter parameter, int[] marker) throws Exception {
        Object value = null;
        if (parameter.getType() == Boolean.class||parameter.getType() == boolean.class) {
            value = action.getParamToBoolean(parameter.getName());
            marker[0] = 1;
        } else if (parameter.getType() == Boolean[].class) {
            value = action.getParamToBooleanArray(parameter.getName());
            marker[0] = 1;
        } else if (Collection.class.isAssignableFrom(parameter.getType())) {
            if (parameter.getParameterizedType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                if (parameterizedType.getActualTypeArguments()[0] == Boolean.class) {
                    Collection collection = FastTypeHelper.getCollectionInstance(parameter.getType());
                    if (collection != null) {
                        Boolean[] paramToArray = action.getParamToBooleanArray(parameter.getName());
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
