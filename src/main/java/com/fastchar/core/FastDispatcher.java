package com.fastchar.core;


import com.fastchar.annotation.*;
import com.fastchar.asm.FastParameter;
import com.fastchar.exception.FastActionException;
import com.fastchar.exception.FastReturnException;
import com.fastchar.exception.FastWebException;
import com.fastchar.interfaces.IFastCache;
import com.fastchar.interfaces.IFastInterceptor;
import com.fastchar.interfaces.IFastMethodRead;
import com.fastchar.interfaces.IFastRootInterceptor;
import com.fastchar.local.FastCharLocal;
import com.fastchar.out.FastOut;
import com.fastchar.servlet.FastFilterChain;
import com.fastchar.servlet.FastHttpHeaders;
import com.fastchar.servlet.http.FastHttpServletRequest;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.servlet.http.cache.FastResponseCacheConfig;
import com.fastchar.utils.*;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FastChar核心路由转发器
 *
 * @author 沈建（Janesen）
 */
@SuppressWarnings("unchecked")
public final class FastDispatcher {

    static final ConcurrentHashMap<String, FastRoute> FAST_ROUTE_MAP = new ConcurrentHashMap<>(16);
    static final Set<Class<?>> RESOLVED = new HashSet<>(16);

    static final String THREAD_NAME_PREFIX = "FastChar-FastDispatcher";

    synchronized static void initDispatcher() {
        RESOLVED.clear();
    }


