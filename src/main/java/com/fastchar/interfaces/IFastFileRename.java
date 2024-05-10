package com.fastchar.interfaces;

import com.fastchar.annotation.AFastPriority;

import java.io.File;

/**
 * 文件重命名接口
 */
@AFastPriority
public interface IFastFileRename  {
    /**
     * 重命名文件，并返回新的文件名
     * @param target 目标文件
     * @param md5Name 是否md5加密
     * @return 新的文件
     */
    File rename(File target, boolean md5Name);
}
