package com.fastchar.extend.yml;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastMapWrap;
import com.fastchar.core.FastResource;
import com.fastchar.exception.FastFileException;
import com.fastchar.local.FastCharLocal;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Map;

@SuppressWarnings("IOStreamConstructor")
@AFastClassFind(value = "org.yaml.snakeyaml.Yaml",url = "https://mvnrepository.com/artifact/org.yaml/snakeyaml")
public class FastYaml extends FastMapWrap {

    private FastResource fastResource;
    //是否开启自动更新文件，当yml文件更新后将自动更新加载
    private boolean autoReload;

    private long lastModified;

    public FastYaml() {
        //强制开启${}表达式属性值
        forceAttr = true;
    }

    public String getFilePath() {
        return fastResource.getFile().getAbsolutePath();
    }

    public FastYaml setFilePath(String filePath) {
        this.fastResource = new FastResource(filePath);
        return this;
    }

    public FastYaml setFile(FastResource file) {
        this.fastResource = file;
        return this;
    }

    public boolean isAutoReload() {
        return autoReload;
    }

    public FastYaml setAutoReload(boolean autoReload) {
        this.autoReload = autoReload;
        return this;
    }

    private boolean isNeedInitMap() {
        return map == null || (autoReload  && fastResource.lastModified() > lastModified);
    }


    @Override
    public Map<?, ?> getMap() {
        if (isNeedInitMap()) {
            synchronized (this) {
                if (isNeedInitMap()) {
                    LoaderOptions loaderOptions = new LoaderOptions();
                    Yaml yaml = new Yaml(new SafeConstructor(loaderOptions));

                    try(InputStream in = fastResource.getInputStream()) {
                        Map<String, Object> map = yaml.load(in);
                        setMap(map);
                    } catch (Exception e) {
                        FastChar.getLogger().error(this.getClass(), e);
                    }
                    lastModified = fastResource.lastModified();
                }
            }
        }
        return map;
    }


    public FastYaml put(Object attr, Object value) {
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
            FileWriter fileWriter = new FileWriter(proFile);
            DumperOptions options = new DumperOptions();
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
            Yaml yaml = new Yaml(options);
            yaml.dump(map, fileWriter);
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }

}
