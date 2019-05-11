package com.fastchar.multipart;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
/**
 * from com.oreilly.servlet.multipart
 */
public class MacBinaryDecoderOutputStream extends FilterOutputStream {
    private int bytesFiltered = 0;
    private int dataForkLength = 0;

    public MacBinaryDecoderOutputStream(OutputStream out) {
        super(out);
    }

    public void write(int b) throws IOException {
        if (this.bytesFiltered <= 86 && this.bytesFiltered >= 83) {
            int leftShift = (86 - this.bytesFiltered) * 8;
            this.dataForkLength |= (b & 255) << leftShift;
        } else if (this.bytesFiltered < 128 + this.dataForkLength && this.bytesFiltered >= 128) {
            this.out.write(b);
        }

        ++this.bytesFiltered;
    }

    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (this.bytesFiltered >= 128 + this.dataForkLength) {
            this.bytesFiltered += len;
        } else if (this.bytesFiltered >= 128 && this.bytesFiltered + len <= 128 + this.dataForkLength) {
            this.out.write(b, off, len);
            this.bytesFiltered += len;
        } else {
            for(int i = 0; i < len; ++i) {
                this.write(b[off + i]);
            }
        }

    }
}
