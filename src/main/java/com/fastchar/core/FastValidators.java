package com.fastchar.core;

import com.fastchar.interfaces.IFastValidator;
import com.fastchar.utils.FastStringUtils;

import java.util.List;

public final class FastValidators {

    FastValidators() {
    }


    public Object validate(String validator, Object[] arguments, String paramName, Object paramValue) {
        if (FastStringUtils.isEmpty(validator)) {
            return null;
        }
        List<IFastValidator> iFastValidators = FastChar.getOverrides().singleInstances(false, IFastValidator.class);
        for (IFastValidator iFastValidator : iFastValidators) {
            if (iFastValidator != null) {
                String valid = iFastValidator.validate(validator, arguments,paramName, paramValue);
                if (valid != null) {
                    return valid;
                }
            }
        }
        return null;
    }


}
