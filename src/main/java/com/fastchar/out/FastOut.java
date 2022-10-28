package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastStringUtils;
import org.apache.catalina.connector.ClientAbortException;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Date;

public abstract class FastOut<T> {

    protected Object data;
    protected Date outTime;
    protected String contentType;
    protected String charset = FastChar.getConstant().getCharset();
    protected String description;
    protected int status = 200;
    private transient boolean logged;


    /**
     * 响应数据
     *
     * @param action 控制器
     */
    public abstract void response(FastAction action) throws Exception;

    /**
     * 设置子类自定义的字段值
     *
     * @param fieldName  字段名
     * @param fieldValue 字段值
     */
    public FastOut<?> setValue(String fieldName, Object fieldValue) {
        try {
            Field declaredField = this.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(this, fieldValue);
        } catch (Exception ignored) {
        }
        return this;
    }

    public String toContentType(FastAction action) {
        return toContentType(action, true);
    }
    public String toContentType(FastAction action,boolean checkAccept) {
        //通过参数强制设置contentType
        if (action.isParamNotEmpty(FastAction.PARAM_ACCPET) && checkAccept) {
            this.contentType = action.getParam(FastAction.PARAM_ACCPET);
        }
        if (FastStringUtils.isEmpty(contentType)) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(contentType);
        if (FastStringUtils.isNotEmpty(charset)) {
            if (!contentType.endsWith(";")) {
                stringBuilder.append(";");
            }
            stringBuilder.append("charset=");
            stringBuilder.append(charset);
        }
        return stringBuilder.toString();
    }


    public String getContentType() {
        return contentType;
    }

    public T setContentType(String contentType) {
        this.contentType = contentType;
        return (T) this;
    }

    public String getCharset() {
        return charset;
    }

    public T setCharset(String charset) {
        this.charset = charset;
        return (T) this;
    }

    public Object getData() {
        return data;
    }

    public T setData(Object data) {
        this.data = data;
        return (T) this;
    }

    public Date getOutTime() {
        if (outTime == null) {
            outTime = new Date();
        }
        return outTime;
    }

    public T setOutTime(Date outTime) {
        this.outTime = outTime;
        return (T) this;
    }

    public int getStatus() {
        return status;
    }

    public T setStatus(int status) {
        this.status = status;
        return (T) this;
    }

    public String getDescription() {
        return description;
    }

    public T setDescription(String description) {
        this.description = description;
        return (T) this;
    }

    protected void write(FastHttpServletResponse response, String content) throws IOException {
        try (OutputStreamWriter streamWriter = new OutputStreamWriter(response.getOutputStream(), charset)) {
            streamWriter.write(content);
            streamWriter.flush();
        } catch (ClientAbortException ignored) {
            //这个异常是由于客户端断开连接，可以忽略
        }
    }

    protected void write(FastHttpServletResponse response, InputStream inputStream, int inputSize) throws IOException {

        //此处禁止做编码处理，源输入流返回
        try (OutputStream streamWriter = response.getOutputStream()) {
            byte[] buffer = new byte[inputSize];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                streamWriter.write(buffer, 0, len);
            }
            streamWriter.flush();
        } catch (ClientAbortException ignored) {
            //这个异常是由于客户端断开连接，可以忽略
        } finally {
            FastFileUtils.closeQuietly(inputStream);
        }
    }

    protected void write(FastHttpServletResponse response, InputStream inputStream, int inputSize, long start, long end) {
        //此处禁止做编码处理，源输入流返回
        try (OutputStream streamWriter = response.getOutputStream()) {
            if (inputStream.skip(start) != start) {
                throw new RuntimeException("InputStream skip error");
            }

            byte[] buffer = new byte[inputSize];
            long position = start;
            for (int len; position <= end && (len = inputStream.read(buffer)) != -1; ) {
                if (position + len <= end) {
                    streamWriter.write(buffer, 0, len);
                    position += len;
                } else {
                    for (int i = 0; i < len && position <= end; i++) {
                        streamWriter.write(buffer[i]);
                        position++;
                    }
                }
            }
            streamWriter.flush();
        } catch (ClientAbortException ignored) {
            //这个异常是由于客户端断开连接，可以忽略
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FastFileUtils.closeQuietly(inputStream);
        }
    }

    protected PrintWriter getWriter(FastHttpServletResponse response) throws IOException {
        return new PrintWriter(new OutputStreamWriter(response.getOutputStream(), charset));
    }





    public enum Type {
        TEXT,
        JSON,
        JSP,
        HTML,
        FREEMARKER,
        THYMELEAF,
        VELOCITY,
        FILE,
        REDIRECT,
        FORWARD,
        XML,
        NOTNULL,
        STATUS,
        IMAGE,
        STREAM
    }

    public static Class<? extends FastOut<?>> convertType(FastOut.Type type) {
        if (type == Type.TEXT) {
            return FastOutText.class;
        }
        if (type == Type.JSON) {
            return FastOutJson.class;
        }
        if (type == Type.JSP) {
            return FastOutJsp.class;
        }
        if (type == Type.FREEMARKER) {
            return FastOutFreemarker.class;
        }
        if (type == Type.THYMELEAF) {
            return FastOutThymeleaf.class;
        }
        if (type == Type.VELOCITY) {
            return FastOutVelocity.class;
        }
        if (type == Type.HTML) {
            return FastOutHtml.class;
        }
        if (type == Type.FILE) {
            return FastOutFile.class;
        }
        if (type == Type.REDIRECT) {
            return FastOutRedirect.class;
        }
        if (type == Type.FORWARD) {
            return FastOutForward.class;
        }
        if (type == Type.XML) {
            return FastOutXml.class;
        }
        if (type == Type.NOTNULL) {
            return FastOutParamError.class;
        }
        if (type == Type.STATUS) {
            return FastOutStatus.class;
        }
        if (type == Type.IMAGE) {
            return FastOutImage.class;
        }
        if (type == Type.STREAM) {
            return FastOutStream.class;
        }
        return null;
    }

    ;

    public boolean isLogged() {
        return logged;
    }

    public FastOut<?> setLogged(boolean logged) {
        this.logged = logged;
        return this;
    }
}
