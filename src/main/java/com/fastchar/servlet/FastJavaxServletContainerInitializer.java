package com.fastchar.servlet;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastFilterByJavax;
import com.fastchar.interfaces.IFastServletContainerInitializer;

import javax.servlet.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class FastJavaxServletContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        FastFilterByJavax.initFastEngine(ctx, null);

        List<IFastServletContainerInitializer> iFastServletContainerInitializers = FastChar.getOverrides().newInstances(false, IFastServletContainerInitializer.class);
        for (IFastServletContainerInitializer iFastServletContainerInitializer : iFastServletContainerInitializers) {
            iFastServletContainerInitializer.onStartup(c, FastServletContext.newInstance(ctx));
        }

        for (FilterRegistration value : ctx.getFilterRegistrations().values()) {
            if (value.getClassName().equals(FastFilterByJavax.class.getName())) {
                //存在FastFilter过滤器
                return;
            }
        }

        FilterRegistration.Dynamic fastChar = ctx.addFilter("fastchar", FastFilterByJavax.class);
        if (fastChar != null) {
            fastChar.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
        }
    }

}
