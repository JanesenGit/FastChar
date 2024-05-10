package com.fastchar.core;

import com.fastchar.acceptor.FastOverrideScannerAcceptor;
import com.fastchar.annotation.AFastClassFind;
import com.fastchar.enums.FastServletType;
import com.fastchar.interfaces.IFastScannerAcceptor;
import com.fastchar.interfaces.IFastScannerExtract;
import com.fastchar.interfaces.IFastWeb;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unchecked", "UnusedReturnValue", "WeakerAccess"})
final class FastScanner {

    static Pattern CLASS_NAME_PATTERN = Pattern.compile("[0-9a-zA-z_$.]+");

    static final String[] EXCLUDE = new String[]{
            "*/META-INF/*",
            "META-INF/*",
            "*/WEB-INF/*",
            "WEB-INF/*"};

    static final String[] WEB = new String[]{"web"};

    /**
     * 是否是主项目的jar包
     **/
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_MAIN_PROJECT = "FastChar-Main-Project";
    /**
     * 是否运行扫描此jar包
     **/
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_SCANNER = "FastChar-Scanner";
    /**
     * 是否解压jar里相关资源
     **/
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT = "FastChar-Extract";
    /**
     * 解压指定的文件，支持匹配符
     **/
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT_FILE = "FastChar-Extract-File";
    /**
     * 排除解压指定文件，支持匹配符
     **/
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_EXCLUDE = "FastChar-Exclude";
    /**
     * 插件版本号
     **/
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_PLUGIN_VERSION = "FastChar-Plugin-Version";
    static final String TICKET_FILE_NAME = ".fastchar_jar";
    static final String MANIFEST_CLASS_PATH = "Class-Path";

    private final transient Map<String, FastClassLoader> jarLoaders = new HashMap<>(6);
    private final transient Map<String, FastClassLoader> pathLoaders = new HashMap<>(6);
    private final List<ScannerJar> jars = new ArrayList<>(16);
    private final Set<Class<?>> scannedClasses = new LinkedHashSet<>();//扫描到的class
    private final Set<String> scannedFiles = new LinkedHashSet<>();//扫描到的本地file
    private final Set<String> scannedResources = new LinkedHashSet<>();//扫描到的资源文件
    private final Set<String> scannedJars = new LinkedHashSet<>();//扫描到的jar包

    private final Set<String> checkedURLJar = new LinkedHashSet<>();//已通过url处理了的jar包

    private final FastTicket fastTicket = new FastTicket(TICKET_FILE_NAME);


    FastScanner() {
    }

    void startScannerProject() throws Exception {
        scannerSrc();
        if (FastChar.getPath().isProjectJar()) {
            //必须手动指定，因为主项目jar是启动器，findLoaderURL 方法无法查找到主项目的jar
            checkURLByJar(true, false, new File(FastChar.getPath().getProjectJarFilePath()).toURI().toURL());
        }

        //查找主项目的jar包并优先解析，一般用于，项目启动容器是独立的，而主项目jar包是作为第三方引用
        List<URL> urls = findLoaderURL();
        for (URL url : urls) {
            checkURLByJar(false, true, url);
        }
    }

    void startScannerOther() throws Exception {
        scannerLib();
        scannerJar();
        scannerSrc();
        scannerWeb();
    }

    void registerWeb() throws Exception {
        //先注册IFastWeb核心
        for (Class<?> aClass : scannedClasses) {
            if (aClass == null) {
                continue;
            }
            if (IFastWeb.class.isAssignableFrom(aClass) && aClass != IFastWeb.class) {
                FastEngine.instance().getWebs().putFastWeb((Class<? extends IFastWeb>) aClass);
            }
        }
    }

    void registerOverrider() throws Exception {
        //最先注册Override的类
        for (Class<?> aClass : scannedClasses) {
            if (aClass == null) {
                continue;
            }
            new FastOverrideScannerAcceptor().onScannerClass(FastEngine.instance(), aClass);
        }
    }

