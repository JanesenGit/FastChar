package com.fastchar.core;

import com.fastchar.exception.FastFileException;

import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastMD5Utils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FastChar核心文件操作类
 *
 * @param <T>
 * @author 沈建（Janesen）
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
                .setAttachDirectory(attachDirectory).setFileName(fileName).setParamName(fileName);
    }

    public static FastFile<?> newInstance(String fileName) {
        return FastChar.getOverrides().newInstance(FastFile.class)
                .setAttachDirectory(FastChar.getConstant().getAttachDirectory())
                .setParamName(fileName)
                .setFileName(fileName);
    }

    public static FastFile<?> newInstance(File file) {
        return FastChar.getOverrides()
                .newInstance(FastFile.class)
                .setAttachDirectory(file.getParent())
                .setParamName(file.getName())
                .setFileName(file.getName());
    }

    public static FastFile<?> newInstance(URL url) {
        return newInstance(url, new File(url.getPath()).getName());
    }

    public static FastFile<?> newInstance(URL url, String fileName) {
        return newInstance(url, fileName, fileName);
    }

    public static FastFile<?> newInstance(URL url, String fileName, String uploadFileName) {
        return FastChar.getOverrides()
                .newInstance(FastFile.class)
                .setNetUrl(url)
                .setParamName("NET")
                .setUploadFileName(uploadFileName)
                .setFileName(fileName);
    }


    public static String buildFileKey(String path) {
        File file = new File(path);
        String prefix = "";
        //生成以后缀名为
        String[] split = file.getName().split("\\.");
        if (split.length > 1) {
            prefix = split[split.length - 1] + "-";
        }
        return prefix + FastMD5Utils.MD5To16(FastStringUtils.buildOnlyCode(path));
    }


    protected FastFile() {
    }

    private String key;
    protected URL netUrl;
    private String paramName;
    private String fileName;
    private String attachDirectory;
    private String uploadFileName;
    private String contentType;
    private final ConcurrentHashMap<String, Object> attrs = new ConcurrentHashMap<>();
    private final FastMapWrap mapWrap = FastMapWrap.newInstance(attrs);

    /**
     * 获取文件的唯一标识key
     *
     * @return 字符串
     */
    public String getKey() {
        if (FastStringUtils.isEmpty(key)) {
            String prefix = "";
            if (FastStringUtils.isNotEmpty(uploadFileName)) {
                //生成以后缀名为
                String[] split = uploadFileName.split("\\.");
                if (split.length > 1) {
                    prefix = split[split.length - 1] + "-";
                }
            }
            this.key = prefix + FastMD5Utils.MD5To16(FastStringUtils.buildOnlyCode(paramName));
        }
        return key;
    }

    /**
     * 设置文件的唯一标识key
     *
     * @param key 唯一标识string
     * @return 当前对象
     */
    public FastFile<T> setKey(String key) {
        this.key = key;
        return this;
    }

    /**
     * 获取附件post提交的参数名
     *
     * @return 字符串
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * 设置附件post提交的参数名
     *
     * @param paramName 参数名
     * @return 当前对象
     */
    public FastFile<T> setParamName(String paramName) {
        this.paramName = paramName;
        return this;
    }

    /**
     * 获取附件保存后的文件名
     *
     * @return 字符串
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 设置附件保存后的文件名
     *
     * @param fileName 文件名
     * @return 当前对象
     */
    public FastFile<T> setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * 获取附件保存到文件夹目录地址
     *
     * @return 字符串
     */
    public String getAttachDirectory() {
        return attachDirectory;
    }

    /**
     * 设置附件保存到文件夹目录地址
     *
     * @param attachDirectory 目录地址
     * @return 当前对象
     */
    public FastFile<T> setAttachDirectory(String attachDirectory) {
        this.attachDirectory = attachDirectory;
        return this;
    }

    /**
     * 获取附件上传时原始的文件名
     *
     * @return 字符串
     */
    public String getUploadFileName() {
        return uploadFileName;
    }

    /**
     * 设置附件的原始文件名
     *
     * @param uploadFileName 文件名
     * @return 当前对象
     */
    public FastFile<T> setUploadFileName(String uploadFileName) {
        this.uploadFileName = uploadFileName;
        if (FastChar.getConstant().isDecodeUploadFileName()) {
            if (FastStringUtils.isNotEmpty(this.uploadFileName)) {
                try {
                    this.uploadFileName = URLDecoder.decode(this.uploadFileName, FastChar.getConstant().getDecodeUploadFileNameEncoding());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return this;
    }

    /**
     * 获取附件的content-type类型
     *
     * @return 字符串
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * 设置附件的content-type类型
     *
     * @param contentType 文件类型
     * @return 当前对象
     */
    public FastFile<T> setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public FastFile<T> setNetUrl(URL netUrl) {
        this.netUrl = netUrl;
        return this;
    }

    /**
     * 获得文件扩展名，包含(.) 例如：.png
     *
     * @return 字符串
     */
    public String getExtensionName() {
        if (fileName != null && fileName.length() > 0 && fileName.lastIndexOf(".") > -1) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }

    /**
     * 获取短文件名,不带扩展名，原始文件名：test.png 获取后为：test
     *
     * @param fileName 文件名
     * @return 字符串
     */
    public static String getShortName(String fileName) {
        if (fileName != null && fileName.length() > 0 && fileName.lastIndexOf(".") > -1) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }


    public boolean isImageFile() {
        return FastFileUtils.isImageFile(uploadFileName) ||
                FastFileUtils.isImageFile(fileName) ||
                FastFileUtils.isImageFileByMimeType(contentType);
    }

    public boolean isTxtFile() {
        return FastFileUtils.isTxtFile(uploadFileName) ||
                FastFileUtils.isTxtFile(fileName) ||
                FastFileUtils.isTxtFileByMimeType(contentType);
    }

    public boolean isExcelFile() {
        return FastFileUtils.isExcelFile(uploadFileName) ||
                FastFileUtils.isExcelFile(fileName) ||
                FastFileUtils.isExcelFileByMimeType(contentType);
    }

    public boolean isWordFile() {
        return FastFileUtils.isWordFile(uploadFileName) ||
                FastFileUtils.isWordFile(fileName) ||
                FastFileUtils.isWordFileByMimeType(contentType);
    }

    public boolean isPDFFile() {
        return FastFileUtils.isPDFFile(uploadFileName) ||
                FastFileUtils.isPDFFile(fileName) ||
                FastFileUtils.isPDFFileByMimeType(contentType);
    }

    public boolean isPPTFile() {
        return FastFileUtils.isPPTFile(uploadFileName) ||
                FastFileUtils.isPPTFile(fileName) ||
                FastFileUtils.isPPTFileByMimeType(contentType);
    }

    public boolean isMP4File() {
        return FastFileUtils.isMP4File(uploadFileName) ||
                FastFileUtils.isMP4File(fileName) ||
                FastFileUtils.isMP4FileByMimeType(contentType);
    }

    public boolean isMOVFile() {
        return FastFileUtils.isMOVFile(uploadFileName) ||
                FastFileUtils.isMOVFile(fileName) ||
                FastFileUtils.isMOVFileByMimeType(contentType);
    }

    public boolean isAVIFile() {
        return FastFileUtils.isAVIFile(uploadFileName) ||
                FastFileUtils.isAVIFile(fileName) ||
                FastFileUtils.isAVIFileByMimeType(contentType);
    }

    public boolean isMP3File() {
        return FastFileUtils.isMP3File(uploadFileName) ||
                FastFileUtils.isMP3File(fileName) ||
                FastFileUtils.isMP3FileByMimeType(contentType);
    }

    public boolean isTargetFile(String... extensions) {
        return FastFileUtils.isTargetFile(uploadFileName, extensions) || FastFileUtils.isTargetFile(fileName, extensions);
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

    /**
     * 删除附件
     *
     * @throws IOException 异常信息
     */
    public void delete() throws IOException {
        FastFileUtils.forceDelete(getFile());
    }


    /**
     * 获取附件的访问地址
     *
     * @return 字符串
     * @throws Exception 异常信息
     */
    public String getUrl() throws Exception {
        if (netUrl != null) {
            return netUrl.toString();
        }
        if (getFile() == null) {
            return null;
        }
        String replace = getFile().getAbsolutePath().replace(FastChar.getPath().getWebRootPath(), "");
        return FastStringUtils.strip(replace, "/");
    }


    /**
     * 设置附件的扩展属性
     *
     * @param name  属性名
     * @param value 属性值
     */
    public void setAttr(String name, Object value) {
        this.attrs.put(name, value);
    }

    /**
     * 判断是否存在扩展属性
     *
     * @param name 属性名
     * @return 布尔值
     */
    public boolean existAttr(String name) {
        return this.attrs.contains(name);
    }

    /**
     * 获取扩展属性值
     *
     * @param name 属性名
     * @return Object
     */
    public Object getAttr(String name) {
        return this.attrs.get(name);
    }

    /**
     * 获取附件扩展属性Map对象
     *
     * @return FastMapWrap对象
     */
    public FastMapWrap getAttrWrap() {
        return mapWrap;
    }

    /**
     * 获取附件所有的扩展属性值
     *
     * @return ConcurrentHashMap对象
     */
    public ConcurrentHashMap<String, Object> getAttrs() {
        return attrs;
    }
}
