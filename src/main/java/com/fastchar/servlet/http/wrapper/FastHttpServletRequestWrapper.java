package com.fastchar.servlet.http.wrapper;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastFile;
import com.fastchar.exception.FastFileException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastPart;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FastHttpServletRequestWrapper extends FastHttpServletRequest {

    public static FastHttpServletRequestWrapper newInstance(Object target) {
        return new FastHttpServletRequestWrapper(target);
    }

    private List<FastFile<?>> files = null;

    public FastHttpServletRequestWrapper(Object target) {
        super(target);
    }

    public List<FastFile<?>> getFiles() {
        if (files == null) {
            files = new ArrayList<>();
            if (!isMultipart()) {
                return files;
            }
            try {
                for (FastPart part : getParts()) {
                    String fileName = part.getSubmittedFileName();
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
        List<FastFile<?>> curr = new ArrayList<>();
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
