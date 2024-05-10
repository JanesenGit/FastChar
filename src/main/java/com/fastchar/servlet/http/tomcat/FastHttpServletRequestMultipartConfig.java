package com.fastchar.servlet.http.tomcat;

import com.fastchar.core.FastChar;
import com.fastchar.servlet.FastServletHelper;
import com.fastchar.utils.FastClassUtils;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;

import java.lang.reflect.Field;

public class FastHttpServletRequestMultipartConfig {

    private final Object request;

    public FastHttpServletRequestMultipartConfig(Object request) {
        this.request = request;
    }

    public void multipartConfig() {
        if (request instanceof RequestFacade) {
            try {
                RequestFacade requestFacade = (RequestFacade) request;
                Field requestField = RequestFacade.class.getDeclaredField("request");
                requestField.setAccessible(true);
                Request realRequest = (Request) requestField.get(requestFacade);
                Wrapper wrapper = realRequest.getWrapper();

                Field multipartConfigElementField = FastClassUtils.getDeclaredField(wrapper.getClass(), "multipartConfigElement");
                if (multipartConfigElementField != null) {
                    multipartConfigElementField.setAccessible(true);
                    if (FastServletHelper.isJavaxServlet()) {
                        javax.servlet.MultipartConfigElement configElement = new javax.servlet.MultipartConfigElement("", FastChar.getConstant().getAttachMaxPostSize(), -1L, 0);
                        multipartConfigElementField.set(wrapper, configElement);
                    } else if (FastServletHelper.isJakartaServlet()) {
                        jakarta.servlet.MultipartConfigElement configElement = new jakarta.servlet.MultipartConfigElement("", FastChar.getConstant().getAttachMaxPostSize(), -1L, 0);
                        multipartConfigElementField.set(wrapper, configElement);
                    }
                    multipartConfigElementField.setAccessible(false);
                }
                requestField.setAccessible(false);
            } catch (Exception e) {
                FastChar.getLogger().error(this.getClass(), e);
            }
        }
    }

}
