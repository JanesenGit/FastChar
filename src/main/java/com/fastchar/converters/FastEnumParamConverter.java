package com.fastchar.converters;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastHandler;
import com.fastchar.interfaces.IFastParamConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

/**
 * FastAction 路由方法中形参类型为Enum、Enum[]、List&lt;Enum&gt;的参数转换器
 */
@SuppressWarnings("unchecked")
public class FastEnumParamConverter implements IFastParamConverter {

    @Override
    public Object convertValue(FastAction action, FastParameter parameter, FastHandler handler) throws Exception {
        Object value = null;
        if (parameter.getType().isEnum()) {
            handler.setCode(1);
            value = action.getParamToEnum(parameter.getName(),(Class<? extends Enum<?>>) parameter.getType());
        } else if (Enum[].class.isAssignableFrom(parameter.getType())) {
            handler.setCode(1);
            value = action.getParamToEnumArray(parameter.getName(),(Class<? extends Enum<?>>) parameter.getType().getComponentType());
        }else if (Collection.class.isAssignableFrom(parameter.getType())) {
            if (parameter.getParameterizedType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                Class<?> actualTypeArgument = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                if (actualTypeArgument.isEnum()) {
                    Collection<Object> collection = FastTypeHelper.getCollectionInstance(parameter.getType());
                    if (collection != null) {
                        Object[] paramToEnumArray = action.getParamToEnumArray(parameter.getName(),(Class<? extends Enum<?>>) actualTypeArgument);
                        collection.addAll(Arrays.asList(paramToEnumArray));
                        value = collection;
                    }
                    handler.setCode(1);
                }
            }
        }
        return value;
    }
}
