package com.fastchar.core;

import com.fastchar.annotation.AFastCheck;
import com.fastchar.asm.FastParameter;
import com.fastchar.exception.FastActionException;
import com.fastchar.response.FastResponseCacheConfig;
import com.fastchar.response.FastResponseCacheInfo;
import com.fastchar.response.FastResponseWrapper;
import com.fastchar.exception.FastReturnException;
import com.fastchar.interfaces.IFastInterceptor;
import com.fastchar.interfaces.IFastRootInterceptor;
import com.fastchar.out.FastOut;
import com.fastchar.out.FastOutForward;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastStringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

public final class FastRoute {

    String route;
    Method method;
    private boolean beforeInvoked;
    boolean responseInvoked;
    private  boolean afterInvoked;
    boolean actionLog = true;
    Set<String> crossAllowDomains = new HashSet<>();
    int priority;
    int firstMethodLineNumber;
    int lastMethodLineNumber;
    long beforeInterceptorUseTotal;
    long afterInterceptorUseTotal;
    Class<? extends FastOut<?>> returnOut;
    List<FastParameter> methodParameter = new ArrayList<>();
    Class<? extends FastAction> actionClass;
    FastResponseCacheConfig responseCache;
    List<Class<? extends IFastRootInterceptor>> rootInterceptor = new ArrayList<>();
    List<RouteInterceptor> doBeforeInterceptor = new ArrayList<>();
    List<RouteInterceptor> doAfterInterceptor = new ArrayList<>();

    Set<String> httpMethods = new HashSet<>();

    transient FastAction fastAction;
    private transient int doBeforeIndex = -1;
    private transient int doAfterIndex = -1;

    private transient long beforeInterceptorTime;
    private transient long afterInterceptorTime;
    transient Date inTime;
    transient List<StackTraceElement> stackTraceElements = new ArrayList<>();
    transient HttpServletRequest request;
    transient HttpServletResponse response;
    transient FastUrl fastUrl;
    transient FastAction forwarder;

