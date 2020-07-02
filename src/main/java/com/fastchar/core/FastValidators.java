package com.fastchar.core;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.interfaces.IFastParamConverter;
import com.fastchar.interfaces.IFastScannerAccepter;
import com.fastchar.interfaces.IFastValidator;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.validators.FastRegularValidator;

import java.util.*;

public final class FastValidators {

    FastValidators() {
    }

    public FastValidators putRegular(String key, String regular) {
        FastRegularValidator.REGULARS.put(key, regular);
        return this;
    }

    public Object validate(String paramName, Object paramValue, String... validators) {
        List<IFastValidator> iFastValidators = FastChar.getOverrides().singleInstances(false, IFastValidator.class);
        for (IFastValidator iFastValidator : iFastValidators) {
            if (iFastValidator != null) {
                for (String s : validators) {
                    String valid = iFastValidator.validate(s, paramName, paramValue);
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
        List<IFastValidator> iFastValidators = FastChar.getOverrides().singleInstances(false, IFastValidator.class);
        for (IFastValidator iFastValidator : iFastValidators) {
            if (iFastValidator != null) {
                for (String s : validators) {
                    keys.addAll(iFastValidator.pluckKeys(s));
                }
            }
        }
        return keys;
    }


}
