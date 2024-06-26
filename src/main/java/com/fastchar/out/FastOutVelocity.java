package com.fastchar.out;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastResource;
import com.fastchar.local.FastCharLocal;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;
import org.apache.velocity.VelocityContext;

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;

/**
 * 响应输出Velocity模板
 */
@AFastClassFind(value = "org.apache.velocity.VelocityContext", url = "https://mvnrepository.com/artifact/org.apache.velocity/velocity-engine-core")
public class FastOutVelocity extends FastOut<FastOutVelocity> {

    public FastOutVelocity() {
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

        FastHttpServletResponse response = action.getResponse();
        FastHttpServletRequest request = action.getRequest();
        response.setContentType(toContentType(action, false));
        response.setCharacterEncoding(getCharset());

        FastResource webResource = FastChar.getWebResources().getResource(String.valueOf(data));
        if (webResource == null) {
            action.response502(FastChar.getLocal().getInfo(FastCharLocal.VELOCITY_ERROR1, data));
            return;
        }


        VelocityContext context = new VelocityContext();
        Map<String, Object> finalContext = FastChar.getTemplates().getFinalContext();
        for (Map.Entry<String, Object> stringObjectEntry : finalContext.entrySet()) {
            context.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }

        for (Enumeration<String> attrs = request.getAttributeNames(); attrs.hasMoreElements(); ) {
            String attrName = attrs.nextElement();
            context.put(attrName, request.getAttribute(attrName));
        }
        for (Enumeration<String> attrs = request.getSession().getAttributeNames(); attrs.hasMoreElements(); ) {
            String attrName = attrs.nextElement();
            context.put(attrName, request.getSession().getAttribute(attrName));
        }
        try (PrintWriter writer = getWriter(response)) {
            FastChar.getTemplates().getVelocity().evaluate(context, writer, "FastChar-Velocity", new InputStreamReader(webResource.getInputStream(), FastChar.getConstant().getCharset()));
            writer.flush();
        }
    }
}