    public static List<String> getClassRoutes(Class<? extends FastAction> targetClass) throws Exception {
        FastAction fastAction = targetClass.newInstance();
        IFastMethodRead parameterConverter = FastChar.getOverrides().newInstance(IFastMethodRead.class);

        List<IFastMethodRead.MethodLine> lineNumber = parameterConverter.getMethodLineNumber(targetClass, "getRoute");
        String classRoute = fastAction.getRoute();
        if (FastStringUtils.isEmpty(classRoute)) {
            int line = 1;
            if (!lineNumber.isEmpty()) {
                line = lineNumber.get(0).getLastLine();
            }
            throw new FastActionException(FastChar.getLocal().getInfo(FastCharLocal.ACTION_ERROR1) +
                    "\n\tat " + new StackTraceElement(targetClass.getName(), "getRoute",
                    targetClass.getSimpleName() + ".java", line));
        }
        List<String> classRoutes = new ArrayList<>(16);
        classRoutes.add(classRoute);
        if (targetClass.isAnnotationPresent(AFastRoute.class)) {
            AFastRoute fastRoute = targetClass.getAnnotation(AFastRoute.class);
            if (fastRoute.head()) {
                if (fastRoute.value().length > 0) {
                    classRoutes.clear();
                }
                for (String configRoute : fastRoute.value()) {
                    String route = FastStringUtils.stripEnd(configRoute, "/") + "/" +
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
        List<String> classMethodRoutes = new ArrayList<>(16);
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
        if ("getRoute".equalsIgnoreCase(declaredMethod.getName())) {
            return classMethodRoutes;
        }
        List<String> classRoutes = getClassRoutes(targetClass);
        List<String> methodRoutes = new ArrayList<>(16);
        methodRoutes.add(declaredMethod.getName());
        if (declaredMethod.isAnnotationPresent(AFastRoute.class)) {
            AFastRoute fastRoute = declaredMethod.getAnnotation(AFastRoute.class);
            if (fastRoute.head()) {
                if (fastRoute.value().length > 0) {
                    methodRoutes.clear();
                }
                for (String configRoute : fastRoute.value()) {
                    String route = FastStringUtils.stripEnd(configRoute, "/") + "/" + declaredMethod.getName();
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
            if (FastStringUtils.isEmpty(route)) {
                continue;
            }
            route = FastStringUtils.stripEnd(route, "/") + "/";
            for (String methodRoute : methodRoutes) {
                if (FastStringUtils.isEmpty(methodRoute)) {
                    continue;
                }
                String classMethodRoute = route + FastStringUtils.strip(methodRoute, "/");
                if (classMethodRoutes.contains(classMethodRoute)) {
                    continue;
                }
                classMethodRoutes.add(classMethodRoute);
            }
        }
        return classMethodRoutes;
    }

    synchronized static void actionResolver(Class<? extends FastAction> actionClass) throws Exception {
        if (FastChar.getConstant().isLogActionResolver()) {
            FastChar.getLogger().info(actionClass, FastChar.getLocal().getInfo(FastCharLocal.ACTION_ERROR6, actionClass.getName()));
        }
        if (FastChar.getServletContext() == null) {
            return;
        }
        if (RESOLVED.contains(actionClass)) {
            return;
        }
        RESOLVED.add(actionClass);
        Class<? extends FastOut<?>> defaultOut = FastEngine.instance().getActions().getDefaultOut();

        IFastMethodRead parameterConverter = FastChar.getOverrides().newInstance(IFastMethodRead.class);

        if (actionClass.isAnnotationPresent(AFastAction.class)) {
            AFastAction annotation = actionClass.getAnnotation(AFastAction.class);
            if (!annotation.enable()) {//被禁止
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLogger().warn(FastAction.class, FastChar.getLocal().getInfo(FastCharLocal.ACTION_ERROR5, actionClass));
                }
                return;
            }
        }
        if (actionClass.isAnnotationPresent(AFastResponse.class)) {
            AFastResponse response = actionClass.getAnnotation(AFastResponse.class);
            defaultOut = FastOut.convertType(response.value());
        }
        Method[] declaredMethods = actionClass.getDeclaredMethods();
        Map<String, Integer> methodCount = new HashMap<>(16);
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

            List<IFastMethodRead.MethodLine> lines = new ArrayList<>(5);
            List<FastParameter> parameter = parameterConverter.getParameter(declaredMethod, lines);
            List<String> classMethodRoutes = getClassMethodRoutes(actionClass, declaredMethod);

            for (String classMethodRoute : classMethodRoutes) {
                FastRoute fastRoute = new FastRoute();
                fastRoute.actionClass = actionClass;
                fastRoute.method = declaredMethod;
                if (!lines.isEmpty()) {
                    fastRoute.firstMethodLineNumber = lines.get(methodCount.get(declaredMethod.getName()) - 1).getFirstLine() - 1;
                    fastRoute.lastMethodLineNumber = lines.get(methodCount.get(declaredMethod.getName()) - 1).getLastLine();
                } else {
                    fastRoute.firstMethodLineNumber = 1;
                    fastRoute.lastMethodLineNumber = 1;
                }
                fastRoute.methodParameter = parameter;
                fastRoute.route = classMethodRoute;
                fastRoute.crossAllowDomains = new HashSet<>(FastChar.getConstant().getCrossAllowDomains());

                /*ActionClass注解处理   ----  开始 **/
                if (actionClass.isAnnotationPresent(AFastPriority.class)) {
                    AFastPriority annotation = actionClass.getAnnotation(AFastPriority.class);
                    fastRoute.priority = annotation.value();
                }

                if (actionClass.isAnnotationPresent(AFastRoute.class)) {
                    AFastRoute classRouteAnnotation = actionClass.getAnnotation(AFastRoute.class);
                    fastRoute.crossAllowDomains.addAll(Arrays.asList(classRouteAnnotation.crossDomains()));
                    if (classRouteAnnotation.cross()) {
                        fastRoute.crossAllowDomains.add("*");
                    }
                    fastRoute.interceptorAfter = classRouteAnnotation.interceptorAfter();
                    fastRoute.interceptorBefore = classRouteAnnotation.interceptorBefore();
                    fastRoute.contentTypes.addAll(Arrays.asList(classRouteAnnotation.contentTypes()));
                    fastRoute.httpMethods.addAll(Arrays.asList(classRouteAnnotation.httpMethods()));
                }

                if (actionClass.isAnnotationPresent(AFastHttpMethod.class)) {
                    AFastHttpMethod annotation = actionClass.getAnnotation(AFastHttpMethod.class);
                    fastRoute.httpMethods.addAll(Arrays.asList(annotation.value()));
                }
                /*ActionClass注解处理   ----  结束 **/


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
                    AFastRoute methodRouteAnnotation = declaredMethod.getAnnotation(AFastRoute.class);
                    fastRoute.crossAllowDomains.addAll(Arrays.asList(methodRouteAnnotation.crossDomains()));
                    if (methodRouteAnnotation.cross()) {
                        fastRoute.crossAllowDomains.add("*");
                    }
                    fastRoute.interceptorAfter = methodRouteAnnotation.interceptorAfter();
                    fastRoute.interceptorBefore = methodRouteAnnotation.interceptorBefore();
                    fastRoute.contentTypes.addAll(Arrays.asList(methodRouteAnnotation.contentTypes()));
                    fastRoute.httpMethods.addAll(Arrays.asList(methodRouteAnnotation.httpMethods()));
                }

                if (declaredMethod.isAnnotationPresent(AFastCache.class)) {
                    AFastCache annotation = declaredMethod.getAnnotation(AFastCache.class);
                    FastResponseCacheConfig cacheConfig = new FastResponseCacheConfig();
                    cacheConfig.setCache(annotation.enable());
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
                    if (FastStringUtils.isNotEmpty(annotation.key())) {
                        cacheConfig.setCacheKey(annotation.key());
                    }
                    if (FastStringUtils.isNotEmpty(annotation.tag())) {
                        cacheConfig.setCacheTag(annotation.tag());
                    }
                    fastRoute.responseCacheConfig = cacheConfig;
                }
                fastRoute.returnOut = defaultOut;

                initAnnotationInterceptor(actionClass, fastRoute, 1);
                initAnnotationInterceptor(declaredMethod, fastRoute, 2);

                if (FAST_ROUTE_MAP.containsKey(classMethodRoute)) {
                    FastRoute existFastRoute = FAST_ROUTE_MAP.get(classMethodRoute);
                    if (!FastClassUtils.isSameRefined(actionClass, existFastRoute.getActionClass())) {

                        StackTraceElement newStack = new StackTraceElement(actionClass.getName(), fastRoute.getMethod().getName(),
                                actionClass.getSimpleName() + ".java", fastRoute.firstMethodLineNumber);

                        StackTraceElement currStack = new StackTraceElement(existFastRoute.getActionClass().getName(), existFastRoute.getMethod().getName(),
                                existFastRoute.getActionClass().getSimpleName() + ".java", existFastRoute.firstMethodLineNumber);
                        if (fastRoute.getPriority() == existFastRoute.getPriority()) {
                            throw new FastActionException(FastChar.getLocal().getInfo(FastCharLocal.ACTION_ERROR2, "'" + classMethodRoute + "'") +
                                    "\n\tat " + newStack +
                                    "\n\tat " + currStack
                            );
                        }
                        if (fastRoute.getPriority() > existFastRoute.getPriority()) {
                            FastChar.getLogger().warn(FastAction.class, FastChar.getLocal().getInfo(FastCharLocal.ACTION_ERROR3,
                                    "'" + classMethodRoute + "'") + "\n\t\tnew at " + newStack + "\n\t\told at " + currStack);
                        } else {
                            FastChar.getLogger().warn(FastAction.class, FastChar.getLocal().getInfo(FastCharLocal.ACTION_ERROR3,
                                    "'" + classMethodRoute + "'") + "\n\t\tnew at " + currStack + "\n\t\told at " + newStack);
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
                    FastChar.getLogger().info(actionClass, logRote);
                }
            }
        }
        if (FastAction.class.isAssignableFrom(actionClass.getSuperclass())) {
            if (actionClass.getSuperclass() != FastAction.class) {
                actionResolver((Class<? extends FastAction>) actionClass.getSuperclass());
            }
        }
    }


    private static void initAnnotationInterceptor(GenericDeclaration target, FastRoute fastRoute, int index) throws Exception {
        IFastMethodRead read = FastChar.getOverrides().newInstance(IFastMethodRead.class);
        if (target != null) {
            Annotation[] annotations = target.getAnnotations();

            for (Annotation annotation : annotations) {
                if (annotation instanceof AFastAfter) {
                    AFastAfter afterInterceptor = (AFastAfter) annotation;
                    for (Class<? extends IFastInterceptor> aClass : afterInterceptor.value()) {
                        List<IFastMethodRead.MethodLine> lineNumber = read.getMethodLineNumber(aClass, "onInterceptor");
                        FastInterceptorInfo<IFastInterceptor> routeInterceptor = new FastInterceptorInfo<>();
                        routeInterceptor.index = index;
                        routeInterceptor.priority = afterInterceptor.priority();
                        routeInterceptor.interceptor = aClass;
                        if (!lineNumber.isEmpty()) {
                            routeInterceptor.firstMethodLineNumber = lineNumber.get(0).getFirstLine();
                            routeInterceptor.lastMethodLineNumber = lineNumber.get(0).getLastLine();
                        }
                        fastRoute.addAfterInterceptor(routeInterceptor);
                    }

                } else if (annotation instanceof AFastBefore) {
                    AFastBefore beforeInterceptor = (AFastBefore) annotation;
                    for (Class<? extends IFastInterceptor> aClass : beforeInterceptor.value()) {
                        List<IFastMethodRead.MethodLine> lineNumber = read.getMethodLineNumber(aClass, "onInterceptor");
                        FastInterceptorInfo<IFastInterceptor> routeInterceptor = new FastInterceptorInfo<>();
                        routeInterceptor.index = index;
                        routeInterceptor.priority = beforeInterceptor.priority();
                        routeInterceptor.interceptor = aClass;
                        if (!lineNumber.isEmpty()) {
                            routeInterceptor.firstMethodLineNumber = lineNumber.get(0).getFirstLine();
                            routeInterceptor.lastMethodLineNumber = lineNumber.get(0).getLastLine();
                        }
                        fastRoute.addBeforeInterceptor(routeInterceptor);
                    }
                }
            }
        }
    }

    synchronized static void initMethodInterceptors() throws Exception {
        IFastMethodRead read = FastChar.getOverrides().newInstance(IFastMethodRead.class);
        for (Map.Entry<String, FastRoute> stringFastRouteEntry : FAST_ROUTE_MAP.entrySet()) {
            FastRoute fastRoute = stringFastRouteEntry.getValue();
            List<FastInterceptorInfo<IFastInterceptor>> beforeInterceptors = FastEngine.instance().getInterceptors().getBeforeInterceptors(stringFastRouteEntry.getKey());
            for (FastInterceptorInfo<IFastInterceptor> beforeInterceptor : beforeInterceptors) {
                List<IFastMethodRead.MethodLine> lineNumber = read.getMethodLineNumber(beforeInterceptor.interceptor, "onInterceptor");
                FastInterceptorInfo<IFastInterceptor> routeInterceptor = new FastInterceptorInfo<>();
                routeInterceptor.index = 0;
                routeInterceptor.priority = beforeInterceptor.priority;
                routeInterceptor.interceptor = beforeInterceptor.interceptor;
                if (!lineNumber.isEmpty()) {
                    routeInterceptor.firstMethodLineNumber = lineNumber.get(0).getFirstLine();
                    routeInterceptor.lastMethodLineNumber = lineNumber.get(0).getLastLine();
                }
                fastRoute.addBeforeInterceptor(routeInterceptor);
            }

            List<FastInterceptorInfo<IFastInterceptor>> afterInterceptors = FastEngine.instance().getInterceptors().getAfterInterceptors(stringFastRouteEntry.getKey());
            for (FastInterceptorInfo<IFastInterceptor> afterInterceptor : afterInterceptors) {
                List<IFastMethodRead.MethodLine> lineNumber = read.getMethodLineNumber(afterInterceptor.interceptor, "onInterceptor");
                FastInterceptorInfo<IFastInterceptor> routeInterceptor = new FastInterceptorInfo<>();
                routeInterceptor.index = 0;
                routeInterceptor.priority = afterInterceptor.priority;
                routeInterceptor.interceptor = afterInterceptor.interceptor;
                if (!lineNumber.isEmpty()) {
                    routeInterceptor.firstMethodLineNumber = lineNumber.get(0).getFirstLine();
                    routeInterceptor.lastMethodLineNumber = lineNumber.get(0).getLastLine();
                }
                fastRoute.addAfterInterceptor(routeInterceptor);
            }
        }
    }

    public static void flush() {
        List<String> waitRemove = new ArrayList<>(16);
        for (Map.Entry<String, FastRoute> stringFastRouteEntry : FAST_ROUTE_MAP.entrySet()) {
            if (FastClassUtils.isRelease(stringFastRouteEntry.getValue().actionClass)) {
                waitRemove.add(stringFastRouteEntry.getKey());

                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLogger().warn(FastDispatcher.class,
                            FastChar.getLocal().getInfo(FastCharLocal.ROUTE_ERROR4, stringFastRouteEntry.getKey()));
                }
            }
        }
        for (String s : waitRemove) {
            FAST_ROUTE_MAP.remove(s);
        }
        RESOLVED.clear();
    }


    private final FastFilterChain filterChain;
    private final FastHttpServletRequest request;
    private final FastHttpServletResponse response;
    private String contentUrl;
    private List<FastInterceptorInfo<IFastRootInterceptor>> rootInterceptor;
    private int interceptorIndex = -1;
    private FastAction forwarder;

    public FastDispatcher(FastHttpServletRequest request, FastHttpServletResponse response) {
        this(null, request, response);
    }

    public FastDispatcher(FastFilterChain filterChain, FastHttpServletRequest request, FastHttpServletResponse response) {
        this.filterChain = filterChain;
        this.request = request;
        this.response = response;
        this.init();
    }

    private void init() {
        String requestUrl = request.getRequestURL().toString();
        //此处获取contentUrl 不可使用request.getServletPath()
        this.contentUrl = FastUrlParser.getContentPath(requestUrl);
        this.rootInterceptor = FastEngine.instance().getInterceptors().getRootInterceptors(contentUrl);
        this.interceptorIndex = -1;
    }

    public FastFilterChain getFilterChain() {
        return filterChain;
    }

    public FastHttpServletRequest getRequest() {
        return request;
    }

    public FastHttpServletResponse getResponse() {
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


    public void invoke() throws FastWebException {
        try {
            Date inTime = new Date();

            Thread.currentThread().setName(THREAD_NAME_PREFIX + " [ " + contentUrl + " ] [ " + System.currentTimeMillis() + " ]");

            if (!validateUrl(contentUrl)) {
                doFilter();
                return;
            }

            List<FastUrl> parse = FastUrlParser.parse(contentUrl);
            if ("options".equalsIgnoreCase(request.getMethod())) {
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLogger().info(this.getClass(), "OPTIONS REQUEST : " + request.getRequestURL());
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
                                "\n\t\tREQUEST-ORIGIN : " + request.getHeader("origin")
                                + "\n\n";
                        FastChar.getLogger().error(this.getClass(), errorInfo);
                    }
                }
                if (!hasRoute) {
                    if (FastChar.getConstant().isDebug()) {
                        FastChar.getLogger().error(this.getClass(), "NOT FOUND : " + request.getRequestURL());
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
                IFastRootInterceptor interceptor = FastChar.getOverrides().singleInstance(rootInterceptor.get(interceptorIndex).interceptor);
                interceptor.onInterceptor(request, response, this);
                return;
            }

            if (FastUrlParser.isFileUrl(contentUrl)) {
                if (FastEngine.instance().getWebResources().isPrivateResourcePath(contentUrl)) {
                    response404(inTime);
                    return;
                }
                FastResource fastResource = FastEngine.instance().getWebResources().getPublicResource(contentUrl);
                if (fastResource != null) {
                    responseResource(inTime, fastResource);
                    return;
                }
                doFilter();
                return;
            }

            for (FastUrl fastUrl : parse) {
                FastRoute baseRoute = FAST_ROUTE_MAP.get(fastUrl.getMethodRoute());
                if (baseRoute != null) {
                    FastRoute fastRoute = baseRoute.copy();
                    if (fastRoute == null) {
                        if (FastChar.getConstant().isDebug()) {
                            FastChar.getLogger().warn(FastDispatcher.class,
                                    FastChar.getLocal().getInfo(FastCharLocal.ROUTE_ERROR2, fastUrl.getMethodRoute()));
                        }
                        continue;
                    }
                    try {
                        if (!fastRoute.checkMethod(request.getMethod())) {
                            if (FastChar.getConstant().isDebug()) {
                                FastChar.getLogger().warn(FastDispatcher.class,
                                        FastChar.getLocal().getInfo(FastCharLocal.ROUTE_ERROR3, fastUrl.getMethodRoute(), request.getMethod()));
                            }
                            response404(inTime);
                            return;
                        }
                        if (!fastRoute.checkContentType(request.getContentType())) {
                            if (FastChar.getConstant().isDebug()) {
                                FastChar.getLogger().warn(FastDispatcher.class,
                                        FastChar.getLocal().getInfo(FastCharLocal.ROUTE_ERROR3, fastUrl.getMethodRoute(), request.getMethod()));
                            }
                            response404(inTime);
                            return;
                        }
                        fastRoute.inTime = inTime;
                        fastRoute.request = request;
                        fastRoute.response = response;
                        fastRoute.fastUrl = fastUrl;
                        fastRoute.forwarder = forwarder;
                        fastRoute.rootInterceptor = new ArrayList<>(rootInterceptor);
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
                        FastChar.getLogger().warn(FastDispatcher.class,
                                FastChar.getLocal().getInfo(FastCharLocal.ROUTE_ERROR2, fastUrl.getMethodRouteIndex()));
                        continue;
                    }
                    try {
                        fastRoute.inTime = inTime;
                        fastRoute.request = request;
                        fastRoute.response = response;
                        fastRoute.fastUrl = fastUrl;
                        fastRoute.forwarder = forwarder;
                        fastRoute.rootInterceptor = new ArrayList<>(rootInterceptor);
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
            Thread.currentThread().setName(THREAD_NAME_PREFIX + " [ " + System.currentTimeMillis() + " ]");
        }
    }

    private boolean initCrossDomain(FastRoute route) {
        boolean cross = false;
        String origin = getRequest().getHeader(FastHttpHeaders.ORIGIN);
        if (FastStringUtils.isEmpty(origin)) {
            origin = getRequest().getHeader(FastHttpHeaders.REFERER);
        }
        if (FastStringUtils.isEmpty(origin)) {
            return false;
        }
        Set<String> crossAllowDomains = route.crossAllowDomains;
        String[] split = FastStringUtils.splitByWholeSeparator(origin, ";");
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
            response.setHeader(FastHttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            response.setHeader(FastHttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            response.setHeader(FastHttpHeaders.ACCESS_CONTROL_MAX_AGE, "3600");
            response.setHeader(FastHttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, PUT, OPTIONS, DELETE");
            response.setHeader(FastHttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, FastStringUtils.join(FastChar.getConstant().getCrossHeaders(), ","));
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
            fastErrorAction.servletContext = FastEngine.instance().getServletContext();
            fastErrorAction.fastUrl = new FastUrl();
            if (FastStringUtils.isEmpty(contentUrl)) {
                contentUrl = "/";
            }
            fastErrorAction.response404("the route '" + contentUrl + "' not found!");
        } finally {
            fastRoute404.release();
        }
    }

    private void responseResource(Date inTime, FastResource resource) {
        contentUrl = FastStringUtils.stripEnd(contentUrl, "/");
        FastRoute fastRouteResource = new FastRoute();
        try {
            fastRouteResource.inTime = inTime;
            fastRouteResource.route = contentUrl;

            FastAction fastResourceAction = new FastAction() {
                @Override
                public String getRoute() {
                    return null;
                }
            };
            fastResourceAction.setLog(false);
            fastRouteResource.fastAction = fastResourceAction;
            fastResourceAction.fastRoute = fastRouteResource;
            fastResourceAction.request = request;
            fastResourceAction.response = response;
            fastResourceAction.servletContext = FastEngine.instance().getServletContext();
            fastResourceAction.fastUrl = new FastUrl();
            if (FastStringUtils.isEmpty(contentUrl)) {
                contentUrl = "/";
            }
            fastResourceAction.responseResource(resource);
        } finally {
            fastRouteResource.release();
        }
    }

    private void doFilter() throws Exception {
        if (this.filterChain != null) {
            this.filterChain.doFilter(this.request, this.response);
        }
    }
}
