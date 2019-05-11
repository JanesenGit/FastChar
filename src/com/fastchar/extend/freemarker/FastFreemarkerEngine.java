package com.fastchar.extend.freemarker;

import com.fastchar.core.FastChar;
import com.fastchar.utils.FastStringUtils;
import freemarker.template.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

public class FastFreemarkerEngine extends Configuration {

    public FastFreemarkerEngine() {
        setServletContextForTemplateLoading(FastChar.getServletContext(), "/");
        if (FastChar.getConstant().isDebug()) {
            setTemplateUpdateDelay(0);
        } else {
            setTemplateUpdateDelay(3600);
        }
        setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
        setDefaultEncoding(FastChar.getConstant().getEncoding());
        setOutputEncoding(FastChar.getConstant().getEncoding());
        setLocale(Locale.getDefault());
        setLocalizedLookup(false);

        setNumberFormat("#0.#####");
        setDateFormat("yyyy-MM-dd");
        setTimeFormat("HH:mm:ss");
        setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
    }


    public String run(Map<String, Object> dataModel, String filePath) {
        try {
            if (dataModel == null || FastStringUtils.isEmpty(filePath)) {
                return null;
            }
            dataModel.putAll(FastChar.getTemplates().getFinalContext());
            Template template = getTemplate(filePath);
            StringWriter writer = new StringWriter();
            template.process(dataModel, writer);
            String data = writer.toString();
            writer.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
