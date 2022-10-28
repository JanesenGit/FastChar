package com.fastchar.core;

import com.fastchar.accepter.FastOverrideScannerAccepter;
import com.fastchar.annotation.AFastClassFind;
import com.fastchar.interfaces.IFastScannerAccepter;
import com.fastchar.interfaces.IFastScannerExtract;
import com.fastchar.interfaces.IFastWeb;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.*;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unchecked", "UnusedReturnValue", "WeakerAccess"})
public final class FastScanner {
    static final String[] EXCLUDE = new String[]{
            "*/META-INF/*",
            "META-INF/*",
            "*/WEB-INF/*",
            "WEB-INF/*"};

    static final String[] WEB = new String[]{"web", "webroot", "root", "web-inf"};

    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_WEB = "FastChar-Web";
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_SCANNER = "FastChar-Scanner";
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT = "FastChar-Extract";
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT_FILE = "FastChar-Extract-File";
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_EXCLUDE = "FastChar-Exclude";
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_DESC = "FastChar-Desc";
    static final String MANIFEST_ATTRIBUTE_FAST_CHAR_PLUGIN_VERSION = "FastChar-Plugin-Version";
    static final String TICKET_FILE_NAME = ".fastchar_jar";

    private final transient Map<String, FastClassLoader> jarLoaders = new HashMap<>(6);
    private final transient Map<String, FastClassLoader> pathLoaders = new HashMap<>(6);
    private final Set<Class<?>> disabledClass = new LinkedHashSet<>();//跳过的class
    private final List<ScannerJar> jars = new ArrayList<>(16);
    private final Set<Class<?>> scannedClass = new LinkedHashSet<>();//扫描到的class
    private final Set<String> scannedFile = new LinkedHashSet<>();//扫描到的file
    private final Set<String> scannedJar = new LinkedHashSet<>();//扫描到的jar包
    private final Map<String, List<ScannerJar>> sameJarMap = new HashMap<>(6);
    private final FastTicket fastTicket = new FastTicket(TICKET_FILE_NAME);


    private boolean printClassNotFound = false;


    FastScanner() {
    }

    public boolean isPrintClassNotFound() {
        return printClassNotFound;
    }


    /**
     * 设置是否打印class加载错误的异常
     *
     * @param printClassNotFound 布尔值
     * @return 当前对象
     */
    public FastScanner setPrintClassNotFound(boolean printClassNotFound) {
        this.printClassNotFound = printClassNotFound;
        return this;
    }

    /**
     * 禁用指定class跳过扫描
     *
     * @param targetClass 目标class
     * @return 当前对象
     */
    public FastScanner disabledClass(Class<?> targetClass) {
        disabledClass.add(targetClass);
        return this;
    }

    void startScannerLocal() throws Exception {
        scannerSrc();
    }

    void startScannerOther() throws Exception {
        scannerLib();
        scannerJar();
        scannerSrc();
        scannerWeb();
    }

