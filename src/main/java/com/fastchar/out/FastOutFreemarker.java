package com.fastchar.out;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;
import freemarker.template.Template;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
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

        Template template = FastChar.getTemplates().getFreemarker().getTemplate(String.valueOf(data), FastChar.getConstant().getCharset());
        try (PrintWriter writer = getWriter(response)) {
            template.process(data, writer);
            writer.flush();
        }
    }

}
