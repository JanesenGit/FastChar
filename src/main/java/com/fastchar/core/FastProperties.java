package com.fastchar.core;

import com.fastchar.utils.FastBooleanUtils;
import com.fastchar.utils.FastNumberUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class FastProperties {

    private String filePath;
    private Properties properties;

    public FastProperties() {
    }

    public String getFilePath() {
        return filePath;
    }

    FastProperties setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

     FastProperties setFile(File file) {
         this.filePath = file.getAbsolutePath();
        return this;
    }


    public Properties getProperties() {
        try {
            if (properties == null) {
                properties = new Properties();
                properties.load(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public String getString(String key) {
        return getProperties().getProperty(key);
    }

    public int getInt(String key) {
        return FastNumberUtils.formatToInt(getString(key));
    }

    public int getInt(String key, int defaultValue) {
        return FastNumberUtils.formatToInt(getString(key), defaultValue);
    }

    public boolean getBoolean(String key) {
        return FastBooleanUtils.formatToBoolean(getString(key));
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return FastBooleanUtils.formatToBoolean(getString(key), defaultValue);
    }


    public double getDouble(String key) {
        return FastNumberUtils.formatToDouble(getString(key));
    }

    public double getDouble(String key, double defaultValue) {
        return FastNumberUtils.formatToDouble(getString(key), defaultValue);
    }


}
