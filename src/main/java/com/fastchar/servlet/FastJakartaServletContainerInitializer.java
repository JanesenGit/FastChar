package com.fastchar.servlet;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastFilterByJakarta;
import com.fastchar.interfaces.IFastServletContainerInitializer;
import jakarta.servlet.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class FastJakartaServletContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        FastFilterByJakarta.initFastEngine(ctx, null);

        List<IFastServletContainerInitializer> iFastServletContainerInitializers = FastChar.getOverrides().newInstances(false, IFastServletContainerInitializer.class);
        for (IFastServletContainerInitializer iFastServletContainerInitializer : iFastServletContainerInitializers) {
            iFastServletContainerInitializer.onStartup(c, new FastServletContext(ctx));
        }

        for (FilterRegistration value : ctx.getFilterRegistrations().values()) {
            if (value.getClassName().equals(FastFilterByJakarta.class.getName())) {
                //存在FastFilter过滤器
                return;
            }
        }
        FilterRegistration.Dynamic fastChar = ctx.addFilter("fastchar", FastFilterByJakarta.class);
        if (fastChar != null) {
            fastChar.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
        }
    }
}
