package com.fastchar.multipart;

import java.io.File;

/**
 * from com.oreilly.servlet.multipart
 */
public interface FileRenamePolicy {
    File rename(File var1);
}