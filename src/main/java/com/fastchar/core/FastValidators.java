package com.fastchar.core;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.interfaces.IFastScannerAccepter;
import com.fastchar.interfaces.IFastValidator;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.validators.FastRegularValidator;

import java.util.*;

public final class FastValidators {
    private List<Class<? extends IFastValidator>> validatorClasses = new ArrayList<>();

    FastValidators() {
    }

    public FastValidators add(Class<? extends IFastValidator> converters) {
        if (!FastClassUtils.checkNewInstance(converters)) {
            return this;
        }
        validatorClasses.add(converters);
        sortConverter();
        return this;
    }

    public FastValidators putRegular(String key, String regular) {
        FastRegularValidator.REGULARS.put(key, regular);
        return this;
    }

    private void sortConverter() {
        Comparator<Class<? extends IFastValidator>> comparator = new Comparator<Class<? extends IFastValidator>>() {
            @Override
            public int compare(Class<? extends IFastValidator> o1, Class<? extends IFastValidator> o2) {
                int priority1 = 0;
                int priority2 = 0;

                if (o1.isAnnotationPresent(AFastPriority.class)) {
                    AFastPriority aFastPriority = o1.getAnnotation(AFastPriority.class);
                    priority1 = aFastPriority.value();
                }
                if (o2.isAnnotationPresent(AFastPriority.class)) {
                    AFastPriority aFastPriority = o2.getAnnotation(AFastPriority.class);
                    priority2 = aFastPriority.value();
                }
                return Integer.compare(priority2, priority1);
            }
        };
        Collections.sort(validatorClasses, comparator);
    }


    public Object validate(String paramName, Object paramValue, String... validators) {
        for (Class<? extends IFastValidator> validatorClass : validatorClasses) {
            IFastValidator validator = FastChar.getOverrides().singleInstance(validatorClass);
            if (validator != null) {
                for (String s : validators) {
                    String valid = validator.validate(s, paramName, paramValue);
                    if (valid != null) {
                        return valid;
                    }
                }
            }
        }
        return null;
    }

    public Set<String> pluckKeys(String... validators) {
        Set<String> keys = new HashSet<>();
        for (Class<? extends IFastValidator> validatorClass : validatorClasses) {
            IFastValidator validator = FastChar.getOverrides().singleInstance(validatorClass);
            if (validator != null) {
                for (String s : validators) {
                    keys.addAll(validator.pluckKeys(s));
                }
            }
        }
        return keys;
    }

    public void flush() {
        List<Class<? extends IFastValidator>> waitRemove = new ArrayList<>();
        for (Class<? extends IFastValidator> validatorClass : validatorClasses) {
            if (FastClassUtils.isRelease(validatorClass)) {
                waitRemove.add(validatorClass);
            }
        }
        validatorClasses.removeAll(waitRemove);
    }


}
