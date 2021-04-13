package com.fastchar.core;

import com.fastchar.asm.FastParameter;
import com.fastchar.exception.FastFileException;
import com.fastchar.exception.FastReturnException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.multipart.FastMultipartWrapper;
import com.fastchar.out.*;
import com.fastchar.utils.*;
import org.w3c.dom.Document;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Request请求处理类，FastChar核心类
 *
 * @author 沈建（Janesen）
 */
@SuppressWarnings("all")
public abstract class FastAction {
    HttpServletRequest request;
    HttpServletResponse response;
    ServletContext servletContext;
    List<FastRequestParam> params = new ArrayList<>();
    volatile FastAction forwarder;
    volatile FastUrl fastUrl;
    volatile FastRoute fastRoute;
    volatile FastOut fastOut;
    FastCheck<FastAction> fastCheck = new FastCheck<FastAction>(this);

    private volatile boolean log = true;
    private volatile boolean logResponse = false;
    private volatile int status = 200;

    void release() {
        fastRoute = null;
        fastOut = null;
        params = null;
        fastCheck = null;
    }

    /**
     * 根据key获取一个对象锁
     *
     * @param key 唯一key
     * @return ReentrantLock
     */
    protected ReentrantLock getLock(String key) {
        return FastLockUtils.getLock(key);
    }

    /**
     * 删除一个对象锁
     *
     * @param key 唯一key
     */
    protected void removeLock(String key) {
        FastLockUtils.removeLock(key);
    }

    /**
     * 获得路由地址
     *
     * @return String
     */
    protected abstract String getRoute();


    /**
     * 执行请求
     */
    public final void invoke() {
        if (getFastRoute() != null) {
            getFastRoute().invoke();
        }
    }


    private boolean validParam(String paramName, Object value) {
        Object validate = fastCheck.validate(paramName, value);
        if (validate != null) {
            responseParamError(paramName, validate.toString());
            return false;
        }
        return true;
    }

