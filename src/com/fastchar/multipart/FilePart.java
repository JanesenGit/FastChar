package com.fastchar.multipart;

import javax.servlet.ServletInputStream;
import java.io.*;
/**
 * from com.oreilly.servlet.multipart
 */
public class FilePart extends Part {
    private String fileName;
    private String filePath;
    private String contentType;
    private PartInputStream partInput;
    private FileRenamePolicy policy;

    FilePart(String name, ServletInputStream in, String boundary, String contentType, String fileName, String filePath) throws IOException {
        super(name);
        this.fileName = fileName;
        this.filePath = filePath;
        this.contentType = contentType;
        this.partInput = new PartInputStream(in, boundary);
    }

    public void setRenamePolicy(FileRenamePolicy policy) {
        this.policy = policy;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getContentType() {
        return this.contentType;
    }

    public InputStream getInputStream() {
        return this.partInput;
    }

    public long writeTo(File fileOrDirectory) throws IOException {
        long written = 0L;
        BufferedOutputStream fileOut = null;

        try {
            if (this.fileName != null) {
                File file;
                if (fileOrDirectory.isDirectory()) {
                    file = new File(fileOrDirectory, this.fileName);
                } else {
                    file = fileOrDirectory;
                }

                if (this.policy != null) {
                    file = this.policy.rename(file);
                    this.fileName = file.getName();
                }

                fileOut = new BufferedOutputStream(new FileOutputStream(file));
                written = this.write(fileOut);
            }
        } finally {
            if (fileOut != null) {
                fileOut.close();
            }

        }

        return written;
    }

    public long writeTo(OutputStream out) throws IOException {
        long size = 0L;
        if (this.fileName != null) {
            size = this.write(out);
        }

        return size;
    }

    long write(OutputStream out) throws IOException {
        if (this.contentType.equals("application/x-macbinary")) {
            out = new MacBinaryDecoderOutputStream((OutputStream) out);
        }

        long size = 0L;

        int read;
        for (byte[] buf = new byte[8192]; (read = this.partInput.read(buf)) != -1; size += (long) read) {
            ((OutputStream) out).write(buf, 0, read);
        }

        return size;
    }

    public boolean isFile() {
        return true;
    }
}