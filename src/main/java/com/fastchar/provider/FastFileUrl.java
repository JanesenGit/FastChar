package com.fastchar.provider;

import com.fastchar.core.FastChar;
import com.fastchar.core.FastFile;
import com.fastchar.interfaces.IFastFileUrl;
import com.fastchar.utils.FastStringUtils;

public class FastFileUrl implements IFastFileUrl {
    @Override
    public String getFileUrl(FastFile fastFile) throws Exception {
        String replace = fastFile.getFile().getAbsolutePath().replace(FastChar.getPath().getWebRootPath(), "");
        return FastStringUtils.strip(replace, "/");
    }

}
