package com.fastchar.out;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;
import org.thymeleaf.context.Context;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

/**
 * 响应输出Thymeleaf模板
 */
@AFastClassFind(value = "org.thymeleaf.context.WebContext", url = "https://mvnrepository.com/artifact/org.thymeleaf/thymeleaf")
public class FastOutThymeleaf extends FastOut<FastOutThymeleaf> {
    public FastOutThymeleaf() {
        this.contentType = "text/html";
    }

    /**
     * 响应数据
     */
    @Override
    public void response(FastAction action) throws Exception {
        if (String.valueOf(data).toLowerCase().endsWith(".xml")) {
            this.contentType = "text/xml";
        }
        FastHttpServletResponse response = action.getResponse();
        FastHttpServletRequest request = action.getRequest();
        response.setContentType(toContentType(action, false));
        response.setCharacterEncoding(getCharset());


        Context context = new Context();

        Map<String, Object> finalContext = FastChar.getTemplates().getFinalContext();
        for (Map.Entry<String, Object> stringObjectEntry : finalContext.entrySet()) {
            context.setVariable(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }

        for (Enumeration<String> attrs = request.getAttributeNames(); attrs.hasMoreElements(); ) {
            String attrName = attrs.nextElement();
            context.setVariable(attrName, request.getAttribute(attrName));
        }

        for (Enumeration<String> attrs = request.getSession().getAttributeNames(); attrs.hasMoreElements();) {
            String attrName = attrs.nextElement();
            context.setVariable(attrName, request.getSession().getAttribute(attrName));
        }

        try (PrintWriter writer = getWriter(response)) {
            FastChar.getTemplates().getThymeleaf().process(String.valueOf(data),
                    context, writer);
            writer.flush();
        }
    }
}
