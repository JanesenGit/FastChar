package com.fastchar.core;

import com.fastchar.asm.FastParameter;
import com.fastchar.exception.FastFileException;
import com.fastchar.exception.FastReturnException;
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
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    volatile FastCheck<FastAction> fastCheck = new FastCheck<FastAction>(this);

    private volatile boolean log = true;
    private volatile int status = 200;

    /**
     * 获得路由地址
     * Get routing address
     *
     * @return
     */
    protected abstract String getRoute();


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
     * @return
     */
    public boolean isMultipart() {
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
        return false;
    }


    public String getProjectHost() {
        String[] split = getRequest().getRequestURL().toString().split(FastChar.getConstant().getProjectName());
        return FastStringUtils.stripEnd(split[0], "/") + "/" + FastChar.getConstant().getProjectName() + "/";
    }

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

    public final ServletContext getServletContext() {
        return servletContext;
    }

    public boolean isParamEmpty(String paramName) {
        return FastStringUtils.isEmpty(getParam(paramName));
    }

    public boolean isParamNotEmpty(String paramName) {
        return FastStringUtils.isNotEmpty(getParam(paramName));
    }

    public boolean isParamBlank(String paramName) {
        return FastStringUtils.isBlank(getParam(paramName));
    }

    public boolean isParamNotBlank(String paramName) {
        return FastStringUtils.isNotBlank(getParam(paramName));
    }

    public boolean isParamExists(String paramName) {
        return getParamNames().contains(paramName);
    }

    public FastAction addParam(String paramName, String paramValue) {
        params.add(new FastRequestParam().setName(paramName).setValue(paramValue));
        return this;
    }


    public Set<String> getParamNames() {
        LinkedHashSet<String> strings = new LinkedHashSet<>();
        Enumeration<String> parameterNames = getRequest().getParameterNames();
        while (parameterNames.hasMoreElements()) {
            strings.add(parameterNames.nextElement());
        }
        for (FastRequestParam param : params) {
            strings.add(param.getName());
        }
        return strings;
    }


    public String getParam(String paramName, String defaultValue) {
        return FastStringUtils.defaultValue(getParam(paramName), defaultValue);
    }

    public String getParam(String paramName) {
        return getParam(paramName, false);
    }

    public String getParam(String paramName, boolean notNull) {
        if (notNull) {
            check("@null:" + FastChar.getLocal().getInfo("Param_Error1"));
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


    public String[] getParamToArray(String paramName) {
        return getParamToArray(paramName, false);
    }

    public String[] getParamToArray(String paramName, boolean notNull) {
        String[] parameterValues = getRequest().getParameterValues(paramName);
        List<String> arrays = new ArrayList<>();
        if (parameterValues != null) {
            arrays.addAll(Arrays.asList(parameterValues));
        }
        for (FastRequestParam param : params) {
            if (param.getName().equals(paramName)) {
                arrays.add(param.getValue());
            }
        }

        if (notNull) {
            check("@null:" + FastChar.getLocal().getInfo("Param_Error1"));
        }
        String[] strings = arrays.toArray(new String[]{});
        if (validParam(paramName, strings)) {
            return strings;
        }
        return null;
    }

    public Integer[] getParamToIntArray(String paramName) {
        return getParamToIntArray(paramName, false);
    }

    public Integer[] getParamToIntArray(String paramName, boolean notNull) {
        List<Integer> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            arrays.add(FastNumberUtils.formatToInt(parameterValue));
        }
        return arrays.toArray(new Integer[]{});
    }

    public Double[] getParamToDoubleArray(String paramName) {
        return getParamToDoubleArray(paramName, false);
    }

    public Double[] getParamToDoubleArray(String paramName, boolean notNull) {
        List<Double> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            arrays.add(FastNumberUtils.formatToDouble(parameterValue));
        }
        return arrays.toArray(new Double[]{});
    }

    public Long[] getParamToLongArray(String paramName) {
        return getParamToLongArray(paramName, false);
    }

    public Long[] getParamToLongArray(String paramName, boolean notNull) {
        List<Long> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            arrays.add(FastNumberUtils.formatToLong(parameterValue));
        }
        return arrays.toArray(new Long[]{});
    }

    public Float[] getParamToFloatArray(String paramName) {
        return getParamToFloatArray(paramName, false);
    }

    public Float[] getParamToFloatArray(String paramName, boolean notNull) {
        List<Float> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            arrays.add(FastNumberUtils.formatToFloat(parameterValue));
        }
        return arrays.toArray(new Float[]{});
    }

    public Short[] getParamToShortArray(String paramName) {
        return getParamToShortArray(paramName, false);
    }

    public Short[] getParamToShortArray(String paramName, boolean notNull) {
        List<Short> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            arrays.add(FastNumberUtils.formatToShort(parameterValue));
        }
        return arrays.toArray(new Short[]{});
    }

    public Boolean[] getParamToBooleanArray(String paramName) {
        return getParamToBooleanArray(paramName, false);
    }

    public Boolean[] getParamToBooleanArray(String paramName, boolean notNull) {
        List<Boolean> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            arrays.add(FastBooleanUtils.formatToBoolean(parameterValue));
        }
        return arrays.toArray(new Boolean[]{});
    }

    public Date[] getParamToDateArray(String paramName) {
        return getParamToDateArray(paramName, false);
    }

    public Date[] getParamToDateArray(String paramName, boolean notNull) {
        return getParamToDateArray(paramName, FastChar.getConstant().getDateFormat(), notNull);
    }

    public Date[] getParamToDateArray(String paramName, String dateFormat) {
        return getParamToDateArray(paramName, dateFormat, false);
    }

    public Date[] getParamToDateArray(String paramName, String dateFormat, boolean notNull) {
        List<Date> arrays = new ArrayList<>();
        String[] parameterValues = getParamToArray(paramName, notNull);
        for (String parameterValue : parameterValues) {
            arrays.add(FastDateUtils.parse(parameterValue, dateFormat));
        }
        return arrays.toArray(new Date[]{});
    }

    public <T extends Enum> T[] getParamToEnumArray(String paramName, Class<? extends Enum> enumClass) {
        return getParamToEnumArray(paramName, enumClass, false);
    }

    public <T extends Enum> T[] getParamToEnumArray(String paramName, Class<? extends Enum> enumClass, boolean notNull) {
        String[] parameterValues = getParamToArray(paramName, notNull);
        Object array = Array.newInstance(enumClass, parameterValues.length);
        for (int i = 0; i < parameterValues.length; i++) {
            T e = FastEnumUtils.formatToEnum(enumClass, parameterValues[i]);
            Array.set(array, i, e);
        }
        return (T[]) array;

    }

    public List<String> getParamToList(String prefix) {
        return getParamToList(prefix, false);
    }

    public List<String> getParamToList(String prefix, boolean notNull) {
        List<String> list = new ArrayList<>();
        Set<String> paramNames = getParamNames();
        for (String paramName : paramNames) {
            if (paramName.startsWith(prefix)) {
                String[] parameterValues = getParamToArray(paramName);
                if (parameterValues.length == 0) continue;
                if (parameterValues.length > 1) {
                    list.addAll(Arrays.asList(parameterValues));
                } else {
                    list.add(parameterValues[0]);
                }
            }
        }
        if (list.size() == 0 && notNull) {
            responseParamError(prefix, FastChar.getLocal().getInfo("Param_Error1", prefix));
        }
        return list;
    }

    public List<Integer> getParamToIntList(String prefix) {
        return getParamToIntList(prefix, false);
    }

    public List<Integer> getParamToIntList(String prefix, boolean notNull) {
        List<Integer> list = new ArrayList<>();
        List<String> paramToList = getParamToList(prefix, notNull);
        for (String value : paramToList) {
            list.add(FastNumberUtils.formatToInt(value));
        }
        return list;
    }

    public List<Double> getParamToDoubleList(String prefix) {
        return getParamToDoubleList(prefix, false);
    }

    public List<Double> getParamToDoubleList(String prefix, boolean notNull) {
        List<Double> list = new ArrayList<>();
        List<String> paramToList = getParamToList(prefix, notNull);
        for (String value : paramToList) {
            list.add(FastNumberUtils.formatToDouble(value));
        }
        return list;
    }

    public List<Float> getParamToFloatList(String prefix) {
        return getParamToFloatList(prefix, false);
    }

    public List<Float> getParamToFloatList(String prefix, boolean notNull) {
        List<Float> list = new ArrayList<>();
        List<String> paramToList = getParamToList(prefix, notNull);
        for (String value : paramToList) {
            list.add(FastNumberUtils.formatToFloat(value));
        }
        return list;
    }

    public List<Long> getParamToLongList(String prefix) {
        return getParamToLongList(prefix, false);
    }

    public List<Long> getParamToLongList(String prefix, boolean notNull) {
        List<Long> list = new ArrayList<>();
        List<String> paramToList = getParamToList(prefix, notNull);
        for (String value : paramToList) {
            list.add(FastNumberUtils.formatToLong(value));
        }
        return list;
    }

    public List<Short> getParamToShortList(String prefix) {
        return getParamToShortList(prefix, false);
    }

    public List<Short> getParamToShortList(String prefix, boolean notNull) {
        List<Short> list = new ArrayList<>();
        List<String> paramToList = getParamToList(prefix, notNull);
        for (String value : paramToList) {
            list.add(FastNumberUtils.formatToShort(value));
        }
        return list;
    }


    public <T extends Enum> List<T> getParamToEnumList(String prefix, Class<? extends Enum> enumClass) {
        return getParamToEnumList(prefix, enumClass, false);
    }

    public <T extends Enum> List<T> getParamToEnumList(String prefix, Class<? extends Enum> enumClass, boolean notNull) {
        List<T> list = new ArrayList<>();

        List<String> paramToList = getParamToList(prefix, notNull);
        Object array = Array.newInstance(enumClass, paramToList.size());
        for (int i = 0; i < paramToList.size(); i++) {
            T e = FastEnumUtils.formatToEnum(enumClass, paramToList.get(i));
            Array.set(array, i, e);
        }
        list.addAll(Arrays.asList((T[]) array));
        return list;
    }


    public int getParamToInt(String paramName) {
        return FastNumberUtils.formatToInt(getParam(paramName));
    }

    public int getParamToInt(String paramName, boolean notNull) {
        return FastNumberUtils.formatToInt(getParam(paramName, notNull));
    }

    public int getParamToInt(String paramName, int defaultValue) {
        return FastNumberUtils.formatToInt(getParam(paramName), defaultValue);
    }


    public short getParamToShort(String paramName) {
        return FastNumberUtils.formatToShort(getParam(paramName));
    }

    public short getParamToShort(String paramName, boolean notNull) {
        return FastNumberUtils.formatToShort(getParam(paramName, notNull));
    }


    public short getParamToShort(String paramName, short defaultValue) {
        return FastNumberUtils.formatToShort(getParam(paramName), defaultValue);
    }


    public double getParamToDouble(String paramName) {
        return FastNumberUtils.formatToDouble(getParam(paramName));
    }

    public double getParamToDouble(String paramName, boolean notNull) {
        return FastNumberUtils.formatToDouble(getParam(paramName, notNull));
    }

    public double getParamToDouble(String paramName, double defaultValue) {
        return FastNumberUtils.formatToDouble(getParam(paramName), defaultValue);
    }

    public double getParamToDouble(String paramName, int digit) {
        return FastNumberUtils.formatToDouble(getParam(paramName), digit);
    }

    public double getParamToDouble(String paramName, double defaultValue, int digit) {
        return FastNumberUtils.formatToDouble(getParam(paramName), defaultValue, digit);
    }


    public float getParamToFloat(String paramName) {
        return FastNumberUtils.formatToFloat(getParam(paramName));
    }

    public float getParamToFloat(String paramName, boolean notNull) {
        return FastNumberUtils.formatToFloat(getParam(paramName, notNull));
    }

    public float getParamToFloat(String paramName, float defaultValue) {
        return FastNumberUtils.formatToFloat(getParam(paramName), defaultValue);
    }

    public float getParamToFloat(String paramName, float defaultValue, int digit) {
        return FastNumberUtils.formatToFloat(getParam(paramName), defaultValue, digit);
    }

    public float getParamToFloat(String paramName, int digit) {
        return FastNumberUtils.formatToFloat(getParam(paramName), digit);
    }


    public boolean getParamToBoolean(String paramName) {
        return FastBooleanUtils.formatToBoolean(getParam(paramName));
    }

    public boolean getParamToBoolean(String paramName, boolean notNull) {
        return FastBooleanUtils.formatToBoolean(getParam(paramName, notNull));
    }

    public boolean getParamToBoolean(String paramName, Boolean defaultValue) {
        return FastBooleanUtils.formatToBoolean(getParam(paramName), defaultValue);
    }

    public long getParamToLong(String paramName) {
        return FastNumberUtils.formatToLong(getParam(paramName));
    }

    public long getParamToLong(String paramName, boolean notNull) {
        return FastNumberUtils.formatToLong(getParam(paramName, notNull));
    }

    public long getParamToLong(String paramName, long defaultValue) {
        return FastNumberUtils.formatToLong(getParam(paramName), defaultValue);
    }

    public Date getParamToDate(String paramName) {
        return getParamToDate(paramName, FastChar.getConstant().getDateFormat());
    }

    public Date getParamToDate(String paramName, boolean notNull) {
        return getParamToDate(paramName, FastChar.getConstant().getDateFormat(), notNull);
    }

    public Date getParamToDate(String paramName, Date defaultValue) {
        return getParamToDate(paramName, FastChar.getConstant().getDateFormat(), defaultValue);
    }

    public Date getParamToDate(String paramName, String dateFormat) {
        return getParamToDate(paramName, FastChar.getConstant().getDateFormat(), null);
    }

    public Date getParamToDate(String paramName, String dateFormat, boolean notNull) {
        return FastDateUtils.parse(getParam(paramName, notNull), dateFormat, null);
    }

    public Date getParamToDate(String paramName, String dateFormat, Date defaultValue) {
        return FastDateUtils.parse(getParam(paramName), dateFormat, defaultValue);
    }

    public <T extends Enum> T getParamToEnum(String paramName, Class<? extends Enum> enumClass) {
        return getParamToEnum(paramName, enumClass, null);
    }

    public <T extends Enum> T getParamToEnum(String paramName, Class<? extends Enum> enumClass, boolean notNull) {
        return getParamToEnum(paramName, enumClass, null, notNull);
    }

    public <T extends Enum> T getParamToEnum(String paramName, Class<? extends Enum> enumClass, Enum defaultValue) {
        return getParamToEnum(paramName, enumClass, defaultValue, false);
    }

    private <T extends Enum> T getParamToEnum(String paramName, Class<? extends Enum> enumClass, Enum defaultValue, boolean notNull) {
        String param = getParam(paramName, notNull);
        if (FastStringUtils.isEmpty(param)) {
            return null;
        }
        return (T) FastEnumUtils.formatToEnum(enumClass, param, defaultValue);
    }

    public Map<String, Object> getParamToMap() {
        Map<String, Object> mapParam = new HashMap<>();
        Set<String> paramNames = getParamNames();
        for (String paramName : paramNames) {
            String[] parameterValues = getParamToArray(paramName);
            if (parameterValues.length == 0) continue;
            if (parameterValues.length > 1) {
                mapParam.put(paramName, parameterValues);
            } else {
                mapParam.put(paramName, parameterValues[0]);
            }
        }
        return mapParam;
    }

    //where.attr  where['attr']
    public Map<String, Object> getParamToMap(String prefix) {
        return getParamToMap(prefix, false);
    }

    public Map<String, Object> getParamToMap(String prefix, boolean notNull) {
        Map<String, Object> mapParam = new HashMap<>();
        Set<String> paramNames = getParamNames();

        for (String paramName : paramNames) {
            if (paramName.startsWith(prefix)) {
                String attr = getParamNameAttr(paramName);
                String[] parameterValues = getParamToArray(paramName);
                if (parameterValues.length == 0) continue;
                if (parameterValues.length > 1) {
                    mapParam.put(attr, parameterValues);
                } else {
                    mapParam.put(attr, parameterValues[0]);
                }
            }
        }
        if (notNull && mapParam.size() == 0) {
            responseParamError(prefix, FastChar.getLocal().getInfo("Param_Error1", prefix));
        }
        return mapParam;
    }


    //where[0].name where[0]['name']
    public List<Map<String, Object>> getParamToMapList(String prefix) {
        return getParamToMapList(prefix, false);
    }

    //where[0].name where[0]['name']
    public List<Map<String, Object>> getParamToMapList(String prefix, boolean notNull) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        SortedSet<String> mapNames = new TreeSet<>();
        Set<String> paramNames = getParamNames();
        for (String paramName : paramNames) {
            if (paramName.startsWith(prefix)) {
                String namePrefix = getParamNamePrefix(paramName);
                mapNames.add(namePrefix);
            }
        }
        for (String mapName : mapNames) {
            Map<String, Object> paramToMap = getParamToMap(mapName);
            if (paramToMap.size() == 0) continue;
            mapList.add(paramToMap);
        }
        if (notNull && mapList.size() == 0) {
            responseParamError(prefix, FastChar.getLocal().getInfo("Param_Error1", prefix));
        }
        return mapList;
    }

    public <T extends FastEntity<?>> T getParamToEntity(String prefix, Class<? extends FastEntity> targetClass) {
        return getParamToEntity(prefix, targetClass, false);
    }


    public <T extends FastEntity<?>> T getParamToEntity(String prefix, Class<? extends FastEntity> targetClass,
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


    public <T extends FastEntity<?>> List<T> getParamToEntityList(String prefix, Class<? extends FastEntity> targetClass) {
        return getParamToEntityList(prefix, targetClass, true);
    }

    public <T extends FastEntity<?>> List<T> getParamToEntityList(String prefix, Class<? extends FastEntity> targetClass, boolean notNull) {
        List<T> list = new ArrayList<>();
        List<Map<String, Object>> paramToMapList = getParamToMapList(prefix, notNull);
        for (Map<String, Object> paramToMap : paramToMapList) {
            if (paramToMap.size() == 0) continue;
            FastEntity fastEntity = FastChar.getOverrides().newInstance(targetClass);
            if (fastEntity == null) {
                return null;
            }
            for (String key : paramToMap.keySet()) {
                fastEntity.set(key, paramToMap.get(key));
            }
            if (fastEntity.size() == 0) continue;
            list.add((T) fastEntity);
        }
        return list;
    }


    public <T extends FastEntity<?>> T[] getParamToEntityArray(String prefix, Class<? extends FastEntity> targetClass) {
        return getParamToEntityArray(prefix, targetClass, true);
    }

    public <T extends FastEntity<?>> T[] getParamToEntityArray(String prefix, Class<? extends FastEntity> targetClass, boolean notNull) {
        List<T> paramToEntityList = getParamToEntityList(prefix, targetClass, notNull);
        Object array = Array.newInstance(targetClass, paramToEntityList.size());
        for (int i = 0; i < paramToEntityList.size(); i++) {
            Array.set(array, i, paramToEntityList.get(i));
        }
        return (T[]) array;
    }

    public <T> T getParamToClass(String paramName, Class<T> targetClass) throws Exception {
        return getParamToClass(paramName, targetClass, null);
    }

    public <T> T getParamToClass(String paramName, Class<T> targetClass, Type parameterizedType) throws Exception {
        Object value = FastChar.getConverters().convertParam(this, new FastParameter().setType(targetClass).setParameterizedType(parameterizedType));
        if (value == null) {
            return null;
        }
        return (T) value;
    }


    public FastAction putParamFile(FastFile fastFile) {
        HttpServletRequest request = getRequest();
        if (request instanceof FastMultipartWrapper) {
            FastMultipartWrapper multipartWrapper = (FastMultipartWrapper) request;
            multipartWrapper.putFile(fastFile.getParamName(), fastFile);
        }
        return this;
    }


    public void deleteAllParamFiles() {
        for (FastFile fastFile : getParamListFile()) {
            fastFile.getFile().delete();
        }
    }

    public FastFile<?> getParamFile() {
        List<FastFile<?>> paramToListFile = getParamListFile();
        if (paramToListFile.size() > 0) {
            return paramToListFile.get(0);
        }
        return null;
    }


    public FastFile<?> getParamFile(String paramName) {
        HttpServletRequest request = getRequest();
        if (request instanceof FastMultipartWrapper) {
            FastMultipartWrapper multipartWrapper = (FastMultipartWrapper) request;
            return multipartWrapper.getFile(paramName);
        }
        return null;
    }

    public FastFile<?> getParamFile(String paramName, String moveToDirectory) throws FastFileException {
        FastFile paramToFile = getParamFile(paramName);
        if (paramToFile != null) {
            return paramToFile.moveFile(moveToDirectory);
        }
        return null;
    }


    public List<FastFile<?>> getParamListFile() {
        HttpServletRequest request = getRequest();
        if (request instanceof FastMultipartWrapper) {
            FastMultipartWrapper multipartWrapper = (FastMultipartWrapper) request;
            return multipartWrapper.getFiles();
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

    public Document getParamDataToXml() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(getRequest().getInputStream());
        return doc;
    }

    public String getParamDataToString() throws Exception {
        return FastIOUtils.toString(getRequest().getInputStream(), getRequest().getCharacterEncoding());
    }

    public String getParamDataToString(String encoding) throws Exception {
        return FastIOUtils.toString(getRequest().getInputStream(), encoding);
    }


    public void response(FastOut out) {
        this.fastOut = out;
        this.fastRoute.response();
        throw new FastReturnException();
    }


    public void response404(String message) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutError.class).setStatus(404).setDescription(message);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void response500(Throwable throwable) {
        StringBuilder stringBuilder = new StringBuilder(throwable.toString());
        StackTraceElement[] stackTrace = throwable.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stringBuilder.append("\n\tat ").append(stackTraceElement);
        }
        this.fastOut = FastChar.getOverrides().newInstance(FastOutError.class).setStatus(500)
                .setDescription(throwable.toString())
                .setData(stringBuilder.toString());
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void response502(String message) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutError.class).setStatus(502).setDescription(message);
        this.fastRoute.response();
        throw new FastReturnException();
    }


    public void responseJson(Object data) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutJson.class).setData(data).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

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


    public void responseText(Object data) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutText.class).setData(data).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseText(int status, Object data) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutText.class).setData(data).setStatus(status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseText(Object data, String contentType) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutText.class).setData(data).setContentType(contentType).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseText(int status, Object data, String contentType) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutText.class).setData(data).setContentType(contentType).setStatus(status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseXml(Object data) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutXml.class).setData(data).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseParamError(String paramName) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutParamError.class).setData(paramName).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseParamError(String paramName, String message) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutParamError.class)
                .setMessage(message).setData(paramName).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }


    public void responseHtml(String html) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutHtml.class).setData(html).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseFile(File file) {
        responseFile(file.getAbsolutePath());
    }

    public void responseFile(File file, String fileName) {
        responseFile(file.getAbsolutePath(), fileName);
    }

    public void responseFile(File file, boolean disposition) {
        responseFile(file.getAbsolutePath(), null, disposition);
    }

    public void responseFile(String filePath) {
        responseFile(filePath, null);
    }

    public void responseFile(String filePath, boolean disposition) {
        responseFile(filePath, null, disposition);
    }

    public void responseFile(String filePath, String fileName) {
        responseFile(filePath, fileName, true);
    }

    public void responseFile(String filePath, String fileName, boolean disposition) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutFile.class)
                .setData(filePath)
                .setFileName(fileName)
                .setDisposition(disposition)
                .setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }


    public void responseJsp(String jspPath) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutJsp.class).setData(jspPath).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseFreemarker(String filePath) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutFreemarker.class).setData(filePath).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseFreemarker(String filePath, String contentType) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutFreemarker.class).setContentType(contentType).setData(filePath).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseThymeleaf(String filePath) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutThymeleaf.class).setData(filePath).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseThymeleaf(String filePath, String contentType) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutThymeleaf.class).setContentType(contentType).setData(filePath).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseVelocity(String filePath) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutVelocity.class).setData(filePath).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseVelocity(String filePath, String contentType) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutVelocity.class).setContentType(contentType).setData(filePath).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void responseCaptcha() {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutCaptcha.class).setStatus(this.status);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public boolean validateCaptcha(String code) {
        Object captcha = getSession(FastMD5Utils.MD5(FastChar.getConstant().getProjectName()));
        if (captcha != null) {
            return captcha.toString().equalsIgnoreCase(code);
        }
        return false;
    }


    public void redirect(String url) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutRedirect.class).setData(url).setStatus(302);
        this.fastRoute.response();
        throw new FastReturnException();
    }

    public void redirect301(String url) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutRedirect.class).setData(url).setStatus(301);
        this.fastRoute.response();
        throw new FastReturnException();
    }


    public void forward(String url) {
        this.fastOut = FastChar.getOverrides().newInstance(FastOutForward.class).setData(url);
        this.fastRoute.response();
        throw new FastReturnException();
    }


    public final String getRequestMethod() {
        return request.getMethod();
    }


    public String getContentType() {
        return request.getContentType();
    }

    public final HttpSession getSession() {
        return request.getSession();
    }

    public <T> T getSession(String attr) {
        return (T) request.getSession().getAttribute(attr);
    }

    public void setSession(String attr, Object value) {
        getSession().setAttribute(attr, value);
    }

    public void removeSession(String attr) {
        getSession().removeAttribute(attr);
    }

    public FastAction setRequestAttr(String attr, Object value) {
        request.setAttribute(attr, value);
        return this;
    }

    public void removeAttr(String attr) {
        request.removeAttribute(attr);
    }


    public FastAction setCookie(String name, Object value, int maxAge, String path, String domain) {
        return setCookie(name, value, maxAge, path, domain, null);
    }

    public FastAction setCookie(String name, Object value, int maxAge, String path) {
        return setCookie(name, value, maxAge, path, null, null);
    }

    public FastAction setCookie(String name, Object value, int maxAge, String path, boolean httpOnly) {
        return setCookie(name, value, maxAge, path, null, httpOnly);
    }

    public FastAction setCookie(String name, Object value, int maxAge, boolean httpOnly) {
        return setCookie(name, value, maxAge, null, null, httpOnly);
    }

    public FastAction setCookie(String name, Object value, int maxAge) {
        return setCookie(name, value, maxAge, null, null, null);
    }

    public FastAction setCookie(String name, Object value) {
        return setCookie(name, value, Integer.MAX_VALUE, null, null, null);
    }

    public FastAction setCookie(String name, Object value, boolean httpOnly) {
        return setCookie(name, value, Integer.MAX_VALUE, null, null, httpOnly);
    }


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


    public String getCookie(String name, String defaultValue) {
        Cookie cookie = getCookieObject(name);
        return cookie != null ? cookie.getValue() : defaultValue;
    }

    public String getCookie(String name) {
        return getCookie(name, null);
    }

    public int getCookieToInt(String name) {
        return getCookieToInt(name, 0);
    }

    public int getCookieToInt(String name, int defaultValue) {
        String result = getCookie(name);
        return FastNumberUtils.formatToInt(result, defaultValue);
    }

    public double getCookieToDouble(String name) {
        return getCookieToDouble(name, 0);
    }

    public double getCookieToDouble(String name, double defaultValue) {
        String result = getCookie(name);
        return FastNumberUtils.formatToDouble(result, defaultValue);
    }

    public long getCookieToLong(String name) {
        return getCookieToLong(name, 0);
    }

    public long getCookieToLong(String name, long defaultValue) {
        String result = getCookie(name);
        return FastNumberUtils.formatToLong(result, defaultValue);
    }

    public boolean getCookieToBoolean(String name) {
        return getCookieToBoolean(name, false);
    }

    public boolean getCookieToBoolean(String name, boolean defaultValue) {
        String result = getCookie(name);
        return FastBooleanUtils.formatToBoolean(result, defaultValue);
    }

    public Cookie getCookieObject(String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (cookie.getName().equals(name))
                    return cookie;
        return null;
    }

    public Cookie[] getCookieObjects() {
        Cookie[] result = request.getCookies();
        return result != null ? result : new Cookie[0];
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public List<String> getUrlParams() {
        return this.fastUrl.getUrlParams();
    }

    public String getUrlParam(int index) {
        if (index >= this.fastUrl.getUrlParams().size()) {
            return null;
        }
        return this.fastUrl.getUrlParams().get(index);
    }

    public FastRoute getFastRoute() {
        return fastRoute;
    }

    public FastOut getFastOut() {
        return fastOut;
    }

    public FastAction getForwarder() {
        return forwarder;
    }

    public boolean isLog() {
        return log;
    }

    public FastAction setLog(boolean log) {
        this.log = log;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public FastAction setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getRemoveIp() {
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
        return ip;
    }

    public final String getUserAgent() {
        String agent = getRequest().getHeader("User-Agent");
        return agent;
    }


    public FastAction check(String validator) {
        return fastCheck.check(validator);
    }


}
