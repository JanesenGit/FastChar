package com.fastchar.extend.thymeleaf;

import com.fastchar.core.FastChar;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import javax.servlet.ServletContext;

public class FastTemplateResolver extends ServletContextTemplateResolver {

    public FastTemplateResolver(ServletContext servletContext) {
        super(servletContext);
        initConfig();
    }

    private void initConfig() {
        setPrefix("/");
        setCacheable(true);
        if (FastChar.getConstant().isDebug()) {
            setCacheTTLMs(0L);
        } else {
            setCacheTTLMs(3600000L);//缓存一个小时
        }
        setCharacterEncoding(FastChar.getConstant().getEncoding());
        setTemplateMode(TemplateMode.HTML);
    }

}
