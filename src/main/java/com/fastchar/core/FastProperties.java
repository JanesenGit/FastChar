package com.fastchar.core;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class FastProperties  extends FastMapWrap{

    private String filePath;

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

    FastProperties setContent(String content) throws IOException {
        try {
            Properties properties = new Properties();
            properties.load(new StringReader(content));
            setMap(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    @Override
    public Map<?, ?> getMap() {
        try {
            if (map == null) {
                Properties properties = new Properties();
                properties.load(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
                setMap(properties);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public Properties getProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));
            return properties;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
