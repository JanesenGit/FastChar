package com.fastchar.multipart;

import javax.servlet.ServletInputStream;
import java.io.IOException;
/**
 * from com.oreilly.servlet.multipart
 */
public class BufferedServletInputStream extends ServletInputStream {
    private ServletInputStream in;
    private byte[] buf = new byte[65536];
    private int count;
    private int pos;

    public BufferedServletInputStream(ServletInputStream in) {
        this.in = in;
    }

    private void fill() throws IOException {
        int i = this.in.read(this.buf, 0, this.buf.length);
        if (i > 0) {
            this.pos = 0;
            this.count = i;
        }

    }

    public int readLine(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        } else {
            int avail = this.count - this.pos;
            if (avail <= 0) {
                this.fill();
                avail = this.count - this.pos;
                if (avail <= 0) {
                    return -1;
                }
            }

            int copy = Math.min(len, avail);
            int eol = findeol(this.buf, this.pos, copy);
            if (eol != -1) {
                copy = eol;
            }

            System.arraycopy(this.buf, this.pos, b, off, copy);
            this.pos += copy;

            int total=0;
            for(total = total + copy; total < len && eol == -1; total += copy) {
                this.fill();
                avail = this.count - this.pos;
                if (avail <= 0) {
                    return total;
                }

                copy = Math.min(len - total, avail);
                eol = findeol(this.buf, this.pos, copy);
                if (eol != -1) {
                    copy = eol;
                }

                System.arraycopy(this.buf, this.pos, b, off + total, copy);
                this.pos += copy;
            }

            return total;
        }
    }

    private static int findeol(byte[] b, int pos, int len) {
        int end = pos + len;
        int i = pos;

        do {
            if (i >= end) {
                return -1;
            }
        } while(b[i++] != 10);

        return i - pos;
    }

    public int read() throws IOException {
        if (this.count <= this.pos) {
            this.fill();
            if (this.count <= this.pos) {
                return -1;
            }
        }

        return this.buf[this.pos++] & 255;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int total;
        int copy;
        for(total = 0; total < len; total += copy) {
            int avail = this.count - this.pos;
            if (avail <= 0) {
                this.fill();
                avail = this.count - this.pos;
                if (avail <= 0) {
                    if (total > 0) {
                        return total;
                    }

                    return -1;
                }
            }

            copy = Math.min(len - total, avail);
            System.arraycopy(this.buf, this.pos, b, off + total, copy);
            this.pos += copy;
        }

        return total;
    }
}
