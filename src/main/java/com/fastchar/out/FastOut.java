package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.utils.FastStringUtils;

import java.lang.reflect.Field;
import java.util.Date;

public abstract class FastOut<T> {
    protected Object data;
    protected Date outTime;
    protected String contentType;
    protected String charset = FastChar.getConstant().getEncoding();
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
    public FastOut<?> setFieldValue(String fieldName, Object fieldValue) {
        try {
            Field declaredField = this.getClass().getDeclaredField(fieldName);
            if (declaredField == null) {
                return this;
            }
            declaredField.setAccessible(true);
            declaredField.set(this, fieldValue);
        } catch (Exception ignored) {
        }
        return this;
    }

    public String toContentType(FastAction action) {
        //通过参数强制设置contentType
        if (action.isParamNotEmpty("__accept")) {
            this.contentType = action.getParam("__accept");
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
        IMAGE
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
