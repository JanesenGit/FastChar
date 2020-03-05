package com.fastchar.core;

import com.fastchar.annotation.AFastClassFind;
import com.fastchar.interfaces.IFastScannerAccepter;
import com.fastchar.interfaces.IFastWeb;
import com.fastchar.utils.*;
import sun.misc.URLClassPath;

import java.io.*;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@SuppressWarnings({"unchecked", "UnusedReturnValue", "WeakerAccess"})
public final class FastScanner {
    private static final String[] EXCLUDE = new String[]{"META-INF" + File.separator + "*", "WEB-INF" + File.separator + "*"};
    private static final String[] WEB = new String[]{"web", "WebRoot", "Root", "WEB-INF"};

    private List<ScannerJar> jars = new ArrayList<>();
    private Set<Class<?>> scannedClass = new LinkedHashSet<>();
    private Set<String> scannedFile = new LinkedHashSet<>();
    private Set<String> scannedJar = new LinkedHashSet<>();
    private Map<String, FastClassLoader> jarLoaders = new HashMap<>();

    private List<String> modifyTicket;
    private boolean firstTicket;

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

    String formatPath(String path) {
        try {
            return URLDecoder.decode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
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

            ScannerJar scannerJar = new ScannerJar();
            scannerJar.setPath(formatPath(jarFile.getPath()));
            scannerJar.setName(jarFile.getName());
            String path = formatPath(jarFile.getPath()) + "!/";
            if (!path.startsWith("/")) {
                path = "/" + path;
            }


            if (!FastChar.getPath().existsJarRoot(jarFile) && !hasLoader) {
                URL url = jarFile.toURI().toURL();
                FastClassLoader classLoader = new FastClassLoader(new URL[]{url}, FastScanner.class.getClassLoader());
                if (jarLoaders.containsKey(scannerJar.getPath())) {
                    jarLoaders.get(scannerJar.getPath()).close();
                }
                jarLoaders.put(scannerJar.getPath(), classLoader);
            }

            scannerJar.setUrl(new URL("jar:file:" + path));
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
            FastChar.getObservable().notifyObservers("onScannerFinish");
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
                String formatPath = formatPath(jarFile.getPath());
                if (jarLoaders.containsKey(formatPath)) {
                    FastClassLoader fastClassLoader = jarLoaders.get(formatPath);
                    fastClassLoader.close();
                    jarLoaders.remove(formatPath);
                }
            }
        } catch (Exception ignored) {
        } finally {
            FastEngine.instance().flush();
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
            String path = formatPath(resources.nextElement().getPath());
            File file = new File(path);
            forFiles(file.getAbsolutePath(), file);
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
            Field ucp = classLoader.getClass().getDeclaredField("ucp");
            if (ucp != null) {
                ucp.setAccessible(true);
                URLClassPath urlClassPath = (URLClassPath) ucp.get(classLoader);
                for (URL url : urlClassPath.getURLs()) {
                    checkURL(url);
                }
                ucp.setAccessible(false);
            }
        } else {
            Enumeration<URL> resources = classLoader.getResources("");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                checkURL(url);
            }
        }
    }

    private void checkURL(URL url) throws Exception {
        if (url.getProtocol().equalsIgnoreCase("jar") || url.getPath().endsWith(".jar")) {
            File linkJar = new File(formatPath(url.getPath()));
            File libPath = new File(linkJar.getParent().replace("file:", ""));
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
                try (JarFile jarFile = new JarFile(file)) {
                    Manifest manifest = jarFile.getManifest();
                    if (manifest != null) {
                        Attributes mainAttributes = manifest.getMainAttributes();
                        String webClass = mainAttributes.getValue("FastChar-Web");
                        Class<?> aClass = FastClassUtils.getClass(webClass, false);
                        if (aClass != null && IFastWeb.class.isAssignableFrom(aClass)) {
                            FastEngine.instance().getWebs().putFastWeb((Class<? extends IFastWeb>) aClass);
                        }
                        boolean scanner = FastBooleanUtils.formatToBoolean(mainAttributes.getValue("FastChar-Scanner"), false);
                        if (scanner) {
                            resolveJar(true, file);
                        }
                    }
                }
            }
        }
    }

    private void scannerJar() throws Exception {
        restoreTicket();
        for (ScannerJar scannerJar : jars) {
            if (scannedJar.contains(scannerJar.getName())) {
                continue;
            }
            if (scannerJar.getUrl() == null) {
                continue;
            }
            scannedJar.add(scannerJar.getName());

            JarURLConnection jarURLConnection = (JarURLConnection) scannerJar.getUrl().openConnection();
            try (JarFile jarFile = jarURLConnection.getJarFile()) {
                boolean extract = false;
                String version = "1.0";
                String[] excludes = new String[0];
                String[] extractFiles = new String[0];
                Manifest manifest = jarFile.getManifest();
                if (manifest != null) {
                    Attributes mainAttributes = manifest.getMainAttributes();
                    version = mainAttributes.getValue("Manifest-Version");
                    String exclude = mainAttributes.getValue("FastChat-Exclude");
                    if (FastStringUtils.isNotEmpty(exclude)) {
                        excludes = exclude.split(",");
                    }
                    extract = FastBooleanUtils.formatToBoolean(mainAttributes.getValue("FastChar-Extract"), false);

                    String extractFilesConfig = mainAttributes.getValue("FastChar-Extract-File");
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
                    String logInfo = FastChar.getLocal().getInfo("Scanner_Error1", scannerJar.getName());
                    FastChar.getLog().info(logInfo);
                }

                Enumeration<JarEntry> jarEntries = jarFile.entries();

                while (jarEntries.hasMoreElements()) {
                    boolean extractFile = false;
                    JarEntry jarEntry = jarEntries.nextElement();
                    String jarEntryName = jarEntry.getName();
                    if (jarEntryName.startsWith(".")) continue;

                    String jarRealName = jarEntryName;
                    if (jarRealName.endsWith(".class")) {
                        jarRealName = jarRealName.replace(File.separator, ".")
                                .replace(".class", "");
                    }

                    boolean isExclude = false;
                    for (String exclude : excludes) {
                        if (FastStringUtils.matches(exclude, jarEntryName)) {
                            isExclude = true;
                            if (FastChar.getConstant().isLogExtract()) {
                                FastChar.getLog().info(FastChar.getLocal().getInfo("Scanner_Error3", jarEntryName));
                            }
                            break;
                        }
                    }
                    if (isExclude) {
                        continue;
                    }

                    for (String file : extractFiles) {
                        if (FastStringUtils.matches(file, jarEntryName)) {
                            extractFile = true;
                            break;
                        }
                    }

                    if (jarEntryName.endsWith(".class")) {
                        Class<?> aClass;
                        if (jarLoaders.containsKey(scannerJar.getPath())) {
                            aClass = FastClassUtils.getClass(jarLoaders.get(scannerJar.getPath()), jarRealName, true);
                        } else {
                            aClass = FastClassUtils.getClass(jarRealName, false);
                        }
                        scannedClass.add(aClass);
                        continue;
                    }

                    if (extractFile) {
                        String logInfo = FastChar.getLocal().getInfo("Scanner_Error4", jarEntryName);
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
                                File file = saveJarEntry(scannerJar.getName(), inputStream, jarEntry, FastChar.getPath().getWebRootPath(), version);
                                if (file != null) {
                                    scannedFile.add(file.getAbsolutePath());
                                }
                                if (FastChar.getConstant().isLogExtract()) {
                                    FastChar.getLog().info(FastChar.getLocal().getInfo("Scanner_Error2", jarEntryName));
                                }
                                break;
                            }
                        }
                        if (!isWebSource) {
                            InputStream inputStream = jarFile.getInputStream(jarEntry);
                            File file = saveJarEntry(scannerJar.getName(), inputStream, jarEntry, FastChar.getPath().getClassRootPath(), version);
                            if (file != null) {
                                scannedFile.add(file.getAbsolutePath());
                            }
                            if (FastChar.getConstant().isLogExtract()) {
                                FastChar.getLog().info(FastChar.getLocal().getInfo("Scanner_Error2", jarEntryName));
                            }
                        }
                        if (extractFile) {
                            String logInfo = FastChar.getLocal().getInfo("Scanner_Error5", jarEntryName);
                            FastChar.getLog().info(logInfo);
                        }
                    }
                }
                if (extract) {
                    FastChar.getLog().info(FastChar.getLocal().getInfo("Scanner_Error2", scannerJar.getName()));
                }
            }
        }
        saveTicket();
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
        String className = filePath.replace(File.separator, ".").replace(".class", "");
        return FastClassUtils.getClass(className, printClassNotFound);
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    private File saveJarEntry(String jarName, InputStream inputStream, JarEntry jarEntry,
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
                if (!checkIsModified(file.getAbsolutePath(), jarName)) {
                    return file;
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
                        FastChar.getLog().error(FastChar.getLocal().getInfo("File_Error9", file.getAbsolutePath()));
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
                    modifyTicket.add(0, FastChar.getLocal().getInfo("Ticket_Error1"));
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


    public void flush() {
        List<IFastScannerAccepter> waitRemove = new ArrayList<>();
        for (IFastScannerAccepter iFastScannerAccepter : accepterList) {
            if (FastClassUtils.isRelease(iFastScannerAccepter)) {
                waitRemove.add(iFastScannerAccepter);
            }
        }
        accepterList.removeAll(waitRemove);
    }


    private static class ScannerJar {
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
