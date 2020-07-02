package com.fastchar.interfaces;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public interface IFastScannerExtract {

    boolean onExtract(JarFile jarFile, JarEntry jarEntry);

}
