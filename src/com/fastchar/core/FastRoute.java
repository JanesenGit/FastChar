package com.fastchar.core;

import com.fastchar.annotation.AFastCheck;
import com.fastchar.asm.FastParameter;
import com.fastchar.exception.FastReturnException;
import com.fastchar.interfaces.IFastInterceptor;
import com.fastchar.interfaces.IFastRootInterceptor;
import com.fastchar.out.FastOut;
import com.fastchar.out.FastOutFile;
import com.fastchar.out.FastOutForward;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FastRoute {

    String route;
    Method method;
    boolean beforeInvoked;
    boolean responseInvoked;
    boolean afterInvoked;
    boolean actionLog = true;
    int priority;
    int firstMethodLineNumber;
    int lastMethodLineNumber;
    Class<? extends FastOut> returnOut;
    List<FastParameter> methodParameter = new ArrayList<>();
    Class<? extends FastAction> actionClass;
    List<Class<? extends IFastRootInterceptor>> rootInterceptor = new ArrayList<>();
    private List<RouteInterceptor> doBeforeInterceptor = new ArrayList<>();
    private List<RouteInterceptor> doAfterInterceptor = new ArrayList<>();

    transient FastAction fastAction;
    private transient int doBeforeIndex = -1;
    private transient int doAfterIndex = -1;

    transient Date inTime;
    transient List<StackTraceElement> stackTraceElements = new ArrayList<>();
    transient HttpServletRequest request;
    transient HttpServletResponse response;
    transient FastUrl fastUrl;
    transient FastAction forwarder;

    FastRoute copy() {
        FastRoute fastRoute = new FastRoute();
        fastRoute.method = this.method;
        fastRoute.methodParameter = this.methodParameter;
        fastRoute.priority = this.priority;
        fastRoute.actionClass = this.actionClass;
        fastRoute.route = this.route;
        fastRoute.returnOut = this.returnOut;
        fastRoute.doBeforeInterceptor = this.doBeforeInterceptor;
        fastRoute.doAfterInterceptor = this.doAfterInterceptor;
        fastRoute.firstMethodLineNumber = this.firstMethodLineNumber;
        fastRoute.lastMethodLineNumber = this.lastMethodLineNumber;
        return fastRoute;
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
            if (fastAction == null) {
                fastAction = actionClass.newInstance();
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
            if (doBeforeIndex < doBeforeInterceptor.size()) {
                beforeInvoked = false;
                RouteInterceptor routeInterceptor = doBeforeInterceptor.get(doBeforeIndex);
                IFastInterceptor fastInterceptor = routeInterceptor.interceptorClass.newInstance();
                fastInterceptor.onInterceptor(fastAction);
                return;
            }
            beforeInvoked = true;
            stackTraceElements.addAll(Arrays.asList(Thread.currentThread().getStackTrace()));
            List<Object> methodParams = getMethodParams(fastAction);
            if (responseInvoked) {
                return;
            }
            if (methodParams != null) {
                Object invoke = method.invoke(fastAction, methodParams.toArray());
                if (invoke != null && fastAction.fastOut == null) {
                    if (invoke instanceof FastOut) {
                        fastAction.response((FastOut) invoke);
                    } else if (this.returnOut != null) {
                        FastOut fastOut = FastChar.getOverrides().newInstance(this.returnOut);
                        if (fastOut != null) {
                            fastOut.setData(invoke);
                            fastAction.response(fastOut);
                        }
                    } else if (invoke instanceof File) {
                        fastAction.response(FastChar.getOverrides().newInstance(FastOutFile.class).setData(invoke));
                    }
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
            throwable.printStackTrace();
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
            fastAction.response502(FastChar.getLocal().getInfo("Interceptor_Error2", stackTraceElement.getClassName()) + " at " + stackTraceElement);
        }
    }

    private void responseNone() {
        String position = new StackTraceElement(fastAction.getClass().getName(),
                getMethod().getName(),
                fastAction.getClass().getSimpleName() + ".java",
                firstMethodLineNumber).toString();
        fastAction.response502(FastChar.getLocal().getInfo("Route_Error1", getRoute()) + " at " + position);
    }

    private void responseException(Throwable throwable) {
        if (fastAction != null) {
            stackTraceElements.addAll(Arrays.asList(throwable.getStackTrace()));
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
            FastOut outBase = fastAction.getFastOut();
            if (outBase == null) {
                responseNone();
                return;
            }
            doAfterIndex++;
            if (doAfterIndex < doAfterInterceptor.size()) {
                afterInvoked = false;
                RouteInterceptor routeInterceptor = doAfterInterceptor.get(doAfterIndex);
                IFastInterceptor fastInterceptor = routeInterceptor.interceptorClass.newInstance();
                fastInterceptor.onInterceptor(fastAction);
                return;
            }
            afterInvoked = true;
            outBase.setOutTime(new Date());
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
            throwable.printStackTrace();
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


    public void addBeforeInterceptor(RouteInterceptor routeInterceptor) {
        doBeforeInterceptor.add(routeInterceptor);
    }

    public void addAfterInterceptor(RouteInterceptor routeInterceptor) {
        doAfterInterceptor.add(routeInterceptor);
    }


    public boolean isRootInterceptor(String className) {
        for (Class<? extends IFastRootInterceptor> aClass : rootInterceptor) {
            if (aClass.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBeforeInterceptor(String className) {
        for (RouteInterceptor routeInterceptor : doBeforeInterceptor) {
            if (routeInterceptor.interceptorClass.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAfterInterceptor(String className) {
        for (RouteInterceptor routeInterceptor : doAfterInterceptor) {
            if (routeInterceptor.interceptorClass.getName().equals(className)) {
                return true;
            }
        }
        return false;
    }


    public static class RouteInterceptor {
        int priority;
        int index;
        int firstMethodLineNumber;
        int lastMethodLineNumber;
        Class<? extends IFastInterceptor> interceptorClass;
    }
}

