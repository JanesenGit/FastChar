package com.fastchar.core;


import com.fastchar.exception.FastFileException;
import com.fastchar.multipart.FileRenamePolicy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FastMultipartWrapper extends HttpServletRequestWrapper {
    private FastMultipartRequest multipartRequest = null;

    public FastMultipartWrapper(HttpServletRequest req, String dir) throws IOException, FastFileException {
        super(req);
        this.multipartRequest = new FastMultipartRequest(req, dir);
    }

    public FastMultipartWrapper(HttpServletRequest req, String dir, int maxPostSize) throws IOException, FastFileException {
        super(req);
        checkDirectory(dir);
        this.multipartRequest = new FastMultipartRequest(req, dir, maxPostSize);
    }

    public FastMultipartWrapper(HttpServletRequest req, String dir, int maxPostSize, String encoding) throws IOException, FastFileException {
        super(req);
        checkDirectory(dir);
        this.multipartRequest = new FastMultipartRequest(req, dir, maxPostSize, encoding, new DefaultFileRenamePolicy(false));
    }

    public FastMultipartWrapper(HttpServletRequest req, String dir, int maxPostSize, String encoding, boolean nameMD5) throws IOException, FastFileException {
        super(req);
        checkDirectory(dir);
        this.multipartRequest = new FastMultipartRequest(req, dir, maxPostSize, encoding, new DefaultFileRenamePolicy(nameMD5));
    }


    private void checkDirectory(String dir) throws FastFileException {
        File file = new File(dir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new FastFileException(FastChar.getLocal().getInfo("File_Error1", "'" + dir + "'"));
            }
        }
    }

    public Enumeration getParameterNames() {
        return this.multipartRequest.getParameterNames();
    }

    public String getParameter(String name) {
        return this.multipartRequest.getParameter(name);
    }

    public String[] getParameterValues(String name) {
        return this.multipartRequest.getParameterValues(name);
    }

    public Map getParameterMap() {
        Map map = new HashMap();
        Enumeration enumm = this.getParameterNames();

        while (enumm.hasMoreElements()) {
            String name = (String) enumm.nextElement();
            map.put(name, this.multipartRequest.getParameterValues(name));
        }

        return map;
    }

    public Enumeration getFileNames() {
        return this.multipartRequest.getFileNames();
    }

    public String getFilesystemName(String name) {
        return this.multipartRequest.getFilesystemName(name);
    }

    public String getOriginalFileName(String name) {
        return this.multipartRequest.getOriginalFileName(name);
    }

    public String getContentType(String name) {
        return this.multipartRequest.getContentType(name);
    }

    public FastFile getFile(String name) {
        return this.multipartRequest.getFile(name);
    }

    public void putFile(String name, FastFile fastFile) {
        this.multipartRequest.putFile(name, fastFile);
    }

    public List<FastFile<?>> getFiles() {
        return this.multipartRequest.getFiles();
    }


    class DefaultFileRenamePolicy implements FileRenamePolicy {
        private boolean md5Name;

        public DefaultFileRenamePolicy(boolean md5Name) {
            this.md5Name = md5Name;
        }

        public File rename(File f) {
            return FastChar.getFileRename().rename(f, md5Name);
        }
    }
}
