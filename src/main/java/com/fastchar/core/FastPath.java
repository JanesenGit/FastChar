package com.fastchar.core;

import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public final class FastPath {

    volatile String pid;
    volatile String classRootPath;
    volatile String webRootPath;
    volatile boolean projectJar = false;//Web项目是否打包成jar
    volatile String projectJarFilePath;

    FastPath() {
    }

    public void setProjectJar(Class<?> targetClass) {
        try {
            if (targetClass == null) {
                throw new NullPointerException("targetClass is null");
            }
            URL location = targetClass.getProtectionDomain().getCodeSource().getLocation();
            File sourceFile = new File(location.toURI());
            String fileName = sourceFile.getName().toLowerCase();
            if (fileName.endsWith(".jar")) {
                this.projectJar = true;
                this.projectJarFilePath = sourceFile.getAbsolutePath();
            } else {
                this.projectJar = false;
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void setProjectJar(String projectJarFilePath) throws FileNotFoundException {
        File webJarFile = new File(projectJarFilePath);
        if (!webJarFile.exists()) {
            throw new FileNotFoundException(projectJarFilePath + " not found");
        }
        this.projectJar = true;
        this.projectJarFilePath = projectJarFilePath;
    }


    public boolean isProjectJar() {
        return projectJar;
    }


    public String getProjectJarFilePath() {
        return projectJarFilePath;
    }

    private String getClassRootPathByLocal() {
        try {
            URL resource = FastPath.class.getResource("/");
            if (resource != null) {
                return new File(resource.toURI()).getAbsolutePath();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private String getClassRootPathByJar() {
        String jarFilePath = getProjectJarFilePath();
        if (FastStringUtils.isNotEmpty(jarFilePath)) {
            return "jar:" + new File(jarFilePath).toURI() + "!/";
        }
        return null;
    }


    public String getClassRootPath() {
        if (classRootPath == null) {
            synchronized (this) {
                if (classRootPath == null) {
                    if (isProjectJar()) {
                        classRootPath = getClassRootPathByJar();
                    } else {
                        classRootPath = getClassRootPathByLocal();
                    }
                }
            }
        }
        return classRootPath;
    }


    public String getWebRootPath() {
        if (webRootPath == null) {
            synchronized (this) {
                if (webRootPath == null) {
                    if (isProjectJar()) {
                        webRootPath = FastStringUtils.splitByWholeSeparator(getProjectJarFilePath(), "WEB-INF")[0];
                        File webRootFile = new File(webRootPath);
                        if (webRootFile.isFile()) {
                            webRootPath = webRootFile.getParent();
                        }
                    } else {
                        webRootPath = FastStringUtils.splitByWholeSeparator(getClassRootPath(), "WEB-INF")[0];
                    }
                }
            }
        }
        return webRootPath;
    }


    public String getPid() {
        if (pid == null) {
            synchronized (this) {
                if (pid == null) {
                    pid = FastStringUtils.splitByWholeSeparator(ManagementFactory.getRuntimeMXBean().getName(), "@")[0];
                }
            }
        }
        return pid;
    }

    public FastPath setPid(String pid) {
        this.pid = pid;
        return this;
    }

    public String getAttachmentPath() {
        return FastChar.getConstant().getAttachDirectory();
    }


    public String getTempDir() {
        return Paths.get(getWebRootPath(), "temp").normalize().toAbsolutePath().toString();
    }


    @Override
    public String toString() {
        return "FastPath{" +
                "pid='" + pid + '\'' +
                ", classRootPath='" + classRootPath + '\'' +
                ", webRootPath='" + webRootPath + '\'' +
                ", webJar=" + projectJar +
                ", webJarFilePath='" + projectJarFilePath + '\'' +
                '}';
    }
}
