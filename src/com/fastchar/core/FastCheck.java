package com.fastchar.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

final class FastCheck<T> {
    private T target;
    private boolean rollback;

    FastCheck(T target) {
        this.target = target;
    }

    private List<String> validators = new ArrayList<>();

    public T check(String validator) {
        if (rollback) {
            rollback = false;
            validators.clear();
        }
        validators.add(validator);
        return target;
    }


    public Object validate(String key, Object value) {
        rollback = true;
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            if (length > 0) {
                for (int i = 0; i < length; i++) {
                    Object validate = FastChar.getValidators().validate(key, Array.get(value, i), validators.toArray(new String[]{}));
                    if (validate != null) {
                        return validate;
                    }
                }
            } else {
                return FastChar.getValidators().validate(key, null, validators.toArray(new String[]{}));
            }

        } else {
            return FastChar.getValidators().validate(key, value, validators.toArray(new String[]{}));
        }
        return null;
    }


}
