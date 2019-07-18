package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import org.thymeleaf.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

/**
 * 响应输出Thymeleaf模板
 */
public class FastOutThymeleaf extends FastOut<FastOutThymeleaf> {
    public FastOutThymeleaf() {
        this.contentType = "text/html";
    }

    /**
     * 响应数据
     *
     * @param action
     */
    @Override
    public void response(FastAction action) throws Exception {
        if (String.valueOf(data).toLowerCase().endsWith(".xml")) {
            this.contentType = "text/xml";
        }
        HttpServletResponse response = action.getResponse();
        HttpServletRequest request = action.getRequest();
        response.setContentType(toContentType());
        response.setCharacterEncoding(getCharset());


        WebContext context = new WebContext(request,response,
                action.getServletContext());
        Map<String, Object> finalContext = FastChar.getTemplates().getFinalContext();
        for (String key : finalContext.keySet()) {
            context.setVariable(key, finalContext.get(key));
        }

        for (Enumeration<String> attrs = request.getSession().getAttributeNames(); attrs.hasMoreElements();) {
            String attrName = attrs.nextElement();
            context.setVariable(attrName, request.getSession().getAttribute(attrName));
        }

        PrintWriter writer = response.getWriter();
        FastChar.getTemplates().getThymeleaf().process(String.valueOf(data),
                context, writer);
        writer.flush();
    }
}
