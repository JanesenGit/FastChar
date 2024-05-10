package com.fastchar.core;

import com.fastchar.exception.FastFileException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.system.UnicodeReader;
import com.fastchar.utils.FastDateUtils;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SuppressWarnings("IOStreamConstructor")
public class FastProperties extends FastMapWrap {

    private FastResource fastResource;
    //是否开启自动更新文件，当properties文件更新后将自动更新加载
    private boolean autoReload;

    private long lastModified;

    public FastProperties() {
    }

    public String getFilePath() {
        return fastResource.getFile().getPath();
    }


    FastProperties setFile(FastResource file) {
        this.fastResource = file;
        return this;
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public FastProperties setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
        return this;
    }


    private boolean isNeedInitMap() {
        return map == null || (autoReload && fastResource.lastModified() > lastModified);
    }

    @Override
    public Map<?, ?> getMap() {
        try {
            if (isNeedInitMap()) {
                synchronized (this) {
                    if (isNeedInitMap()) {
                        Properties properties = new Properties();
                        //去除windows下的bom头
                        properties.load(new UnicodeReader(fastResource.getInputStream(), StandardCharsets.UTF_8.name()));
                        setMap(properties);
                        lastModified = fastResource.lastModified();
                    }
                }
            }
        } catch (IOException e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
        return map;
    }

    public Properties getProperties() {
        Properties properties = new Properties();
        properties.putAll(getMap());
        return properties;
    }

    public FastProperties put(Object attr, Object value) {
        super.put(attr, value);
        return this;
    }

    public void save() {
        try {
            if (!fastResource.isFileProtocol()) {
                FastChar.getLogger().error(this.getClass(), "resource is not file ! ");
                return;
            }

            File proFile = fastResource.getFile();
            if (!proFile.exists()) {
                if (!proFile.createNewFile()) {
                    throw new FastFileException(FastChar.getLocal().getInfo(FastCharLocal.FILE_ERROR1, "'" + proFile.getAbsolutePath() + "'"));
                }
            }

            List<String> newStrings = new ArrayList<>(16);
            List<String> strings = FastFileUtils.readLines(proFile);
            Set<String> jumpKeys = new HashSet<>();
            for (String string : strings) {
                if (string.trim().startsWith("#")) {
                    newStrings.add(string);
                    continue;
                }
                String[] split = FastStringUtils.splitByWholeSeparator(string, "=");
                if (split.length == 2) {
                    String key = split[0].trim();
                    if (containsAttr(key)) {
                        newStrings.add(key + "=" + getString(key));
                        jumpKeys.add(key);
                        continue;
                    }
                }
                newStrings.add(string);
            }
            Map<?, ?> map = getMap();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                if (jumpKeys.contains(key.toString())) {
                    continue;
                }
                newStrings.add("\n");
                newStrings.add("#property is created in " + FastDateUtils.getDateString());
                newStrings.add(key + "=" + entry.getValue());
            }
            FastFileUtils.writeStringToFile(proFile, FastStringUtils.join(newStrings, "\n"));
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }

}
