package com.fastchar.core;

import java.lang.reflect.Array;
import java.util.*;

/**
 * 参数校验器
 * @param <T>
 */
final class FastCheck<T> {
    private T target;
    private boolean rollback;
    private boolean hold;

    FastCheck(T target) {
        this.target = target;
    }

    private List<String> validators = new ArrayList<>();
    public T check(int index,String validator) {
        if (rollback) {
            rollback = false;
            validators.clear();
        }
        if (!validators.contains(validator)) {
            validators.add(index, validator);
        }
        return target;
    }
    public T check(String validator) {
        if (rollback) {
            rollback = false;
            validators.clear();
        }
        if (!validators.contains(validator)) {
            validators.add(validator);
        }
        return target;
    }


    public Set<String> getParamNames() {
        return FastChar.getValidators().pluckKeys(validators.toArray(new String[]{}));
    }



    public Object validate(String key, Object value) {
        rollback = !isHold();
        if (validators.size() == 0) {
            return null;
        }
        try {
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
        } finally {
            if (rollback) {
                rollback = false;
                validators.clear();
            }
        }
    }


    public void rollback() {
        rollback = false;
        hold = false;
        validators.clear();
    }

    public boolean isHold() {
        return hold;
    }

    public FastCheck<T> setHold(boolean hold) {
        this.hold = hold;
        return this;
    }
}
