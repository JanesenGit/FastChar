package com.fastchar.interfaces;

import com.fastchar.servlet.FastServletContext;

import java.util.Set;

public interface IFastServletContainerInitializer {
    void onStartup(Set<Class<?>> c, FastServletContext ctx);

}
