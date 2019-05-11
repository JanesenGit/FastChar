package com.fastchar.core;

import java.io.File;
import java.lang.management.ManagementFactory;

public final class FastPath {
    private String pid;
    private String classRootPath;
    private String webRootPath;
    private String libRootPath;


    public String getClassRootPath() {
        if (classRootPath == null) {
            try {
                String path = FastPath.class.getClassLoader().getResource("").toURI().getPath();
                classRootPath = new File(path).getAbsolutePath();
            }
            catch (Exception e) {
                String path = FastPath.class.getClassLoader().getResource("").getPath();
                classRootPath = new File(path).getAbsolutePath();
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
            webRootPath = getClassRootPath().replace("WEB-INF/classes", "");
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
