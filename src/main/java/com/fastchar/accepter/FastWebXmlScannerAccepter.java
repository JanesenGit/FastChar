package com.fastchar.accepter;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastEngine;
import com.fastchar.interfaces.IFastScannerAccepter;
import com.fastchar.servlet.FastServletRegistration;
import com.fastchar.utils.FastNumberUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * web.xml文件扫描接收器
 */
public class FastWebXmlScannerAccepter implements IFastScannerAccepter {
    @Override
    public void onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception {
    }

    @Override
    public void onScannerFile(FastEngine engine, File file) throws Exception {
        if ("web.xml".equalsIgnoreCase(file.getName())) {
            initServletMapping(engine);
            initWebXmlConfig(engine, file);
        }
    }


    private void initWebXmlConfig(FastEngine engine,File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);

            NodeList errorPage = doc.getElementsByTagName("error-page");
            for (int i = 0; i < errorPage.getLength(); i++) {
                Node item = errorPage.item(i);
                if (item instanceof Element) {
                    Element element = (Element) item;
                    NodeList errorCode = element.getElementsByTagName("error-code");
                    if (errorCode.getLength() > 0) {
                        NodeList location = element.getElementsByTagName("location");
                        if (location.getLength() > 0) {
                            int code = FastNumberUtils.formatToInt(errorCode.item(0).getTextContent().trim());
                            String path = location.item(0).getTextContent();
                            if (code == 404) {
                                engine.getConstant().setErrorPage404(path);
                            } else if (code == 500) {
                                engine.getConstant().setErrorPage500(path);
                            } else if (code == 502) {
                                engine.getConstant().setErrorPage502(path);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void initServletMapping(FastEngine engine) {
        if (engine.getActions().isExcludeServlet() && engine.getServletContext() != null) {
            Map<String, ? extends FastServletRegistration> servletRegistrations =
                    engine.getServletContext().getServletRegistrations();
            for (FastServletRegistration value : servletRegistrations.values()) {
                Collection<String> mappings = value.getMappings();
                mappings.remove("/");//排除根级路径
                FastChar.getActions().addExcludeUrls(mappings.toArray(new String[]{}));
            }
        }
    }
}
