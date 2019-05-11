package com.fastchar.extend.ehcache;

import net.sf.ehcache.config.Configuration;

import java.io.InputStream;
import java.net.URL;

public class FastEhCacheConfig {
    private Configuration configuration;
    private String configurationFileName;
    private URL configurationURL;
    private InputStream configurationInputStream;


    public Configuration getConfiguration() {
        return configuration;
    }

    public FastEhCacheConfig setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public String getConfigurationFileName() {
        return configurationFileName;
    }

    public FastEhCacheConfig setConfigurationFileName(String configurationFileName) {
        this.configurationFileName = configurationFileName;
        return this;
    }

    public URL getConfigurationURL() {
        return configurationURL;
    }

    public FastEhCacheConfig setConfigurationURL(URL configurationURL) {
        this.configurationURL = configurationURL;
        return this;
    }

    public InputStream getConfigurationInputStream() {
        return configurationInputStream;
    }

    public FastEhCacheConfig setConfigurationInputStream(InputStream configurationInputStream) {
        this.configurationInputStream = configurationInputStream;
        return this;
    }
}
