package com.fastchar.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

}
