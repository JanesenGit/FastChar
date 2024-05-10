package com.fastchar.out;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastResource;
import com.fastchar.extend.freemarker.FastFreemarkerEngine;
import com.fastchar.local.FastCharLocal;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastStringUtils;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 响应Freemarker模板
 */
@AFastClassFind(value = "freemarker.template.Template", url = "https://mvnrepository.com/artifact/org.freemarker/freemarker")
public class FastOutFreemarker extends FastOut<FastOutFreemarker> {

    public FastOutFreemarker() {
        this.contentType = "text/html";
    }

    @Override
    public void response(FastAction action) throws Exception {
        if (String.valueOf(data).toLowerCase().endsWith(".xml")) {
            this.contentType = "text/xml";
        }
        FastResource webResource = FastChar.getWebResources().getResource(String.valueOf(data));
        if (webResource == null) {
            action.response502(FastChar.getLocal().getInfo(FastCharLocal.VELOCITY_ERROR1, data));
            return;
        }

        FastHttpServletResponse response = action.getResponse();
        FastHttpServletRequest request = action.getRequest();
        response.setContentType(toContentType(action));
        response.setCharacterEncoding(getCharset());

        Map<String, Object> data = new HashMap<>(FastChar.getTemplates().getFinalContext());

        for (Enumeration<String> attrs = request.getAttributeNames(); attrs.hasMoreElements(); ) {
            String attrName = attrs.nextElement();
            data.put(attrName, request.getAttribute(attrName));
        }

        for (Enumeration<String> attrs = request.getSession().getAttributeNames(); attrs.hasMoreElements();) {
            String attrName = attrs.nextElement();
            data.put(attrName, request.getSession().getAttribute(attrName));
        }
        List<String> tempContent = FastFileUtils.readLines(webResource.getInputStream(), StandardCharsets.UTF_8);

        Configuration cfg = new Configuration(FastFreemarkerEngine.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        String templateName = FastChar.getSecurity().MD5_Encrypt(webResource.getURL().toString());
        stringLoader.putTemplate(templateName, FastStringUtils.join(tempContent, "\n"));
        cfg.setTemplateLoader(stringLoader);

        Template template = cfg.getTemplate(templateName,  FastChar.getConstant().getCharset());
        try (PrintWriter writer = getWriter(response)) {
            template.process(data, writer);
            writer.flush();
        }
    }

}
