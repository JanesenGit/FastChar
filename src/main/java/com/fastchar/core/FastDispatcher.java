package com.fastchar.core;


import com.fastchar.annotation.*;
import com.fastchar.asm.FastParameter;
import com.fastchar.asm.FastMethodRead;
import com.fastchar.exception.FastActionException;
import com.fastchar.exception.FastReturnException;
import com.fastchar.exception.FastWebException;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.interfaces.IFastInterceptor;
import com.fastchar.interfaces.IFastRootInterceptor;

import com.fastchar.out.FastOut;
import com.fastchar.response.FastResponseCacheConfig;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastMethodUtils;
import com.fastchar.utils.FastStringUtils;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FastChar核心路由转发器
 * @author 沈建（Janesen）
 */
@SuppressWarnings("unchecked")
public final class FastDispatcher {

    static final ConcurrentHashMap<String, FastRoute> FAST_ROUTE_MAP = new ConcurrentHashMap<>();
    private static final Set<Class<?>> RESOLVED = new HashSet<>();

    static void initDispatcher() {
        FastEngine.instance().getInterceptors().sortRootInterceptor();
        RESOLVED.clear();
    }


    public static List<String> getClassRoutes(Class<? extends FastAction> targetClass) throws Exception {
        FastAction fastAction = targetClass.newInstance();
        FastMethodRead parameterConverter = new FastMethodRead();
        List<FastMethodRead.MethodLine> lineNumber = parameterConverter.getMethodLineNumber(targetClass, "getRoute");
        String classRoute = fastAction.getRoute();
        if (FastStringUtils.isEmpty(classRoute)) {
            int line = 1;
            if (lineNumber.size() > 0) {
                line = lineNumber.get(0).getLastLine();
            }
            throw new FastActionException(FastChar.getLocal().getInfo("Action_Error1") +
                    "\n\tat " + new StackTraceElement(targetClass.getName(), "getRoute",
                    targetClass.getSimpleName() + ".java", line));
        }
        List<String> classRoutes = new ArrayList<>();
        classRoutes.add(classRoute);
        if (targetClass.isAnnotationPresent(AFastRoute.class)) {
            AFastRoute fastRoute = targetClass.getAnnotation(AFastRoute.class);
            if (fastRoute.head()) {
                classRoutes.clear();
                for (String s : fastRoute.value()) {
                    String route = FastStringUtils.stripEnd(s, "/") + "/" +
                            FastStringUtils.stripStart(classRoute, "/");
                    if (classRoutes.contains(route)) {
                        continue;
                    }
                    classRoutes.add(route);
                }
            } else {
                for (String route : fastRoute.value()) {
                    if (classRoutes.contains(route)) {
                        continue;
                    }
                    classRoutes.add(route);
                }
            }
        }
        return classRoutes;
    }

    public static List<String> getClassMethodRoutes(Class<? extends FastAction> targetClass, String declaredMethodName) throws Exception {
        return getClassMethodRoutes(targetClass, targetClass.getDeclaredMethod(declaredMethodName));
    }

