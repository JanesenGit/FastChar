package com.fastchar.core;

import com.fastchar.exception.FastFileException;

import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FastChar核心文件操作类
 * @author 沈建（Janesen）
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class FastFile<T> {

    public static FastFile<?> newInstance(String paramName, String attachDirectory, String fileName, String originalFileName, String contentType) {
        return FastChar.getOverrides().newInstance(FastFile.class)
                .setParamName(paramName)
                .setAttachDirectory(attachDirectory)
                .setFileName(fileName)
                .setUploadFileName(originalFileName)
                .setContentType(contentType);
    }

    public static FastFile<?> newInstance(String attachDirectory, String fileName) {
        return FastChar.getOverrides().newInstance(FastFile.class)
                .setAttachDirectory(attachDirectory).setFileName(fileName);
    }
    public static FastFile<?> newInstance( String fileName) {
        return FastChar.getOverrides().newInstance(FastFile.class)
                .setAttachDirectory(FastChar.getConstant().getAttachDirectory())
                .setFileName(fileName);
    }

    public static FastFile<?> newInstance(File file) {
        return FastChar.getOverrides().newInstance(FastFile.class)
                .setAttachDirectory(file.getParent())
                .setFileName(file.getName());
    }

    protected FastFile() {
    }

    private String key;
    private String paramName;
    private String fileName;
    private String attachDirectory;
    private String uploadFileName;
    private String contentType;
    private final ConcurrentHashMap<String, Object> attrs = new ConcurrentHashMap<>();
    private final FastMapWrap mapWrap = FastMapWrap.newInstance(attrs);

    public String getKey() {
        if (FastStringUtils.isEmpty(key)) {
            this.key = FastChar.getSecurity().MD5_Encrypt(FastStringUtils.buildOnlyCode(paramName));
        }
        return key;
    }

    public FastFile<T> setKey(String key) {
        this.key = key;
        return this;
    }

    public String getParamName() {
        return paramName;
    }

    public FastFile<T> setParamName(String paramName) {
        this.paramName = paramName;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public FastFile<T> setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getAttachDirectory() {
        return attachDirectory;
    }

    public FastFile<T> setAttachDirectory(String attachDirectory) {
        this.attachDirectory = attachDirectory;
        return this;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public FastFile<T> setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public FastFile<T> setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * 获得文件扩展名，包含(.)
     *
     * @return
     */
    public String getExtensionName() {
        if (fileName != null && fileName.length() > 0 && fileName.lastIndexOf(".") > -1) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }

    /**
     * 获取短文件名,不带扩展名
     *
     * @param fileName
     * @return
     */
    public static String getShortName(String fileName) {
        if (fileName != null && fileName.length() > 0 && fileName.lastIndexOf(".") > -1) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }


    public boolean isImageFile() {
        return FastFileUtils.isImageFile(uploadFileName)||
                FastFileUtils.isImageFile(fileName)||
                FastFileUtils.isImageFileByMimeType(contentType);
    }
    public boolean isTxtFile() {
        return FastFileUtils.isTxtFile(uploadFileName)||
                FastFileUtils.isTxtFile(fileName)||
                FastFileUtils.isTxtFileByMimeType(contentType);
    }
    public boolean isExcelFile() {
        return FastFileUtils.isExcelFile(uploadFileName)||
                FastFileUtils.isExcelFile(fileName)||
                FastFileUtils.isExcelFileByMimeType(contentType);
    }
    public boolean isWordFile() {
        return FastFileUtils.isWordFile(uploadFileName)||
                FastFileUtils.isWordFile(fileName)||
                FastFileUtils.isWordFileByMimeType(contentType);
    }
    public boolean isPDFFile() {
        return FastFileUtils.isPDFFile(uploadFileName)||
                FastFileUtils.isPDFFile(fileName)||
                FastFileUtils.isPDFFileByMimeType(contentType);
    }
    public boolean isPPTFile() {
        return FastFileUtils.isPPTFile(uploadFileName)||
                FastFileUtils.isPPTFile(fileName)||
                FastFileUtils.isPPTFileByMimeType(contentType);
    }
    public boolean isMP4File() {
        return FastFileUtils.isMP4File(uploadFileName)||
                FastFileUtils.isMP4File(fileName)||
                FastFileUtils.isMP4FileByMimeType(contentType);
    }

    public boolean isMOVFile() {
        return FastFileUtils.isMOVFile(uploadFileName)||
                FastFileUtils.isMOVFile(fileName)||
                FastFileUtils.isMOVFileByMimeType(contentType);
    }

    public boolean isAVIFile() {
        return FastFileUtils.isAVIFile(uploadFileName)||
                FastFileUtils.isAVIFile(fileName)||
                FastFileUtils.isAVIFileByMimeType(contentType);
    }

    public boolean isTargetFile(String... extensions) {
        return FastFileUtils.isTargetFile(uploadFileName, extensions)||FastFileUtils.isTargetFile(fileName, extensions);
    }

    public boolean isTargetFileByMimeType(String... mimeTypes) {
        return FastFileUtils.isTargetFileByMimeType(contentType, mimeTypes);
    }

    public File getFile() {
        return this.attachDirectory != null && this.fileName != null ? new File(this.attachDirectory, this.fileName) : null;
    }

    public boolean exists() {
        return getFile().exists();
    }

    public <E extends FastFile> E moveFile(File targetDirectory) throws FastFileException, IOException {
        return moveFile(targetDirectory.getAbsolutePath());
    }

    public <E extends FastFile> E moveFile(String targetDirectory) throws FastFileException, IOException {
        File targetFile = new File(targetDirectory, fileName);
        if (!targetFile.getParentFile().exists()) {
            if (!targetFile.getParentFile().mkdirs()) {
                throw new FastFileException(FastChar.getLocal().getInfo(FastCharLocal.FILE_ERROR1, "'" + targetDirectory + "'"));
            }
        }
        File rename = FastChar.getFileRename().rename(targetFile, false);
        if (rename.exists()) {
            rename.delete();
        }
        FastFileUtils.moveFile(getFile(), rename);
        return (E) FastFile.newInstance(paramName, targetDirectory, rename.getName(), uploadFileName, contentType);

    }

    public <E extends FastFile> E renameTo(File targetFile) throws FastFileException, IOException {
        return renameTo(targetFile, false);
    }

    public <E extends FastFile> E renameTo(File targetFile, boolean force) throws FastFileException, IOException {
        if (!targetFile.getParentFile().exists()) {
            if (!targetFile.getParentFile().mkdirs()) {
                throw new FastFileException(FastChar.getLocal().getInfo(FastCharLocal.FILE_ERROR1, "'" + targetFile.getParent() + "'"));
            }
        }
        FastFileUtils.moveFile(getFile(), targetFile, force);
        return (E) FastFile.newInstance(paramName, targetFile.getParent(), targetFile.getName(), uploadFileName, contentType);

    }

    public <E extends FastFile> E copyTo(File targetFile) throws IOException, FastFileException {
        if (!targetFile.getParentFile().exists()) {
            if (!targetFile.getParentFile().mkdirs()) {
                throw new FastFileException(FastChar.getLocal().getInfo(FastCharLocal.FILE_ERROR1, "'" + targetFile.getParent() + "'"));
            }
        }
        FastFileUtils.copyFile(getFile(), targetFile);
        return (E) FastFile.newInstance(paramName, targetFile.getParent(), targetFile.getName(), uploadFileName, contentType);

    }

    public void delete() throws IOException {
        FastFileUtils.forceDelete(getFile());
    }


    public String getUrl() throws Exception {
        if (getFile() == null) {
            return null;
        }
        String replace = getFile().getAbsolutePath().replace(FastChar.getPath().getWebRootPath(), "");
        return FastStringUtils.strip(replace, "/");
    }


    public void setAttr(String name, Object value) {
        this.attrs.put(name, value);
    }

    public boolean existAttr(String name) {
        return this.attrs.contains(name);
    }

    public Object getAttr(String name) {
        return this.attrs.get(name);
    }

    public FastMapWrap getAttrWrap() {
        return mapWrap;
    }

    public ConcurrentHashMap<String, Object> getAttrs() {
        return attrs;
    }
}
