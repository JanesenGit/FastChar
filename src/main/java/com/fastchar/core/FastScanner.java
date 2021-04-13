package com.fastchar.core;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.enums.FastObservableEvent;
import com.fastchar.interfaces.IFastScannerAccepter;
import com.fastchar.interfaces.IFastScannerExtract;
import com.fastchar.interfaces.IFastWeb;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.*;
//import jdk.internal.loader.URLClassPath;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unchecked", "UnusedReturnValue", "WeakerAccess"})
public final class FastScanner {
    private static final String[] EXCLUDE = new String[]{
            "*/META-INF/*",
            "META-INF/*",
            "*/WEB-INF/*",
            "WEB-INF/*"};

    private static final String[] WEB = new String[]{"web", "WebRoot", "Root", "WEB-INF"};

    private static final String MANIFEST_ATTRIBUTE_FAST_CHAR_WEB = "FastChar-Web";
    private static final String MANIFEST_ATTRIBUTE_FAST_CHAR_SCANNER = "FastChar-Scanner";
    private static final String MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT = "FastChar-Extract";
    private static final String MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT_FILE = "FastChar-Extract-File";
    private static final String MANIFEST_ATTRIBUTE_FAST_CHAR_EXCLUDE = "FastChat-Exclude";


    private final transient Map<String, FastClassLoader> jarLoaders = new HashMap<>(6);
    private final List<ScannerJar> jars = new ArrayList<>();
    private final Set<Class<?>> scannedClass = new LinkedHashSet<>();
    private final Set<String> scannedFile = new LinkedHashSet<>();
    private final Set<String> scannedJar = new LinkedHashSet<>();
    private final Map<String, List<ScannerJar>> sameJarMap = new HashMap<>(6);

    private List<String> modifyTicket;
    private boolean firstTicket;

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

    void startLocal() throws Exception {
        scannerSrc();
        registerWeb();
    }

    void startScanner() throws Exception {
        scannerLib();
        scannerJar();
        scannerSrc();
        scannerWeb();
        registerWeb();
    }

