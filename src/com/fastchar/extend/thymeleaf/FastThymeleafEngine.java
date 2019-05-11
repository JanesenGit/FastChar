package com.fastchar.extend.thymeleaf;

import com.fastchar.core.FastChar;
import com.fastchar.utils.FastStringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.StringWriter;
import java.util.Map;

public class FastThymeleafEngine extends TemplateEngine {

    private FastTemplateResolver fastTemplateResolver;

    public FastThymeleafEngine() {
        fastTemplateResolver = new FastTemplateResolver(FastChar.getServletContext());
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
            for (String key : finalContext.keySet()) {
                context.setVariable(key, finalContext.get(key));
            }
            StringWriter writer = new StringWriter();
            process(filePath, context, writer);
            String data = writer.toString();
            writer.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
