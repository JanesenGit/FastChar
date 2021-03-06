package com.fastchar.converters;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastFile;
import com.fastchar.core.FastHandler;
import com.fastchar.interfaces.IFastParamConverter;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * FastAction 路由方法中形参类型为File、File[]、List&lt;File&gt;的参数转换器
 */
@SuppressWarnings("unchecked")
public class FastFileParamConverter implements IFastParamConverter {
    @Override
    public Object convertValue(FastAction action, FastParameter parameter, FastHandler handler) throws Exception {
        Object value = null;
        if (parameter.getType() == File.class) {
            FastFile paramFile = action.getParamFile(parameter.getName());
            if (paramFile != null) {
                value = paramFile.getFile();
            }
            handler.setCode(1);
        } else if (parameter.getType() == FastFile.class) {
            value = action.getParamFile(parameter.getName());
            handler.setCode(1);
        } else if (parameter.getType() == File[].class) {
            List<File> files = new ArrayList<>();
            for (FastFile fastFile : action.getParamListFile()) {
                files.add(fastFile.getFile());
            }
            value = files.toArray(new File[]{});
            handler.setCode(1);
        } else if (parameter.getType() == FastFile[].class) {
            value = action.getParamListFile().toArray(new FastFile[]{});
            handler.setCode(1);
        } else if (Collection.class.isAssignableFrom(parameter.getType())) {
            if (parameter.getParameterizedType() instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
                if (parameterizedType.getActualTypeArguments()[0] == File.class) {
                    Collection collection = FastTypeHelper.getCollectionInstance(parameter.getType());
                    if (collection != null) {
                        List<FastFile<?>> paramListFile = action.getParamListFile();
                        for (FastFile fastFile : paramListFile) {
                            collection.add(fastFile.getFile());
                        }
                        value = collection;
                    }
                    handler.setCode(1);
                }else if (parameterizedType.getActualTypeArguments()[0] == FastFile.class) {
                    Collection collection = FastTypeHelper.getCollectionInstance(parameter.getType());
                    if (collection != null) {
                        List<FastFile<?>> paramListFile = action.getParamListFile();
                        collection.addAll(paramListFile);
                        value = collection;
                    }
                    handler.setCode(1);
                }
            }
        }
        return value;
    }
}