    void registerWeb() {
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

    void notifyAccepter() throws Exception {
        if (FastChar.getConstant().isLogSameJar()) {
            for (List<ScannerJar> value : sameJarMap.values()) {
                if (value.size() > 1) {
                    List<String> infos = new ArrayList<>();
                    for (ScannerJar scannerJar : value) {
                        infos.add(scannerJar.getJarFileName());
                    }
                    FastChar.getLog().warn(FastChar.getLocal().getInfo(FastCharLocal.JAR_ERROR1, FastStringUtils.join(infos, " , ")));
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
            if (!FastChar.getPath().existsJarRoot(jarFile) && !hasLoader) {
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

    public FastScanner loadJar(File... jarFiles) throws Exception {
        resolveJar(false, jarFiles);
        return this;
    }


    public FastScanner updateJar(File... jarFiles) throws Exception {
        try {
            loadJar(jarFiles);
            scannerJar();
            registerWeb();
            FastEngine.instance().getWebs().initWeb(FastEngine.instance());
            notifyAccepter();
            FastChar.getObservable().notifyObservers(FastObservableEvent.onScannerFinish.name());
            if (!FastChar.isMain()) {
                FastEngine.instance().getWebs().runWeb(FastEngine.instance());
            }
        } finally {
            FastEngine.instance().flush();
        }
        return this;
    }

    public FastScanner unloadJar(File... jarFiles) {
        try {
            for (File jarFile : jarFiles) {
                ScannerJar scannerJar = new ScannerJar(jarFile);
                if (jarLoaders.containsKey(scannerJar.getJarCode())) {
                    FastClassLoader fastClassLoader = jarLoaders.get(scannerJar.getJarCode());
                    fastClassLoader.close();
                    jarLoaders.remove(scannerJar.getJarCode());
                }
            }
        } catch (Exception ignored) {
        } finally {
            FastEngine.instance().flush();
        }
        return this;
    }


    private void scannerSrc() throws Exception {
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

    private void scannerWeb() throws Exception {
        if (FastChar.isMain()) {
            return;
        }
        forFiles(FastChar.getPath().getClassRootPath(), new File(FastChar.getPath().getWebRootPath()));
    }

    private void scannerLib() throws Exception {
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

    private void checkURL(URL url) throws Exception {
        URI toURI = url.toURI();
        if (toURI.isOpaque()) {
            return;
        }
        File checkUrlFile = new File(toURI);
        List<File> jarFileList = new ArrayList<>();
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
                    Class<?> aClass = FastClassUtils.getClass(webClass, false);
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
                    sameJarMap.put(scannerJar.getJarName(), new ArrayList<ScannerJar>());
                }
                sameJarMap.get(scannerJar.getJarName()).add(scannerJar);
            }
        }
    }

    private void scannerJar() throws Exception {
        restoreTicket();

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
                String[] excludes = new String[0];
                String[] extractFiles = new String[0];
                Manifest manifest = jarFile.getManifest();
                if (manifest != null) {
                    Attributes mainAttributes = manifest.getMainAttributes();

                    version = mainAttributes.getValue(Attributes.Name.MANIFEST_VERSION);
                    String exclude = mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_EXCLUDE);
                    if (FastStringUtils.isNotEmpty(exclude)) {
                        excludes = exclude.split(",");
                    }
                    extract = FastBooleanUtils.formatToBoolean(mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT), false);

                    String extractFilesConfig = mainAttributes.getValue(MANIFEST_ATTRIBUTE_FAST_CHAR_EXTRACT_FILE);
                    if (FastStringUtils.isNotEmpty(extractFilesConfig)) {
                        extractFiles = extractFilesConfig.split(",");
                    }
                }

                int array1Length = excludes.length;
                int array2Length = EXCLUDE.length;
                excludes = Arrays.copyOf(excludes, excludes.length + EXCLUDE.length);
                System.arraycopy(EXCLUDE, 0, excludes, array1Length, array2Length);

                if (scannerJar.getExtract() != null) {
                    extract = scannerJar.getExtract();
                }
                if (extract) {
                    String logInfo = FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR1,
                            scannerJar.getJarFileName() + " (" + FastFileUtils.getFileSize(scannerJar.getJarFile()) + ") ");
                    FastChar.getLog().info(logInfo);
                }

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
                                FastChar.getLog().info(FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR3, jarEntryName));
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
                        FastChar.getLog().info(logInfo);
                    }

                    if (extractFile || extract) {
                        boolean isWebSource = false;
                        for (String web : WEB) {
                            if (jarEntryName.startsWith(web)) {
                                isWebSource = true;
                                if (FastChar.isMain()) {
                                    break;
                                }
                                InputStream inputStream = jarFile.getInputStream(jarEntry);
                                File file = saveJarEntry(jarFile, inputStream, jarEntry, FastChar.getPath().getWebRootPath(), version);
                                if (file != null) {
                                    scannedFile.add(file.getAbsolutePath());
                                }
                                if (FastChar.getConstant().isLogExtract()) {
                                    FastChar.getLog().info(FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR2, jarEntryName));
                                }
                                break;
                            }
                        }
                        if (!isWebSource) {
                            InputStream inputStream = jarFile.getInputStream(jarEntry);
                            File file = saveJarEntry(jarFile, inputStream, jarEntry, FastChar.getPath().getClassRootPath(), version);
                            if (file != null) {
                                scannedFile.add(file.getAbsolutePath());
                            }
                            if (FastChar.getConstant().isLogExtract()) {
                                FastChar.getLog().info(FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR2, jarEntryName));
                            }
                        }
                        if (extractFile) {
                            String logInfo = FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR5, jarEntryName);
                            FastChar.getLog().info(logInfo);
                        }
                    }
                }
                if (extract) {
                    FastChar.getLog().info(FastChar.getLocal().getInfo(FastCharLocal.SCANNER_ERROR2,
                            scannerJar.getJarFileName() + " (" + FastFileUtils.getFileSize(scannerJar.getJarFile()) + ") "));
                }
            }
        }

        saveTicket();
    }


    private void notifyAccepter(Class<?> targetClass) throws Exception {
        if (targetClass == null) {
            return;
        }
        if (targetClass.isAnnotationPresent(AFastClassFind.class)) {
            AFastClassFind fastFind = targetClass.getAnnotation(AFastClassFind.class);
            for (String s : fastFind.value()) {
                if (FastClassUtils.getClass(s, false) == null) {
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
                forFiles(path, file);
            } else {
                if (file.getName().endsWith(".class")) {
                    Class<?> convertClass = convertClass(path, file);
                    if (convertClass != null) {
                        scannedClass.add(convertClass);
                    }
                } else {
                    scannedFile.add(file.getAbsolutePath());
                }
            }
        }
    }


    private Class<?> convertClass(String path, File file) {
        String filePath = file.getAbsolutePath().replace(FastStringUtils.stripEnd(path, File.separator) + File.separator, "");

        String className = FastStringUtils.replaceFileSeparator(filePath, ".").replace(".class", "");
        return FastClassUtils.getClass(className, printClassNotFound);
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File saveJarEntry(JarFile jarFile, InputStream inputStream, JarEntry jarEntry,
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
                return null;
            }
            File file = new File(targetPath, jarEntryName);
            if (file.exists()) {
                if (!checkIsModified(file.getAbsolutePath(), jarFile.getName())) {
                    return file;
                }
            }
            checkIsModified(file.getAbsolutePath(), jarFile.getName());
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
                int b = inputStream.read();
                while (b != -1) {
                    outputStream.write(b);
                    b = inputStream.read();
                }
                inputStream.close();
                outputStream.close();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void restoreTicket() {
        try {
            File file = new File(FastChar.getPath().getWebInfoPath(), ".fast_jar");
            if (!file.exists()) {
                firstTicket = true;
                if (!file.createNewFile()) {
                    if (FastChar.getConstant().isDebug()) {
                        FastChar.getLog().error(FastChar.getLocal().getInfo(FastCharLocal.FILE_ERROR9, file.getAbsolutePath()));
                    }
                }
            }
            modifyTicket = FastFileUtils.readLines(file);
        } catch (Exception ignored) {
        }
    }

    private void saveTicket() {
        try {
            if (modifyTicket != null) {
                if (firstTicket) {
                    modifyTicket.add(0, FastChar.getLocal().getInfo(FastCharLocal.TICKET_ERROR1));
                }
                File file = new File(FastChar.getPath().getWebInfoPath(), ".fast_jar");
                FastFileUtils.writeLines(file, modifyTicket);
                modifyTicket.clear();
            }
        } catch (Exception ignored) {
        }
    }


    /**
     * 检测Jar包版本是否已更新
     *
     * @return 布尔值
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean checkIsModified(String filePath, String jarName) {
        if (modifyTicket == null) {
            return false;
        }
        String key = FastChar.getSecurity().MD5_Encrypt(filePath);
        String value = FastChar.getSecurity().MD5_Encrypt(jarName + filePath);
        boolean hasModified = true;
        try {
            boolean hasAdded = false;
            for (String string : modifyTicket) {
                if (string.startsWith(key)) {
                    hasAdded = true;
                    if (string.equals(key + "@" + value)) {
                        hasModified = false;
                    } else {
                        hasModified = true;
                        Collections.replaceAll(modifyTicket, string, key + "@" + value);
                    }
                    break;
                }
            }
            if (!hasAdded) {
                modifyTicket.add(key + "@" + value);
            }
        } catch (Exception e) {
            return false;
        }
        return hasModified;
    }


    private static class ScannerJar {
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
