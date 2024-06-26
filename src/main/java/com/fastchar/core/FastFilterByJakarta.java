package com.fastchar.core;

import com.fastchar.enums.FastServletType;
import com.fastchar.exception.FastWebException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.servlet.FastFilterChain;
import com.fastchar.servlet.FastServletContext;
import com.fastchar.servlet.http.wrapper.FastHttpServletRequestWrapper;
import com.fastchar.servlet.http.wrapper.FastHttpServletResponseWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * FastChar核心拦截器
 *
 * @author 沈建（Janesen）
 */
public final class FastFilterByJakarta implements Filter {
    private static boolean INIT_FAST_ENGINE = false;

    public static void initFastEngine(ServletContext context, String webClassName) {
        if (INIT_FAST_ENGINE) {
            return;
        }
        FastEngine.instance().getConstant().servletType = FastServletType.Jakarta;
        try {
            FastEngine.instance().createWebServer(FastFilterByJakarta.class,FastServletContext.newInstance(context), webClassName);
        } catch (Throwable e) {
            String info = FastChar.getLocal().getInfo(FastCharLocal.FAST_CHAR_ERROR4, FastChar.getConstant().getProjectName());
            FastWebException fastWebException = new FastWebException(info, e);
            fastWebException.printStackTrace();
            throw fastWebException;
        }finally {
            INIT_FAST_ENGINE = true;
        }
    }

    @Override
    public synchronized void init(FilterConfig filterConfig) throws ServletException {
        initFastEngine(filterConfig.getServletContext(), filterConfig.getInitParameter("web"));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setCharacterEncoding(FastEngine.instance().getConstant().getCharset());
        request.setCharacterEncoding(FastEngine.instance().getConstant().getCharset());

        new FastDispatcher(FastFilterChain.newInstance(filterChain),
                FastHttpServletRequestWrapper.newInstance(request),
                FastHttpServletResponseWrapper.newInstance(response)).invoke();

    }


    @Override
    public synchronized void destroy() {
        try {
            FastEngine.instance().destroy();
            FastEngine.instance().getLogger().info(FastFilterByJakarta.class, FastChar.getLocal().getInfo(FastCharLocal.FAST_CHAR_ERROR3, FastChar.getConstant().getProjectName()));
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }

}
