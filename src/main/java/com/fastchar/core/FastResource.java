package com.fastchar.core;

import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class FastResource {

    public FastResource(String pathname) {
        if (isJarUrl(pathname)) {
            this.setURL(pathname);
            return;
        }
        File file = new File(pathname);
        if (file.exists()) {
            this.setFile(file);
        } else {
            this.setURL(pathname);
        }
    }

    public FastResource(String parent, String child) {
        if (isJarUrl(parent)) {
            this.setURL(this.wrapUrl(parent, child));
            return;
        }
        this.setFile(new File(parent, child));
    }

    public FastResource(URL url) {
        this.url = url;
    }

    public FastResource(File file) {
        this.setFile(file);
    }


    private URL url;

    private String wrapUrl(String parent, String child) {
        return FastStringUtils.strip(parent, "/") + "/" + FastStringUtils.strip(child, "/");
    }

    private boolean isJarUrl(String path) {
        return path.toLowerCase().startsWith("jar:");
    }

    private boolean isFileUrl(String path) {
        return path.toLowerCase().startsWith("file:");
    }

    public URL getURL() {
        return url;
    }

    public FastResource setURL(URL url) {
        this.url = url;
        return this;
    }

    public FastResource setURL(String url) {
        try {
            setURL(new URL(url));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return this;
    }


    public FastResource setFile(String filePath) {
        return setFile(new File(filePath));
    }

    public FastResource setFile(File file) {
        try {
            this.url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public File getFile() {
        return new File(url.getFile());
    }

    public boolean isFileProtocol() {
        String protocol = url.getProtocol();
        return "file".equalsIgnoreCase(protocol);
    }

    public boolean isJarProtocol() {
        String protocol = url.getProtocol();
        return "jar".equalsIgnoreCase(protocol);
    }


    public boolean isHttpProtocol() {
        String protocol = url.getProtocol();
        return "http".equalsIgnoreCase(protocol);
    }

    public boolean isHttpsProtocol() {
        String protocol = url.getProtocol();
        return "https".equalsIgnoreCase(protocol);
    }

    public synchronized InputStream getInputStream() {
        if (url != null) {
            try {
                return url.openStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }


    public long lastModified() {
        if (isFileProtocol()) {
            return getFile().lastModified();
        }
        return -1;
    }

    public String getName() {
        return getFile().getName();
    }

    public String getParent() {
        return getFile().getParent();
    }

    public String getAbsolutePath() {
        return getFile().getAbsolutePath();
    }

    public long lastModifiedTime() {
        return getFile().lastModified();
    }



}
