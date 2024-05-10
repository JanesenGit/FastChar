package com.fastchar.core;

import com.fastchar.interfaces.IFastResourceFilter;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public final class FastWebResources {

    private final List<String> publicResourcePath = new ArrayList<>();

    private final List<String> privateResourcePath = new ArrayList<>();


    public FastWebResources() {
        for (String web : FastScanner.WEB) {
            publicResourcePath.add("/" + web);
        }
    }

    public boolean isPublicResourcePath(String relativePath) {
        for (String webResource : this.publicResourcePath) {
            if (relativePath.startsWith(webResource)) {
                return true;
            }
        }
        return false;
    }


    public FastWebResources addPublicResourcePath(String... relativePaths) {
        return this.addPublicResourcePath(Arrays.asList(relativePaths));
    }

    public FastWebResources addPublicResourcePath(List<String> relativePaths) {
        for (String path : relativePaths) {
            path = "/" + FastStringUtils.strip(path, "/");
            if (this.publicResourcePath.contains(path)) {
                continue;
            }
            this.publicResourcePath.add(0, path);
        }
        return this;
    }

    /**
     * 排除指定资源对外的访问
     *
     * @param relativePaths 资源地址，支持通配符'*'
     */
    public FastWebResources addPrivateResourcePath(String... relativePaths) {
        return this.addPrivateResourcePath(Arrays.asList(relativePaths));
    }

    /**
     * 排除指定资源对外的访问
     *
     * @param relativePaths 资源地址，支持通配符'*'
     */
    public FastWebResources addPrivateResourcePath(List<String> relativePaths) {
        for (String path : relativePaths) {
            if (this.privateResourcePath.contains(path)) {
                continue;
            }
            this.privateResourcePath.add(0, path);
        }
        return this;
    }


    /**
     * 判断路径是否拒绝对外访问
     *
     * @param path 路径
     * @return boolean
     */
    public boolean isPrivateResourcePath(String path) {
        for (String excludePath : this.privateResourcePath) {
            if (FastStringUtils.matches(excludePath, path)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取web资源
     *
     * @param path 资源相对路径或者绝对路径
     * @return boolean
     */
    public FastResource getResource(String path) {
        return this.getResource(false, path);
    }

    /**
     * 获取web资源
     *
     * @param path 资源相对路径或者绝对路径
     * @return boolean
     */
    public FastResource getPublicResource(String path) {
        return this.getResource(true, path);
    }

    /**
     * 获取web资源
     *
     * @param path 资源相对路径或者绝对路径
     * @return boolean
     */
    private FastResource getResource(boolean publicResource, String path) {
        for (String directory : this.publicResourcePath) {
            FastResource fastResource = this.getResource(publicResource, directory, path);
            if (fastResource != null) {
                return fastResource;
            }
        }
        return null;
    }

    /**
     * 获取web资源列表
     *
     * @param path 资源相对路径或者绝对路径
     * @return boolean
     */
    public List<FastResource> getResources(String path) {
        return getResources(path, null);
    }

    /**
     * 获取web资源列表
     *
     * @param path 资源相对路径或者绝对路径
     * @return boolean
     */
    public List<FastResource> getResources(String path, IFastResourceFilter iFastResourceFilter) {
        return this.getResources(false, path, iFastResourceFilter);
    }

    /**
     * 获取web资源列表
     *
     * @param path 资源相对路径或者绝对路径
     * @return boolean
     */
    public List<FastResource> getPublicResources(String path) {
        return getResources(true, path, null);
    }

    /**
     * 获取web资源列表
     *
     * @param path 资源相对路径或者绝对路径
     * @return boolean
     */
    public List<FastResource> getPublicResources(String path, IFastResourceFilter iFastResourceFilter) {
        return getResources(true, path, iFastResourceFilter);
    }



    /**
     * 获取web资源列表
     *
     * @param path 资源相对路径或者绝对路径
     * @return boolean
     */
    public List<FastResource> getResources(boolean publicResource, String path, IFastResourceFilter iFastResourceFilter) {
        List<FastResource> fastResource = new ArrayList<>(16);
        for (String directory : this.publicResourcePath) {
            List<FastResource> resources = this.getResources(publicResource, directory, path, iFastResourceFilter);
            for (FastResource resource : resources) {
                if (!this.containsResource(fastResource, resource)) {
                    fastResource.add(resource);
                }
            }
        }
        return fastResource;
    }


    /**
     * 获取相对路径【与前端网页地址栏路径分割一致 ‘/’ 】
     *
     * @param resource 资源对象
     * @return 相对路径地址 以 ‘/’ 开头 例如：/app/user.js
     */
    public String getRelativePath(FastResource resource) {
        String subPath = FastEngine.instance().getJarResources().getRelativePath(resource);
        if (FastStringUtils.isEmpty(subPath)) {
            //注意此处需要替换的是文件路径地址，需要分割时，请使用 File.separator 进行分割
            String path = resource.getFile().getAbsolutePath();
            subPath = path.replace(FastChar.getPath().getWebRootPath(), "");
        }
        for (String webPath : this.publicResourcePath) {
            subPath = subPath.replace(webPath, "");
        }
        return "/" + FastStringUtils.strip(subPath, "/");
    }


    private boolean containsResource(List<FastResource> resources, FastResource resource) {
        for (FastResource fastResource : resources) {
            if (fastResource.getAbsolutePath().equalsIgnoreCase(resource.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }


    private FastResource getResource(boolean publicResource, String resourceRoot, String path) {
        path = FastStringUtils.splitByWholeSeparator(path, "?")[0];
        if (FastChar.getPath().isProjectJar()) {
            FastResource webResourceByJar = getResourceByJar(publicResource, resourceRoot, path);
            if (webResourceByJar != null) {
                return webResourceByJar;
            }
            return getResourceByLocal(publicResource, resourceRoot, path);
        } else {
            FastResource webResourceByLocal = getResourceByLocal(publicResource, resourceRoot, path);
            if (webResourceByLocal != null) {
                return webResourceByLocal;
            }
            return getResourceByJar(publicResource, resourceRoot, path);
        }
    }

    private List<FastResource> getResources(boolean publicResource, String resourceRoot, String path, IFastResourceFilter iFastResourceFilter) {
        path = FastStringUtils.splitByWholeSeparator(path, "?")[0];
        List<FastResource> fastResource = new ArrayList<>(16);
        fastResource.addAll(getResourcesByJar(publicResource, resourceRoot, path, iFastResourceFilter));
        fastResource.addAll(getResourcesByLocal(publicResource, resourceRoot, path, iFastResourceFilter));
        return fastResource;
    }


    private FastResource getResourceByLocal(boolean publicResource, String resourceRoot, String path) {
        FastResource resourceByLocal = this.getResourceByLocal(publicResource, FastChar.getPath().getWebRootPath(), resourceRoot, path);
        if (resourceByLocal != null) {
            return resourceByLocal;
        }
        return this.getResourceByLocal(publicResource, FastChar.getPath().getClassRootPath(), resourceRoot, path);
    }

    private FastResource getResourceByLocal(boolean publicResource, String basePath, String resourceRoot, String path) {
        //注意，此处禁止判断path路径是否存在，避免出现 '/' 根地址导致项目卡死
        //注意，必须是以项目地址开头

        File targetFile = Paths.get(basePath, path).toFile();
        if (publicResource) {
            if (isPublicResourcePath(path) && targetFile.exists()) {
                return new FastResource(targetFile);
            }
        } else if (targetFile.exists()) {
            return new FastResource(targetFile);
        }


        targetFile = Paths.get(basePath, resourceRoot, path).toFile();
        if (targetFile.exists()) {
            return new FastResource(targetFile);
        }
        return null;
    }


    private List<FastResource> getResourcesByLocal(boolean publicResource, String resourceRoot, String path, IFastResourceFilter iFastResourceFilter) {
        List<FastResource> resources = new ArrayList<>(16);
        resources.addAll(this.getResourcesByLocal(publicResource, FastChar.getPath().getWebRootPath(), resourceRoot, path, iFastResourceFilter));
        resources.addAll(this.getResourcesByLocal(publicResource, FastChar.getPath().getClassRootPath(), resourceRoot, path, iFastResourceFilter));
        return resources;
    }

    private List<FastResource> getResourcesByLocal(boolean publicResource, String basePath, String resourceRoot, String path, IFastResourceFilter iFastResourceFilter) {
        List<FastResource> resources = new ArrayList<>(16);
        Set<String> containsPath = new HashSet<>(16);

        //注意，此处禁止判断path路径是否存在，避免出现 '/' 根地址导致项目卡死
        //注意，必须是以项目地址开头

        File targetFile = Paths.get(basePath, path).toFile();
        if (publicResource) {
            if (isPublicResourcePath(path) && targetFile.exists()) {
                resources.addAll(listResources(containsPath, targetFile, iFastResourceFilter));
            }
        } else if (targetFile.exists()) {
            resources.addAll(listResources(containsPath, targetFile, iFastResourceFilter));
        }

        targetFile = Paths.get(basePath, resourceRoot, path).toFile();
        if (targetFile.exists()) {
            resources.addAll(listResources(containsPath, targetFile, iFastResourceFilter));
        }
        return resources;
    }


    private FastResource getResourceByJar(boolean publicResource, String resourceRoot, String path) {
        //此处禁止使用 File.separator 进行分割拼接，因为jar包内路径的分隔符java统一为 ‘/’
        String resourcePath = resourceRoot + "/" + FastStringUtils.strip(path, "/");
        FastResource jarResource = FastEngine.instance().getJarResources().getResource(resourcePath);
        if (jarResource != null) {
            return jarResource;
        }
        if (publicResource) {
            if (isPublicResourcePath(path)) {
                return FastEngine.instance().getJarResources().getResource(path);
            }
        } else {
            return FastEngine.instance().getJarResources().getResource(path);
        }
        return null;
    }

    private List<FastResource> getResourcesByJar(boolean publicResource, String resourceRoot, String path, IFastResourceFilter iFastResourceFilter) {
        List<FastResource> resources = new ArrayList<>(16);

        //此处禁止使用 File.separator 进行分割拼接，因为jar包内路径的分隔符java统一为 ‘/’
        String resourcePath = resourceRoot + "/" + FastStringUtils.strip(path, "/");
        resources.addAll(FastEngine.instance().getJarResources().getResources(resourcePath, iFastResourceFilter));

        if (publicResource) {
            if (isPublicResourcePath(path)) {
                resources.addAll(FastEngine.instance().getJarResources().getResources(path, iFastResourceFilter));
            }
        } else {
            resources.addAll(FastEngine.instance().getJarResources().getResources(path, iFastResourceFilter));
        }
        return resources;
    }


    private List<FastResource> listResources(Set<String> containsPath, File file, IFastResourceFilter iFastResourceFilter) {
        List<FastResource> resources = new ArrayList<>(16);
        List<File> listFiles = this.listFiles(file, iFastResourceFilter);
        for (File listFile : listFiles) {
            if (containsPath.contains(listFile.getAbsolutePath())) {
                continue;
            }
            containsPath.add(listFile.getAbsolutePath());
            resources.add(new FastResource(listFile));
        }
        return resources;
    }

    private List<File> listFiles(File file, IFastResourceFilter iFastResourceFilter) {
        List<File> files = new ArrayList<>(16);
        if (file.isDirectory()) {
            File[] fileArray = file.listFiles();
            if (fileArray != null) {
                for (File subFile : fileArray) {
                    files.addAll(this.listFiles(subFile, iFastResourceFilter));
                }
            }
        } else {
            if (iFastResourceFilter != null && !iFastResourceFilter.onAccept(new FastResource(file))) {
                return files;
            }
            files.add(file);
        }
        return files;
    }


}


