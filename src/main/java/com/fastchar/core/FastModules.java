package com.fastchar.core;

import com.fastchar.enums.FastObservableEvent;
import com.fastchar.utils.FastFileUtils;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 模块加载器
 *
 * @author 沈建（Janesen）
 * @date 2021/5/18 18:19
 */
@SuppressWarnings("UnusedReturnValue")
public final class FastModules {

    /**
     * 加载系统模块
     *
     * @param jarFiles 模块封装的jar包
     * @return 当前对象
     * @throws Exception 异常信息
     */
    public FastModules loadModule(File... jarFiles) throws Exception {
        try {
            FastChar.getScanner().resolveJar(false, jarFiles);
            FastChar.getScanner().scannerJar();
            FastChar.getScanner().registerOverrider();
            FastChar.getScanner().registerWeb();
            FastEngine.instance().getWebs().initWeb(FastEngine.instance());
            FastChar.getScanner().notifyAccepter();
            FastChar.getObservable().notifyObservers(FastObservableEvent.onScannerFinish.name());
            if (!FastChar.isMain()) {
                FastEngine.instance().getWebs().runWeb(FastEngine.instance());
            }
        } finally {
            FastEngine.instance().flush();
        }
        return this;
    }


    /**
     * 加载系统模块
     *
     * @param paths 模块代码的绝对路径
     * @return 当前对象
     * @throws Exception 异常信息
     */
    public FastModules loadModule(String... paths) throws Exception {
        try {
            FastChar.getScanner().resolvePath(paths);
            FastChar.getScanner().registerOverrider();
            FastChar.getScanner().registerWeb();
            FastEngine.instance().getWebs().initWeb(FastEngine.instance());
            FastChar.getScanner().notifyAccepter();
            FastChar.getObservable().notifyObservers(FastObservableEvent.onScannerFinish.name());
            if (!FastChar.isMain()) {
                FastEngine.instance().getWebs().runWeb(FastEngine.instance());
            }
        } finally {
            FastEngine.instance().flush();
        }
        return this;
    }


    /**
     * 卸载系统模块
     *
     * @param jarFiles 模块封装的jar包
     * @return 当前对象
     * @throws Exception 异常信息
     */
    public FastModules unloadModule(File... jarFiles) throws Exception {
        try {
            for (File jarFile : jarFiles) {
                FastScanner.ScannerJar scannerJar = new FastScanner.ScannerJar(jarFile);
                if (FastChar.getScanner().getJarLoaders().containsKey(scannerJar.getJarCode())) {
                    FastClassLoader fastClassLoader = FastChar.getScanner().getJarLoaders().get(scannerJar.getJarCode());
                    fastClassLoader.close();
                    FastChar.getScanner().getJarLoaders().remove(scannerJar.getJarCode());
                }
            }
        } finally {
            FastEngine.instance().flush();
        }
        return this;
    }


    /**
     * 删除Jar包模块在当前项目下解压的所有资源文件
     *
     * @param jarFiles 模块的Jar包
     * @return 当前对象
     */
    public FastModules removeResources(File... jarFiles) throws Exception {
        for (File file : jarFiles) {
            try (JarFile jarFile = new JarFile(file)) {
                Enumeration<JarEntry> jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry jarEntry = jarEntries.nextElement();
                    String jarEntryName = jarEntry.getName();
                    if (jarEntryName.startsWith(".")) {
                        continue;
                    }
                    if (jarEntryName.endsWith(".class")) {
                        continue;
                    }

                    boolean isWebSource = false;
                    for (String web : FastScanner.WEB) {
                        if (jarEntryName.startsWith(web)) {
                            isWebSource = true;
                            if (jarEntryName.startsWith("WebRoot")) {
                                jarEntryName = jarEntryName.substring("WebRoot".length());
                            } else if (jarEntryName.startsWith("web")) {
                                jarEntryName = jarEntryName.substring("web".length());
                            }
                            FastFileUtils.forceDelete(new File(FastChar.getPath().getWebRootPath(), jarEntryName));
                            break;
                        }
                    }
                    if (!isWebSource) {
                        FastFileUtils.forceDelete(new File(FastChar.getPath().getClassRootPath(), jarEntryName));
                    }
                }
            }
        }
        return this;
    }


    /**
     * 卸载系统模块
     *
     * @param paths 模块代码的绝对路径
     * @return 当前对象
     * @throws Exception 异常信息
     */
    public FastModules unloadModule(String... paths) throws Exception {
        try {
            for (String path : paths) {
                if (FastChar.getScanner().getPathLoaders().containsKey(path)) {
                    FastClassLoader fastClassLoader = FastChar.getScanner().getPathLoaders().get(path);
                    fastClassLoader.close();
                    FastChar.getScanner().getPathLoaders().remove(path);
                }
            }
        } finally {
            FastEngine.instance().flush();
        }
        return this;
    }


    /**
     * 获取已经加载的模块Jar包
     *
     * @return File集合
     * @throws Exception 异常信息
     */
    public List<File> getJarLoadModules() throws Exception {
        List<File> jarLoadFiles = new ArrayList<>(16);
        Map<String, FastClassLoader> jarLoaders = FastChar.getScanner().getJarLoaders();
        for (FastClassLoader value : jarLoaders.values()) {
            URL[] urLs = value.getURLs();
            for (URL urL : urLs) {
                jarLoadFiles.add(new File(urL.toURI()));
            }
        }
        return jarLoadFiles;
    }


    /**
     * 获取已经加载的模块代码绝对路径
     *
     * @return 路径集合
     */
    public List<String> getPathLoadModules() {
        Map<String, FastClassLoader> pathLoaders = FastChar.getScanner().getPathLoaders();
        return new ArrayList<>(pathLoaders.keySet());
    }


}
