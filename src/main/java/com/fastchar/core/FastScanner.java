package com.fastchar.core;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.interfaces.IFastScannerAccepter;
import com.fastchar.interfaces.IFastWeb;
import com.fastchar.utils.*;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@SuppressWarnings({"unchecked", "UnusedReturnValue", "WeakerAccess"})
public final class FastScanner {
    private static final String[] EXCLUDE = new String[]{"META-INF/*", "WEB-INF/*"};
    private static final String[] WEB = new String[]{"web", "WebRoot", "Root", "WEB-INF"};

    private Set<String> srcScanned = new HashSet<>();
    private Set<String> libScanned = new HashSet<>();
    private Set<String> webScanned = new HashSet<>();
    private List<ScannerJar> jars = new ArrayList<>();
    private Set<Class<?>> scannedClass = new LinkedHashSet<>();
    private Set<String> scannedFile = new LinkedHashSet<>();

    private List<IFastScannerAccepter> accepterList = new ArrayList<>();
    private boolean printClassNotFound = false;


    FastScanner() {
    }

    public boolean isPrintClassNotFound() {
        return printClassNotFound;
    }

    public FastScanner setPrintClassNotFound(boolean printClassNotFound) {
        this.printClassNotFound = printClassNotFound;
        return this;
    }


    void startScanner() throws Exception {
        scannerLib();
        scannerJar();
        scannerSrc();
        scannerWeb();

        //先注册IFastWeb核心
        for (Class<?> aClass : scannedClass) {
            if (aClass == null) {
                continue;
            }
            if (IFastWeb.class.isAssignableFrom(aClass) && aClass != IFastWeb.class) {
                FastEngine.instance().getWebs().addFastWeb((Class<? extends IFastWeb>) aClass);
            }
        }

    }

    void notifyAccepter() throws Exception {
        //必须先通知class
        for (Class<?> aClass : scannedClass) {
            notifyAccepter(aClass);
        }
        for (String s : scannedFile) {
            notifyAccepter(new File(s));
        }
        scannedClass.clear();
        scannedFile.clear();
        srcScanned.clear();
        libScanned.clear();
        webScanned.clear();
    }


    /**
     * 扫码jar包，/WEB-INF/lib 目录下
     *
     * @param jarNames
     * @return
     */
    public FastScanner includeJar(String... jarNames) throws Exception {
        return includeJar(null, jarNames);
    }

    /**
     * 扫码jar包 /WEB-INF/lib 目录下
     *
     * @param extract  是否解压
     * @param jarNames
     * @return
     */
    public FastScanner includeJar(Boolean extract, String... jarNames) throws Exception {
        for (String jarName : jarNames) {
            if (FastStringUtils.isEmpty(jarName)) {
                continue;
            }
            if (!jarName.endsWith(".jar")) {
                jarName += ".jar";
            }
            File file = new File(FastChar.getPath().getLibRootPath(), jarName);
            includeJar(extract, file);
        }
        return this;
    }

    public FastScanner includeJar(File... jarFiles) throws Exception {
        return includeJar(null, jarFiles);
    }

    public FastScanner includeJar(Boolean extract, File... jarFiles) throws Exception {
        for (File jarFile : jarFiles) {
            if (jarFile == null) {
                continue;
            }
            if (!jarFile.exists()) {
                FastChar.getLog().error(FastScanner.class, "the file '" + jarFile.getAbsolutePath() + "' not exists!");
                continue;
            }

            if (!FastChar.getPath().existsJarRoot(jarFile)) {
                FastJarLoader.addJar(jarFile);
            }

            ScannerJar scannerJar = new ScannerJar();
            scannerJar.setExtract(extract);
            scannerJar.setPath(jarFile.getPath());
            scannerJar.setName(jarFile.getName());

            String path = jarFile.getPath() + "!/";
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            scannerJar.setUrl(new URL("jar:file:" + path));
            jars.add(scannerJar);
        }
        return this;
    }


    public FastScanner addAccepter(IFastScannerAccepter... accepter) {
        accepterList.addAll(Arrays.asList(accepter));
        return this;
    }


