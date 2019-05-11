package com.fastchar.interfaces;

import com.fastchar.core.FastAction;

public interface IFastInterceptor {

    void onInterceptor(FastAction fastAction) throws Exception;
}
