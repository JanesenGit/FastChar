package com.fastchar.converters;

import java.util.*;

class FastTypeHelper {

    static Collection getCollectionInstance(Class<?> targetClass) {
        if (targetClass == List.class||targetClass == ArrayList.class) {
            return new ArrayList();
        }else if (targetClass == Set.class||targetClass == HashSet.class) {
            return new HashSet();
        }
        return null;
    }
}
