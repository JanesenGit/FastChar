package com.fastchar.utils;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public final class FastJarLoader {
	private static URLClassLoader classloader = (URLClassLoader)Thread.currentThread().getContextClassLoader();
	public static void addJar(File file){
		loopFiles(file);
	}

	/**    
	 * 循环遍历目录，找出所有的资源路径。
	 * @param file 当前遍历文件
	 */
	private static void loopDirs(File file) {
		if (file.isDirectory()) {
			addURL(file);
			File[] tmps = file.listFiles();
			for (File tmp : tmps) {
				loopDirs(tmp);
			}
		}
	}

	/**    
	 * 循环遍历目录，找出所有的jar包。
	 * @param file 当前遍历文件
	 */
	private static void loopFiles(File file) {
		if (file.isDirectory()) {
			File[] tmps = file.listFiles();
			for (File tmp : tmps) {
				loopFiles(tmp);
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}