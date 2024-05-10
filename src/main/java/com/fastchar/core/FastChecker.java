package com.fastchar.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 参数校验器
 *
 * @author 沈建（Janesen）
 */
final class FastChecker<T> {
    private final T target;
    private boolean rollback;
    private boolean hold;

    FastChecker(T target) {
        this.target = target;
    }

    private final List<FastCheckEntry> validators = new ArrayList<>(16);

    public T check(int index, String validator, Object... arguments) {
        if (rollback) {
            rollback = false;
            validators.clear();
        }
        validators.add(index, new FastCheckEntry(validator, arguments));
        return target;
    }

    public T check(String validator, Object... arguments) {
        if (rollback) {
            rollback = false;
            validators.clear();
        }
        validators.add(new FastCheckEntry(validator, arguments));
        return target;
    }


    public Object validate(String paramName, Object paramValue) {
        rollback = !isHold();
        if (validators.isEmpty()) {
            return null;
        }
        try {
            for (FastCheckEntry validator : validators) {
                if (paramValue != null && paramValue.getClass().isArray()) {
                    int length = Array.getLength(paramValue);
                    if (length > 0) {
                        for (int i = 0; i < length; i++) {
                            Object validate = FastChar.getValidators().validate(validator.getValidator(), validator.getArguments(), paramName, Array.get(paramValue, i));
                            if (validate != null) {
                                return validate;
                            }
                        }
                    }
                }
                return FastChar.getValidators().validate(validator.getValidator(), validator.getArguments(), paramName, paramValue);
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

    public FastChecker<T> setHold(boolean hold) {
        this.hold = hold;
        return this;
    }


    private static class FastCheckEntry {
        private String validator;
        private Object[] arguments;

        public FastCheckEntry(String validator, Object[] arguments) {
            this.validator = validator;
            this.arguments = arguments;
        }

        public String getValidator() {
            return validator;
        }

        public FastCheckEntry setValidator(String validator) {
            this.validator = validator;
            return this;
        }

        public Object[] getArguments() {
            return arguments;
        }

        public FastCheckEntry setArguments(Object[] arguments) {
            this.arguments = arguments;
            return this;
        }
    }
}