    void registerWeb() throws Exception {
        //先注册IFastWeb核心
        for (Class<?> aClass : scannedClass) {
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
        for (Class<?> aClass : scannedClass) {
            if (aClass == null) {
                continue;
            }
            new FastOverrideScannerAccepter().onScannerClass(FastEngine.instance(), aClass);
        }
    }

    void notifyAccepter() throws Exception {
        if (FastChar.getConstant().isLogSameJar()) {
            for (List<ScannerJar> value : sameJarMap.values()) {
                if (value.size() > 1) {
                    List<String> infos = new ArrayList<>(16);
                    for (ScannerJar scannerJar : value) {
                        infos.add(scannerJar.getJarFileName());
                    }
                    FastChar.getLog().warn(this.getClass(),FastChar.getLocal().getInfo(FastCharLocal.JAR_ERROR1, FastStringUtils.join(infos, " , ")));
                }
            }
        }
        //必须先通知class
        for (Class<?> aClass : scannedClass) {
            notifyAccepter(aClass);
        }
        for (String s : scannedFile) {
            notifyAccepter(new File(s));
        }
        scannedClass.clear();
        scannedFile.clear();
        scannedJar.clear();
        jars.clear();
        sameJarMap.clear();
    }

    void resolveJar(boolean hasLoader, File... jarFiles) throws Exception {
        for (File jarFile : jarFiles) {
            if (jarFile == null) {
                continue;
            }
            if (!jarFile.getName().toLowerCase().endsWith(".jar")) {
                continue;
            }
            if (!jarFile.exists()) {
                FastChar.getLog().error(FastScanner.class, "the file '" + jarFile.getAbsolutePath() + "' not exists!");
                continue;
            }

            ScannerJar scannerJar = new ScannerJar(jarFile);
            //非WebRoot下的jar使用自定义的加载器
            if (!FastChar.getPath().existJarRoot(jarFile) && !hasLoader) {
                URL url = jarFile.toURI().toURL();
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
        if (FastChar.isMain()) {
            return;
        }
        forFiles(FastChar.getPath().getClassRootPath(), new File(FastChar.getPath().getWebRootPath()));
    }

    void scannerLib() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (FastChar.isMain()) {
            URL[] urLs = ((URLClassLoader) classLoader).getURLs();
            for (URL url : urLs) {
                checkURL(url);
            }
        } else {
            String libRootPath = FastChar.getPath().getLibRootPath();
            File libRoot = new File(libRootPath);
            checkURL(libRoot.toURI().toURL());
        }
    }

    void checkURL(URL url) throws Exception {
        URI toURI = url.toURI();
        if (toURI.isOpaque()) {
            return;
        }
        File checkUrlFile = new File(toURI);
        List<File> jarFileList = new ArrayList<>(16);
        if (checkUrlFile.isFile() && checkUrlFile.getName().toLowerCase().endsWith(".jar")) {
            jarFileList.add(checkUrlFile);
        }
        File[] jarFiles = checkUrlFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        });
        if (jarFiles != null) {
            jarFileList.addAll(Arrays.asList(jarFiles));
        }
        if (jarFileList.size() == 0) {
            return;
        }
        for (File file : jarFileList) {
            try (JarFile jarFile = new JarFile(file)) {
                Manifest manifest = jarFile.getManifest();
                if (manifest != null) {
                    Attributes mainAttributes = manifest.getMainAttributes();
                    String webClass = mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_WEB);
                    Class<?> aClass = FastClassUtils.getClass(webClass, printClassNotFound);
                    if (aClass != null && IFastWeb.class.isAssignableFrom(aClass)) {
                        FastEngine.instance().getWebs().putFastWeb((Class<? extends IFastWeb>) aClass);
                    }
                    boolean scanner = FastBooleanUtils.formatToBoolean(mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_SCANNER), false);
                    if (scanner) {
                        resolveJar(true, file);
                    }
                }
            }

