package com.fastchar.extend.freemarker;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastTemplate;
import com.fastchar.utils.FastStringUtils;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

public class FastFreemarkerEngine extends Configuration  implements IFastTemplate {

    public FastFreemarkerEngine() {
        setServletContextForTemplateLoading(FastChar.getServletContext(), "/");
        setDefault(this);
    }

    private void setDefault(Configuration configuration) {
        if (configuration == null) {
            return;
        }
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        if (FastChar.getConstant().isDebug()) {
            configuration.setTemplateUpdateDelay(0);
        } else {
            configuration.setTemplateUpdateDelay(3600);
        }
        configuration.setDefaultEncoding(FastChar.getConstant().getEncoding());
        configuration.setOutputEncoding(FastChar.getConstant().getEncoding());
        configuration.setLocale(Locale.getDefault());
        configuration.setLocalizedLookup(false);

        configuration.setNumberFormat("#0.#####");
        configuration.setDateFormat("yyyy-MM-dd");
        configuration.setTimeFormat("HH:mm:ss");
        configuration.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
    }

    public String run(Map<String, Object> params, String template) {
        try {
            if (params == null || FastStringUtils.isEmpty(template)) {
                return null;
            }
            Configuration cfg = new Configuration();
            params.putAll(FastChar.getTemplates().getFinalContext());
            StringTemplateLoader stringLoader = new StringTemplateLoader();
            String templateName = FastChar.getSecurity().MD5_Encrypt(template);
            stringLoader.putTemplate(templateName, template);
            cfg.setTemplateLoader(stringLoader);

            Template temp = cfg.getTemplate(templateName, "utf-8");
            StringWriter writer = new StringWriter();
            temp.process(params, writer);
            String data = writer.toString();
            writer.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String run(Map<String, Object> params, File template) {
        try {
            if (params == null || template == null || !template.exists()) {
                return null;
            }
            params.putAll(FastChar.getTemplates().getFinalContext());
            Template temp = getTemplate(template.getAbsolutePath());
            StringWriter writer = new StringWriter();
            temp.process(params, writer);
            String data = writer.toString();
            writer.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
