package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import freemarker.template.Template;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应Freemarker模板
 */
public class FastOutFreemarker extends FastOut<FastOutFreemarker> {

    public FastOutFreemarker() {
        this.contentType = "text/html";
    }

    @Override
    public void response(FastAction action) throws Exception {
        if (String.valueOf(data).toLowerCase().endsWith(".xml")) {
            this.contentType = "text/xml";
        }
        HttpServletResponse response = action.getResponse();
        HttpServletRequest request = action.getRequest();
        response.setContentType(toContentType());
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


        Template template = FastChar.getTemplates().getFreemarker().getTemplate(String.valueOf(data));
        PrintWriter writer = response.getWriter();
        template.process(data, writer);
        writer.flush();

    }

}
