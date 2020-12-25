package com.fastchar.provider;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastFileRename;

import java.io.File;
import java.io.IOException;

public class FastFileRename implements IFastFileRename {

    @Override
    public File rename(File f, boolean md5Name) {
        f = formatFileName(f, md5Name);
        if (this.createNewFile(f)) {
            return f;
        } else {
            String name = f.getName();
            String body;
            String ext;
            int dot = name.lastIndexOf(".");
            if (dot != -1) {
                body = name.substring(0, dot);
                ext = name.substring(dot);
            } else {
                body = name;
                ext = "";
            }


            String newName;
            for (int count = 0; !this.createNewFile(f) && count < Integer.MAX_VALUE; f = new File(f.getParent(), newName)) {
                ++count;
                newName = body + "(" + count + ")" + ext;
            }
            return f;
        }
    }


    private File formatFileName(File file, boolean md5Name) {
        if (md5Name) {
            String name = file.getName();
            String body;
            String ext;
            int dot = name.lastIndexOf(".");
            if (dot != -1) {
                body = name.substring(0, dot);
                ext = name.substring(dot);
            } else {
                body = name;
                ext = "";
            }
            return new File(file.getParent(), FastChar.getSecurity().MD5_Encrypt(body) + ext);
        }
        return file;
    }

    private boolean createNewFile(File f) {
        try {
            return f.createNewFile();
        } catch (IOException var3) {
            return false;
        }
    }

}