    void notifyAcceptor() throws Exception {
        //必须先通知class
        for (Class<?> aClass : scannedClasses) {
            notifyAcceptor(aClass);
        }

        for (String filePath : scannedFiles) {
            notifyAcceptor(new FastResource(new File(filePath)));
        }

        for (String resourcePath : scannedResources) {
            notifyAcceptor(new FastResource(resourcePath));
        }

        scannedClasses.clear();
        scannedFiles.clear();
        scannedJars.clear();
        jars.clear();
        scannedResources.clear();
        checkedURLJar.clear();

    }

    void resolveJar(boolean hasLoader, File... jarFiles) throws Exception {
        for (File file : jarFiles) {
            if (file == null) {
                continue;
            }
            if (!file.getName().toLowerCase().endsWith(".jar")) {
                continue;
            }
            if (!file.exists()) {
                FastChar.getLogger().error(FastScanner.class, "the file '" + file.getAbsolutePath() + "' not exists!");
                continue;
            }
            ScannerJar scannerJar = new ScannerJar(file);
            if (!hasLoader) {
                URL url = file.toURI().toURL();
                FastClassLoader classLoader = new FastClassLoader(new URL[]{url}, FastScanner.class.getClassLoader());
                if (jarLoaders.containsKey(scannerJar.getJarCode())) {
                    jarLoaders.get(scannerJar.getJarCode()).close();
                }
                jarLoaders.put(scannerJar.getJarCode(), classLoader);
            }
            jars.add(scannerJar);
        }
    }

    void resolvePath(String... paths) throws Exception {
        for (String path : paths) {
            String classRootPath = path;
            File guessClassRootFile = new File(path, "WEB-INF/classes");
            if (guessClassRootFile.exists()) {
                classRootPath = guessClassRootFile.getAbsolutePath();
            }

            FastClassLoader classLoader = new FastClassLoader(new URL[]{new File(classRootPath).toURI().toURL()}, FastScanner.class.getClassLoader());
            if (pathLoaders.containsKey(path)) {
                pathLoaders.get(path).close();
            }
            pathLoaders.put(path, classLoader);
            forFiles(pathLoaders.get(path), classRootPath, new File(path));
        }
    }