    /**
     * 判断是否是multipart/form-data表单格式
     *
     * @return 布尔值
     */
    public boolean isMultipart() {
        if (request != null) {
            String type = null;
            String type1 = request.getHeader("Content-Type");
            String type2 = request.getContentType();
            if (type1 == null && type2 != null) {
                type = type2;
            } else if (type2 == null && type1 != null) {
                type = type1;
            } else if (type1 != null && type2 != null) {
                type = type1.length() > type2.length() ? type1 : type2;
            }

            if (type != null && type.toLowerCase().startsWith("multipart/form-data")) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获得项目的主路径地址，例如：http://localhost:8080/fastchar_test/
     *
     * @return 项目主路径
     */
    public String getProjectHost() {
        if (request != null) {
            try {
                URL url = new URL(getRequest().getRequestURL().toString());
                String projectHost;
                if (url.getPort() != -1) {
                    projectHost = url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "/" + FastChar.getConstant().getProjectName();
                } else {
                    projectHost = url.getProtocol() + "://" + url.getHost() + "/" + FastChar.getConstant().getProjectName();
                }
                return FastStringUtils.strip(projectHost, "/") + "/";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获得请求对象
     *
     * @return HttpServletRequest
     */
    public HttpServletRequest getRequest() {
        if (isMultipart()) {
            try {
                if (!(request instanceof FastMultipartWrapper)) {
                    request = new FastMultipartWrapper(request,
                            FastChar.getConstant().getAttachDirectory(),
                            FastChar.getConstant().getAttachMaxPostSize(),
                            FastChar.getConstant().getEncoding(),
                            FastChar.getConstant().isAttachNameMD5());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return request;
    }

    /**
     * 获取Web全局上下文
     *
     * @return ServletContext
     */
    public final ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * 判断参数是否为空
     *
     * @param paramName 参数名称
     * @return 布尔值
     */
    public boolean isParamEmpty(String paramName) {
        return FastStringUtils.isEmpty(getParam(paramName));
    }

    /**
     * 判断参数是否不为空
     *
     * @param paramName 参数名
     * @return 布尔值
     */
    public boolean isParamNotEmpty(String paramName) {
        return FastStringUtils.isNotEmpty(getParam(paramName));
    }

    /**
     * 判断参数是否为【空白】值
     *
     * @param paramName 参数名
     * @return 布尔值
     */
    public boolean isParamBlank(String paramName) {
        return FastStringUtils.isBlank(getParam(paramName));
    }

    /**
     * 判断参数是否不为【空白】值
     *
     * @param paramName 参数名
     * @return 布尔值
     */
    public boolean isParamNotBlank(String paramName) {
        return FastStringUtils.isNotBlank(getParam(paramName));
    }

    /**
     * 判断参数是否存在
     *
     * @param paramName 参数名
     * @return 布尔值
     */
    public boolean isParamExists(String paramName) {
        return getParamNames().contains(paramName);
    }

    /**
     * 添加请求的参数
     *
     * @param paramName  参数名
     * @param paramValue 参数值
     * @return 当前对象
     */
    public FastAction addParam(String paramName, String paramValue) {
        params.add(new FastRequestParam().setName(paramName).setValue(paramValue));
        return this;
    }

    /**
     * 设置请求的参数，将覆盖request中的参数
     *
     * @param paramName  参数名
     * @param paramValue 参数值
     * @return 当前对象
     */
    public FastAction setParam(String paramName, String paramValue) {
        List<FastRequestParam> waitRemove = new ArrayList<>();
        for (FastRequestParam param : params) {
            if (param.getName().equals(paramName)) {
                waitRemove.add(param);
            }
        }
        if (waitRemove.size() > 0) {
            params.removeAll(waitRemove);
        }
        params.add(new FastRequestParam().setName(paramName).setValue(paramValue).setDoSet(true));
        return this;
    }


    /**
     * 获取所有参数名称集合
     *
     * @return Set&lt;String&gt;
     */
    public Set<String> getParamNames() {
        LinkedHashSet<String> strings = new LinkedHashSet<>();
        Enumeration<String> parameterNames = getRequest().getParameterNames();
        while (parameterNames.hasMoreElements()) {
            strings.add(parameterNames.nextElement());
        }
        for (FastRequestParam param : params) {
            strings.add(param.getName());
        }
        strings.addAll(fastCheck.getParamNames());
        return strings;
    }


    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @return String
     */
    public String getParam(String paramName, String defaultValue) {
        return FastStringUtils.defaultValue(getParam(paramName), defaultValue);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @return String
     */
    public String getParam(String paramName) {
        return getParam(paramName, false);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return String
     */
    public String getParam(String paramName, boolean notNull) {
        if (notNull) {
            check(0, "@null:" + FastChar.getLocal().getInfo(FastCharLocal.PARAM_ERROR1));
        }
        for (FastRequestParam param : params) {
            if (param.getName().equals(paramName)) {
                if (validParam(paramName, param.getValue())) {
                    return param.getValue();
                }
            }
        }
        String value = getRequest().getParameter(paramName);
        if (validParam(paramName, value)) {
            return value;
        }
        return null;
    }


    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @return String[]
     */
    public String[] getParamToArray(String paramName) {
        return getParamToArray(paramName, false);
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return String[]
     */
    public String[] getParamToArray(String paramName, boolean notNull) {
        String[] parameterValues = getRequest().getParameterValues(paramName);
        List<String> arrays = new ArrayList<>();

        //2021-1-11 新增
        boolean breakRequestValue = false;
        for (FastRequestParam param : params) {
            if (param.getName().equals(paramName)) {
                arrays.add(param.getValue());
                breakRequestValue = param.isDoSet();
            }
        }

        if (parameterValues != null && !breakRequestValue) {
            arrays.addAll(Arrays.asList(parameterValues));
        }
        if (arrays.size() == 0 && notNull) {
            responseParamError(paramName, FastChar.getLocal().getInfo(FastCharLocal.PARAM_ERROR1, paramName));
        }
        String[] strings = arrays.toArray(new String[]{});
        if (validParam(paramName, strings)) {
            return strings;
        }
        return null;
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @return Integer[]
     */
    public Integer[] getParamToIntArray(String paramName) {
        return getParamToIntArray(paramName, false);
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return Integer[]
     */
    public Integer[] getParamToIntArray(String paramName, boolean notNull) {
        List<Integer> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            if (FastStringUtils.isEmpty(parameterValue)) {
                continue;
            }
            arrays.add(FastNumberUtils.formatToInt(parameterValue));
        }
        return arrays.toArray(new Integer[]{});
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @return Double[]
     */
    public Double[] getParamToDoubleArray(String paramName) {
        return getParamToDoubleArray(paramName, false);
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return Double[]
     */
    public Double[] getParamToDoubleArray(String paramName, boolean notNull) {
        List<Double> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            if (FastStringUtils.isEmpty(parameterValue)) {
                continue;
            }
            arrays.add(FastNumberUtils.formatToDouble(parameterValue));
        }
        return arrays.toArray(new Double[]{});
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @return Long[]
     */
    public Long[] getParamToLongArray(String paramName) {
        return getParamToLongArray(paramName, false);
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return Long[]
     */
    public Long[] getParamToLongArray(String paramName, boolean notNull) {
        List<Long> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            if (FastStringUtils.isEmpty(parameterValue)) {
                continue;
            }
            arrays.add(FastNumberUtils.formatToLong(parameterValue));
        }
        return arrays.toArray(new Long[]{});
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @return Float[]
     */
    public Float[] getParamToFloatArray(String paramName) {
        return getParamToFloatArray(paramName, false);
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return Float[]
     */
    public Float[] getParamToFloatArray(String paramName, boolean notNull) {
        List<Float> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            if (FastStringUtils.isEmpty(parameterValue)) {
                continue;
            }
            arrays.add(FastNumberUtils.formatToFloat(parameterValue));
        }
        return arrays.toArray(new Float[]{});
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @return Short[]
     */
    public Short[] getParamToShortArray(String paramName) {
        return getParamToShortArray(paramName, false);
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return Short[]
     */
    public Short[] getParamToShortArray(String paramName, boolean notNull) {
        List<Short> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            if (FastStringUtils.isEmpty(parameterValue)) {
                continue;
            }
            arrays.add(FastNumberUtils.formatToShort(parameterValue));
        }
        return arrays.toArray(new Short[]{});
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @return Boolean[]
     */
    public Boolean[] getParamToBooleanArray(String paramName) {
        return getParamToBooleanArray(paramName, false);
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return Boolean[]
     */
    public Boolean[] getParamToBooleanArray(String paramName, boolean notNull) {
        List<Boolean> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            if (FastStringUtils.isEmpty(parameterValue)) {
                continue;
            }
            arrays.add(FastBooleanUtils.formatToBoolean(parameterValue));
        }
        return arrays.toArray(new Boolean[]{});
    }

    /**
     * 获得参数数组，符合系统全局日期格式
     *
     * @param paramName 参数名
     * @return Date[]
     */
    public Date[] getParamToDateArray(String paramName) {
        return getParamToDateArray(paramName, false);
    }

    /**
     * 获得参数数组 ，符合系统全局日期格式
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return Date[]
     */
    public Date[] getParamToDateArray(String paramName, boolean notNull) {
        return getParamToDateArray(paramName, FastChar.getConstant().getDateFormat(), notNull);
    }

    /**
     * 获得参数数组，符合系统全局日期格式
     *
     * @param paramName  参数名
     * @param dateFormat 日期格式类型，例如：yyyy-MM-dd
     * @return Date[]
     */
    public Date[] getParamToDateArray(String paramName, String dateFormat) {
        return getParamToDateArray(paramName, dateFormat, false);
    }

    /**
     * 获得参数数组，符合系统全局日期格式
     *
     * @param paramName  参数名
     * @param dateFormat 日期格式类型，例如：yyyy-MM-dd
     * @param notNull    是否不为空
     * @return Date[]
     */
    public Date[] getParamToDateArray(String paramName, String dateFormat, boolean notNull) {
        List<Date> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            if (FastStringUtils.isEmpty(parameterValue)) {
                continue;
            }
            arrays.add(FastDateUtils.parse(parameterValue, dateFormat));
        }
        return arrays.toArray(new Date[]{});
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @param enumClass 枚举的类
     * @param <T>       继承Enum的类
     * @return T[]
     */
    public <T extends Enum> T[] getParamToEnumArray(String paramName, Class<T> enumClass) {
        return getParamToEnumArray(paramName, enumClass, false);
    }

    /**
     * 获得参数数组
     *
     * @param paramName 参数名
     * @param enumClass 枚举的类
     * @param notNull   是否不为空
     * @param <T>       继承Enum的类
     * @return T[]
     */
    public <T extends Enum> T[] getParamToEnumArray(String paramName, Class<T> enumClass, boolean notNull) {
        String[] parameterValues = getParamToArray(paramName, notNull);
        Object array = Array.newInstance(enumClass, parameterValues.length);
        for (int i = 0; i < parameterValues.length; i++) {
            String parameterValue = parameterValues[i];
            if (FastStringUtils.isEmpty(parameterValue)) {
                continue;
            }
            T e = FastEnumUtils.formatToEnum(enumClass, parameterValue);
            Array.set(array, i, e);
        }
        return (T[]) array;

    }

    /**
     * 获得参数集合
     *
     * @param prefix 参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @return List&lt;String&gt;
     */
    public List<String> getParamToList(String prefix) {
        return getParamToList(prefix, false);
    }

    /**
     * 获得参数集合
     *
     * @param prefix  参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @param notNull 是否不为空
     * @return List&lt;String&gt;
     */
    public List<String> getParamToList(String prefix, boolean notNull) {
        fastCheck.setHold(true);
        List<String> list = new ArrayList<>();
        Set<String> paramNames = getParamNames();
        for (String paramName : paramNames) {
            if (paramName.startsWith(prefix)) {
                String[] parameterValues = getParamToArray(paramName);
                if (parameterValues.length == 0) {
                    continue;
                }
                if (parameterValues.length > 1) {
                    list.addAll(Arrays.asList(parameterValues));
                } else {
                    list.add(parameterValues[0]);
                }
            }
        }
        fastCheck.rollback();
        if (list.size() == 0 && notNull) {
            responseParamError(prefix, FastChar.getLocal().getInfo(FastCharLocal.PARAM_ERROR1, prefix));
        }
        return list;
    }

    /**
     * 获得参数集合
     *
     * @param prefix 参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @return List&lt;Integer&gt;
     */
    public List<Integer> getParamToIntList(String prefix) {
        return getParamToIntList(prefix, false);
    }

    /**
     * 获得参数集合
     *
     * @param prefix  参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @param notNull 是否不为空
     * @return List&lt;Integer&gt;
     */
    public List<Integer> getParamToIntList(String prefix, boolean notNull) {
        List<Integer> list = new ArrayList<>();
        List<String> paramToList = getParamToList(prefix, notNull);
        for (String value : paramToList) {
            if (FastStringUtils.isEmpty(value)) {
                continue;
            }
            list.add(FastNumberUtils.formatToInt(value));
        }
        return list;
    }

    /**
     * 获得参数集合
     *
     * @param prefix 参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @return List&lt;Double&gt;
     */
    public List<Double> getParamToDoubleList(String prefix) {
        return getParamToDoubleList(prefix, false);
    }

    /**
     * 获得参数集合
     *
     * @param prefix  参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @param notNull 是否不为空
     * @return List&lt;Double&gt;
     */
    public List<Double> getParamToDoubleList(String prefix, boolean notNull) {
        List<Double> list = new ArrayList<>();
        List<String> paramToList = getParamToList(prefix, notNull);
        for (String value : paramToList) {
            if (FastStringUtils.isEmpty(value)) {
                continue;
            }
            list.add(FastNumberUtils.formatToDouble(value));
        }
        return list;
    }

    /**
     * 获得参数集合
     *
     * @param prefix 参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @return List&lt;Float&gt;
     */
    public List<Float> getParamToFloatList(String prefix) {
        return getParamToFloatList(prefix, false);
    }

    /**
     * 获得参数集合
     *
     * @param prefix  参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @param notNull 是否不为空
     * @return List&lt;Float&gt;
     */
    public List<Float> getParamToFloatList(String prefix, boolean notNull) {
        List<Float> list = new ArrayList<>();
        List<String> paramToList = getParamToList(prefix, notNull);
        for (String value : paramToList) {
            if (FastStringUtils.isEmpty(value)) {
                continue;
            }
            list.add(FastNumberUtils.formatToFloat(value));
        }
        return list;
    }

    /**
     * 获得参数集合
     *
     * @param prefix 参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @return List&lt;Long&gt;
     */
    public List<Long> getParamToLongList(String prefix) {
        return getParamToLongList(prefix, false);
    }

    /**
     * 获得参数集合
     *
     * @param prefix  参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @param notNull 是否不为空
     * @return List&lt;Long&gt;
     */
    public List<Long> getParamToLongList(String prefix, boolean notNull) {
        List<Long> list = new ArrayList<>();
        List<String> paramToList = getParamToList(prefix, notNull);
        for (String value : paramToList) {
            if (FastStringUtils.isEmpty(value)) {
                continue;
            }
            list.add(FastNumberUtils.formatToLong(value));
        }
        return list;
    }

    /**
     * 获得参数集合
     *
     * @param prefix 参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @return List&lt;Short&gt;
     */
    public List<Short> getParamToShortList(String prefix) {
        return getParamToShortList(prefix, false);
    }

    /**
     * 获得参数集合
     *
     * @param prefix  参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @param notNull 是否不为空
     * @return List&lt;Short&gt;
     */
    public List<Short> getParamToShortList(String prefix, boolean notNull) {
        List<Short> list = new ArrayList<>();
        List<String> paramToList = getParamToList(prefix, notNull);
        for (String value : paramToList) {
            if (FastStringUtils.isEmpty(value)) {
                continue;
            }
            list.add(FastNumberUtils.formatToShort(value));
        }
        return list;
    }

    /**
     * 获得参数集合
     *
     * @param prefix    参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @param enumClass 枚举的类
     * @param <T>       继承Enum的类
     * @return List&lt;T&gt;
     */
    public <T extends Enum> List<T> getParamToEnumList(String prefix, Class<T> enumClass) {
        return getParamToEnumList(prefix, enumClass, false);
    }

    /**
     * 获得参数集合
     *
     * @param prefix    参数前缀，例如参数为：map.userId 那么参数前缀为：map
     * @param enumClass 枚举的类
     * @param notNull   是否不为空
     * @param <T>       继承Enum的泛型类
     * @return List&lt;T&gt;
     */
    public <T extends Enum> List<T> getParamToEnumList(String prefix, Class<T> enumClass, boolean notNull) {
        List<T> list = new ArrayList<>();

        List<String> paramToList = getParamToList(prefix, notNull);
        Object array = Array.newInstance(enumClass, paramToList.size());
        for (int i = 0; i < paramToList.size(); i++) {
            String value = paramToList.get(i);
            if (FastStringUtils.isEmpty(value)) {
                continue;
            }
            T e = FastEnumUtils.formatToEnum(enumClass, value);
            Array.set(array, i, e);
        }
        list.addAll(Arrays.asList((T[]) array));
        return list;
    }


    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @return int
     */
    public int getParamToInt(String paramName) {
        return FastNumberUtils.formatToInt(getParam(paramName));
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return int
     */
    public int getParamToInt(String paramName, boolean notNull) {
        return FastNumberUtils.formatToInt(getParam(paramName, notNull));
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @return int
     */
    public int getParamToInt(String paramName, int defaultValue) {
        return FastNumberUtils.formatToInt(getParam(paramName), defaultValue);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @return short
     */
    public short getParamToShort(String paramName) {
        return FastNumberUtils.formatToShort(getParam(paramName));
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return short
     */
    public short getParamToShort(String paramName, boolean notNull) {
        return FastNumberUtils.formatToShort(getParam(paramName, notNull));
    }


    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @return int
     */
    public short getParamToShort(String paramName, short defaultValue) {
        return FastNumberUtils.formatToShort(getParam(paramName), defaultValue);
    }


    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @return double
     */
    public double getParamToDouble(String paramName) {
        return FastNumberUtils.formatToDouble(getParam(paramName));
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return double
     */
    public double getParamToDouble(String paramName, boolean notNull) {
        return FastNumberUtils.formatToDouble(getParam(paramName, notNull));
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @return double
     */
    public double getParamToDouble(String paramName, double defaultValue) {
        return FastNumberUtils.formatToDouble(getParam(paramName), defaultValue);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param digit     精度，例如传2 表示保留2位小数
     * @return double
     */
    public double getParamToDouble(String paramName, int digit) {
        return FastNumberUtils.formatToDouble(getParam(paramName), digit);
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @param digit        精度，例如传2 表示保留2位小数
     * @return double
     */
    public double getParamToDouble(String paramName, double defaultValue, int digit) {
        return FastNumberUtils.formatToDouble(getParam(paramName), defaultValue, digit);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @return float
     */
    public float getParamToFloat(String paramName) {
        return FastNumberUtils.formatToFloat(getParam(paramName));
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return float
     */
    public float getParamToFloat(String paramName, boolean notNull) {
        return FastNumberUtils.formatToFloat(getParam(paramName, notNull));
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @return float
     */
    public float getParamToFloat(String paramName, float defaultValue) {
        return FastNumberUtils.formatToFloat(getParam(paramName), defaultValue);
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @param digit        精度，例如传2 表示保留2位小数
     * @return float
     */
    public float getParamToFloat(String paramName, float defaultValue, int digit) {
        return FastNumberUtils.formatToFloat(getParam(paramName), defaultValue, digit);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param digit     精度，例如传2 表示保留2位小数
     * @return double
     */
    public float getParamToFloat(String paramName, int digit) {
        return FastNumberUtils.formatToFloat(getParam(paramName), digit);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @return boolean
     */
    public boolean getParamToBoolean(String paramName) {
        return FastBooleanUtils.formatToBoolean(getParam(paramName));
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return boolean
     */
    public boolean getParamToBoolean(String paramName, boolean notNull) {
        return FastBooleanUtils.formatToBoolean(getParam(paramName, notNull));
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @return boolean
     */
    public boolean getParamToBoolean(String paramName, Boolean defaultValue) {
        return FastBooleanUtils.formatToBoolean(getParam(paramName), defaultValue);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @return long
     */
    public long getParamToLong(String paramName) {
        return FastNumberUtils.formatToLong(getParam(paramName));
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return long
     */
    public long getParamToLong(String paramName, boolean notNull) {
        return FastNumberUtils.formatToLong(getParam(paramName, notNull));
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @return long
     */
    public long getParamToLong(String paramName, long defaultValue) {
        return FastNumberUtils.formatToLong(getParam(paramName), defaultValue);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @return Date
     */
    public Date getParamToDate(String paramName) {
        return getParamToDate(paramName, FastChar.getConstant().getDateFormat());
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param notNull   是否不为空
     * @return Date
     */
    public Date getParamToDate(String paramName, boolean notNull) {
        return getParamToDate(paramName, FastChar.getConstant().getDateFormat(), notNull);
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param defaultValue 默认值
     * @return Date
     */
    public Date getParamToDate(String paramName, Date defaultValue) {
        return getParamToDate(paramName, FastChar.getConstant().getDateFormat(), defaultValue);
    }

    /**
     * 获得参数
     *
     * @param paramName  参数名
     * @param dateFormat 日期格式，例如：yyyy-MM-dd
     * @return Date
     */
    public Date getParamToDate(String paramName, String dateFormat) {
        return getParamToDate(paramName, dateFormat, null);
    }

    /**
     * 获得参数
     *
     * @param paramName  参数名
     * @param dateFormat 日期格式，例如：yyyy-MM-dd
     * @param notNull    是否不为空
     * @return Date
     */
    public Date getParamToDate(String paramName, String dateFormat, boolean notNull) {
        return FastDateUtils.parse(getParam(paramName, notNull), dateFormat, null);
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param dateFormat   日期格式，例如：yyyy-MM-dd
     * @param defaultValue 默认值
     * @return Date
     */
    public Date getParamToDate(String paramName, String dateFormat, Date defaultValue) {
        return FastDateUtils.parse(getParam(paramName), dateFormat, defaultValue);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param enumClass 枚举的类
     * @param <T>       继承Enum的类
     * @return T
     */
    public <T extends Enum> T getParamToEnum(String paramName, Class<? extends Enum> enumClass) {
        return getParamToEnum(paramName, enumClass, null);
    }

    /**
     * 获得参数
     *
     * @param paramName 参数名
     * @param enumClass 枚举的类
     * @param notNull   是否不为空
     * @param <T>       继承Enum的类
     * @return T
     */
    public <T extends Enum> T getParamToEnum(String paramName, Class<? extends Enum> enumClass, boolean notNull) {
        return getParamToEnum(paramName, enumClass, null, notNull);
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param enumClass    枚举的类
     * @param defaultValue 默认值
     * @param <T>          继承Enum的类
     * @return T
     */
    public <T extends Enum> T getParamToEnum(String paramName, Class<? extends Enum> enumClass, Enum defaultValue) {
        return getParamToEnum(paramName, enumClass, defaultValue, false);
    }

    /**
     * 获得参数
     *
     * @param paramName    参数名
     * @param enumClass    枚举的类
     * @param defaultValue 默认值
     * @param notNull      是否不为空
     * @param <T>          继承Enum的类
     * @return T
     */
    private <T extends Enum> T getParamToEnum(String paramName, Class<? extends Enum> enumClass, Enum defaultValue, boolean notNull) {
        String param = getParam(paramName, notNull);
        if (FastStringUtils.isEmpty(param)) {
            return null;
        }
        return (T) FastEnumUtils.formatToEnum(enumClass, param, defaultValue);
    }

    /**
     * 获得参数的map对象
     *
     * @return Map&lt;String, Object&gt;
     */
    public Map<String, Object> getParamToMap() {
        fastCheck.setHold(true);
        Map<String, Object> mapParam = new HashMap<>();
        Set<String> paramNames = getParamNames();
        for (String paramName : paramNames) {
            String[] parameterValues = getParamToArray(paramName);
            if (parameterValues.length == 0) {
                continue;
            }
            if (parameterValues.length > 1) {
                mapParam.put(paramName, parameterValues);
            } else {
                mapParam.put(paramName, parameterValues[0]);
            }
        }
        fastCheck.rollback();
        return mapParam;
    }

    /**
     * 获得参数的map对象
     *
     * @param prefix 参数前缀，例如参数名：where.attr或where['attr']  前缀都为：where
     * @return Map&lt;String, Object&gt;
     */
    public Map<String, Object> getParamToMap(String prefix) {
        return getParamToMap(prefix, false);
    }

    /**
     * 获得参数的map对象
     *
     * @param prefix  参数前缀，例如参数名：where.attr或where['attr']  前缀都为：where
     * @param notNull 是否不为空
     * @return Map&lt;String, Object&gt;
     */
    public Map<String, Object> getParamToMap(String prefix, boolean notNull) {
        fastCheck.setHold(true);
        Map<String, Object> mapParam = new HashMap<>();

        //2020-3-13 新增
        mapParam.put("^prefix", prefix);

        Set<String> paramNames = getParamNames();

        for (String paramName : paramNames) {
            if (paramName.startsWith(prefix)) {
                String attr = getParamNameAttr(paramName);
                String[] parameterValues = getParamToArray(paramName);
                if (parameterValues.length == 0) {
                    continue;
                }
                if (parameterValues.length > 1) {
                    mapParam.put(attr, parameterValues);
                } else {
                    mapParam.put(attr, parameterValues[0]);
                }
            }
        }
        fastCheck.rollback();
        if (notNull && mapParam.size() == 0) {
            responseParamError(prefix, FastChar.getLocal().getInfo(FastCharLocal.PARAM_ERROR1, prefix));
        }
        return mapParam;
    }


    /**
     * 获得参数的map集合
     *
     * @param prefix 参数前缀，参数前缀，例如参数名：where[i].name或where[i]['name']  前缀都为：where，其中 i 是可变的数字
     * @return List&lt;Map&lt;String, Object&gt;&gt;
     */
    public List<Map<String, Object>> getParamToMapList(String prefix) {
        return getParamToMapList(prefix, false);
    }

    /**
     * 获得参数的map集合
     *
     * @param prefix  参数前缀，参数前缀，例如参数名：where[i].name或where[i]['name']  前缀都为：where，其中 i 是可变的数字
     * @param notNull 是否不为空
     * @return List&lt;Map&lt;String, Object&gt;&gt;
     */
    public List<Map<String, Object>> getParamToMapList(String prefix, boolean notNull) {
        fastCheck.setHold(true);
        List<Map<String, Object>> mapList = new ArrayList<>();
        Set<String> mapNames = new LinkedHashSet<>();
        Set<String> paramNames = getParamNames();
        for (String paramName : paramNames) {
            if (paramName.startsWith(prefix)) {
                String namePrefix = getParamNamePrefix(paramName);
                mapNames.add(namePrefix);
            }
        }
        for (String mapName : mapNames) {
            Map<String, Object> paramToMap = getParamToMap(mapName);
            if (paramToMap.size() == 0) {
                continue;
            }
            mapList.add(paramToMap);
        }
        if (notNull && mapList.size() == 0) {
            responseParamError(prefix, FastChar.getLocal().getInfo(FastCharLocal.PARAM_ERROR1, prefix));
        }
        fastCheck.rollback();
        return mapList;
    }

    /**
     * 获得entity实体
     *
     * @param prefix      参数前缀，例如参数名：where.attr或where['attr']  前缀都为：where
     * @param targetClass 实体类
     * @param <T>         继承FastEntity的类
     * @return T
     */
    public <T extends FastEntity<?>> T getParamToEntity(String prefix, Class<T> targetClass) {
        return getParamToEntity(prefix, targetClass, false);
    }


    /**
     * 获得entity实体
     *
     * @param prefix      参数前缀，例如参数名：where.attr或where['attr']  前缀都为：where
     * @param targetClass 实体类
     * @param notNull     是否不为空
     * @param <T>         继承FastEntity的类
     * @return T
     */
    public <T extends FastEntity<?>> T getParamToEntity(String prefix, Class<T> targetClass,
                                                        boolean notNull) {
        FastEntity fastEntity = FastChar.getOverrides().newInstance(targetClass);
        if (fastEntity == null) {
            return null;
        }
        Map<String, Object> paramToMap = getParamToMap(prefix, notNull);
        for (String key : paramToMap.keySet()) {
            fastEntity.set(key, paramToMap.get(key));
        }
        if (fastEntity.size() == 0) {
            return null;
        }
        return (T) fastEntity;
    }


    /**
     * 获得entity实体集合
     *
     * @param prefix      参数前缀，例如参数名：where[i].name或where[i]['name']  前缀都为：where，其中 i 是可变的数字
     * @param targetClass 实体类
     * @param <T>         继承FastEntity的类
     * @return List&lt;T&gt;
     */
    public <T extends FastEntity<?>> List<T> getParamToEntityList(String prefix, Class<T> targetClass) {
        return getParamToEntityList(prefix, targetClass, true);
    }

    /**
     * 获得entity实体集合
     *
     * @param prefix      参数前缀，例如参数名：where[i].name或where[i]['name']  前缀都为：where，其中 i 是可变的数字
     * @param targetClass 实体类
     * @param notNull     是否不为空
     * @param <T>         继承FastEntity的类
     * @return List&lt;T&gt;
     */
    public <T extends FastEntity<?>> List<T> getParamToEntityList(String prefix, Class<T> targetClass, boolean notNull) {
        List<T> list = new ArrayList<>();
        List<Map<String, Object>> paramToMapList = getParamToMapList(prefix, notNull);
        for (Map<String, Object> paramToMap : paramToMapList) {
            if (paramToMap.size() == 0) {
                continue;
            }
            FastEntity fastEntity = FastChar.getOverrides().newInstance(targetClass);
            if (fastEntity == null) {
                return null;
            }
            for (String key : paramToMap.keySet()) {
                fastEntity.set(key, paramToMap.get(key));
            }
            if (fastEntity.size() == 0) {
                continue;
            }
            list.add((T) fastEntity);
        }
        return list;
    }


    /**
     * 获得entity实体数组
     *
     * @param prefix      参数前缀，例如参数名：where[i].name或where[i]['name']  前缀都为：where，其中 i 是可变的数字
     * @param targetClass 实体类
     * @param <T>         继承FastEntity的类
     * @return T[]
     */
    public <T extends FastEntity<?>> T[] getParamToEntityArray(String prefix, Class<T> targetClass) {
        return getParamToEntityArray(prefix, targetClass, true);
    }

    /**
     * 获得entity实体数组
     *
     * @param prefix      参数前缀，例如参数名：where[i].name或where[i]['name']  前缀都为：where，其中 i 是可变的数字
     * @param targetClass 实体类
     * @param notNull     是否不为空
     * @param <T>         继承FastEntity的类
     * @return T[]
     */
    public <T extends FastEntity<?>> T[] getParamToEntityArray(String prefix, Class<T> targetClass, boolean notNull) {
        List<T> paramToEntityList = getParamToEntityList(prefix, targetClass, notNull);
        Object array = Array.newInstance(targetClass, paramToEntityList.size());
        for (int i = 0; i < paramToEntityList.size(); i++) {
            Array.set(array, i, paramToEntityList.get(i));
        }
        return (T[]) array;
    }

    /**
     * 获得任意类型的参数，触发IFastParamConverter转换器进行转换
     *
     * @param paramName   参数名
     * @param targetClass 目标类型
     * @param <T>         泛型类
     * @return T
     * @throws Exception 抛出转换异常
     */
    public <T> T getParamToClass(String paramName, Class<T> targetClass) throws Exception {
        return getParamToClass(paramName, targetClass, null);
    }

    /**
     * 获得任意类型的参数，触发IFastParamConverter转换器进行转换
     *
     * @param paramName         参数名
     * @param targetClass       目标类型
     * @param parameterizedType 目标类型
     * @param <T>               泛型类
     * @return T
     * @throws Exception 抛出转换异常
     */
    public <T> T getParamToClass(String paramName, Class<T> targetClass, Type parameterizedType) throws Exception {
        Object value = FastChar.getConverters().convertParam(this, new FastParameter().setType(targetClass).setParameterizedType(parameterizedType));
        if (value == null) {
            return null;
        }
        return (T) value;
    }


    /**
     * 添加参数文件对象
     *
     * @param fastFile 文件
     * @return 当前对象
     */
    public <T extends FastFile<?>> FastAction addParamFile(T fastFile) {
        HttpServletRequest request = getRequest();
        if (request instanceof FastMultipartWrapper) {
            FastMultipartWrapper multipartWrapper = (FastMultipartWrapper) request;
            multipartWrapper.putFile(fastFile.getParamName(), fastFile);
        }
        return this;
    }

    /**
     * 删除当前request下的所有本地附件
     */
    public void deleteAllParamFiles() {
        List<FastFile<?>> paramListFile = getParamListFile();
        if (paramListFile != null) {
            for (FastFile fastFile : paramListFile) {
                fastFile.getFile().delete();
            }
        }
    }

    /**
     * 获得上传的附件
     *
     * @return FastFile
     */
    public <T extends FastFile<?>> T getParamFile() {
        List<FastFile<?>> paramToListFile = getParamListFile();
        if (paramToListFile != null && paramToListFile.size() > 0) {
            return (T) paramToListFile.get(0);
        }
        return null;
    }

    /**
     * 获得上传的附件
     *
     * @param paramName 参数名
     * @return FastFile
     */
    public <T extends FastFile<?>> T getParamFile(String paramName) {
        HttpServletRequest request = getRequest();
        if (request instanceof FastMultipartWrapper) {
            FastMultipartWrapper multipartWrapper = (FastMultipartWrapper) request;
            return (T) multipartWrapper.getFile(paramName);
        }
        return null;
    }

    /**
     * 获得上传的附件数组
     *
     * @param paramName 参数名
     * @return List&lt;FastFile&lt;?&gt;&gt;
     */
    public <T extends FastFile<?>> List<T> getParamFileList(String paramName) {
        FastFile<?>[] paramFiles = getParamFiles(paramName);
        if (paramFiles != null) {
            return (List<T>) Arrays.asList(paramFiles);
        }
        return null;
    }

    /**
     * 获得上传的附件数组
     *
     * @param paramName 参数名
     * @return FastFile[]
     */
    public <T extends FastFile<?>> T[] getParamFiles(String paramName) {
        HttpServletRequest request = getRequest();
        if (request instanceof FastMultipartWrapper) {
            FastMultipartWrapper multipartWrapper = (FastMultipartWrapper) request;
            return (T[]) multipartWrapper.getFiles(paramName);
        }
        return null;
    }

    /**
     * 获得上传的附件
     *
     * @param paramName       参数名
     * @param moveToDirectory 保存到指定的目录下
     * @return FastFile
     * @throws FastFileException 抛出文件异常
     */
    public <T extends FastFile<?>> T getParamFile(String paramName, String moveToDirectory) throws FastFileException, IOException {
        FastFile paramToFile = getParamFile(paramName);
        if (paramToFile != null) {
            return (T) paramToFile.moveFile(moveToDirectory);
        }
        return null;
    }

    /**
     * 获得上传的附件数组
     *
     * @param paramName 参数名
     * @return List&lt;FastFile&lt;?&gt;&gt;
     */
    public <T extends FastFile<?>> List<T> getParamFileList(String paramName, String moveToDirectory) throws FastFileException, IOException {
        FastFile<?>[] paramFiles = getParamFiles(paramName, moveToDirectory);
        if (paramFiles != null) {
            return (List<T>) Arrays.asList(paramFiles);
        }
        return null;
    }

    /**
     * 获得上传的附件
     *
     * @param paramName       参数名
     * @param moveToDirectory 保存到指定的目录下
     * @return FastFile
     * @throws FastFileException 抛出文件异常
     */
    public <T extends FastFile<?>> T[] getParamFiles(String paramName, String moveToDirectory) throws FastFileException, IOException {
        FastFile<?>[] paramToFiles = getParamFiles(paramName);
        if (paramToFiles != null && FastStringUtils.isNotEmpty(moveToDirectory)) {
            for (FastFile<?> paramToFile : paramToFiles) {
                paramToFile = paramToFile.moveFile(moveToDirectory);
            }
        }
        return (T[]) paramToFiles;
    }


    /**
     * 获得上传的附件集合
     *
     * @return List&lt;FastFile&lt;?&gt;&gt;
     */
    public <T extends FastFile<?>> List<T> getParamListFile() {
        HttpServletRequest request = getRequest();
        if (request instanceof FastMultipartWrapper) {
            FastMultipartWrapper multipartWrapper = (FastMultipartWrapper) request;
            return (List<T>) multipartWrapper.getFiles();
        }
        return new ArrayList<>();
    }


    private String getParamNameAttr(String paramName) {
        //where['attr']  where[0]['attr']
        String regStr = "(.*)\\[(['\"])(.*)\\2\\]";
        Matcher matcher = Pattern.compile(regStr).matcher(paramName);
        if (matcher.find()) {
            return matcher.group(3);
        }
        //where.attr  where[0].attr
        if (paramName.indexOf(".") > 0) {
            String[] split = paramName.split("\\.");
            return split[split.length - 1];
        }
        return paramName;
    }

    private String getParamNamePrefix(String paramName) {
        //where['attr']  where[0]['attr']   前缀：where   where[0]
        String regStr = "(.*)\\[(['\"])(.*)\\2\\]";
        Matcher matcher = Pattern.compile(regStr).matcher(paramName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        //where.attr  where[0].attr    前缀：where   where[0]
        return paramName.split("\\.")[0];
    }

    /**
     * 获得提交的xml数据
     *
     * @return Document
     * @throws Exception 抛出异常
     */
    public Document getParamDataToXml() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(getRequest().getInputStream());
        return doc;
    }

    /**
     * 获得提交的string数据
     *
     * @return String
     * @throws Exception 抛出异常
     */
    public String getParamDataToString() throws Exception {
        return FastIOUtils.toString(getRequest().getInputStream(), getRequest().getCharacterEncoding());
    }

    /**
     * 获得提交的string数据
     *
     * @param encoding 编码格式
     * @return String
     * @throws Exception 抛出异常
     */
    public String getParamDataToString(String encoding) throws Exception {
        return FastIOUtils.toString(getRequest().getInputStream(), encoding);
    }


    /**
     * 响应数据
     *
     * @param out 响应对象
     */
    public void response(FastOut out) {
        fastCheck.rollback();
        this.fastOut = out;
        this.fastRoute.response();
        throw new FastReturnException();
    }

    /**
     * 响应Http状态码
     *
     * @param status 状态码
     */
    public void responseStatus(int status) {
        response(FastChar.getOverrides().newInstance(FastOutStatus.class).setData(status));
    }

    /**
     * 响应404界面
     *
     * @param message 404的消息提醒
     */
    public void response404(String message) {
        response(FastChar.getOverrides().newInstance(FastOutError.class).setStatus(404).setDescription(message));
    }

    /**
     * 响应500界面
     *
     * @param throwable 异常信息
     */
    public void response500(Throwable throwable) {
        StringBuilder stringBuilder = new StringBuilder(throwable.toString());
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stringBuilder.append("\n\tat ").append(stackTraceElement);
        }
        response(FastChar.getOverrides().newInstance(FastOutError.class).setStatus(500)
                .setDescription(throwable.toString())
                .setData(stringBuilder.toString()));
    }

    /**
     * 响应502界面
     *
     * @param message 502的消息提醒
     */
    public void response502(String message) {
        response(FastChar.getOverrides().newInstance(FastOutError.class).setStatus(502).setDescription(message));
    }


    /**
     * 响应json数据
     *
     * @param data 数据
     */
    public void responseJson(Object data) {
        response(FastChar.getOverrides().newInstance(FastOutJson.class).setData(data).setStatus(this.status));
    }

    /**
     * 响应json数据
     *
     * @param jsonFile json文件
     */
    public void responseJson(File jsonFile) {
        response(FastChar.getOverrides().newInstance(FastOutJson.class).setData(jsonFile).setStatus(this.status));
    }


    /**
     * 响应指定格式的json数据，格式为：{code: *,success: *,message:*,data:*}
     *
     * @param code    错误码 0 为正常
     * @param message 消息提示
     * @param data    响应的数据
     */
    public void responseJson(int code, String message, Object... data) {
        Map<String, Object> json = new HashMap<>();
        json.put("code", code);
        json.put("success", code == 0);
        json.put("message", message);
        for (int i = 0; i < data.length; i++) {
            if (i == 0) {
                json.put("data", data[i]);
            } else {
                json.put("data_" + i, data[i]);
            }
        }
        responseJson(json);
    }


    /**
     * 响应文本
     *
     * @param data 数据
     */
    public void responseText(Object data) {
        response(FastChar.getOverrides().newInstance(FastOutText.class).setData(data).setStatus(this.status));
    }

    /**
     * 响应文本
     *
     * @param status 响应状态码
     * @param data   数据
     */
    public void responseText(int status, Object data) {
        response(FastChar.getOverrides().newInstance(FastOutText.class).setData(data).setStatus(status));
    }

    /**
     * 响应文本
     *
     * @param data        数据
     * @param contentType 响应格式，例如：image/jpeg
     */
    public void responseText(Object data, String contentType) {
        response(FastChar.getOverrides().newInstance(FastOutText.class).setData(data).setContentType(contentType).setStatus(this.status));
    }

    /**
     * 响应文本
     *
     * @param status      状态码
     * @param data        数据
     * @param contentType 响应格式，例如：image/jpeg
     */
    public void responseText(int status, Object data, String contentType) {
        response(FastChar.getOverrides().newInstance(FastOutText.class).setData(data).setContentType(contentType).setStatus(status));
    }

    /**
     * 响应xml
     *
     * @param data 数据
     */
    public void responseXml(Object data) {
        response(FastChar.getOverrides().newInstance(FastOutXml.class).setData(data).setStatus(this.status));
    }

    /**
     * 响应xml
     *
     * @param xmlFile xml文件
     */
    public void responseXml(File xmlFile) {
        response(FastChar.getOverrides().newInstance(FastOutXml.class).setData(xmlFile).setStatus(this.status));
    }

    /**
     * 响应参数错误
     *
     * @param paramName 参数名
     */
    public void responseParamError(String paramName) {
        response(FastChar.getOverrides().newInstance(FastOutParamError.class).setData(paramName).setStatus(this.status));
    }

    /**
     * 响应参数错误
     *
     * @param paramName 参数名
     * @param message   错误消息
     */
    public void responseParamError(String paramName, String message) {
        response(FastChar.getOverrides().newInstance(FastOutParamError.class)
                .setMessage(message).setData(paramName).setStatus(this.status));
    }


    /**
     * 响应网页
     *
     * @param html 网页内容
     */
    public void responseHtml(Object html) {
        response(FastChar.getOverrides().newInstance(FastOutHtml.class).setData(html).setStatus(this.status));
    }


    /**
     * 响应文件
     *
     * @param file 文件
     */
    public void responseFile(File file) {
        responseFile(file.getAbsolutePath());
    }

    /**
     * 响应文件
     *
     * @param file     文件
     * @param fileName 文件名，一般用作下载时浏览器保存的文件名
     */
    public void responseFile(File file, String fileName) {
        responseFile(file.getAbsolutePath(), fileName);
    }

    /**
     * 响应文件
     *
     * @param file        文件
     * @param disposition 是否提示浏览器下载
     */
    public void responseFile(File file, boolean disposition) {
        responseFile(file.getAbsolutePath(), null, disposition);
    }

    /**
     * 响应文件
     *
     * @param filePath 文件本地路径
     */
    public void responseFile(String filePath) {
        responseFile(filePath, null);
    }

    /**
     * 响应文件
     *
     * @param filePath    文件本地路径
     * @param disposition 是否提示浏览器下载
     */
    public void responseFile(String filePath, boolean disposition) {
        responseFile(filePath, null, disposition);
    }

    /**
     * 响应文件
     *
     * @param filePath 文件本地路径
     * @param fileName 文件名，一般用作下载时浏览器保存的文件名
     */
    public void responseFile(String filePath, String fileName) {
        responseFile(filePath, fileName, true);
    }

    /**
     * 响应文件
     *
     * @param filePath    文件本地路径
     * @param fileName    文件名，一般用作下载时浏览器保存的文件名
     * @param disposition 是否提示浏览器下载
     */
    public void responseFile(String filePath, String fileName, boolean disposition) {
        response(FastChar.getOverrides().newInstance(FastOutFile.class)
                .setData(filePath)
                .setFileName(fileName)
                .setDisposition(disposition)
                .setStatus(this.status));
    }


    /**
     * 响应jsp
     *
     * @param jspPath jsp文件路径
     */
    public void responseJsp(String jspPath) {
        response(FastChar.getOverrides().newInstance(FastOutJsp.class).setData(jspPath).setStatus(this.status));
    }

    /**
     * 响应Freemaker模板，使用Freemaker模板引擎
     *
     * @param filePath 模板路径
     */
    public void responseFreemarker(String filePath) {
        response(FastChar.getOverrides().newInstance(FastOutFreemarker.class).setData(filePath).setStatus(this.status));
    }

    /**
     * 响应Freemaker模板，使用Freemaker模板引擎
     *
     * @param filePath    模板路径
     * @param contentType 响应格式，例如：image/jpeg
     */
    public void responseFreemarker(String filePath, String contentType) {
        response(FastChar.getOverrides().newInstance(FastOutFreemarker.class).setContentType(contentType).setData(filePath).setStatus(this.status));
    }

    /**
     * 响应Thymeleaf模板，使用Thymeleaf模板引擎
     *
     * @param filePath 模板路径
     */
    public void responseThymeleaf(String filePath) {
        response(FastChar.getOverrides().newInstance(FastOutThymeleaf.class).setData(filePath).setStatus(this.status));
    }

    /**
     * 响应Thymeleaf模板，使用Thymeleaf模板引擎
     *
     * @param filePath    模板路径
     * @param contentType 响应格式，例如：image/jpeg
     */
    public void responseThymeleaf(String filePath, String contentType) {
        response(FastChar.getOverrides().newInstance(FastOutThymeleaf.class).setContentType(contentType).setData(filePath).setStatus(this.status));
    }

    /**
     * 响应Velocity模板，使用Velocity模板引擎
     *
     * @param filePath 模板路径
     */
    public void responseVelocity(String filePath) {
        response(FastChar.getOverrides().newInstance(FastOutVelocity.class).setData(filePath).setStatus(this.status));
    }

    /**
     * 响应Velocity模板，使用Velocity模板引擎
     *
     * @param filePath    模板路径
     * @param contentType 响应格式，例如：image/jpeg
     */
    public void responseVelocity(String filePath, String contentType) {
        response(FastChar.getOverrides().newInstance(FastOutVelocity.class).setContentType(contentType).setData(filePath).setStatus(this.status));
    }

    /**
     * 响应图片验证码
     */
    public void responseCaptcha() {
        response(FastChar.getOverrides().newInstance(FastOutCaptcha.class).setStatus(this.status));
    }

    /**
     * 响应图片
     *
     * @param image 图片流
     */
    public void responseImage(RenderedImage image) {
        response(FastChar.getOverrides().newInstance(FastOutImage.class).setData(image).setStatus(this.status));
    }

    /**
     * 响应图片
     *
     * @param image      图片流
     * @param formatName 图片格式
     */
    public void responseImage(RenderedImage image, String formatName) {
        response(FastChar.getOverrides().newInstance(FastOutImage.class).setData(image).setFormatName(formatName).setStatus(this.status));
    }


    /**
     * 判断验证码是否正确
     *
     * @param code 图片验证码
     * @return boolean
     */
    public boolean validateCaptcha(String code) {
        Object captcha = getSession(FastMD5Utils.MD5(FastChar.getConstant().getProjectName()));
        if (captcha != null) {
            return captcha.toString().equalsIgnoreCase(code);
        }
        return false;
    }


    /**
     * 重置验证码
     */
    public void resetCaptcha() {
        removeSession(FastMD5Utils.MD5(FastChar.getConstant().getProjectName()));
    }


    /**
     * 重定向请求
     *
     * @param url 路径
     */
    public void redirect(String url) {
        response(FastChar.getOverrides().newInstance(FastOutRedirect.class).setData(url).setStatus(302));
    }

    /**
     * 重定向请求，响应状态码为：301
     *
     * @param url 路径
     */
    public void redirect301(String url) {
        response(FastChar.getOverrides().newInstance(FastOutRedirect.class).setData(url).setStatus(301));
    }


    /**
     * 转发请求
     *
     * @param url 路径
     */
    public void forward(String url) {
        response(FastChar.getOverrides().newInstance(FastOutForward.class).setData(url));
    }


    /**
     * 获得请求的类型，POST或GET
     *
     * @return String
     */
    public final String getRequestMethod() {
        return request.getMethod();
    }

    /**
     * 获得请求的contentType
     *
     * @return String
     */
    public String getContentType() {
        return request.getContentType();
    }

    /**
     * 获取Session对象
     *
     * @return HttpSession
     */
    public final HttpSession getSession() {
        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(FastChar.getConstant().getSessionMaxInterval());
        return session;
    }

    /**
     * 获取Session属性值
     *
     * @param attr 属性
     * @param <T>  泛型
     * @return T
     */
    public <T> T getSession(String attr) {
        return (T) request.getSession().getAttribute(attr);
    }

    /**
     * 设置session
     *
     * @param attr  属性名
     * @param value 属性值
     */
    public void setSession(String attr, Object value) {
        getSession().setAttribute(attr, value);
    }


    /**
     * 删除session
     *
     * @param attr 属性名
     */
    public void removeSession(String attr) {
        getSession().removeAttribute(attr);
    }

    /**
     * 设置Request属性值
     *
     * @param attr  属性名
     * @param value 属性值
     * @return 当前对象
     */
    public FastAction setRequestAttr(String attr, Object value) {
        request.setAttribute(attr, value);
        return this;
    }

    /**
     * 设置Request属性值
     *
     * @param attrs 属性map集合
     * @return 当前对象
     */
    public FastAction setRequestAttr(Map<String, Object> attrs) {
        for (String s : attrs.keySet()) {
            setRequestAttr(s, attrs.get(s));
        }
        return this;
    }

    /**
     * 获取Request属性值
     *
     * @param attr 属性名
     * @return 属性值
     */
    public Object getRequesetAttr(String attr) {
        return request.getAttribute(attr);
    }


    /**
     * 删除Request属性
     *
     * @param attr 名称
     */
    public void removeRequestAttr(String attr) {
        request.removeAttribute(attr);
    }


    /**
     * 获取请求的头信息
     *
     * @param name 名称
     * @return 字符串
     */
    public String getRequestHeader(String name) {
        return getRequest().getHeader(name);
    }

    /**
     * 获取请求的头信息
     *
     * @param name 名称
     * @return Enumeration&lt;String&gt;
     */
    public Enumeration<String> getRequestHeaders(String name) {
        return getRequest().getHeaders(name);
    }

    /**
     * 获取请求的头信息
     *
     * @return Enumeration&lt;String&gt;
     */
    public Enumeration<String> getRequestHeaderNames() {
        return getRequest().getHeaderNames();
    }


    /**
     * 设置cookie
     *
     * @param name   名称
     * @param value  值
     * @param maxAge 有效期，单位：秒
     * @param path   路径
     * @param domain 域名
     * @return 当前对象
     */
    public FastAction setCookie(String name, Object value, int maxAge, String path, String domain) {
        return setCookie(name, value, maxAge, path, domain, null);
    }

    /**
     * 设置cookie
     *
     * @param name   名称
     * @param value  值
     * @param maxAge 有效期，单位：秒
     * @param path   路径
     * @return 当前对象
     */
    public FastAction setCookie(String name, Object value, int maxAge, String path) {
        return setCookie(name, value, maxAge, path, null, null);
    }

    /**
     * 设置cookie
     *
     * @param name     名称
     * @param value    值
     * @param maxAge   有效期，单位：秒
     * @param path     路径
     * @param httpOnly 是否启用HttpOnly，启用后将无法通过js获取当前cookie值
     * @return 当前对象
     */
    public FastAction setCookie(String name, Object value, int maxAge, String path, boolean httpOnly) {
        return setCookie(name, value, maxAge, path, null, httpOnly);
    }

    /**
     * 设置cookie
     *
     * @param name     名称
     * @param value    值
     * @param maxAge   有效期，单位：秒
     * @param httpOnly 是否启用HttpOnly，启用后将无法通过js获取当前cookie值
     * @return 当前对象
     */
    public FastAction setCookie(String name, Object value, int maxAge, boolean httpOnly) {
        return setCookie(name, value, maxAge, null, null, httpOnly);
    }

    /**
     * 设置cookie
     *
     * @param name   名称
     * @param value  值
     * @param maxAge 有效期，单位：秒
     * @return 当前对象
     */
    public FastAction setCookie(String name, Object value, int maxAge) {
        return setCookie(name, value, maxAge, null, null, null);
    }

    /**
     * 设置cookie
     *
     * @param name  名称
     * @param value 值
     * @return 当前对象
     */
    public FastAction setCookie(String name, Object value) {
        return setCookie(name, value, Integer.MAX_VALUE, null, null, null);
    }

    /**
     * 设置cookie
     *
     * @param name     名称
     * @param value    值
     * @param httpOnly 是否启用HttpOnly，启用后将无法通过js获取当前cookie值
     * @return 当前对象
     */
    public FastAction setCookie(String name, Object value, boolean httpOnly) {
        return setCookie(name, value, Integer.MAX_VALUE, null, null, httpOnly);
    }


    /**
     * 设置cookie
     *
     * @param name     名称
     * @param value    值
     * @param maxAge   有效期，单位：秒
     * @param path     路径
     * @param domain   域名
     * @param httpOnly 是否启用HttpOnly，启用后将无法通过js获取当前cookie值
     * @return 当前对象
     */
    public FastAction setCookie(String name, Object value, int maxAge, String path, String domain,
                                Boolean httpOnly) {
        Cookie cookie = new Cookie(name, String.valueOf(value));
        cookie.setMaxAge(maxAge);
        if (path == null) {
            path = "/";
        }
        cookie.setPath(path);

        if (domain != null) {
            cookie.setDomain(domain);
        }
        if (httpOnly != null) {
            cookie.setHttpOnly(httpOnly);
        }
        response.addCookie(cookie);
        return this;
    }


    /**
     * 获得cookie值
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return String
     */
    public String getCookie(String name, String defaultValue) {
        Cookie cookie = getCookieObject(name);
        return cookie != null ? cookie.getValue() : defaultValue;
    }

    /**
     * 获得cookie值
     *
     * @param name 名称
     * @return String
     */
    public String getCookie(String name) {
        return getCookie(name, null);
    }

    /**
     * 获得cookie值
     *
     * @param name 名称
     * @return int
     */
    public int getCookieToInt(String name) {
        return getCookieToInt(name, 0);
    }

    /**
     * 获得cookie值
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return int
     */
    public int getCookieToInt(String name, int defaultValue) {
        String result = getCookie(name);
        return FastNumberUtils.formatToInt(result, defaultValue);
    }

    /**
     * 获得cookie值
     *
     * @param name 名称
     * @return double
     */
    public double getCookieToDouble(String name) {
        return getCookieToDouble(name, 0);
    }

    /**
     * 获得cookie值
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return double
     */
    public double getCookieToDouble(String name, double defaultValue) {
        String result = getCookie(name);
        return FastNumberUtils.formatToDouble(result, defaultValue);
    }

    /**
     * 获得cookie值
     *
     * @param name 名称
     * @return long
     */
    public long getCookieToLong(String name) {
        return getCookieToLong(name, 0);
    }

    /**
     * 获得cookie值
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return long
     */
    public long getCookieToLong(String name, long defaultValue) {
        String result = getCookie(name);
        return FastNumberUtils.formatToLong(result, defaultValue);
    }

    /**
     * 获得cookie值
     *
     * @param name 名称
     * @return boolean
     */
    public boolean getCookieToBoolean(String name) {
        return getCookieToBoolean(name, false);
    }

    /**
     * 获得cookie值
     *
     * @param name         名称
     * @param defaultValue 默认值
     * @return boolean
     */
    public boolean getCookieToBoolean(String name, boolean defaultValue) {
        String result = getCookie(name);
        return FastBooleanUtils.formatToBoolean(result, defaultValue);
    }

    /**
     * 获得cookie
     *
     * @param name 名称
     * @return Cookie
     */
    public Cookie getCookieObject(String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * 删除cookie
     *
     * @param name 名称
     * @return 当前对象
     */
    public FastAction removeCookie(String name) {
        return removeCookie(name, null, null);
    }

    /**
     * 删除cookie
     *
     * @param name 名称
     * @param path 相对路径
     * @return 当前对象
     */
    public FastAction removeCookie(String name, String path) {
        return removeCookie(name, path, null);
    }

    /**
     * 删除cookie
     *
     * @param name   名称
     * @param path   相对路径
     * @param domain domain
     * @return 当前对象
     */
    public FastAction removeCookie(String name, String path, String domain) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        if (path == null) {
            path = "/";
        }
        cookie.setPath(path);

        if (domain != null) {
            cookie.setDomain(domain);
        }
        response.addCookie(cookie);
        return this;
    }

    /**
     * 获得cookie数组
     *
     * @return Cookie[]
     */
    public Cookie[] getCookieObjects() {
        Cookie[] result = request.getCookies();
        return result != null ? result : new Cookie[0];
    }

    /**
     * 获取响应对象
     *
     * @return HttpServletResponse
     */
    public HttpServletResponse getResponse() {
        return response;
    }

    /**
     * 获取路径参数，例如实际路径为：/user  请求路径为：/user/1/abc 那么路径参数为：[1,abc]
     *
     * @return List&lt;String&gt;
     */
    public List<String> getUrlParams() {
        return this.fastUrl.getUrlParams();
    }

    /**
     * 获取路径参数，例如实际路径为：/user  请求路径为：/user/1/abc 那么路径参数为：[1,abc]
     *
     * @param index 索引
     * @return String
     */
    public String getUrlParam(int index) {
        if (index >= this.fastUrl.getUrlParams().size()) {
            return null;
        }
        return this.fastUrl.getUrlParams().get(index);
    }

    /**
     * 获取当前Action的路由对象
     *
     * @return FastRoute
     */
    public FastRoute getFastRoute() {
        return fastRoute;
    }

    /**
     * 获取最终响应的类型
     *
     * @return FastOut
     */
    public FastOut getFastOut() {
        return fastOut;
    }

    /**
     * 获取转发到当前action的action
     *
     * @return FastAction
     */
    public FastAction getForwarder() {
        return forwarder;
    }

    /**
     * 是否打印日志
     *
     * @return 布尔值，默认：true
     */
    public boolean isLog() {
        return log;
    }

    /**
     * 设置是否打印日志
     *
     * @param log 布尔值
     * @return 当前对象
     */
    public FastAction setLog(boolean log) {
        this.log = log;
        return this;
    }

    /**
     * 是否打印响应日志
     *
     * @return 布尔值
     */
    public boolean isLogResponse() {
        return logResponse;
    }

    /**
     * 设置是否打印响应日志
     *
     * @param logResponse 布尔值
     * @return 当前对象
     */
    public FastAction setLogResponse(boolean logResponse) {
        this.logResponse = logResponse;
        return this;
    }

    /**
     * 获取设置的响应状态码
     *
     * @return 状态码
     */
    public int getStatus() {
        return status;
    }

    /**
     * 设置响应状态码
     *
     * @param status 状态码
     * @return 当前对象
     */
    public FastAction setStatus(int status) {
        this.status = status;
        return this;
    }

    /**
     * 获取对方的ip地址
     *
     * @return String
     */
    public String getRemoteIp() {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");//nginx 服务器
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.length() > 15) {
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        if ("0:0:0:0:0:0:0:1".equalsIgnoreCase(ip)) {
            return "localhost";
        }
        return ip;
    }


    /**
     * 获得request的user-agent
     *
     * @return String
     */
    public final String getUserAgent() {
        String agent = getRequest().getHeader("User-Agent");
        return agent;
    }

    /**
     * 添加参数验证，会触发IFastValidator验证码器，只对getParam*相关方法有效
     *
     * @param validator 验证标识
     * @return 当前对象
     */
    public FastAction check(String validator) {
        return fastCheck.check(validator);
    }

    /**
     * 添加参数验证，会触发IFastValidator验证码器，只对getParam*相关方法有效
     *
     * @param validator 验证标识
     * @param index     插入指定位置
     * @return 当前对象
     */
    public FastAction check(int index, String validator) {
        return fastCheck.check(index, validator);
    }

}
