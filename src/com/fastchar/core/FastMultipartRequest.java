package com.fastchar.core;

import com.fastchar.exception.FastFileException;

import com.fastchar.multipart.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class FastMultipartRequest {
    private static final int DEFAULT_MAX_POST_SIZE = 1048576;
    protected Hashtable parameters;
    protected Hashtable files;

    public FastMultipartRequest(HttpServletRequest request, String saveDirectory) throws FastFileException, IOException {
        this(request, saveDirectory, 1048576);
    }

    public FastMultipartRequest(HttpServletRequest request, String saveDirectory, int maxPostSize) throws FastFileException, IOException {
        this(request, saveDirectory, maxPostSize, (String) null, (FileRenamePolicy) null);
    }

    public FastMultipartRequest(HttpServletRequest request, String saveDirectory, String encoding) throws FastFileException, IOException {
        this(request, saveDirectory, 1048576, encoding, (FileRenamePolicy) null);
    }

    public FastMultipartRequest(HttpServletRequest request, String saveDirectory, int maxPostSize, FileRenamePolicy policy) throws FastFileException, IOException {
        this(request, saveDirectory, maxPostSize, (String) null, policy);
    }

    public FastMultipartRequest(HttpServletRequest request, String saveDirectory, int maxPostSize, String encoding) throws FastFileException, IOException {
        this(request, saveDirectory, maxPostSize, encoding, (FileRenamePolicy) null);
    }

    public FastMultipartRequest(HttpServletRequest request, String saveDirectory, int maxPostSize, String encoding, FileRenamePolicy policy) throws FastFileException, IOException {
        this.parameters = new Hashtable();
        this.files = new Hashtable();
        if (request == null) {
            throw new FastFileException(FastChar.getLocal().getInfo("File_Error2"));
        } else if (saveDirectory == null) {
            throw new FastFileException(FastChar.getLocal().getInfo("File_Error3"));
        } else if (maxPostSize <= 0) {
            throw new FastFileException(FastChar.getLocal().getInfo("File_Error4"));
        } else {
            File dir = new File(saveDirectory);
            if (!dir.isDirectory()) {
                throw new FastFileException(FastChar.getLocal().getInfo("File_Error5"));
            } else if (!dir.canWrite()) {
                throw new FastFileException(FastChar.getLocal().getInfo("File_Error6"));
            } else {
                MultipartParser parser = new MultipartParser(request, maxPostSize, true, true, encoding);
                Vector existingValues;
                if (request.getQueryString() != null) {
                    Hashtable queryParameters = HttpUtils.parseQueryString(request.getQueryString());
                    Enumeration queryParameterNames = queryParameters.keys();

                    while (queryParameterNames.hasMoreElements()) {
                        Object paramName = queryParameterNames.nextElement();
                        String[] values = (String[]) ((String[]) queryParameters.get(paramName));
                        existingValues = new Vector();

                        for (int i = 0; i < values.length; ++i) {
                            existingValues.add(values[i]);
                        }

                        this.parameters.put(paramName, existingValues);
                    }
                }

                Part part;
                while ((part = parser.readNextPart()) != null) {
                    String name = part.getName();
                    if (name == null) {
                        throw new IOException("Malformed input: parameter name missing (known Opera 7 bug)");
                    }

                    String fileName;
                    if (part.isParam()) {
                        ParamPart paramPart = (ParamPart) part;
                        fileName = paramPart.getStringValue();
                        existingValues = (Vector) this.parameters.get(name);
                        if (existingValues == null) {
                            existingValues = new Vector();
                            this.parameters.put(name, existingValues);
                        }

                        existingValues.addElement(fileName);
                    } else if (part.isFile()) {
                        FilePart filePart = (FilePart) part;
                        fileName = filePart.getFileName();
                        if (fileName != null) {
                            filePart.setRenamePolicy(policy);
                            filePart.writeTo(dir);
                            FastFile fastFile = FastChar.getOverrides().newInstance(FastFile.class, name, dir.toString(), filePart.getFileName(), fileName, filePart.getContentType());
                            this.files.put(name, fastFile);
                        } else {
                            FastFile fastFile = FastChar.getOverrides().newInstance(FastFile.class, name, null, null, null, null);
                            this.files.put(name, fastFile);
                        }
                    }
                }

            }
        }
    }

    public FastMultipartRequest(ServletRequest request, String saveDirectory) throws IOException, FastFileException {
        this((HttpServletRequest) request, saveDirectory);
    }

    public FastMultipartRequest(ServletRequest request, String saveDirectory, int maxPostSize) throws IOException, FastFileException {
        this((HttpServletRequest) request, saveDirectory, maxPostSize);
    }

    public Enumeration getParameterNames() {
        return this.parameters.keys();
    }

    public Enumeration getFileNames() {
        return this.files.keys();
    }

    public String getParameter(String name) {
        try {
            Vector values = (Vector) this.parameters.get(name);
            if (values != null && values.size() != 0) {
                String value = (String) values.elementAt(values.size() - 1);
                return value;
            } else {
                return null;
            }
        } catch (Exception var4) {
            return null;
        }
    }

    public String[] getParameterValues(String name) {
        try {
            Vector values = (Vector) this.parameters.get(name);
            if (values != null && values.size() != 0) {
                String[] valuesArray = new String[values.size()];
                values.copyInto(valuesArray);
                return valuesArray;
            } else {
                return null;
            }
        } catch (Exception var4) {
            return null;
        }
    }


    public String getFilesystemName(String name) {
        try {
            FastFile file = (FastFile) this.files.get(name);
            return file.getFileName();
        } catch (Exception var3) {
            return null;
        }
    }

    public String getOriginalFileName(String name) {
        try {
            FastFile file = (FastFile) this.files.get(name);
            return file.getUploadFileName();
        } catch (Exception var3) {
            return null;
        }
    }

    public String getContentType(String name) {
        try {
            FastFile file = (FastFile) this.files.get(name);
            return file.getContentType();
        } catch (Exception var3) {
            return null;
        }
    }

    public FastFile getFile(String name) {
        try {
            FastFile file = (FastFile) this.files.get(name);
            return file;
        } catch (Exception var3) {
            return null;
        }
    }

    public void putFile(String name, FastFile fastFile) {
        this.files.put(name, fastFile);
    }

    public List<FastFile<?>> getFiles() {
        List<FastFile<?>> fastFiles = new ArrayList<>();
        try {
            for (Object o : this.files.values()) {
                fastFiles.add((FastFile<?>) o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fastFiles;
    }


}