    void scannerSrc() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("");
        while (resources.hasMoreElements()) {
            URL nextElement = resources.nextElement();
            URI toURI = nextElement.toURI();
            if (!toURI.isOpaque()) {
                File file = new File(toURI);
                forFiles(file.getAbsolutePath(), file);
            }
        }
    }

    void scannerWeb() throws Exception {
        forFiles(FastChar.getPath().getClassRootPath(), new File(FastChar.getPath().getWebRootPath()));
    }

    void scannerLib() throws Exception {
        List<URL> urls = findLoaderURL();
        for (URL url : urls) {
            checkURLByJar(url);
        }
    }

    void checkURLByJar(URL url) throws Exception {
        this.checkURLByJar(false, false, url);
    }

    void checkURLByJar(boolean mainProjectJar, boolean onlyCheckMainProject, URL url) throws Exception {
        URI toURI = url.toURI();
        if (toURI.isOpaque()) {
            return;
        }
        File checkUrlFile = new File(toURI);
        List<File> jarFileList = new ArrayList<>(16);
        if (checkUrlFile.isFile()) {
            if (checkUrlFile.getName().toLowerCase().endsWith(".jar")) {
                jarFileList.add(checkUrlFile);
            }
        } else if (checkUrlFile.isDirectory()) {
            File[] jarFiles = checkUrlFile.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
            if (jarFiles != null) {
                jarFileList.addAll(Arrays.asList(jarFiles));
            }
        }
        if (jarFileList.isEmpty()) {
            return;
        }
        for (File file : jarFileList) {
            if (checkedURLJar.contains(file.getAbsolutePath())) {
                continue;
            }

            String classPath = null;
            boolean scanner = false, mainProject = false;
            try (JarFile jarFile = new JarFile(file)) {
                Manifest manifest = jarFile.getManifest();
                if (manifest != null) {
                    Attributes mainAttributes = manifest.getMainAttributes();
                    mainProject = FastBooleanUtils.formatToBoolean(mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_MAIN_PROJECT), false);
                    classPath = mainAttributes.getValue(MANIFEST_CLASS_PATH);
                    scanner = FastBooleanUtils.formatToBoolean(mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_SCANNER), false);
                }
                if (mainProjectJar) {
                    mainProject = true;
                }
                if (mainProject) {
                    FastEngine.instance().setProjectManifest(manifest);
                }
            }

            //如果只查找主项目jar包，则跳过其他jar包的扫描
            if (!onlyCheckMainProject) {
                checkedURLJar.add(file.getAbsolutePath());

                if (scanner) {
                    resolveJar(true, file);
                }

                checkClassPath(file, classPath);
            }

            if (mainProject) {

                //改变项目结构为project-jar模式
                FastEngine.instance().getPath().setProjectJar(file.getAbsolutePath());

                //优先解析主项目jar包
                extractedJar(new ScannerJar(file).setMainProject(true));
            }
        }
    }

    void checkClassPath(File targetJarFile, String classPath) {
        try {
            if (FastStringUtils.isNotEmpty(classPath)) {
                String[] paths = classPath.split(" ");
                for (String path : paths) {
                    if (FastStringUtils.isEmpty(path)) {
                        continue;
                    }
                    this.checkURLByJar(new File(Paths.get(targetJarFile.getParent(), path).normalize().toAbsolutePath().toString()).toURI().toURL());
                }
            }
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }


    void scannerJar() throws Exception {
        for (ScannerJar scannerJar : jars) {
            extractedJar(scannerJar);
        }
        fastTicket.saveTicket();
    }

    Map<String, FastClassLoader> getJarLoaders() {
        return jarLoaders;
    }

    Map<String, FastClassLoader> getPathLoaders() {
        return pathLoaders;
    }

    private List<URL> findLoaderURL() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<URL> loaderURL = findLoaderURL(classLoader);

        String[] classPaths = ManagementFactory.getRuntimeMXBean().getClassPath().split(File.pathSeparator);
        for (String classPath : classPaths) {
            try {
                File file = new File(classPath);
                if (file.isFile()) {
                    URL url = file.toURI().toURL();
                    if (loaderURL.contains(url)) {
                        continue;
                    }
                    loaderURL.add(url);
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return loaderURL;
    }

    private List<URL> findLoaderURL(ClassLoader loader) {
        List<URL> urlList = new ArrayList<>();
        if (loader instanceof URLClassLoader) {
            URL[] urls = ((URLClassLoader) loader).getURLs();
            for (URL url : urls) {
                if (urlList.contains(url)) {
                    continue;
                }
                urlList.add(url);
            }
        }
        if (loader.getParent() != null) {
            List<URL> loaderURL = findLoaderURL(loader.getParent());
            for (URL url : loaderURL) {
                if (urlList.contains(url)) {
                    continue;
                }
                urlList.add(url);
            }
        }
        return urlList;
    }


    private void extractedJar(ScannerJar scannerJar) throws IOException {
        if (scannedJars.contains(scannerJar.getJarFilePath())) {
            return;
        }
        if (scannerJar.getJarFile() == null) {
            return;
        }
        scannedJars.add(scannerJar.getJarFilePath());

        try (JarFile jarFile = new JarFile(scannerJar.getJarFile())) {
            boolean extract = false;
            String version = "1.0";
            String fastCharPluginVersion = null;
            String[] excludes = new String[0];
            String[] extractFiles = new String[0];
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
                Attributes mainAttributes = manifest.getMainAttributes();

                version = mainAttributes.getValue(Attributes.Name.MANIFEST_VERSION);
                fastCharPluginVersion = mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_PLUGIN_VERSION);
                String exclude = mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_EXCLUDE);
                if (FastStringUtils.isNotEmpty(exclude)) {
                    excludes = FastStringUtils.splitByWholeSeparator(exclude, ",");
                }
                extract = FastBooleanUtils.formatToBoolean(mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT), false);
                String extractFilesConfig = mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT_FILE);
                if (FastStringUtils.isNotEmpty(extractFilesConfig)) {
                    extractFiles = FastStringUtils.splitByWholeSeparator(extractFilesConfig, ",");
                }
            }


            if (FastStringUtils.isEmpty(version)) {
                version = "1.0";
            }
            if (FastStringUtils.isNotEmpty(fastCharPluginVersion)) {
                version = version + "@" + fastCharPluginVersion;
            }
            int array1Length = excludes.length;
            int array2Length = EXCLUDE.length;
            excludes = Arrays.copyOf(excludes, excludes.length + EXCLUDE.length);
            System.arraycopy(EXCLUDE, 0, excludes, array1Length, array2Length);

            if (scannerJar.getExtract() != null) {
                extract = scannerJar.getExtract();
            }
            boolean jarModified = fastTicket.pushTicket(FastChar.getSecurity().MD5_Encrypt(jarFile.getName()), FastChar.getSecurity().MD5_Encrypt(version), true);

            String logHead = "";
            if (scannerJar.isMainProject()) {
                logHead = "[ ProjectJar ] ";
            }

            String logJarInfo = FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR1,
                    " " + logHead + scannerJar.getJarFileName() + " (" + FastFileUtils.getFileSize(scannerJar.getJarFile()) + ") ");

            if (jarModified) {
                logJarInfo = FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR8,
                        " " + logHead + scannerJar.getJarFileName() + " (" + FastFileUtils.getFileSize(scannerJar.getJarFile()) + ") ");
            }
            FastChar.getLogger().info(FastScanner.class, logJarInfo);

            Enumeration<JarEntry> jarEntries = jarFile.entries();

            while (jarEntries.hasMoreElements()) {
                boolean extractFile = false;
                JarEntry jarEntry = jarEntries.nextElement();
                String jarEntryName = jarEntry.getName();
                if (jarEntryName.startsWith(".")) {
                    continue;
                }

                String jarRealName = jarEntryName;
                if (jarRealName.endsWith(".class")) {
                    jarRealName = FastStringUtils.replaceFileSeparator(jarRealName, ".")
                            .replace(".class", "");
                }

                boolean isExclude = false;
                for (String exclude : excludes) {
                    String excludeValue = FastStringUtils.replaceFileSeparator(exclude, "@");
                    String jarEntryNameValue = FastStringUtils.replaceFileSeparator(jarEntryName, "@");
                    if (FastStringUtils.matches(excludeValue, jarEntryNameValue)) {
                        isExclude = true;
                        if (FastChar.getConstant().isLogExtract()) {
                            FastChar.getLogger().info(FastScanner.class, FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR3, jarEntryName));
                        }
                        break;
                    }
                }
                if (isExclude) {
                    continue;
                }

                for (String file : extractFiles) {
                    String fileValue = FastStringUtils.replaceFileSeparator(file, "@");
                    String jarEntryNameValue = FastStringUtils.replaceFileSeparator(jarEntryName, "@");
                    if (FastStringUtils.matches(fileValue, jarEntryNameValue)) {
                        extractFile = true;
                        break;
                    }
                }

                //class文件跳过解压操作
                if (jarEntryName.toLowerCase().endsWith(".class")) {
                    Class<?> aClass = null;
                    if (CLASS_NAME_PATTERN.matcher(jarRealName).matches()) {
                        if (jarLoaders.containsKey(scannerJar.getJarCode())) {
                            aClass = FastClassUtils.loadClass(jarLoaders.get(scannerJar.getJarCode()), jarRealName, true);
                        } else {
                            aClass = FastClassUtils.loadClass(jarRealName, false);
                        }
                    }
                    if (aClass != null) {
                        scannedClasses.add(aClass);
                    }
                    continue;
                }

                //jar包项目，不解压jar包里的文件，或者配置了代理静态资源 或者是主项目的jar包
                if (FastChar.getConstant().isEmbedServer() || FastChar.getConstant().isProxyResource() || scannerJar.isMainProject()) {
                    extractFile = false;
                    extract = false;
                }

                if (extractFile) {
                    String logInfo = FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR4, jarEntryName);
                    FastChar.getLogger().info(FastScanner.class, logInfo);
                }

                boolean isWebSource = false;
                for (String web : WEB) {
                    if (jarEntryName.toLowerCase().startsWith(web)) {
                        isWebSource = true;
                        break;
                    }
                }

                if (extractFile || extract) {
                    if (isWebSource) {
                        if (FastChar.getServletType() != FastServletType.None) {
                            InputStream inputStream = jarFile.getInputStream(jarEntry);
                            File file = saveJarEntry(jarFile, inputStream, jarEntry, FastChar.getPath().getWebRootPath(), jarModified);
                            if (file != null) {
                                scannedFiles.add(file.getAbsolutePath());
                            }
                        }
                    } else {
                        InputStream inputStream = jarFile.getInputStream(jarEntry);
                        File file = saveJarEntry(jarFile, inputStream, jarEntry, FastChar.getPath().getClassRootPath(), jarModified);
                        if (file != null) {
                            scannedFiles.add(file.getAbsolutePath());
                        }
                    }
                    if (FastChar.getConstant().isLogExtract()) {
                        FastChar.getLogger().info(FastScanner.class, FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR2, jarEntryName));
                    }
                    if (extractFile) {
                        String logInfo = FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR5, jarEntryName);
                        FastChar.getLogger().info(FastScanner.class, logInfo);
                    }
                } else if (!jarEntry.isDirectory()) {
                    scannedResources.add("jar:" + scannerJar.getJarFile().toURI() + "!/" + jarEntryName);
                }
            }

            if (jarModified) {
                FastChar.getLogger().info(FastScanner.class, FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR11,
                        " " + logHead + scannerJar.getJarFileName() + " (" + FastFileUtils.getFileSize(scannerJar.getJarFile()) + ") "));
            } else {
                FastChar.getLogger().info(FastScanner.class, FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR2,
                        " " + logHead + scannerJar.getJarFileName() + " (" + FastFileUtils.getFileSize(scannerJar.getJarFile()) + ") "));
            }
        }
    }

    private void notifyAcceptor(Class<?> targetClass) throws Exception {
        if (targetClass == null) {
            return;
        }
        if (targetClass.isAnnotationPresent(AFastClassFind.class)) {
            AFastClassFind fastFind = targetClass.getAnnotation(AFastClassFind.class);
            for (String s : fastFind.value()) {
                if (FastClassUtils.loadClass(s, false) == null) {
                    return;
                }
            }
        }

        List<IFastScannerAcceptor> iFastScannerAccepterList = FastChar.getOverrides().newInstances(false, IFastScannerAcceptor.class);
        for (IFastScannerAcceptor iFastScannerAcceptor : iFastScannerAccepterList) {
            if (iFastScannerAcceptor == null) {
                continue;
            }
            iFastScannerAcceptor.onScannerClass(FastEngine.instance(), targetClass);
        }
    }

    private void notifyAcceptor(FastResource file) throws Exception {
        if (file == null) {
            return;
        }
        List<IFastScannerAcceptor> iFastScannerAcceptorList = FastChar.getOverrides().newInstances(false, IFastScannerAcceptor.class);
        for (IFastScannerAcceptor iFastScannerAcceptor : iFastScannerAcceptorList) {
            if (iFastScannerAcceptor == null) {
                continue;
            }
            iFastScannerAcceptor.onScannerResource(FastEngine.instance(), file);
        }
    }


    private void forFiles(String path, File parentFile) throws Exception {
        forFiles(FastScanner.class.getClassLoader(), path, parentFile);
    }

    private void forFiles(ClassLoader classLoader, String classRootPath, File parentFile) throws Exception {
        File[] files = parentFile.listFiles(pathname -> {
            if (pathname.getName().startsWith(".")) {
                return false;
            }
            if (pathname.isHidden()) {
                return false;
            }
            if (pathname.getName().toLowerCase().endsWith(".jar")) {
                return false;
            }
            return !pathname.getName().startsWith("META-INF");
        });

        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                forFiles(classLoader, classRootPath, file);
            } else {
                if (file.getName().endsWith(".class")) {
                    if (new File(classRootPath).exists()) {
                        Class<?> convertClass = convertClass(classLoader, classRootPath, file);
                        if (convertClass != null) {
                            scannedClasses.add(convertClass);
                        }
                    }
                } else {
                    scannedFiles.add(file.getAbsolutePath());
                }
            }
        }
    }

    private Class<?> convertClass(ClassLoader classLoader, String classRootPath, File file) {
        String filePath = file.getAbsolutePath().replace(FastStringUtils.stripEnd(classRootPath, File.separator) + File.separator, "");
        String className = FastStringUtils.replaceFileSeparator(filePath, ".").replace(".class", "");
        if (CLASS_NAME_PATTERN.matcher(className).matches()) {
            return FastClassUtils.loadClass(classLoader, className, false);
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File saveJarEntry(JarFile jarFile, InputStream inputStream, JarEntry jarEntry,
                              String targetPath, boolean jarModified) {
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
                return null;
            }
            File file = new File(targetPath, jarEntryName);

            if (file.exists()) {
                if (!jarModified) {
                    return file;
                }
            }
            List<IFastScannerExtract> iFastScannerExtracts = FastChar.getOverrides().newInstances(false, IFastScannerExtract.class);
            for (IFastScannerExtract iFastScannerExtract : iFastScannerExtracts) {
                if (iFastScannerExtract != null) {
                    if (!iFastScannerExtract.onExtract(jarFile, jarEntry)) {
                        return null;
                    }
                }
            }

            if (!jarEntry.isDirectory()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.getParentFile().mkdirs();

                FileOutputStream outputStream = new FileOutputStream(file);
                FastIOUtils.copy(inputStream, outputStream);
                FastIOUtils.closeQuietly(outputStream);
                if (FastChar.getConstant().isLogExtractNewFile()) {
                    FastChar.getLogger().info(FastScanner.class, FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR7, jarEntryName));
                }
            }
            return file;
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        } finally {
            FastIOUtils.closeQuietly(inputStream);
        }
        return null;
    }


    static class ScannerJar {
        private static final Pattern JAR_VERSION_PATTERN = Pattern.compile("(([0-9]+\\.?)+)");

        public ScannerJar(File jarFile) {
            this.jarFile = jarFile;
            this.jarFileName = jarFile.getName();
            this.jarFilePath = jarFile.getAbsolutePath();
            this.jarName = this.jarFileName.substring(0, this.jarFileName.lastIndexOf("."));
            Matcher matcher = JAR_VERSION_PATTERN.matcher(this.jarName);
            if (matcher.find()) {
                this.jarVersion = matcher.group(1);
            } else {
                this.jarVersion = "1.0.0";
            }
            this.jarName = this.jarName.replace("-" + this.jarVersion, "")
                    .replace(this.jarVersion, "");
            this.jarCode = FastMD5Utils.MD5(jarFile.getAbsolutePath());
        }

        private boolean mainProject;
        private File jarFile;
        private String jarFileName;
        private String jarFilePath;
        private Boolean extract;
        private final String jarCode;
        private String jarVersion;
        private String jarName;

        public File getJarFile() {
            return jarFile;
        }

        public ScannerJar setJarFile(File jarFile) {
            this.jarFile = jarFile;
            return this;
        }

        public String getJarFileName() {
            return jarFileName;
        }

        public ScannerJar setJarFileName(String jarFileName) {
            this.jarFileName = jarFileName;
            return this;
        }

        public Boolean getExtract() {
            return extract;
        }

        public ScannerJar setExtract(Boolean extract) {
            this.extract = extract;
            return this;
        }

        public String getJarCode() {
            return jarCode;
        }

        public String getJarVersion() {
            return jarVersion;
        }


        public String getJarName() {
            return jarName;
        }


        public ScannerJar setJarName(String jarName) {
            this.jarName = jarName;
            return this;
        }

        public ScannerJar setJarVersion(String jarVersion) {
            this.jarVersion = jarVersion;
            return this;
        }

        public String getJarFilePath() {
            return jarFilePath;
        }

        public ScannerJar setJarFilePath(String jarFilePath) {
            this.jarFilePath = jarFilePath;
            return this;
        }

        public boolean isMainProject() {
            return mainProject;
        }

        public ScannerJar setMainProject(boolean mainProject) {
            this.mainProject = mainProject;
            return this;
        }

        @Override
        public String toString() {
            return "ScannerJar{" +
                    "jarFile=" + jarFile +
                    ", jarFileName='" + jarFileName + '\'' +
                    ", extract=" + extract +
                    ", jarCode='" + jarCode + '\'' +
                    ", jarVersion='" + jarVersion + '\'' +
                    ", jarName='" + jarName + '\'' +
                    '}';
        }
    }


}
