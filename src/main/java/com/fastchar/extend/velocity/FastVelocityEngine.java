package com.fastchar.extend.velocity;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastTemplate;
import com.fastchar.utils.FastStringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;

public class FastVelocityEngine extends VelocityEngine implements IFastTemplate {

    public FastVelocityEngine() {
        setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, FastChar.getPath().getWebRootPath());
        setProperty(Velocity.ENCODING_DEFAULT, FastChar.getConstant().getEncoding());
        setProperty(Velocity.INPUT_ENCODING, FastChar.getConstant().getEncoding());
        setProperty(Velocity.ENCODING_DEFAULT, FastChar.getConstant().getEncoding());
        setProperty(Velocity.VM_LIBRARY_AUTORELOAD, false);

        setProperty(Velocity.RESOURCE_MANAGER_DEFAULTCACHE_SIZE, 0);
        if (FastChar.getConstant().isDebug()) {
            setProperty(Velocity.RESOURCE_LOADER_CACHE, false);
        } else {
            setProperty(Velocity.RESOURCE_LOADER_CACHE, true);
            setProperty(Velocity.RESOURCE_LOADER_CHECK_INTERVAL, 3600);
        }
    }


    public String run(VelocityContext context, String filePath) {
        try {
            if (context == null || FastStringUtils.isEmpty(filePath)) {
                return null;
            }
            Map<String, Object> finalContext = FastChar.getTemplates().getFinalContext();
            for (String key : finalContext.keySet()) {
                context.put(key, finalContext.get(key));
            }


            Template template = getTemplate(filePath);
            StringWriter writer = new StringWriter();
            template.merge(context, writer);
            String data = writer.toString();
            writer.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String run(Map<String, Object> params, String template) {
        try {
            if (params == null || FastStringUtils.isEmpty(template)) {
                return null;
            }
            VelocityContext context = new VelocityContext();
            params.putAll(FastChar.getTemplates().getFinalContext());
            for (String key : params.keySet()) {
                context.put(key, params.get(key));
            }

            StringWriter writer = new StringWriter();
            evaluate(context, writer, "", template);
            String data = writer.toString();
            writer.close();
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String run(Map<String, Object> params, File template) {
        if (params == null || template == null || !template.exists()) {
            return null;
        }
        VelocityContext context = new VelocityContext();
        params.putAll(FastChar.getTemplates().getFinalContext());
        for (String key : params.keySet()) {
            context.put(key, params.get(key));
        }
        return run(context, template.getAbsolutePath());
    }
}