            if (FastChar.getConstant().isLogSameJar()) {
                ScannerJar scannerJar = new ScannerJar(file);
                if (!sameJarMap.containsKey(scannerJar.getJarName())) {
                    sameJarMap.put(scannerJar.getJarName(), new ArrayList<ScannerJar>(16));
                }
                sameJarMap.get(scannerJar.getJarName()).add(scannerJar);
            }
        }
    }

    void scannerJar() throws Exception {
        for (ScannerJar scannerJar : jars) {
            if (scannedJar.contains(scannerJar.getJarFileName())) {
                continue;
            }
            if (scannerJar.getJarFile() == null) {
                continue;
            }
            scannedJar.add(scannerJar.getJarFileName());

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
                        excludes = FastStringUtils.splitByWholeSeparator(exclude,",");
                    }
                    extract = FastBooleanUtils.formatToBoolean(mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT), false);

                    String extractFilesConfig = mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT_FILE);
                    if (FastStringUtils.isNotEmpty(extractFilesConfig)) {
                        extractFiles = FastStringUtils.splitByWholeSeparator(extractFilesConfig,",");
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

                String logJarInfo = FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR1,
                        " " + scannerJar.getJarFileName() + " (" + FastFileUtils.getFileSize(scannerJar.getJarFile()) + ") ");

                if (jarModified) {
                    logJarInfo = FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR8,
                            " " + scannerJar.getJarFileName() + " (" + FastFileUtils.getFileSize(scannerJar.getJarFile()) + ") ");
                }
                FastChar.getLog().info(FastScanner.class,logJarInfo);

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
                                FastChar.getLog().info(FastScanner.class,FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR3, jarEntryName));
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

                    if (jarEntryName.endsWith(".class")) {
                        Class<?> aClass;
                        if (jarLoaders.containsKey(scannerJar.getJarCode())) {
                            aClass = FastClassUtils.getClass(jarLoaders.get(scannerJar.getJarCode()), jarRealName, true);
                        } else {
                            aClass = FastClassUtils.getClass(jarRealName, false);
                        }
                        scannedClass.add(aClass);
                        continue;
                    }

                    if (extractFile) {
                        String logInfo = FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR4, jarEntryName);
                        FastChar.getLog().info(FastScanner.class,logInfo);
                    }

                    if (extractFile || extract) {
                        boolean isWebSource = false;
                        for (String web : WEB) {
                            if (jarEntryName.toLowerCase().startsWith(web)) {
                                isWebSource = true;
                                if (FastChar.isMain()) {
                                    break;
                                }
                                InputStream inputStream = jarFile.getInputStream(jarEntry);
                                File file = saveJarEntry(jarFile, inputStream, jarEntry, FastChar.getPath().getWebRootPath(), jarModified);
                                if (file != null) {
                                    scannedFile.add(file.getAbsolutePath());
                                }
                                if (FastChar.getConstant().isLogExtract()) {
                                    FastChar.getLog().info(FastScanner.class,FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR2, jarEntryName));
                                }
                                break;
                            }
                        }
                        if (!isWebSource) {
                            InputStream inputStream = jarFile.getInputStream(jarEntry);
                            File file = saveJarEntry(jarFile, inputStream, jarEntry, FastChar.getPath().getClassRootPath(), jarModified);
                            if (file != null) {
                                scannedFile.add(file.getAbsolutePath());
                            }
                            if (FastChar.getConstant().isLogExtract()) {
                                FastChar.getLog().info(FastScanner.class,FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR2, jarEntryName));
                            }
                        }
                        if (extractFile) {
                            String logInfo = FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR5, jarEntryName);
                            FastChar.getLog().info(FastScanner.class,logInfo);
                        }
                    }
                }
                FastChar.getLog().info(FastScanner.class,FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR2,
                        " " + scannerJar.getJarFileName() + " (" + FastFileUtils.getFileSize(scannerJar.getJarFile()) + ") "));
            }
        }

        fastTicket.saveTicket();
    }

    private void notifyAccepter(Class<?> targetClass) throws Exception {
        if (targetClass == null) {
            return;
        }
        if (disabledClass.contains(targetClass)) {
            return;
        }
        if (targetClass.isAnnotationPresent(AFastClassFind.class)) {
            AFastClassFind fastFind = targetClass.getAnnotation(AFastClassFind.class);
            for (String s : fastFind.value()) {
                if (FastClassUtils.getClass(s, printClassNotFound) == null) {
                    return;
                }
            }
        }

        List<IFastScannerAccepter> iFastScannerAccepterList = FastChar.getOverrides().newInstances(false, IFastScannerAccepter.class);
        for (IFastScannerAccepter iFastScannerAccepter : iFastScannerAccepterList) {
            if (iFastScannerAccepter == null) {
                continue;
            }
            iFastScannerAccepter.onScannerClass(FastEngine.instance(), targetClass);
        }
    }

    private void notifyAccepter(File file) throws Exception {
        if (file == null) {
            return;
        }
        List<IFastScannerAccepter> iFastScannerAccepterList = FastChar.getOverrides().newInstances(false, IFastScannerAccepter.class);
        for (IFastScannerAccepter iFastScannerAccepter : iFastScannerAccepterList) {
            if (iFastScannerAccepter == null) {
                continue;
            }
            iFastScannerAccepter.onScannerFile(FastEngine.instance(), file);
        }
    }

    private void forFiles(String path, File parentFile) throws Exception {
        forFiles(FastScanner.class.getClassLoader(), path, parentFile);
    }

    private void forFiles(ClassLoader classLoader, String classRootPath, File parentFile) throws Exception {
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
            if (file.isDirectory()) {
                forFiles(classLoader, classRootPath, file);
            } else {
                if (file.getName().endsWith(".class")) {
                    if (new File(classRootPath).exists()) {
                        Class<?> convertClass = convertClass(classLoader, classRootPath, file);
                        if (convertClass != null) {
                            scannedClass.add(convertClass);
                        }
                    }
                } else {
                    scannedFile.add(file.getAbsolutePath());
                }
            }
        }
    }

    private Class<?> convertClass(ClassLoader classLoader, String classRootPath, File file) {
        String filePath = file.getAbsolutePath().replace(FastStringUtils.stripEnd(classRootPath, File.separator) + File.separator, "");
        String className = FastStringUtils.replaceFileSeparator(filePath, ".").replace(".class", "");
        return FastClassUtils.getClass(classLoader, className, printClassNotFound);
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
                    FastChar.getLog().info(FastScanner.class,FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR7, jarEntryName));
                }
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            FastIOUtils.closeQuietly(inputStream);
        }
        return null;
    }


    Map<String, FastClassLoader> getJarLoaders() {
        return jarLoaders;
    }

    Map<String, FastClassLoader> getPathLoaders() {
        return pathLoaders;
    }

    static class ScannerJar {
        private static final Pattern JAR_VERSION_PATTERN = Pattern.compile("(([0-9]+\\.?)+)");

        public ScannerJar(File jarFile) {
            this.jarFile = jarFile;
            this.jarFileName = jarFile.getName();
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

        private File jarFile;
        private String jarFileName;
        private Boolean extract;
        private final String jarCode;
        private final String jarVersion;
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
