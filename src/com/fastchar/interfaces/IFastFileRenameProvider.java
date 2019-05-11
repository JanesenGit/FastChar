package com.fastchar.interfaces;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.core.FastConstant;

import java.io.File;

@AFastPriority
public interface IFastFileRenameProvider {
    File rename(File f, boolean md5Name);
}