    FastRoute copy() {
        if (this.actionClass == null || FastClassUtils.isRelease(this.actionClass)) {
            return null;
        }

        FastRoute fastRoute = new FastRoute();
        fastRoute.method = this.method;
        fastRoute.methodParameter = this.methodParameter;
        fastRoute.priority = this.priority;
        fastRoute.actionClass = this.actionClass;
        fastRoute.route = this.route;
        fastRoute.crossAllowDomains = this.crossAllowDomains;
        fastRoute.returnOut = this.returnOut;
        fastRoute.doBeforeInterceptor = new ArrayList<>(this.doBeforeInterceptor);
        fastRoute.doAfterInterceptor = new ArrayList<>(this.doAfterInterceptor);
        fastRoute.firstMethodLineNumber = this.firstMethodLineNumber;
        fastRoute.lastMethodLineNumber = this.lastMethodLineNumber;
        fastRoute.responseCache = this.responseCache;
        fastRoute.httpMethods = this.httpMethods;
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

    void release() {
        method = null;
        returnOut = null;
        responseCache = null;
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
    }

    void sortInterceptors() {
        Comparator<RouteInterceptor> comparator = new Comparator<RouteInterceptor>() {
            @Override
            public int compare(RouteInterceptor o1, RouteInterceptor o2) {
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
            }
        };
        Collections.sort(doBeforeInterceptor, comparator);
        Collections.sort(doAfterInterceptor, comparator);
    }


    public void invoke() {
        try {
            if (responseCache != null && responseCache.isCache()) {
                responseCache.setCacheTag(actionClass.getName());
                if (FastStringUtils.isEmpty(responseCache.getCacheKey())) {
                    responseCache.setCacheKey(FastChar.getSecurity().MD5_Encrypt(request.getMethod() + request.getRequestURI() + request.getQueryString()));
                }
                FastResponseWrapper.setCacheInfo(responseCache);
                FastResponseCacheInfo cacheInfo = FastChar.getCache().get(responseCache.getCacheTag(), responseCache.getCacheKey());
                if (cacheInfo != null && !cacheInfo.isTimeout()) {
                    cacheInfo.response(request, response);
                    return;
                }
            } else {
                FastResponseWrapper.setCacheInfo(null);
            }


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
            }
            if (responseInvoked) {
                response();
                return;
            }

            doBeforeIndex++;
            if (doBeforeIndex == 0) {
                beforeInterceptorTime = System.currentTimeMillis();
            }
            if (doBeforeIndex < doBeforeInterceptor.size()) {
                beforeInvoked = false;
                RouteInterceptor routeInterceptor = doBeforeInterceptor.get(doBeforeIndex);
                IFastInterceptor fastInterceptor = FastChar.getOverrides().singleInstance(routeInterceptor.interceptorClass);
                fastInterceptor.onInterceptor(fastAction);
                return;
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
                if (fastAction.fastOut == null) {
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
            RouteInterceptor routeInterceptor = doBeforeInterceptor.get(i);
            stackTraceElement = new StackTraceElement(
                    routeInterceptor.interceptorClass.getName(),
                    "onInterceptor",
                    routeInterceptor.interceptorClass.getSimpleName() + ".java",
                    routeInterceptor.firstMethodLineNumber
            );
            stackTraceElements.add(stackTraceElement);
        }
        if (stackTraceElement != null) {
            new FastActionException(FastChar.getLocal().getInfo("Interceptor_Error1", stackTraceElement.getClassName())
                    + "\n\tat " + stackTraceElement).printStackTrace();
            fastAction.response502(FastChar.getLocal().getInfo("Interceptor_Error1", stackTraceElement.getClassName()) + " at " + stackTraceElement);
        }
    }

    private void responseAfterInterceptorException() {
        afterInvoked = true;
        StackTraceElement stackTraceElement = null;
        for (int i = 0; i < doAfterIndex + 1; i++) {
            RouteInterceptor routeInterceptor = doAfterInterceptor.get(i);
            stackTraceElement = new StackTraceElement(
                    routeInterceptor.interceptorClass.getName(),
                    "onInterceptor",
                    routeInterceptor.interceptorClass.getSimpleName() + ".java",
                    routeInterceptor.firstMethodLineNumber
            );
            stackTraceElements.add(stackTraceElement);
        }
        if (stackTraceElement != null) {
            new FastActionException(FastChar.getLocal().getInfo("Interceptor_Error2", stackTraceElement.getClassName())
                    + "\n\tat " + stackTraceElement).printStackTrace();
            fastAction.response502(FastChar.getLocal().getInfo("Interceptor_Error2", stackTraceElement.getClassName()) + " at " + stackTraceElement);
        }
    }

    private void responseNone() {
        String position = new StackTraceElement(fastAction.getClass().getName(),
                getMethod().getName(),
                fastAction.getClass().getSimpleName() + ".java",
                firstMethodLineNumber).toString();

        new FastActionException(FastChar.getLocal().getInfo("Route_Error1", FastStringUtils.wrap(getRoute(), "'")) +
                "\n\tat " + position).printStackTrace();

        fastAction.response502(FastChar.getLocal().getInfo("Route_Error1", FastStringUtils.wrap(getRoute(), "'")) + " at " + position);
    }

    private void responseException(Throwable throwable) {
        throwable.printStackTrace();
        if (fastAction != null) {
            if (FastChar.getConstant().isDebug()) {
                stackTraceElements.addAll(Arrays.asList(throwable.getStackTrace()));
            }
            fastAction.response500(throwable);
        }
    }

    private List<Object> getMethodParams(FastAction action) throws Exception {
        List<Object> params = new ArrayList<>();
        for (FastParameter parameter : methodParameter) {
            for (Annotation annotation : parameter.getAnnotations()) {
                if (annotation.annotationType() == AFastCheck.class) {
                    AFastCheck aFastCheck = (AFastCheck) annotation;
                    for (String s : aFastCheck.value()) {
                        action.check(s);
                    }
                }
            }
            params.add(parseData(action, parameter));
        }
        return params;
    }


    private Object parseData(FastAction action, FastParameter parameter) throws Exception {
        return FastEngine.instance().getConverters()
                .convertParam(action, parameter);
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
            if (doAfterIndex < doAfterInterceptor.size()) {
                afterInvoked = false;
                RouteInterceptor routeInterceptor = doAfterInterceptor.get(doAfterIndex);
                IFastInterceptor fastInterceptor = FastChar.getOverrides().singleInstance(routeInterceptor.interceptorClass);
                fastInterceptor.onInterceptor(fastAction);
                return;
            }
            afterInvoked = true;
            afterInterceptorUseTotal = System.currentTimeMillis() - afterInterceptorTime;
            outBase.setOutTime(new Date());
            fastAction.getResponse().setHeader("Powered-By", "FastChar " + FastConstant.FastCharVersion);
            outBase.response(fastAction);
            //转发请求 取消日志打印
            if (!FastOutForward.class.isAssignableFrom(outBase.getClass())) {
                FastRequestLog.log(fastAction);
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


    public String getRoute() {
        return route;
    }


    public Method getMethod() {
        return method;
    }


    public Class<? extends FastAction> getActionClass() {
        return actionClass;
    }


    public int getPriority() {
        return priority;
    }


    public Date getInTime() {
        return inTime;
    }


    void addBeforeInterceptor(RouteInterceptor routeInterceptor) {
        if (isBeforeInterceptor(routeInterceptor.interceptorClass.getName())) {
            return;
        }
        doBeforeInterceptor.add(routeInterceptor);
    }

    void addAfterInterceptor(RouteInterceptor routeInterceptor) {
        if (isAfterInterceptor(routeInterceptor.interceptorClass.getName())) {
            return;
        }
        doAfterInterceptor.add(routeInterceptor);
    }


    boolean isRootInterceptor(String className) {
        for (Class<? extends IFastRootInterceptor> aClass : rootInterceptor) {
            if (aClass.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    boolean isBeforeInterceptor(String className) {
        for (RouteInterceptor routeInterceptor : doBeforeInterceptor) {
            if (routeInterceptor.interceptorClass.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    boolean isAfterInterceptor(String className) {
        for (RouteInterceptor routeInterceptor : doAfterInterceptor) {
            if (routeInterceptor.interceptorClass.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }


     static class RouteInterceptor {
        int priority;
        int index;
        int firstMethodLineNumber;
        int lastMethodLineNumber;
        Class<? extends IFastInterceptor> interceptorClass;
    }

}