    public static List<String> getClassMethodRoutes(Class<? extends FastAction> targetClass, Method declaredMethod) throws Exception {
        List<String> classMethodRoutes = new ArrayList<>();
        if (Modifier.isStatic(declaredMethod.getModifiers())) {
            return classMethodRoutes;
        }
        if (Modifier.isTransient(declaredMethod.getModifiers())) {
            return classMethodRoutes;
        }
        if (Modifier.isAbstract(declaredMethod.getModifiers())) {
            return classMethodRoutes;
        }
        if (!Modifier.isPublic(declaredMethod.getModifiers())) {
            return classMethodRoutes;
        }
        if (declaredMethod.getName().equalsIgnoreCase("getRoute")) {
            return classMethodRoutes;
        }
        List<String> classRoutes = getClassRoutes(targetClass);
        List<String> methodRoutes = new ArrayList<>();
        methodRoutes.add(declaredMethod.getName());
        if (declaredMethod.isAnnotationPresent(AFastRoute.class)) {
            AFastRoute fastRoute = declaredMethod.getAnnotation(AFastRoute.class);
            if (fastRoute.head()) {
                methodRoutes.clear();
                for (String s : fastRoute.value()) {
                    String route = FastStringUtils.stripEnd(s, "/") + "/" + declaredMethod.getName();
                    if (methodRoutes.contains(route)) {
                        continue;
                    }
                    methodRoutes.add(route);
                }
            } else {
                for (String route : fastRoute.value()) {
                    if (methodRoutes.contains(route)) {
                        continue;
                    }
                    methodRoutes.add(route);
                }
            }
        }

        for (String route : classRoutes) {
            if (FastStringUtils.isEmpty(route)) continue;
            route = FastStringUtils.stripEnd(route, "/") + "/";
            for (String methodRoute : methodRoutes) {
                if (FastStringUtils.isEmpty(methodRoute)) continue;
                String classMethodRoute = route + FastStringUtils.strip(methodRoute, "/");
                if (classMethodRoutes.contains(classMethodRoute)) {
                    continue;
                }
                classMethodRoutes.add(classMethodRoute);
            }
        }
        return classMethodRoutes;
    }

