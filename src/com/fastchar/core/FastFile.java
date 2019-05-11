package com.fastchar.core;

import com.fastchar.exception.FastFileException;

import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("unchecked")
public class FastFile<T> {
    private String key;
    private String paramName;
    private String fileName;
    private String attachDirectory;
    private String uploadFileName;
    private String contentType;

    public FastFile(String paramName, String attachDirectory, String fileName, String originalFileName, String contentType) {
        this.paramName = paramName;
        this.attachDirectory = attachDirectory;
        this.fileName = fileName;
        this.uploadFileName = originalFileName;
        this.contentType = contentType;
        this.key = FastChar.getSecurity().MD5_Encrypt(FastStringUtils.buildOnlyCode(paramName));
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getAttachDirectory() {
        return attachDirectory;
    }

    public void setAttachDirectory(String attachDirectory) {
        this.attachDirectory = attachDirectory;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 获得文件扩展名，包含(.)
     * @return
     */
    public String getExtensionName() {
        if(fileName!=null && fileName.length()>0 && fileName.lastIndexOf(".")>-1){
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }

    /**
     * 获取短文件名,不带扩展名
     * @param fileName
     * @return
     */
    public static String getShortName(String fileName){
        if(fileName != null && fileName.length()>0 && fileName.lastIndexOf(".")>-1){
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }


    public boolean isImageFile() {
        return FastFileUtils.isImageFile(fileName);
    }

    public boolean isMP4File() {
        return FastFileUtils.isMP4File(fileName);
    }

    public boolean isAVIFile() {
        return FastFileUtils.isAVIFile(fileName);
    }

    public boolean isTargetFile(String... extensions) {
        return FastFileUtils.isTargetFile(fileName, extensions);
    }

    public File getFile() {
        return this.attachDirectory != null && this.fileName != null ? new File(this.attachDirectory, this.fileName) : null;
    }


    public <E extends FastFile> E moveFile(String targetDirectory) throws FastFileException {
        try {
            File targetFile = new File(targetDirectory, fileName);
            if (!targetFile.getParentFile().exists()) {
                if (!targetFile.getParentFile().mkdirs()) {
                    throw new FastFileException(FastChar.getLocal().getInfo("File_Error1", "'" + targetDirectory + "'"));
                }
            }
            File rename = FastChar.getFileRename().rename(targetFile, false);
            if (rename.exists()) {
                rename.delete();
            }
            FastFileUtils.moveFile(getFile(), rename);
            return (E) FastChar.getOverrides().newInstance(FastFile.class, paramName, targetDirectory, rename.getName(), uploadFileName, contentType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (E) this;
    }

    public <E extends FastFile> E renameTo(File targetFile) {
        try {
            FastFileUtils.moveFile(getFile(), targetFile);
            return (E) FastChar.getOverrides().newInstance(FastFile.class,paramName, targetFile.getParent(), targetFile.getName(), uploadFileName, contentType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (E) this;
    }

    public <E extends FastFile> E copyTo(File targetFile){
        try {
            FastFileUtils.copyFile(getFile(), targetFile);
            return (E) FastChar.getOverrides().newInstance(FastFile.class,paramName, targetFile.getParent(), targetFile.getName(), uploadFileName, contentType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (E) this;
    }

    public void delete() throws IOException {
        FastFileUtils.forceDelete(getFile());
    }
}
