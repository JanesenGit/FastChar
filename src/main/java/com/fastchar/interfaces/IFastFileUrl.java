package com.fastchar.interfaces;

import com.fastchar.core.FastFile;

/**
 * 获取FastFile的网络访问地址接口
 */
public interface IFastFileUrl {
    /**
     * 获取FastFile网络访问地址
     * @param fastFile 文件对象
     * @return 网络地址
     * @throws Exception 异常
     */
    String getFileUrl(FastFile fastFile) throws Exception;
}
