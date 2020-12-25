package com.fastchar.core;

import com.fastchar.local.FastCharLocal;
import com.fastchar.response.FastResponseWrapper;
import com.fastchar.interfaces.IFastWeb;
import com.fastchar.exception.FastWebException;

import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * FastChar核心拦截器
 * @author 沈建（Janesen）
 */
@SuppressWarnings("unchecked")
public final class FastFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            long time = System.currentTimeMillis();
            FastEngine engine = FastEngine.instance();
            engine.init(filterConfig.getServletContext());
            String web = filterConfig.getInitParameter("web");
            Class<?> aClass = FastClassUtils.getClass(web);
            if (aClass != null) {
                if (!IFastWeb.class.isAssignableFrom(aClass)) {
                    FastWebException fastWebException = new FastWebException(FastChar.getLocal().getInfo(FastCharLocal.CLASS_ERROR1, aClass.getSimpleName(), IFastWeb.class.getSimpleName()) +
                            "\n\tat " + new StackTraceElement(aClass.getName(), aClass.getSimpleName(), aClass.getSimpleName() + ".java", 1));
                    fastWebException.printStackTrace();
                    throw fastWebException;
                }
                engine.getWebs().putFastWeb((Class<? extends IFastWeb>) aClass);
            }
            engine.run();
            engine.getLog().info(FastFilter.class, engine.getLog().lightStyle(FastChar.getLocal().getInfo(FastCharLocal.FAST_CHAR_ERROR1, FastChar.getConstant().getProjectName(), (System.currentTimeMillis() - time) / 1000.0)));
            engine.getConstant().setWebStarted(true);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                FastEngine.instance().destroy();
            } catch (Exception ignored) {}

            throw new FastWebException(e);
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setCharacterEncoding(FastEngine.instance().getConstant().getEncoding());
        request.setCharacterEncoding(FastEngine.instance().getConstant().getEncoding());

        FastResponseWrapper responseWrapper = new FastResponseWrapper(response);
        new FastDispatcher(filterChain, request, responseWrapper).invoke();
    }

    @Override
    public void destroy() {
        try {
            FastEngine.instance().destroy();
            FastEngine.instance().getLog().info(FastFilter.class, FastEngine.instance().getLog().lightStyle(FastChar.getLocal().getInfo(FastCharLocal.FAST_CHAR_ERROR3, FastChar.getConstant().getProjectName())));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
