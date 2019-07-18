package com.fastchar.converters;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;
import com.fastchar.interfaces.IFastParamConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

/**
 * FastAction 路由方法中形参类型为Date、Date[]、List&lt;Date&gt;的参数转换器
 */
@SuppressWarnings("unchecked")
public class FastDateParamConverter implements IFastParamConverter {
    @Override
    public Object convertValue(FastAction action, FastParameter parameter, int[] marker) throws Exception {
        Object value = null;
        if (parameter.getType() == Date.class) {
            value = action.getParamToDate(parameter.getName());
            marker[0] = 1;
        } else if (parameter.getType() == Date[].class) {
            value = action.getParamToDateArray(parameter.getName());
            marker[0] = 1;
        } else if (Collection.class.isAssignableFrom(parameter.getType())) {
            if (parameter.getParameterizedType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                if (parameterizedType.getActualTypeArguments()[0] == Date.class) {
                    Collection collection = FastTypeHelper.getCollectionInstance(parameter.getType());
                    if (collection != null) {
                        Date[] paramToArray = action.getParamToDateArray(parameter.getName());
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
