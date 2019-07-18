package com.fastchar.multipart;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
/**
 * from com.oreilly.servlet.multipart
 */
public class MultipartParser {
    private ServletInputStream in;
    private String boundary;
    private FilePart lastFilePart;
    private byte[] buf;
    private static String DEFAULT_ENCODING = "ISO-8859-1";
    private String encoding;

    public MultipartParser(HttpServletRequest req, int maxSize) throws IOException {
        this(req, maxSize, true, true);
    }

    public MultipartParser(HttpServletRequest req, int maxSize, boolean buffer, boolean limitLength) throws IOException {
        this(req, maxSize, buffer, limitLength, (String)null);
    }

    public MultipartParser(HttpServletRequest req, int maxSize, boolean buffer, boolean limitLength, String encoding) throws IOException {
        this.buf = new byte[8192];
        this.encoding = DEFAULT_ENCODING;
        if (encoding != null) {
            this.setEncoding(encoding);
        }

        String type = null;
        String type1 = req.getHeader("Content-Type");
        String type2 = req.getContentType();
        if (type1 == null && type2 != null) {
            type = type2;
        } else if (type2 == null && type1 != null) {
            type = type1;
        } else if (type1 != null && type2 != null) {
            type = type1.length() > type2.length() ? type1 : type2;
        }

        if (type != null && type.toLowerCase().startsWith("multipart/form-data")) {
            int length = req.getContentLength();
            if (length > maxSize) {
                throw new IOException("Posted content length of " + length + " exceeds limit of " + maxSize);
            } else {
                String boundary = this.extractBoundary(type);
                if (boundary == null) {
                    throw new IOException("Separation boundary was not specified");
                } else {
                    ServletInputStream in = req.getInputStream();
                    if (buffer) {
                        in = new BufferedServletInputStream((ServletInputStream)in);
                    }

                    if (limitLength && length > 0) {
                        in = new LimitedServletInputStream((ServletInputStream)in, length);
                    }

                    this.in = (ServletInputStream)in;
                    this.boundary = boundary;

                    String line;
                    do {
                        line = this.readLine();
                        if (line == null) {
                            throw new IOException("Corrupt form data: premature ending");
                        }
                    } while(!line.startsWith(boundary));

                }
            }
        } else {
            throw new IOException("Posted content type isn't multipart/form-data");
        }
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Part readNextPart() throws IOException {
        if (this.lastFilePart != null) {
            this.lastFilePart.getInputStream().close();
            this.lastFilePart = null;
        }

        Vector headers = new Vector();
        String line = this.readLine();
        if (line == null) {
            return null;
        } else if (line.length() == 0) {
            return null;
        } else {
            String name;
            while(line != null && line.length() > 0) {
                name = null;
                boolean getNextLine = true;

                while(true) {
                    while(getNextLine) {
                        name = this.readLine();
                        if (name != null && (name.startsWith(" ") || name.startsWith("\t"))) {
                            line = line + name;
                        } else {
                            getNextLine = false;
                        }
                    }

                    headers.addElement(line);
                    line = name;
                    break;
                }
            }

            if (line == null) {
                return null;
            } else {
                name = null;
                String filename = null;
                String origname = null;
                String contentType = "text/plain";
                Enumeration enu = headers.elements();

                while(enu.hasMoreElements()) {
                    String headerline = (String)enu.nextElement();
                    if (headerline.toLowerCase().startsWith("content-disposition:")) {
                        String[] dispInfo = this.extractDispositionInfo(headerline);
                        name = dispInfo[1];
                        filename = dispInfo[2];
                        origname = dispInfo[3];
                    } else if (headerline.toLowerCase().startsWith("content-type:")) {
                        String type = extractContentType(headerline);
                        if (type != null) {
                            contentType = type;
                        }
                    }
                }

                if (filename == null) {
                    return new ParamPart(name, this.in, this.boundary, this.encoding);
                } else {
                    if (filename.equals("")) {
                        filename = null;
                    }

                    this.lastFilePart = new FilePart(name, this.in, this.boundary, contentType, filename, origname);
                    return this.lastFilePart;
                }
            }
        }
    }

    private String extractBoundary(String line) {
        int index = line.lastIndexOf("boundary=");
        if (index == -1) {
            return null;
        } else {
            String boundary = line.substring(index + 9);
            if (boundary.charAt(0) == '"') {
                index = boundary.lastIndexOf(34);
                boundary = boundary.substring(1, index);
            }

            boundary = "--" + boundary;
            return boundary;
        }
    }

    private String[] extractDispositionInfo(String line) throws IOException {
        String[] retval = new String[4];
        String origline = line;
        line = line.toLowerCase();
        int start = line.indexOf("content-disposition: ");
        int end = line.indexOf(";");
        if (start != -1 && end != -1) {
            String disposition = line.substring(start + 21, end).trim();
            if (!disposition.equals("form-data")) {
                throw new IOException("Invalid content disposition: " + disposition);
            } else {
                start = line.indexOf("name=\"", end);
                end = line.indexOf("\"", start + 7);
                int startOffset = 6;
                if (start == -1 || end == -1) {
                    start = line.indexOf("name=", end);
                    end = line.indexOf(";", start + 6);
                    if (start == -1) {
                        throw new IOException("Content disposition corrupt: " + origline);
                    }

                    if (end == -1) {
                        end = line.length();
                    }

                    startOffset = 5;
                }

                String name = origline.substring(start + startOffset, end);
                String filename = null;
                String origname = null;
                start = line.indexOf("filename=\"", end + 2);
                end = line.indexOf("\"", start + 10);
                if (start != -1 && end != -1) {
                    filename = origline.substring(start + 10, end);
                    origname = filename;
                    int slash = Math.max(filename.lastIndexOf(47), filename.lastIndexOf(92));
                    if (slash > -1) {
                        filename = filename.substring(slash + 1);
                    }
                }

                retval[0] = disposition;
                retval[1] = name;
                retval[2] = filename;
                retval[3] = origname;
                return retval;
            }
        } else {
            throw new IOException("Content disposition corrupt: " + origline);
        }
    }

    private static String extractContentType(String line) throws IOException {
        line = line.toLowerCase();
        int end = line.indexOf(";");
        if (end == -1) {
            end = line.length();
        }

        return line.substring(13, end).trim();
    }

    private String readLine() throws IOException {
        StringBuffer sbuf = new StringBuffer();

        int result;
        do {
            result = this.in.readLine(this.buf, 0, this.buf.length);
            if (result != -1) {
                sbuf.append(new String(this.buf, 0, result, this.encoding));
            }
        } while(result == this.buf.length);

        if (sbuf.length() == 0) {
            return null;
        } else {
            int len = sbuf.length();
            if (len >= 2 && sbuf.charAt(len - 2) == '\r') {
                sbuf.setLength(len - 2);
            } else if (len >= 1 && sbuf.charAt(len - 1) == '\n') {
                sbuf.setLength(len - 1);
            }

            return sbuf.toString();
        }
    }
}