    static boolean actionResolver(Class<? extends FastAction> actionClass) throws Exception {
        if (FastChar.getServletContext() == null) {
            return false;
        }
        if (RESOLVED.contains(actionClass)) {
            return true;
        }
        RESOLVED.add(actionClass);
        Class<? extends FastOut<?>> defaultOut = FastEngine.instance().getActions().getDefaultOut();

        FastMethodRead parameterConverter = new FastMethodRead();

        if (actionClass.isAnnotationPresent(AFastAction.class)) {
            AFastAction annotation = actionClass.getAnnotation(AFastAction.class);
            if (!annotation.value()) {//被禁止
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().warn(FastChar.getLocal().getInfo("Action_Error5", actionClass));
                }
                return false;
            }
        }
        if (actionClass.isAnnotationPresent(AFastResponse.class)) {
            AFastResponse response = actionClass.getAnnotation(AFastResponse.class);
            defaultOut = FastOut.convertType(response.value());
        }
        Method[] declaredMethods = actionClass.getDeclaredMethods();
        Map<String, Integer> methodCount = new HashMap<>();
        for (Method declaredMethod : declaredMethods) {
            if (Modifier.isStatic(declaredMethod.getModifiers())) {
                continue;
            }
            if (Modifier.isTransient(declaredMethod.getModifiers())) {
                continue;
            }
            if (Modifier.isAbstract(declaredMethod.getModifiers())) {
                continue;
            }
            if (!Modifier.isPublic(declaredMethod.getModifiers())) {
                continue;
            }
            if (!methodCount.containsKey(declaredMethod.getName())) {
                methodCount.put(declaredMethod.getName(), 0);
            }
            methodCount.put(declaredMethod.getName(), methodCount.get(declaredMethod.getName()) + 1);

            List<FastMethodRead.MethodLine> lines = new ArrayList<>();
            List<FastParameter> parameter = parameterConverter.getParameter(declaredMethod, lines);
            List<String> classMethodRoutes = getClassMethodRoutes(actionClass, declaredMethod);

            for (String classMethodRoute : classMethodRoutes) {
                FastRoute fastRoute = new FastRoute();
                fastRoute.actionClass = actionClass;
                fastRoute.method = declaredMethod;
                if (lines.size() > 0) {
                    fastRoute.firstMethodLineNumber = lines.get(methodCount.get(declaredMethod.getName()) - 1).getFirstLine() - 1;
                    fastRoute.lastMethodLineNumber = lines.get(methodCount.get(declaredMethod.getName()) - 1).getLastLine();
                } else {
                    fastRoute.firstMethodLineNumber = 1;
                    fastRoute.lastMethodLineNumber = 1;
                }
                fastRoute.methodParameter = parameter;
                fastRoute.route = classMethodRoute;
                fastRoute.crossAllowDomains = new HashSet<>(FastChar.getConstant().getCrossAllowDomains());
                if (actionClass.isAnnotationPresent(AFastPriority.class)) {
                    AFastPriority annotation = actionClass.getAnnotation(AFastPriority.class);
                    fastRoute.priority = annotation.value();
                }

                if (actionClass.isAnnotationPresent(AFastRoute.class)) {
                    AFastRoute annotation = actionClass.getAnnotation(AFastRoute.class);
                    fastRoute.crossAllowDomains.addAll(Arrays.asList(annotation.crossDomains()));
                    if (annotation.cross()) {
                        fastRoute.crossAllowDomains.add("*");
                    }
                }

                if (actionClass.isAnnotationPresent(AFastHttpMethod.class)) {
                    AFastHttpMethod annotation = actionClass.getAnnotation(AFastHttpMethod.class);
                    fastRoute.httpMethods.addAll(Arrays.asList(annotation.value()));
                }

                if (FastMethodUtils.isOverride(declaredMethod)) {
                    fastRoute.priority = Math.max(AFastPriority.P_NORMAL, fastRoute.priority);
                }

                if (declaredMethod.isAnnotationPresent(AFastPriority.class)) {
                    AFastPriority annotation = declaredMethod.getAnnotation(AFastPriority.class);
                    fastRoute.priority = annotation.value();
                }

                if (declaredMethod.isAnnotationPresent(AFastResponse.class)) {
                    AFastResponse annotation = declaredMethod.getAnnotation(AFastResponse.class);
                    defaultOut = FastOut.convertType(annotation.value());
                }

                if (declaredMethod.isAnnotationPresent(AFastHttpMethod.class)) {
                    AFastHttpMethod annotation = declaredMethod.getAnnotation(AFastHttpMethod.class);
                    fastRoute.httpMethods.addAll(Arrays.asList(annotation.value()));
                }

                if (declaredMethod.isAnnotationPresent(AFastRoute.class)) {
                    AFastRoute annotation = declaredMethod.getAnnotation(AFastRoute.class);
                    fastRoute.crossAllowDomains.addAll(Arrays.asList(annotation.crossDomains()));
                    if (annotation.cross()) {
                        fastRoute.crossAllowDomains.add("*");
                    }
                }

                if (declaredMethod.isAnnotationPresent(AFastCache.class)) {
                    AFastCache annotation = declaredMethod.getAnnotation(AFastCache.class);
                    FastResponseCacheConfig cacheConfig = new FastResponseCacheConfig();
                    cacheConfig.setCache(annotation.enable());
                    cacheConfig.setCacheKey(annotation.value());
                    cacheConfig.setTimeout(annotation.timeout());
                    if (annotation.checkClass()) {
                        if (!FastChar.getOverrides().check(IFastCache.class)) {
                            cacheConfig.setCache(false);
                        }
                    }
                    if (annotation.checkDebug()) {
                        if (FastChar.getConstant().isDebug()) {
                            cacheConfig.setCache(false);
                        }
                    }
                    fastRoute.responseCache = cacheConfig;
                }

                fastRoute.returnOut = defaultOut;

                initInterceptor(null, fastRoute, 0);
                initInterceptor(actionClass, fastRoute, 1);
                initInterceptor(declaredMethod, fastRoute, 2);

                if (FAST_ROUTE_MAP.containsKey(classMethodRoute)) {
                    FastRoute existFastRoute = FAST_ROUTE_MAP.get(classMethodRoute);
                    if (!FastClassUtils.isSameRefined(actionClass, existFastRoute.getActionClass())) {

                        StackTraceElement newStack = new StackTraceElement(actionClass.getName(), fastRoute.getMethod().getName(),
                                actionClass.getSimpleName() + ".java", fastRoute.firstMethodLineNumber);

                        StackTraceElement currStack = new StackTraceElement(existFastRoute.getActionClass().getName(), existFastRoute.getMethod().getName(),
                                existFastRoute.getActionClass().getSimpleName() + ".java", existFastRoute.firstMethodLineNumber);
                        if (fastRoute.getPriority() == existFastRoute.getPriority()) {
                            throw new FastActionException(FastChar.getLocal().getInfo("Action_Error2", "'" + classMethodRoute + "'") +
                                    "\n\tat " + newStack +
                                    "\n\tat " + currStack
                            );
                        }
                        if (fastRoute.getPriority() > existFastRoute.getPriority()) {
                            FastChar.getLog().warn(FastAction.class, FastChar.getLog().warnStyle(FastChar.getLocal().getInfo("Action_Error3",
                                    "'" + classMethodRoute + "'") + "\n\t\tnew at " + newStack + "\n\t\told at " + currStack));
                        } else {
                            FastChar.getLog().warn(FastAction.class, FastChar.getLog().warnStyle(FastChar.getLocal().getInfo("Action_Error3",
                                    "'" + classMethodRoute + "'") + "\n\t\tnew at " + currStack + "\n\t\told at " + newStack));
                            continue;
                        }
                    }
                }
                fastRoute.sortInterceptors();
                FAST_ROUTE_MAP.put(classMethodRoute, fastRoute);
                if (FastChar.getConstant().isLogRoute()) {
                    String logRote = classMethodRoute + "\n\t at " + new StackTraceElement(actionClass.getName(),
                            fastRoute.method.getName(),
                            actionClass.getSimpleName() + ".java", fastRoute.firstMethodLineNumber);
                    FastChar.getLog().info(actionClass, logRote);
                }
            }
        }
        if (FastAction.class.isAssignableFrom(actionClass.getSuperclass())) {
            if (actionClass.getSuperclass() != FastAction.class) {
                actionResolver((Class<? extends FastAction>) actionClass.getSuperclass());
            }
        }
        return true;
    }


    private static void initInterceptor(GenericDeclaration target, FastRoute fastRoute, int index) throws Exception {
        FastMethodRead read = new FastMethodRead();
        if (target != null) {
            AFastBefore beforeInterceptor = null;
            AFastAfter afterInterceptor = null;
            if (target instanceof Class<?>) {
                beforeInterceptor = ((Class<?>) target).getAnnotation(AFastBefore.class);
                afterInterceptor = ((Class<?>) target).getAnnotation(AFastAfter.class);
            } else if (target instanceof Method) {
                beforeInterceptor = ((Method) target).getAnnotation(AFastBefore.class);
                afterInterceptor = ((Method) target).getAnnotation(AFastAfter.class);
            }
            if (beforeInterceptor != null) {
                for (Class<? extends IFastInterceptor> aClass : beforeInterceptor.value()) {
                    List<FastMethodRead.MethodLine> lineNumber = read.getMethodLineNumber(aClass, "onInterceptor");
                    FastRoute.RouteInterceptor routeInterceptor = new FastRoute.RouteInterceptor();
                    routeInterceptor.index = index;
                    routeInterceptor.priority = beforeInterceptor.priority();
                    routeInterceptor.interceptorClass = aClass;
                    if (lineNumber.size() > 0) {
                        routeInterceptor.firstMethodLineNumber = lineNumber.get(0).getFirstLine();
                        routeInterceptor.lastMethodLineNumber = lineNumber.get(0).getLastLine();
                    }
                    fastRoute.addBeforeInterceptor(routeInterceptor);
                }
            }

            if (afterInterceptor != null) {
                for (Class<? extends IFastInterceptor> aClass : afterInterceptor.value()) {
                    List<FastMethodRead.MethodLine> lineNumber = read.getMethodLineNumber(aClass, "onInterceptor");
                    FastRoute.RouteInterceptor routeInterceptor = new FastRoute.RouteInterceptor();
                    routeInterceptor.index = index;
                    routeInterceptor.priority = afterInterceptor.priority();
                    routeInterceptor.interceptorClass = aClass;
                    if (lineNumber.size() > 0) {
                        routeInterceptor.firstMethodLineNumber = lineNumber.get(0).getFirstLine();
                        routeInterceptor.lastMethodLineNumber = lineNumber.get(0).getLastLine();
                    }
                    fastRoute.addAfterInterceptor(routeInterceptor);
                }
            }
        } else {
            List<FastInterceptors.InterceptorInfo<IFastInterceptor>> beforeInterceptors = FastEngine.instance().getInterceptors().getBeforeInterceptors(fastRoute.getRoute());
            for (FastInterceptors.InterceptorInfo<IFastInterceptor> beforeInterceptor : beforeInterceptors) {
                List<FastMethodRead.MethodLine> lineNumber = read.getMethodLineNumber(beforeInterceptor.getInterceptor(), "onInterceptor");
                FastRoute.RouteInterceptor routeInterceptor = new FastRoute.RouteInterceptor();
                routeInterceptor.index = index;
                routeInterceptor.priority = beforeInterceptor.getPriority();
                routeInterceptor.interceptorClass = beforeInterceptor.getInterceptor();
                if (lineNumber.size() > 0) {
                    routeInterceptor.firstMethodLineNumber = lineNumber.get(0).getFirstLine();
                    routeInterceptor.lastMethodLineNumber = lineNumber.get(0).getLastLine();
                }
                fastRoute.addBeforeInterceptor(routeInterceptor);
            }

            List<FastInterceptors.InterceptorInfo<IFastInterceptor>> afterInterceptors = FastEngine.instance().getInterceptors().getAfterInterceptors(fastRoute.getRoute());
            for (FastInterceptors.InterceptorInfo<IFastInterceptor> afterInterceptor : afterInterceptors) {
                List<FastMethodRead.MethodLine> lineNumber = read.getMethodLineNumber(afterInterceptor.getInterceptor(), "onInterceptor");
                FastRoute.RouteInterceptor routeInterceptor = new FastRoute.RouteInterceptor();
                routeInterceptor.index = index;
                routeInterceptor.priority = afterInterceptor.getPriority();
                routeInterceptor.interceptorClass = afterInterceptor.getInterceptor();
                if (lineNumber.size() > 0) {
                    routeInterceptor.firstMethodLineNumber = lineNumber.get(0).getFirstLine();
                    routeInterceptor.lastMethodLineNumber = lineNumber.get(0).getLastLine();
                }
                fastRoute.addAfterInterceptor(routeInterceptor);
            }
        }
    }


    private FilterChain filterChain;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String contentUrl;
    private List<Class<? extends IFastRootInterceptor>> rootInterceptor;
    private int interceptorIndex = -1;
    private FastAction forwarder;

    public FastDispatcher(HttpServletRequest request, HttpServletResponse response) {
        this(null, request, response);
    }

    public FastDispatcher(FilterChain filterChain, HttpServletRequest request, HttpServletResponse response) {
        this.filterChain = filterChain;
        this.request = request;
        this.response = response;
        this.init();
    }

    private void init() {
        String requestUrl = request.getRequestURL().toString();
        this.contentUrl = FastUrlParser.getContentPath(requestUrl);
        this.rootInterceptor = FastEngine.instance().getInterceptors().getRootInterceptors(contentUrl);
        this.interceptorIndex = -1;
    }

    public FilterChain getFilterChain() {
        return filterChain;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public FastDispatcher setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
        return this;
    }

    public FastAction getForwarder() {
        return forwarder;
    }

    public FastDispatcher setForwarder(FastAction forwarder) {
        this.forwarder = forwarder;
        return this;
    }

    /**
     * 验证路径是否可被转发到FastChar
     *
     * @param contentPath 路径
     * @return 布尔值
     */
    private boolean validateUrl(String contentPath) {
        List<String> excludeUrls = FastChar.getActions().getExcludeUrls();
        for (String excludeUrl : excludeUrls) {
            if (FastStringUtils.matches(excludeUrl, contentPath) || FastStringUtils.matches(excludeUrl, contentPath + "/")) {
                return false;
            }
        }
        return true;
    }


    public static void flush() {
        List<String> waitRemove = new ArrayList<>();
        for (String s : FAST_ROUTE_MAP.keySet()) {
            if (FastClassUtils.isRelease(FAST_ROUTE_MAP.get(s).actionClass)) {
                waitRemove.add(s);

                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().warn(FastDispatcher.class,
                            FastChar.getLocal().getInfo("Route_Error4", s));
                }
            }
        }
        for (String s : waitRemove) {
            FAST_ROUTE_MAP.remove(s);
        }
        RESOLVED.clear();
    }

    public void invoke() throws FastWebException {
        try {
            if (!validateUrl(contentUrl)) {
                doFilter();
                return;
            }

            List<FastUrl> parse = FastUrlParser.parse(contentUrl);
            if (request.getMethod().equalsIgnoreCase("options")) {
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().info("OPTIONS REQUEST : " + request.getRequestURL());
                }
                boolean hasCross = false;
                boolean hasRoute = false;
                for (FastUrl fastUrl : parse) {
                    FastRoute fastRoute = FAST_ROUTE_MAP.get(fastUrl.getMethodRoute());
                    if (fastRoute != null) {
                        hasCross = initCrossDomain(fastRoute);
                        hasRoute = true;
                        break;
                    }
                    fastRoute = FAST_ROUTE_MAP.get(fastUrl.getMethodRouteIndex());
                    if (fastRoute != null) {
                        hasCross = initCrossDomain(fastRoute);
                        hasRoute = true;
                        break;
                    }
                }
                if (!hasCross) {
                    if (FastChar.getConstant().isDebug()) {
                        String errorInfo = "CROSS-DOMAIN DISABLED-URL : " + request.getRequestURL() +
                                "\n\t\tREQUEST-ORIGIN : " + request.getHeader("origin");
                        FastChar.getLog().error(errorInfo);
                    }
                }
                if (!hasRoute) {
                    if (FastChar.getConstant().isDebug()) {
                        FastChar.getLog().error("NOT FOUND : " + request.getRequestURL());
                    }
                }
                response.setStatus(200);
                try (PrintWriter writer = response.getWriter()) {
                    writer.write(0);
                    writer.flush();
                }
                return;
            }

            interceptorIndex++;
            if (interceptorIndex < rootInterceptor.size()) {
                IFastRootInterceptor interceptor = FastChar.getOverrides().singleInstance(rootInterceptor.get(interceptorIndex));
                interceptor.onInterceptor(request, response, this);
                return;
            }

            Date inTime = new Date();
            for (int i = 0; i < parse.size(); i++) {
                FastUrl fastUrl = parse.get(i);
                FastRoute baseRoute = FAST_ROUTE_MAP.get(fastUrl.getMethodRoute());
                if (baseRoute != null) {
                    FastRoute fastRoute = baseRoute.copy();
                    if (fastRoute == null) {
                        if (FastChar.getConstant().isDebug()) {
                            FastChar.getLog().warn(FastDispatcher.class,
                                    FastChar.getLocal().getInfo("Route_Error2", fastUrl.getMethodRoute()));
                        }
                        continue;
                    }
                    try {
                        if (!fastRoute.checkMethod(request.getMethod())) {
                            if (FastChar.getConstant().isDebug()) {
                                FastChar.getLog().warn(FastDispatcher.class,
                                        FastChar.getLocal().getInfo("Route_Error3", fastUrl.getMethodRoute(), request.getMethod()));
                            }
                            response404(inTime);
                            return;
                        }
                        fastRoute.inTime = inTime;
                        fastRoute.request = request;
                        fastRoute.response = response;
                        fastRoute.fastUrl = fastUrl;
                        fastRoute.forwarder = forwarder;
                        fastRoute.rootInterceptor = rootInterceptor;
                        if (FastChar.getConstant().isDebug()) {
                            fastRoute.stackTraceElements.addAll(Arrays.asList(Thread.currentThread().getStackTrace()));
                        }
                        initCrossDomain(fastRoute);
                        fastRoute.invoke();
                    } finally {
                        fastRoute.release();
                    }
                    return;
                }
                FastRoute baseRouteIndex = FAST_ROUTE_MAP.get(fastUrl.getMethodRouteIndex());
                if (baseRouteIndex != null) {
                    FastRoute fastRoute = baseRouteIndex.copy();
                    if (fastRoute == null) {
                        FastChar.getLog().warn(FastDispatcher.class,
                                FastChar.getLocal().getInfo("Route_Error2", fastUrl.getMethodRouteIndex()));
                        continue;
                    }
                    try {
                        fastRoute.inTime = inTime;
                        fastRoute.request = request;
                        fastRoute.response = response;
                        fastRoute.fastUrl = fastUrl;
                        fastRoute.forwarder = forwarder;
                        fastRoute.rootInterceptor = rootInterceptor;
                        if (FastChar.getConstant().isDebug()) {
                            fastRoute.stackTraceElements.addAll(Arrays.asList(Thread.currentThread().getStackTrace()));
                        }
                        initCrossDomain(fastRoute);
                        fastRoute.invoke();
                    } finally {
                        fastRoute.release();
                    }
                    return;
                }
                if (i == 0) {
                    if (contentUrl.lastIndexOf(".") > 0) {
                        doFilter();
                        return;
                    }
                }
            }
            response404(inTime);
        } catch (Throwable e) {
            Throwable throwable = e.getCause();
            if (throwable == null) {
                throwable = e;
            }
            if (throwable instanceof FastReturnException) {
                return;
            }
            throw new FastWebException(e);
        } finally {
            FastChar.removeThreadLocalAction();
        }
    }


    private boolean initCrossDomain(FastRoute route) {
        boolean cross = false;
        String origin = getRequest().getHeader("origin");
        if (FastStringUtils.isEmpty(origin)) {
            return false;
        }
        Set<String> crossAllowDomains = route.crossAllowDomains;
        String[] split = origin.split(";");
        for (String address : split) {
            if (!cross) {
                for (String crossAllowDomain : crossAllowDomains) {
                    if (FastStringUtils.matches(crossAllowDomain, address)) {
                        cross = true;
                        break;
                    }
                }
            }
        }

        if (cross) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "3600");
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
            response.setHeader("Access-Control-Allow-Headers", FastStringUtils.join(FastChar.getConstant().getCrossHeaders(), ","));
        }
        return cross;
    }

    private void response404(Date inTime) {
        contentUrl = FastStringUtils.stripEnd(contentUrl, "/");
        FastRoute fastRoute404 = new FastRoute();
        try {
            fastRoute404.inTime = inTime;
            fastRoute404.route = contentUrl;

            FastAction fastErrorAction = new FastAction() {
                @Override
                public String getRoute() {
                    return null;
                }
            };
            fastRoute404.fastAction = fastErrorAction;
            fastErrorAction.fastRoute = fastRoute404;
            fastErrorAction.request = request;
            fastErrorAction.response = response;
            fastErrorAction.fastUrl = new FastUrl();
            if (FastStringUtils.isEmpty(contentUrl)) {
                contentUrl = "/";
            }
            fastErrorAction.response404("the route '" + contentUrl + "' not found!");
        } finally {
            fastRoute404.release();
        }
    }

    private void doFilter() throws Exception {
        if (this.filterChain != null) {
            this.filterChain.doFilter(this.request, this.response);
        }
    }
}
