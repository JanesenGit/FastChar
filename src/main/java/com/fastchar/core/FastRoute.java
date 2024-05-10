package com.fastchar.core;

import com.fastchar.annotation.AFastCheck;
import com.fastchar.asm.FastParameter;
import com.fastchar.exception.FastActionException;
import com.fastchar.exception.FastReturnException;
import com.fastchar.interfaces.IFastInterceptor;
import com.fastchar.interfaces.IFastRootInterceptor;
import com.fastchar.local.FastCharLocal;
import com.fastchar.out.FastOut;
import com.fastchar.out.FastOutForward;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.servlet.http.cache.FastResponseCacheConfig;
import com.fastchar.servlet.http.cache.FastResponseCacheInfo;
import com.fastchar.servlet.http.wrapper.FastHttpServletResponseWrapper;
import com.fastchar.utils.FastRequestUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public final class FastRoute {

    String route;
    Method method;
    private boolean beforeInvoked;
    boolean responseInvoked;
    private boolean afterInvoked;
    boolean actionLog = true;
    boolean interceptorBefore = true;
    boolean interceptorAfter = true;
    boolean hitCacheInfo = false;

    Set<String> crossAllowDomains = new HashSet<>();
    int priority;
    int firstMethodLineNumber;
    int lastMethodLineNumber;
    long beforeInterceptorUseTotal;
    long afterInterceptorUseTotal;
    Class<? extends FastOut<?>> returnOut;
    List<FastParameter> methodParameter = new ArrayList<>(5);
    Class<? extends FastAction> actionClass;
    FastResponseCacheConfig responseCacheConfig;
    List<FastInterceptorInfo<IFastRootInterceptor>> rootInterceptor = new ArrayList<>(5);
    List<FastInterceptorInfo<IFastInterceptor>> doBeforeInterceptor = new ArrayList<>(5);
    List<FastInterceptorInfo<IFastInterceptor>> doAfterInterceptor = new ArrayList<>(5);

    Set<String> httpMethods = new HashSet<>();
    Set<String> contentTypes = new HashSet<>();

    transient volatile FastAction fastAction;
    private transient int doBeforeIndex = -1;
    private transient int doAfterIndex = -1;

    private transient long beforeInterceptorTime;
    private transient long afterInterceptorTime;
    transient Date inTime;
    transient List<StackTraceElement> stackTraceElements = new ArrayList<>(16);
    transient FastHttpServletRequest request;
    transient FastHttpServletResponse response;
    transient FastUrl fastUrl;
    transient FastAction forwarder;

    FastRoute copy() {
        if (this.actionClass == null) {
            return null;
        }

        FastRoute fastRoute = new FastRoute();
        fastRoute.method = this.method;
        fastRoute.methodParameter = this.methodParameter;
        fastRoute.priority = this.priority;
        fastRoute.actionClass = this.actionClass;
        fastRoute.route = this.route;
        fastRoute.interceptorBefore = this.interceptorBefore;
        fastRoute.interceptorAfter = this.interceptorAfter;
        fastRoute.firstMethodLineNumber = this.firstMethodLineNumber;
        fastRoute.lastMethodLineNumber = this.lastMethodLineNumber;
        fastRoute.returnOut = this.returnOut;

        //以下属性需要构建新的对象
        fastRoute.crossAllowDomains = new HashSet<>(this.crossAllowDomains);
        fastRoute.rootInterceptor = new ArrayList<>(this.rootInterceptor);
        fastRoute.doBeforeInterceptor = new ArrayList<>(this.doBeforeInterceptor);
        fastRoute.doAfterInterceptor = new ArrayList<>(this.doAfterInterceptor);
        if (this.responseCacheConfig != null) {
            fastRoute.responseCacheConfig = this.responseCacheConfig.copy();
        }
        fastRoute.httpMethods = new HashSet<>(this.httpMethods);
        fastRoute.contentTypes = new HashSet<>(this.contentTypes);

        //!!!注意:  新增属性复制的时候，如果属性类型为引用类型一定要检查对象是否重复引用问题！！！
        return fastRoute;
    }

    boolean checkMethod(String method) {
        boolean hasMethods = false;
        for (String httpMethod : this.httpMethods) {
            httpMethod = httpMethod.replace(" ", "");
            if (FastStringUtils.isEmpty(httpMethod)) {
                continue;
            }
            hasMethods = true;
            if (httpMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return !hasMethods;
    }

    boolean checkContentType(String contentType) {
        if (FastStringUtils.isEmpty(contentType)) {
            return true;
        }
        String truthContentType = FastStringUtils.splitByWholeSeparator(contentType, ";")[0].replace(" ", "");
        boolean hasContentTypes = false;
        for (String contentTypeValue : this.contentTypes) {
            contentTypeValue = FastStringUtils.splitByWholeSeparator(contentTypeValue, ";")[0].replace(" ", "");
            if (FastStringUtils.isEmpty(contentTypeValue)) {
                continue;
            }
            hasContentTypes = true;
            if (contentTypeValue.equalsIgnoreCase(truthContentType)) {
                return true;
            }
        }
        return !hasContentTypes;
    }


    void release() {
        method = null;
        returnOut = null;
        responseCacheConfig = null;
        methodParameter = null;
        actionClass = null;
        forwarder = null;
        request = null;
        response = null;
        fastUrl = null;
        if (rootInterceptor != null) {
            rootInterceptor.clear();
        }
        rootInterceptor = null;

        if (doBeforeInterceptor != null) {
            doBeforeInterceptor.clear();
        }
        doBeforeInterceptor = null;

        if (doAfterInterceptor != null) {
            doAfterInterceptor.clear();
        }
        doAfterInterceptor = null;

        if (stackTraceElements != null) {
            stackTraceElements.clear();
        }
        stackTraceElements = null;

        if (fastAction != null) {
            fastAction.release();
        }
        fastAction = null;

        if (httpMethods != null) {
            httpMethods.clear();
        }
        httpMethods = null;

        if (contentTypes != null) {
            contentTypes.clear();
        }
        contentTypes = null;
    }

    void sortInterceptors() {
        Comparator<FastInterceptorInfo<?>> comparator = (o1, o2) -> {
            if (o1.priority > o2.priority) {
                return -1;
            }
            if (o1.priority < o2.priority) {
                return 1;
            }
            if (o1.index > o2.index) {
                return 1;
            }
            if (o1.index < o2.index) {
                return -1;
            }
            return 0;
        };
        doBeforeInterceptor.sort(comparator);
        doAfterInterceptor.sort(comparator);
    }

    void clearInterceptors() {
        rootInterceptor.clear();
        doBeforeInterceptor.clear();
        doAfterInterceptor.clear();
    }


    public void invoke() {
        try {
            Thread.currentThread().setName("FastChar-FastRoute [ " + actionClass.getName() + " : " + method.getName() + " ] [ " + System.currentTimeMillis() + " ]");

            if (fastAction == null) {
                synchronized (this) {
                    if (fastAction == null) {
                        fastAction = FastChar.getOverrides().newInstance(actionClass);
                        fastAction.request = request;
                        fastAction.response = response;
                        fastAction.servletContext = FastEngine.instance().getServletContext();
                        fastAction.fastUrl = fastUrl;
                        fastAction.fastRoute = this;
                        fastAction.forwarder = this.forwarder;
                        if (fastUrl.getParams() != null) {
                            fastAction.params.addAll(fastUrl.getParams());
                        }
                        if (this.forwarder != null) {
                            fastAction.params.addAll(forwarder.params);
                        }
                        FastChar.setThreadLocalAction(fastAction);
                    }
                }
            }

            if (responseCacheConfig != null && responseCacheConfig.isCache() && response instanceof FastHttpServletResponseWrapper) {
                FastHttpServletResponseWrapper responseWrapper = (FastHttpServletResponseWrapper) response;
                if (FastStringUtils.isEmpty(responseCacheConfig.getCacheTag())) {
                    responseCacheConfig.setCacheTag(actionClass.getName());
                }

                if (FastStringUtils.isEmpty(responseCacheConfig.getCacheKey())) {
                    responseCacheConfig.setCacheKey(FastChar.getSecurity().MD5_Encrypt(request.getMethod() +
                            request.getRequestURI() + FastRequestUtils.getRequestParamString(request)));
                }

                responseWrapper.setCacheConfig(responseCacheConfig);

                FastResponseCacheInfo cacheInfo = FastChar.getCache().get(responseCacheConfig.getCacheTag(), responseCacheConfig.getCacheKey());
                if (cacheInfo != null) {
                    if (cacheInfo.isTimeout()) {
                        FastChar.getCache().delete(responseCacheConfig.getCacheTag(), responseCacheConfig.getCacheKey());
                    } else {
                        hitCacheInfo = true;
                        beforeInvoked = true;
                        afterInvoked = true;
                        responseInvoked = true;
                        cacheInfo.response(request, response);
                        return;
                    }
                }
            }


            if (responseInvoked) {
                response();
                return;
            }

            doBeforeIndex++;
            if (doBeforeIndex == 0) {
                beforeInterceptorTime = System.currentTimeMillis();
            }
            if (interceptorBefore) {
                if (doBeforeIndex < doBeforeInterceptor.size()) {
                    beforeInvoked = false;
                    FastInterceptorInfo<IFastInterceptor> routeInterceptor = doBeforeInterceptor.get(doBeforeIndex);
                    IFastInterceptor fastInterceptor = FastChar.getOverrides().singleInstance(routeInterceptor.interceptor);
                    fastInterceptor.onInterceptor(fastAction);
                    return;
                }
            }

            beforeInterceptorUseTotal = System.currentTimeMillis() - beforeInterceptorTime;
            beforeInvoked = true;
            if (FastChar.getConstant().isDebug()) {
                stackTraceElements.addAll(Arrays.asList(Thread.currentThread().getStackTrace()));
            }
            List<Object> methodParams = getMethodParams(fastAction);
            if (responseInvoked) {
                return;
            }
            method.setAccessible(true);
            Object invoke = method.invoke(fastAction, methodParams.toArray());
            if (invoke != null && fastAction.fastOut == null) {
                if (invoke instanceof FastOut) {
                    fastAction.response((FastOut<?>) invoke);
                } else if (this.returnOut != null) {
                    FastOut<?> fastOut = FastChar.getOverrides().newInstance(this.returnOut);
                    if (fastOut != null) {
                        fastOut.setData(invoke);
                        fastAction.response(fastOut);
                    }
                } else if (invoke instanceof File) {
                    fastAction.responseFile((File) invoke);
                }
            }
        } catch (Throwable e) {
            Throwable throwable = e.getCause();
            if (throwable == null) {
                throwable = e;
            }
            if (throwable instanceof FastReturnException) {
                return;
            }
            responseException(throwable);
        } finally {
            if (fastAction != null) {
                if (!beforeInvoked) {
                    responseBeforeInterceptorException();
                }
                if (fastAction.fastOut == null && !hitCacheInfo) {
                    responseNone();
                }
            }
        }
    }

    private void responseBeforeInterceptorException() {
        beforeInvoked = true;
        actionLog = false;
        StackTraceElement stackTraceElement = null;
        for (int i = 0; i < doBeforeIndex + 1; i++) {
            FastInterceptorInfo<IFastInterceptor> routeInterceptor = doBeforeInterceptor.get(i);
            stackTraceElement = new StackTraceElement(
                    routeInterceptor.interceptor.getName(),
                    "onInterceptor",
                    routeInterceptor.interceptor.getSimpleName() + ".java",
                    routeInterceptor.firstMethodLineNumber
            );
            stackTraceElements.add(stackTraceElement);
        }
        if (stackTraceElement != null) {
            new FastActionException(FastChar.getLocal().getInfo(FastCharLocal.INTERCEPTOR_ERROR1, stackTraceElement.getClassName())
                    + "\n\tat " + stackTraceElement).printStackTrace();
            fastAction.response502(FastChar.getLocal().getInfo(FastCharLocal.INTERCEPTOR_ERROR1, stackTraceElement.getClassName()) + " at " + stackTraceElement);
        }
    }

    private void responseAfterInterceptorException() {
        afterInvoked = true;
        StackTraceElement stackTraceElement = null;
        for (int i = 0; i < doAfterIndex + 1; i++) {
            FastInterceptorInfo<IFastInterceptor> routeInterceptor = doAfterInterceptor.get(i);
            stackTraceElement = new StackTraceElement(
                    routeInterceptor.interceptor.getName(),
                    "onInterceptor",
                    routeInterceptor.interceptor.getSimpleName() + ".java",
                    routeInterceptor.firstMethodLineNumber
            );
            stackTraceElements.add(stackTraceElement);
        }
        if (stackTraceElement != null) {
            new FastActionException(FastChar.getLocal().getInfo(FastCharLocal.INTERCEPTOR_ERROR2, stackTraceElement.getClassName())
                    + "\n\tat " + stackTraceElement).printStackTrace();
            fastAction.response502(FastChar.getLocal().getInfo(FastCharLocal.INTERCEPTOR_ERROR2, stackTraceElement.getClassName()) + " at " + stackTraceElement);
        }
    }

    private void responseNone() {
        String position = new StackTraceElement(fastAction.getClass().getName(),
                getMethod().getName(),
                fastAction.getClass().getSimpleName() + ".java",
                firstMethodLineNumber).toString();

        new FastActionException(FastChar.getLocal().getInfo(FastCharLocal.ROUTE_ERROR1, FastStringUtils.wrap(getRoute(), "'")) +
                "\n\tat " + position).printStackTrace();

        fastAction.response502(FastChar.getLocal().getInfo(FastCharLocal.ROUTE_ERROR1, FastStringUtils.wrap(getRoute(), "'")) + " at " + position);
    }

    private void responseException(Throwable throwable) {
        FastChar.getLogger().error(fastAction != null ? fastAction.getClass() : getClass(), throwable);
        if (fastAction != null) {
            if (FastChar.getConstant().isDebug()) {
                stackTraceElements.addAll(Arrays.asList(throwable.getStackTrace()));
            }
            fastAction.response500(throwable);
        }
    }

    private List<Object> getMethodParams(FastAction action) throws Exception {
        List<Object> params = new ArrayList<>(5);
        for (FastParameter parameter : methodParameter) {
            for (Annotation annotation : parameter.getAnnotations()) {
                if (annotation.annotationType() == AFastCheck.class) {
                    AFastCheck aFastCheck = (AFastCheck) annotation;
                    List<Object> arguments = new ArrayList<>();
                    Collections.addAll(arguments, aFastCheck.arguments());
                    Object[] argumentsArray = arguments.toArray();
                    for (String validator : aFastCheck.value()) {
                        action.check(validator, argumentsArray);
                    }
                }
            }
            params.add(parseData(action, parameter));
        }
        return params;
    }


    private Object parseData(FastAction action, FastParameter parameter) throws Exception {
        return FastEngine.instance().getConverters().convertParam(action, parameter);
    }


    void response() {
        try {
            if (fastAction == null) {
                return;
            }
            responseInvoked = true;
            beforeInvoked = true;
            FastOut<?> outBase = fastAction.getFastOut();
            if (outBase == null) {
                responseNone();
                return;
            }

            doAfterIndex++;
            if (doAfterIndex == 0) {
                afterInterceptorTime = System.currentTimeMillis();
            }
            if (interceptorAfter) {
                if (doAfterIndex < doAfterInterceptor.size()) {
                    afterInvoked = false;
                    FastInterceptorInfo<IFastInterceptor> routeInterceptor = doAfterInterceptor.get(doAfterIndex);
                    IFastInterceptor fastInterceptor = FastChar.getOverrides().singleInstance(routeInterceptor.interceptor);
                    fastInterceptor.onInterceptor(fastAction);
                    return;
                }
            }
            afterInvoked = true;
            afterInterceptorUseTotal = System.currentTimeMillis() - afterInterceptorTime;
            outBase.setOutTime(new Date());
            if (FastChar.getConstant().isMarkers()) {
                fastAction.getResponse().setHeader("Powered-By", "FastChar " + FastConstant.FAST_CHAR_VERSION);
            }
            outBase.response(fastAction);
            //转发请求 取消日志打印
            if (!FastOutForward.class.isAssignableFrom(outBase.getClass())) {
                FastActionLogger.log(fastAction);
            }
        } catch (Throwable e) {
            Throwable throwable = e.getCause();
            if (throwable == null) {
                throwable = e;
            }
            if (throwable instanceof FastReturnException) {
                return;
            }
            responseException(throwable);
        } finally {
            if (!afterInvoked) {
                responseAfterInterceptorException();
            }
        }
    }


    void addBeforeInterceptor(FastInterceptorInfo<IFastInterceptor> routeInterceptor) {
        if (isBeforeInterceptor(routeInterceptor.interceptor.getName())) {
            return;
        }
        doBeforeInterceptor.add(routeInterceptor);
    }

    void addAfterInterceptor(FastInterceptorInfo<IFastInterceptor> routeInterceptor) {
        if (isAfterInterceptor(routeInterceptor.interceptor.getName())) {
            return;
        }
        doAfterInterceptor.add(routeInterceptor);
    }

    boolean isRootInterceptor(String className) {
        for (FastInterceptorInfo<IFastRootInterceptor> iFastRootInterceptorFastInterceptorInfo : rootInterceptor) {
            if (iFastRootInterceptorFastInterceptorInfo.interceptor.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    boolean isBeforeInterceptor(String className) {
        for (FastInterceptorInfo<IFastInterceptor> routeInterceptor : doBeforeInterceptor) {
            if (routeInterceptor.interceptor.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    boolean isAfterInterceptor(String className) {
        for (FastInterceptorInfo<IFastInterceptor> routeInterceptor : doAfterInterceptor) {
            if (routeInterceptor.interceptor.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }


    public Date getInTime() {
        return inTime;
    }

    public FastRoute setInTime(Date inTime) {
        this.inTime = inTime;
        return this;
    }

    public long getAfterInterceptorTime() {
        return afterInterceptorTime;
    }

    public FastRoute setAfterInterceptorTime(long afterInterceptorTime) {
        this.afterInterceptorTime = afterInterceptorTime;
        return this;
    }

    public long getBeforeInterceptorTime() {
        return beforeInterceptorTime;
    }

    public FastRoute setBeforeInterceptorTime(long beforeInterceptorTime) {
        this.beforeInterceptorTime = beforeInterceptorTime;
        return this;
    }

    public int getDoAfterIndex() {
        return doAfterIndex;
    }

    public FastRoute setDoAfterIndex(int doAfterIndex) {
        this.doAfterIndex = doAfterIndex;
        return this;
    }

    public int getDoBeforeIndex() {
        return doBeforeIndex;
    }

    public FastRoute setDoBeforeIndex(int doBeforeIndex) {
        this.doBeforeIndex = doBeforeIndex;
        return this;
    }

    public FastAction getFastAction() {
        return fastAction;
    }

    public FastRoute setFastAction(FastAction fastAction) {
        this.fastAction = fastAction;
        return this;
    }

    public List<StackTraceElement> getStackTraceElements() {
        return stackTraceElements;
    }

    public FastRoute setStackTraceElements(List<StackTraceElement> stackTraceElements) {
        this.stackTraceElements = stackTraceElements;
        return this;
    }

    public FastHttpServletRequest getRequest() {
        return request;
    }

    public FastRoute setRequest(FastHttpServletRequest request) {
        this.request = request;
        return this;
    }

    public FastHttpServletResponse getResponse() {
        return response;
    }

    public FastRoute setResponse(FastHttpServletResponse response) {
        this.response = response;
        return this;
    }

    public FastUrl getFastUrl() {
        return fastUrl;
    }

    public FastRoute setFastUrl(FastUrl fastUrl) {
        this.fastUrl = fastUrl;
        return this;
    }

    public FastAction getForwarder() {
        return forwarder;
    }

    public FastRoute setForwarder(FastAction forwarder) {
        this.forwarder = forwarder;
        return this;
    }

    public boolean isActionLog() {
        return actionLog;
    }

    public Method getMethod() {
        return method;
    }

    public String getRoute() {
        return route;
    }

    public boolean isBeforeInvoked() {
        return beforeInvoked;
    }

    public boolean isResponseInvoked() {
        return responseInvoked;
    }

    public boolean isAfterInvoked() {
        return afterInvoked;
    }

    public boolean isInterceptorBefore() {
        return interceptorBefore;
    }

    public boolean isInterceptorAfter() {
        return interceptorAfter;
    }

    public boolean isHitCacheInfo() {
        return hitCacheInfo;
    }

    public Set<String> getCrossAllowDomains() {
        return crossAllowDomains;
    }

    public int getPriority() {
        return priority;
    }

    public int getFirstMethodLineNumber() {
        return firstMethodLineNumber;
    }

    public int getLastMethodLineNumber() {
        return lastMethodLineNumber;
    }

    public long getBeforeInterceptorUseTotal() {
        return beforeInterceptorUseTotal;
    }

    public long getAfterInterceptorUseTotal() {
        return afterInterceptorUseTotal;
    }

    public Set<String> getContentTypes() {
        return contentTypes;
    }

    public Set<String> getHttpMethods() {
        return httpMethods;
    }

    public Class<? extends FastOut<?>> getReturnOut() {
        return returnOut;
    }

    public List<FastParameter> getMethodParameter() {
        return methodParameter;
    }

    public Class<? extends FastAction> getActionClass() {
        return actionClass;
    }

    public FastResponseCacheConfig getResponseCacheConfig() {
        return responseCacheConfig;
    }

    public List<FastInterceptorInfo<IFastRootInterceptor>> getRootInterceptor() {
        return rootInterceptor;
    }

    public List<FastInterceptorInfo<IFastInterceptor>> getDoBeforeInterceptor() {
        return doBeforeInterceptor;
    }

    public List<FastInterceptorInfo<IFastInterceptor>> getDoAfterInterceptor() {
        return doAfterInterceptor;
    }
}

