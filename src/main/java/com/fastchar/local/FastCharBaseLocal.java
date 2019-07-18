package com.fastchar.local;

import com.fastchar.interfaces.IFastLocal;
import com.fastchar.utils.FastClassUtils;

import java.lang.reflect.Field;
import java.text.MessageFormat;

public class FastCharBaseLocal implements IFastLocal {
    @Override
    public String getInfo(String key, Object... args) {
        try {
            Field declaredField = FastClassUtils.getDeclaredField(this.getClass(), key);
            if (declaredField != null) {
                declaredField.setAccessible(true);
                return MessageFormat.format(String.valueOf(declaredField.get(this)), args);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



}
