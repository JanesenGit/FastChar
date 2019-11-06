package com.fastchar.converters;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastEntity;
import com.fastchar.core.FastHandler;
import com.fastchar.interfaces.IFastParamConverter;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

/**
 * FastAction 路由方法中形参类型为FastEntity、FastEntity[]、List&lt;FastEntity&gt;的参数转换器
 */
@SuppressWarnings("unchecked")
public class FastEntityParamConverter implements IFastParamConverter {
    @Override
    public Object convertValue(FastAction action, FastParameter parameter, FastHandler handler) throws Exception {
        Object value = null;
        if (FastEntity.class.isAssignableFrom(parameter.getType())) {
            value = action.getParamToEntity(parameter.getName(), (Class<? extends FastEntity>) parameter.getType());
            handler.setCode(1);
        } else if (FastEntity[].class.isAssignableFrom(parameter.getType())) {
            value = action.getParamToEntityArray(parameter.getName(),(Class<? extends FastEntity>) parameter.getType().getComponentType());
            handler.setCode(1);
        }else if (Collection.class.isAssignableFrom(parameter.getType())) {
            if (parameter.getParameterizedType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                Class<?> actualTypeArgument = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                if (FastEntity.class.isAssignableFrom(actualTypeArgument)) {
                    Collection collection = FastTypeHelper.getCollectionInstance(parameter.getType());
                    if (collection != null) {
                        Object[] paramToEntityArray = action.getParamToEntityArray(parameter.getName(),(Class<? extends FastEntity>) actualTypeArgument);
                        collection.addAll(Arrays.asList(paramToEntityArray));
                        value = collection;
                    }
                    handler.setCode(1);
                }
            }
        }
        return value;
    }
}
