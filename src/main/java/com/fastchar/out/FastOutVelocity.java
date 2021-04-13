package com.fastchar.out;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.local.FastCharLocal;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
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

        HttpServletResponse response = action.getResponse();
        HttpServletRequest request = action.getRequest();
        response.setContentType(toContentType(action));
        response.setCharacterEncoding(getCharset());

        File templateFile = new File(FastChar.getPath().getWebRootPath(), String.valueOf(data));
        if (!templateFile.exists()) {
            action.response502(FastChar.getLocal().getInfo(FastCharLocal.VELOCITY_ERROR1, data));
            return;
        }

        Template template = FastChar.getTemplates().getVelocity().getTemplate(String.valueOf(data));

        VelocityContext context = new VelocityContext();
        Map<String, Object> finalContext = FastChar.getTemplates().getFinalContext();
        for (String key : finalContext.keySet()) {
            context.put(key, finalContext.get(key));
        }

        for (Enumeration<String> attrs = request.getAttributeNames(); attrs.hasMoreElements(); ) {
            String attrName = attrs.nextElement();
            context.put(attrName, request.getAttribute(attrName));
        }
        for (Enumeration<String> attrs = request.getSession().getAttributeNames(); attrs.hasMoreElements(); ) {
            String attrName = attrs.nextElement();
            context.put(attrName, request.getSession().getAttribute(attrName));
        }
        try (PrintWriter writer = response.getWriter()) {
            template.merge(context, writer);
            writer.flush();
        }
    }
}
