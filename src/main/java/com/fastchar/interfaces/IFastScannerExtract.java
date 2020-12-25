package com.fastchar.interfaces;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 解压Jar包的回调类
 */
public interface IFastScannerExtract {

    /**
     * 解压jar包的文件回调
     * @param jarFile jar文件
     * @param jarEntry 需要解压的文件流
     * @return 布尔值 true：允许 false：不允许
     */
    boolean onExtract(JarFile jarFile, JarEntry jarEntry);

}
