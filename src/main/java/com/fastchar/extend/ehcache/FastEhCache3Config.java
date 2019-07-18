package com.fastchar.extend.ehcache;

import com.fastchar.interfaces.IFastConfig;
import org.ehcache.config.Configuration;

import java.io.InputStream;
import java.net.URL;

public class FastEhCache3Config implements IFastConfig {
    private Configuration configuration;
    private String configurationFileName;
    private URL configurationURL;

    public Configuration getConfiguration() {
        return configuration;
    }

    public FastEhCache3Config setConfiguration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    public String getConfigurationFileName() {
        return configurationFileName;
    }

    public FastEhCache3Config setConfigurationFileName(String configurationFileName) {
        this.configurationFileName = configurationFileName;
        return this;
    }

    public URL getConfigurationURL() {
        return configurationURL;
    }

    public FastEhCache3Config setConfigurationURL(URL configurationURL) {
        this.configurationURL = configurationURL;
        return this;
    }

}
