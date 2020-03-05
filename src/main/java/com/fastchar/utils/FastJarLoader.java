package com.fastchar.utils;
import java.io.File;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public final class FastJarLoader {
	private static URLClassLoader classloader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
	public static void loadJar(File file){
		loopFiles(file);
	}

	private static void loopFiles(File file) {
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
            if (listFiles == null) {
                return;
            }
			for (File child : listFiles) {
				loopFiles(child);
			}
		}
		else {
            if (file.getAbsolutePath().endsWith(".jar")) {
                addURL(file);
            }
		}
	}

	private static void addURL(File file) {
		try {
			Method add = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            add.setAccessible(true);
            add.invoke(classloader, file.toURI().toURL());
            add.setAccessible(false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}