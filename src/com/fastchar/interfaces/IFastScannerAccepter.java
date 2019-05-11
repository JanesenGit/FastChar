package com.fastchar.interfaces;

import com.fastchar.core.FastEngine;

import java.io.File;

public interface IFastScannerAccepter {

    boolean onScannerClass(FastEngine engine, Class<?> scannedClass) throws Exception;

    boolean onScannerFile(FastEngine engine, File file) throws Exception;

}