    private void scannerSrc() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("");
        while (resources.hasMoreElements()) {
            String path = resources.nextElement().getPath();
            File file = new File(path);
            if (srcScanned.contains(file.getAbsolutePath())) {
                continue;
            }
            srcScanned.add(file.getAbsolutePath());
            forFiles(path, file);
        }

    }

    private void scannerWeb() throws Exception {
        if (webScanned.contains(FastChar.getPath().getWebRootPath())) {
            return;
        }
        webScanned.add(FastChar.getPath().getWebRootPath());
        forFiles(null, new File(FastChar.getPath().getWebRootPath()));
    }

    private void scannerLib() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if (url.getProtocol().equalsIgnoreCase("jar")) {
                File linkJar = new File(url.getPath());
                File libPath = new File(linkJar.getParent().replace("file:", ""));
                if (libScanned.contains(libPath.getPath())) {
                    continue;
                }
                libScanned.add(libPath.getPath());
                File[] jarFiles = libPath.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".jar");
                    }
                });
                if (jarFiles == null || jarFiles.length == 0) {
                    return;
                }
                for (File file : jarFiles) {
                    JarFile jarFile = new JarFile(file);
                    Manifest manifest = jarFile.getManifest();
                    if (manifest != null) {
                        Attributes mainAttributes = manifest.getMainAttributes();
                        String webClass = mainAttributes.getValue("FastChar-Web");
                        Class<?> aClass = FastClassUtils.getClass(webClass, false);
                        if (aClass != null && IFastWeb.class.isAssignableFrom(aClass)) {
                            FastEngine.instance().getWebs().addFastWeb((Class<? extends IFastWeb>) aClass);
                        }
                        boolean scanner = FastBooleanUtils.formatToBoolean(mainAttributes.getValue("FastChar-Scanner"), false);
                        if (scanner) {
                            includeJar(file);
                        }
                    }
                }

            }
        }
    }

    private void scannerJar() throws Exception {
        for (ScannerJar jar : jars) {
            if (jar.getUrl() == null) {
                continue;
            }
            if (srcScanned.contains(jar.getUrl().toString())) {
                return;
            }
            srcScanned.add(jar.getUrl().toString());

            JarURLConnection jarURLConnection = (JarURLConnection) jar.getUrl().openConnection();
            JarFile jarFile = jarURLConnection.getJarFile();

            boolean extract = false;
            String version = "1.0";
            String[] excludes = new String[0];
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                Attributes mainAttributes = manifest.getMainAttributes();
                version = mainAttributes.getValue("Manifest-Version");
                String exclude = mainAttributes.getValue("FastChat-Exclude");
                if (FastStringUtils.isNotEmpty(exclude)) {
                    excludes = exclude.split(",");
                }
                extract = FastBooleanUtils.formatToBoolean(mainAttributes.getValue("FastChar-Extract"), false);

            }

            int array1Length = excludes.length;
            int array2Length = EXCLUDE.length;
            excludes = Arrays.copyOf(excludes, excludes.length + EXCLUDE.length);
            System.arraycopy(EXCLUDE, 0, excludes, array1Length, array2Length);

            if (jar.getExtract() != null) {
                extract = jar.getExtract();
            }
            if (extract) {
                FastChar.getLog().info(FastChar.getLocal().getInfo("Scanner_Error1", jar.getName()));
            }

            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry jarEntry = jarEntries.nextElement();
                String jarEntryName = jarEntry.getName();
                if (jarEntryName.startsWith(".")) continue;

                String jarRealName = jarEntryName;
                if (jarRealName.endsWith(".class")) {
                    jarRealName = jarRealName.replace("/", ".")
                            .replace(".class", "");
                }

                boolean isExclude = false;
                for (String exclude : excludes) {
                    if (FastStringUtils.matches(exclude, jarEntryName)) {
                        isExclude = true;
                        FastChar.getLog().info(FastChar.getLocal().getInfo("Scanner_Error3", jarEntryName));
                        break;
                    }
                }
                if (isExclude) {
                    continue;
                }
                if (extract) {
                    boolean isWebSource = false;
                    for (String web : WEB) {
                        if (jarEntryName.startsWith(web)) {
                            InputStream inputStream = jarFile.getInputStream(jarEntry);
                            saveJarEntry(inputStream, jarEntry, FastChar.getPath().getWebRootPath(), version);
                            isWebSource = true;
                            break;
                        }
                    }
                    if (!isWebSource) {
                        InputStream inputStream = jarFile.getInputStream(jarEntry);
                        saveJarEntry(inputStream, jarEntry, FastChar.getPath().getClassRootPath(), version);
                    }
                } else {
                    if (jarEntryName.endsWith(".class")) {
                        Class<?> aClass = FastClassUtils.getClass(jarRealName, false);
                        scannedClass.add(aClass);
                    }
                }
            }
            if (extract) {
                FastChar.getLog().info(FastChar.getLocal().getInfo("Scanner_Error2", jar.getName()));
            }
        }
    }


    private void notifyAccepter(Class<?> targetClass) throws Exception {
        if (targetClass == null) return;
        if (targetClass.isAnnotationPresent(AFastClassFind.class)) {
            AFastClassFind fastFind = targetClass.getAnnotation(AFastClassFind.class);
            for (String s : fastFind.value()) {
                if (FastClassUtils.getClass(s, false) == null) {
                    return;
                }
            }
        }

        for (IFastScannerAccepter iFastScannerAccepter : accepterList) {
            if (iFastScannerAccepter == null) continue;
            iFastScannerAccepter.onScannerClass(FastEngine.instance(), targetClass);
        }
    }

    private void notifyAccepter(File file) throws Exception {
        if (file == null) return;
        for (IFastScannerAccepter iFastScannerAccepter : accepterList) {
            if (iFastScannerAccepter == null) continue;
            iFastScannerAccepter.onScannerFile(FastEngine.instance(), file);
        }
    }

    private void forFiles(String path, File parentFile) throws Exception {
        File[] files = parentFile.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith(".")) {
                    return false;
                }
                if (pathname.isHidden()) {
                    return false;
                }
                if (pathname.getName().toLowerCase().endsWith(".jar")) {
                    return false;
                }
                if (pathname.getName().startsWith("META-INF")) {
                    return false;
                }
                return true;
            }
        });

        if (files == null) {
            return;
        }
        for (File file : files) {
            if (FastStringUtils.isNotEmpty(FastChar.getConstant().getAttachDirectory())) {
                if (file.getAbsolutePath().startsWith(FastChar.getConstant().getAttachDirectory())) {
                    continue;
                }
            }
            if (file.isDirectory()) {
                forFiles(path, file);
            } else {
                if (file.getName().endsWith(".class")) {
                    Class convertClass = convertClass(path, file);
                    if (convertClass != null) {
                        scannedClass.add(convertClass);
                    }
                } else {
                    scannedFile.add(file.getAbsolutePath());
                }
            }
        }
    }


    private Class convertClass(String path, File file) {
        String filePath = file.getAbsolutePath().replace(FastStringUtils.stripEnd(path, "/") + "/", "");
        String className = filePath.replace(File.separator, ".").replace(".class", "");
        return FastClassUtils.getClass(className, printClassNotFound);
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void saveJarEntry(InputStream inputStream, JarEntry jarEntry,
                              String targetPath, String version) {
        try {
            String jarEntryName = jarEntry.getName();
            if (jarEntryName.startsWith("WebRoot")) {
                jarEntryName = jarEntryName.substring("WebRoot".length());
            } else if (jarEntryName.startsWith("web")) {
                jarEntryName = jarEntryName.substring("web".length());
            }
            if (jarEntryName.startsWith(".")
                    || jarEntryName.contains("private")
                    || jarEntryName.endsWith(".java")) {
                return;
            }
            String srcFilePath = targetPath + "/" + jarEntryName;
            File file = new File(srcFilePath);

            if (file.exists()) {
                return;
            }
            if (jarEntry.isDirectory()) {
                file.mkdirs();
            } else {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.getParentFile().mkdirs();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                int b = inputStream.read();
                while (b != -1) {
                    fileOutputStream.write(b);
                    b = inputStream.read();
                }
                inputStream.close();
                fileOutputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 检测Jar包版本是否已更新
     *
     * @return
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean checkIsModified(String jarName, String jarVersion) {
        String key = FastChar.getSecurity().MD5_Encrypt(jarName);
        String value = FastChar.getSecurity().MD5_Encrypt(jarVersion);
        boolean hasModified = true;
        boolean hasAdded = false;
        try {
            File file = new File(FastChar.getPath().getLibRootPath() + "/.fast_jar");
            List<String> strings = new ArrayList<>();
            if (file.exists()) {
                strings = FastFileUtils.readLines(file);
            } else if (file.createNewFile()) {
                try {
                    String string = " attrib +H " + file.getAbsolutePath(); //设置文件属性为隐藏
                    Runtime.getRuntime().exec(string);
                } catch (Exception ignored) {
                }
            }
            for (String string : strings) {
                if (string.startsWith(key)) {
                    hasAdded = true;
                    if (string.equals(key + "@" + value)) {
                        hasModified = false;
                    } else {
                        hasModified = true;
                        Collections.replaceAll(strings, string, key + "@" + value);
                    }
                    break;
                }
            }
            if (!hasAdded) {
                strings.add(key + "@" + value);
            }
            FastFileUtils.writeLines(file, strings);
        } catch (Exception e) {
            return false;
        }
        return hasModified;
    }


    private class ScannerJar {
        private URL url;
        private String path;
        private String name;
        private Boolean extract;

        public URL getUrl() {
            return url;
        }

        public ScannerJar setUrl(URL url) {
            this.url = url;
            return this;
        }

        public Boolean getExtract() {
            return extract;
        }

        public ScannerJar setExtract(Boolean extract) {
            this.extract = extract;
            return this;
        }

        public String getPath() {
            return path;
        }

        public ScannerJar setPath(String path) {
            this.path = path;
            return this;
        }

        public String getName() {
            return name;
        }

        public ScannerJar setName(String name) {
            this.name = name;
            return this;
        }

    }


}
