package com.fastchar.core;

import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.JarFile;

public final class FastPath {
    private String pid;
    private String classRootPath;
    private String webRootPath;
    private String libRootPath;
    private String webInfoPath;

    FastPath() {
    }

    public String getClassRootPath() {
        if (classRootPath == null) {
            try {
                URL resource = FastPath.class.getResource("/");
                if (resource != null) {
                    classRootPath = new File(resource.toURI()).getAbsolutePath();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return classRootPath;
    }

    FastPath setClassRootPath(String classRootPath) {
        this.classRootPath = classRootPath;
        return this;
    }

    public String getWebRootPath() {
        if (webRootPath == null) {
            webRootPath = getClassRootPath().split("WEB-INF")[0];
        }
        return webRootPath;
    }

    void setWebRootPath(String webRootPath) {
        this.webRootPath = webRootPath;
    }

    public String getLibRootPath() {
        if (libRootPath == null) {
            libRootPath = getClassRootPath().replace("classes", "lib");
        }
        return libRootPath;
    }

    public String getWebInfoPath() {
        if (webInfoPath == null) {
            webInfoPath = new File(getClassRootPath().split("WEB-INF")[0], "WEB-INF").getAbsolutePath();
        }
        return webInfoPath;
    }

    public FastPath setWebInfoPath(String webInfoPath) {
        this.webInfoPath = webInfoPath;
        return this;
    }

    FastPath setLibRootPath(String libRootPath) {
        this.libRootPath = libRootPath;
        return this;
    }

    public String getPid() {
        if (pid == null) {
            pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        }
        return pid;
    }

    public FastPath setPid(String pid) {
        this.pid = pid;
        return this;
    }


    public boolean existsJarRoot(File jar) {
        if (jar == null) {
            return false;
        }
        return jar.getAbsolutePath().startsWith(getLibRootPath());
    }
}
