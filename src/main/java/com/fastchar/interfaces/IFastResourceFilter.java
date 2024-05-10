package com.fastchar.interfaces;

import com.fastchar.core.FastResource;

public interface IFastResourceFilter {

    boolean onAccept(FastResource resource);

}
