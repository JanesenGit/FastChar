package com.fastchar.core;

import com.fastchar.interfaces.IFastResourceFilter;
import com.fastchar.utils.FastStringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FastJarResources {

    //注意此处的路径分隔符统一为 ‘/’ 不区分系统
    private final List<String> jarResources = new ArrayList<>();

    public FastJarResources addResourcePath(String... resources) {
        return this.addResourcePath(Arrays.asList(resources));
    }

    public FastJarResources addResourcePath(List<String> resources) {
        for (String resource : resources) {
            if (!resource.toLowerCase().startsWith("jar:")) {
                continue;
            }
            if (this.jarResources.contains(resource)) {
                continue;
            }
            this.jarResources.add(resource);
        }
        return this;
    }

    public List<String> getResources() {
        return jarResources;
    }

    public boolean containsResource(String relativePth) {
        return this.getResource(relativePth) != null;
    }

    public String matchResource(String relativePth) {
        String safePath = FastStringUtils.strip(relativePth, "/");

        //注意此处的路径分隔符统一为 ‘/’ 不区分系统
        for (String jarResource : jarResources) {
            String jarName = FastStringUtils.strip(jarResource.split("!/")[1], "/");
            if (jarName.equalsIgnoreCase(safePath)) {
                return jarResource;
            }
        }
        return null;
    }

    public List<String> matchResources(String path, IFastResourceFilter iFastResourceFilter) {
        String safePath = FastStringUtils.strip(path, "/");

        List<String> result = new ArrayList<>(16);
        //注意此处的路径分隔符统一为 ‘/’ 不区分系统
        for (String jarResource : jarResources) {
            String jarName = FastStringUtils.strip(jarResource.split("!/")[1], "/");
            if (jarName.startsWith(safePath)) {
                try {
                    if (iFastResourceFilter != null && !iFastResourceFilter.onAccept(new FastResource(new URL(jarResource)))) {
                        continue;
                    }
                    result.add(jarResource);
                } catch (MalformedURLException e) {
                    FastChar.getLogger().error(this.getClass(), e);
                }
            }
        }
        return result;
    }


    /**
     * 获取相对路径【与前端网页地址栏路径分割一致 ‘/’ 】
     *
     * @param resource 资源对象
     * @return 相对路径地址 以 ‘/’ 开头 例如：/app/user.js
     */
    public String getRelativePath(FastResource resource) {
        if (resource.isJarProtocol()) {
            //此处禁止使用 File.separator 进行分割拼接，因为jar包内路径的分隔符java统一为 ‘/’
            String path = resource.getURL().toString();
            String[] wholeSeparator = FastStringUtils.splitByWholeSeparator(path, "!/");
            String realPath = wholeSeparator[wholeSeparator.length - 1];
            return "/" + FastStringUtils.strip(realPath, "/");
        }
        return null;
    }


    public FastResource getResource(String relativePth) {
        String matchJarResource = this.matchResource(relativePth);
        if (FastStringUtils.isNotEmpty(matchJarResource)) {
            return new FastResource(matchJarResource);
        }
        return null;
    }


    public List<FastResource> getResources(String path, IFastResourceFilter iFastResourceFilter) {
        List<FastResource> resources = new ArrayList<>(16);
        List<String> matchJarResources = this.matchResources(path, iFastResourceFilter);
        for (String matchJarResource : matchJarResources) {
            resources.add(new FastResource(matchJarResource));
        }
        return resources;
    }


}
