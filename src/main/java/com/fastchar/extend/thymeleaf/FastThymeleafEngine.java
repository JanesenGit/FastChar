package com.fastchar.extend.thymeleaf;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastTemplate;
import com.fastchar.servlet.FastServletHelper;
import com.fastchar.utils.FastStringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.io.File;
import java.io.StringWriter;
import java.util.Map;

@AFastClassFind(value = {"org.thymeleaf.TemplateEngine"}, url = {"https://mvnrepository.com/artifact/org.thymeleaf/thymeleaf"})
public class FastThymeleafEngine extends TemplateEngine implements IFastTemplate {

    private FastTemplateResolver fastTemplateResolver;

    public FastThymeleafEngine() {
        if (FastServletHelper.isJavaxServlet()) {
            fastTemplateResolver = new FastTemplateResolver((javax.servlet.ServletContext) FastChar.getServletContext().getTarget());
            setTemplateResolver(fastTemplateResolver);
        }
        addTemplateResolver(new StringTemplateResolver());
    }

    public FastTemplateResolver getFastTemplateResolver() {
        return fastTemplateResolver;
    }


    public String run(Context context, String filePath) {
        try {
            if (context == null || FastStringUtils.isEmpty(filePath)) {
                return null;
            }
            Map<String, Object> finalContext = FastChar.getTemplates().getFinalContext();
            for (Map.Entry<String, Object> stringObjectEntry : finalContext.entrySet()) {
                context.setVariable(stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }
            StringWriter writer = new StringWriter();
            process(filePath, context, writer);
            String data = writer.toString();
            writer.close();
            return data;
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return null;
    }

    @Override
    public String run(Map<String, Object> params, String template) {
        try {
            if (params == null || FastStringUtils.isEmpty(template)) {
                return null;
            }
            Context context = new Context();
            params.putAll(FastChar.getTemplates().getFinalContext());
            for (Map.Entry<String, Object> stringObjectEntry : params.entrySet()) {
                context.setVariable(stringObjectEntry.getKey(), stringObjectEntry.getValue());
            }
            StringWriter writer = new StringWriter();
            process(template, context, writer);
            String data = writer.toString();
            writer.close();
            return data;
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return null;
    }

    @Override
    public String run(Map<String, Object> params, File template) {
        if (params == null || template == null || !template.exists()) {
            return null;
        }
        Context context = new Context();
        params.putAll(FastChar.getTemplates().getFinalContext());
        for (Map.Entry<String, Object> stringObjectEntry : params.entrySet()) {
            context.setVariable(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }
        return run(context, template.getAbsolutePath());
    }

}
