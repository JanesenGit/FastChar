package com.fastchar.multipart;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastFile;
import com.fastchar.exception.FastFileException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.core.ApplicationPart;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * 使用Servlet3.0自带的文件上传功能
 */
public class FastApplicationPartWrapper extends HttpServletRequestWrapper {

    private  List<FastFile<?>> files = null;
    private final HttpServletRequest request;

    public FastApplicationPartWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
        if (ServletFileUpload.isMultipartContent(request)) {
            multiConfig(request);
        }
    }

    private void multiConfig(HttpServletRequest request) {
        if (request instanceof HttpServletRequestWrapper) {
            HttpServletRequestWrapper requestWrapper = (HttpServletRequestWrapper) request;
            if (requestWrapper.getRequest() instanceof HttpServletRequest) {
                request = (HttpServletRequest) requestWrapper.getRequest();
            }
        }
        if (request instanceof RequestFacade) {
            try {
                RequestFacade requestFacade= (RequestFacade) request;
                Field requestField = RequestFacade.class.getDeclaredField("request");
                requestField.setAccessible(true);
                Request realRequest = (Request) requestField.get(requestFacade);
                Wrapper wrapper = realRequest.getWrapper();

                Field multipartConfigElementField = FastClassUtils.getDeclaredField(wrapper.getClass(), "multipartConfigElement");
                if (multipartConfigElementField != null) {
                    multipartConfigElementField.setAccessible(true);
                    MultipartConfigElement configElement = new MultipartConfigElement("",
                            FastChar.getConstant().getAttachMaxPostSize(),
                            -1L, 0);
                    multipartConfigElementField.set(wrapper, configElement);
                    multipartConfigElementField.setAccessible(false);
                }
                requestField.setAccessible(false);
            } catch (Exception ignored) {}
        }
    }

    public List<FastFile<?>> getFiles() {
        if (files == null) {
            files = new ArrayList<>();
            try {
                for (Part part : request.getParts()) {
                    ApplicationPart applicationPart = (ApplicationPart) part;
                    String fileName = applicationPart.getSubmittedFileName();
                    if (FastStringUtils.isNotEmpty(fileName)) {
                        File saveFile = new File(FastChar.getConstant().getAttachDirectory(), fileName);
                        if (!saveFile.getParentFile().exists()) {
                            if (!saveFile.getParentFile().mkdirs()) {
                                throw new FastFileException(FastChar.getLocal().getInfo(FastCharLocal.FILE_ERROR1));
                            }
                        }

                        if (saveFile.exists()) {
                            saveFile = FastChar.getFileRename().rename(saveFile, FastChar.getConstant().isAttachNameMD5());
                        }
                        part.write(saveFile.getAbsolutePath());

                        files.add(FastFile.newInstance(saveFile)
                                .setParamName(part.getName())
                                .setContentType(part.getContentType())
                                .setUploadFileName(fileName));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return files;
    }

    public FastFile<?> getFile(String name) {
        for (FastFile<?> file : getFiles()) {
            if (file.getParamName().equals(name)) {
                return file;
            }
        }
        return null;
    }

    public FastFile<?>[] getFiles(String name) {
        List<FastFile<?>>  curr= new ArrayList<>();
        for (FastFile<?> file : getFiles()) {
            if (file.getParamName().equals(name)) {
                curr.add(file);
            }
        }
        return curr.toArray(new FastFile<?>[]{});
    }

    public void putFile(String name, FastFile<?> fastFile) {
        fastFile.setParamName(name);
        files.add(fastFile);
    }


    public List<String> getFileParamNames() {
        List<String> names = new ArrayList<>();
        for (FastFile<?> file : getFiles()) {
            names.add(file.getParamName());
        }
        return names;
    }

}
