package com.fastchar.extend.ehcache;

import com.fastchar.interfaces.IFastConfig;
import net.sf.ehcache.config.Configuration;

import java.io.InputStream;
import java.net.URL;

public class FastEhCache2Config implements IFastConfig {
    private Configuration configuration;
    private String configurationFileName;
    private URL configurationURL;
    private InputStream configurationInputStream;


    public Configuration getConfiguration() {
        return configuration;
    }

    public FastEhCache2Config setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public String getConfigurationFileName() {
        return configurationFileName;
    }

    public FastEhCache2Config setConfigurationFileName(String configurationFileName) {
        this.configurationFileName = configurationFileName;
        return this;
    }

    public URL getConfigurationURL() {
        return configurationURL;
    }

    public FastEhCache2Config setConfigurationURL(URL configurationURL) {
        this.configurationURL = configurationURL;
        return this;
    }

    public InputStream getConfigurationInputStream() {
        return configurationInputStream;
    }

    public FastEhCache2Config setConfigurationInputStream(InputStream configurationInputStream) {
        this.configurationInputStream = configurationInputStream;
        return this;
    }
}
