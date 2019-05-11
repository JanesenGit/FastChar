package com.fastchar.interfaces;

import com.fastchar.asm.FastParameter;
import com.fastchar.core.FastAction;

import java.lang.reflect.Type;

public interface IFastParamConverter {

    Object convertValue(FastAction action, FastParameter parameter, int[] marker) throws Exception;

}
