package com.fastchar.core;

import com.fastchar.interfaces.IFastInterceptor;
import com.fastchar.interfaces.IFastMethodRead;
import com.fastchar.out.FastOut;
import com.fastchar.out.FastOutForward;
import com.fastchar.out.FastOutRedirect;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public final class FastRequestLog {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private static final String BLOCK_CHAR = "*";
    private static final String SPLIT_CHAR = "-";
    private static final String IN_CHAR = "->";
    private static final String OUT_CHAR = "<-";
    private static final String ROOT_INTERCEPTOR_CHAR = ">!";
    private static final String BEFORE_INTERCEPTOR_CHAR = ">:";
    private static final String AFTER_INTERCEPTOR_CHAR = ":<";


    public static void log(FastAction action) {
        try {
            if (FastChar.getConstant().isDebug() && action.isLog()) {
                if (action.getFastRoute() == null || action.getFastOut() == null) {
                    return;
                }

                if (action.getFastOut().isLogged()) {
                    return;
                }

                Date inTime = action.getFastRoute().inTime;
                Date outTime = action.getFastOut().getOutTime();
                float userTotal = FastNumberUtils.formatToFloat((outTime.getTime() - inTime.getTime()) / 1000.0, 6);

                if (FastChar.getConstant().isLogFilterResponseTime()) {
                    if (userTotal < FastChar.getConstant().getMaxResponseTime()) {
                        return;
                    }
                }

                action.getFastOut().setLogged(true);
                if (action.getForwarder() != null) {
                    log(action.getForwarder());
                }


                RequestStackTrace requestStackTrace = new RequestStackTrace(action).invoke();
                List<StackTraceElement> rootInterceptorStacks = requestStackTrace.getRootInterceptorStacks();
                List<StackTraceElement> beforeInterceptorStacks = requestStackTrace.getBeforeInterceptorStacks();
                List<StackTraceElement> afterInterceptorStacks = requestStackTrace.getAfterInterceptorStacks();
                List<StackTraceElement> actionStacks = requestStackTrace.getActionStacks();


                LinkedHashMap<String, String> printRequestMap = new LinkedHashMap<>();
                int rootIndex = 0;
                for (int i = rootInterceptorStacks.size() - 1; i >= 0; i--) {
                    printRequestMap.put(ROOT_INTERCEPTOR_CHAR + "@RootInterceptor-" + rootIndex, rootInterceptorStacks.get(i).toString());
                    rootIndex++;
                }

                int beforeIndex = 0;
                for (int i = beforeInterceptorStacks.size() - 1; i >= 0; i--) {
                    printRequestMap.put(BEFORE_INTERCEPTOR_CHAR + "@BeforeInterceptor-" + beforeIndex, beforeInterceptorStacks.get(i).toString());
                    beforeIndex++;
                }

                if (FastChar.getConstant().isLogInterceptorUseTotal()) {
                    float useTotal = FastNumberUtils.formatToFloat(action.fastRoute.beforeInterceptorUseTotal / 1000.0, 4);
                    printRequestMap.put(BEFORE_INTERCEPTOR_CHAR + "@BeforeInterceptor-UseTotal", useTotal + " seconds");
                }

                if (action.fastRoute.responseInvoked && action.fastRoute.actionLog) {
                    if (actionStacks.size() > 1) {
                        int actionIndex = 0;
                        for (int i = actionStacks.size() - 1; i >= 0; i--) {
                            printRequestMap.put("Action-" + actionIndex, actionStacks.get(i).toString());
                            actionIndex++;
                        }
                    } else if (actionStacks.size() > 0) {
                        printRequestMap.put("Action", actionStacks.get(0).toString());
                    }
                }

                printRequestMap.put("HttpMethod", action.getRequestMethod());
                if (action.getFastRoute().crossAllowDomains.size() > 0) {
                    printRequestMap.put("CrossDomain-Allows", FastStringUtils.join(action.getFastRoute().crossAllowDomains, ";"));
                    printRequestMap.put("CrossDomain-Headers", FastStringUtils.join(FastChar.getConstant().getCrossHeaders(), ","));
                }

                if (FastChar.getConstant().isLogHeaders()) {
                    StringBuilder stringBuilder = new StringBuilder();
                    Enumeration<String> headerNames = action.getRequest().getHeaderNames();
                    while (headerNames.hasMoreElements()) {
                        String headerName = headerNames.nextElement();
                        stringBuilder.append(headerName).append("=")
                                .append(action.getRequest().getHeader(headerName))
                                .append(";");
                    }
                    printRequestMap.put("HttpHeader", stringBuilder.toString());
                }
                printRequestMap.put("ContentType", action.getContentType());
                printRequestMap.put("Host", FastStringUtils.strip(action.getProjectHost(), "/"));
                printRequestMap.put("Route", action.getFastRoute().getRoute());
                if (action.getUrlParams().size() > 0) {
                    printRequestMap.put("UrlParams", Arrays.toString(action.getUrlParams().toArray()));
                }
                Map<String, Object> paramToMap = action.getParamToMap();
                if (paramToMap.size() > 0) {
                    printRequestMap.put("Params", mapToString(paramToMap));
                }

                if (FastChar.getConstant().isLogRemoteAddress()) {
                    printRequestMap.put("RemoteAddress", action.getRemoteIp());
                }
                printRequestMap.put("InTime", SIMPLE_DATE_FORMAT.format(inTime));

                FastOut<?> fastOut = action.getFastOut();
                LinkedHashMap<String, String> printResponseMap = new LinkedHashMap<>();

                int afterIndex = 0;
                for (int i = afterInterceptorStacks.size() - 1; i >= 0; i--) {
                    printResponseMap.put(AFTER_INTERCEPTOR_CHAR + "@AfterInterceptor-" + afterIndex, afterInterceptorStacks.get(i).toString());
                    afterIndex++;
                }

                if (FastChar.getConstant().isLogInterceptorUseTotal()) {
                    float useTotal = FastNumberUtils.formatToFloat(action.fastRoute.afterInterceptorUseTotal / 1000.0, 6);
                    printResponseMap.put(AFTER_INTERCEPTOR_CHAR + "@AfterInterceptor-UseTotal", useTotal + " seconds");
                }

                if (requestStackTrace.getOutStacks().size() > 1) {
                    for (int i = 0; i < requestStackTrace.getOutStacks().size(); i++) {
                        printResponseMap.put("Out-" + i, requestStackTrace.getOutStacks().get(i).toString());
                    }
                } else if (requestStackTrace.getOutStacks().size() > 0) {
                    printResponseMap.put("Out", requestStackTrace.getOutStacks().get(0).toString());
                }

                if (isErrorStatus(fastOut.getStatus()) || isWarnStatus(fastOut.getStatus())) {
                    printResponseMap.put("Out-Content", String.valueOf(fastOut.getData()));
                } else if (action.isLogResponse()) {
                    printResponseMap.put("Out-Content", String.valueOf(fastOut.getData()));
                }

                printResponseMap.put("ContentType", fastOut.toContentType(action));
                printResponseMap.put("Status", String.valueOf(fastOut.getStatus()));

                if (FastStringUtils.isNotEmpty(fastOut.getDescription())) {
                    printResponseMap.put("Description", fastOut.getDescription());
                }
                if (isErrorStatus(fastOut.getStatus()) &&
                        actionStacks.size() > 0) {
                    int actionIndex = 0;
                    for (int i = actionStacks.size() - 1; i >= 0; i--) {
                        printRequestMap.put("Throwable-" + actionIndex, actionStacks.get(i).toString());
                        actionIndex++;
                    }
                }


                printResponseMap.put("OutTime", SIMPLE_DATE_FORMAT.format(outTime));
                printResponseMap.put("UseTotal", userTotal + " seconds");
                int maxKeyLength = 10;
                for (String key : printRequestMap.keySet()) {
                    if (FastStringUtils.isEmpty(printRequestMap.get(key))) {
                        continue;
                    }
                    maxKeyLength = Math.max(maxKeyLength, key.length());
                }
                for (String key : printResponseMap.keySet()) {
                    if (FastStringUtils.isEmpty(printResponseMap.get(key))) {
                        continue;
                    }
                    maxKeyLength = Math.max(maxKeyLength, key.length());
                }


                StringBuilder print = new StringBuilder();
                print.append(buildSplit(BLOCK_CHAR, "FastCharLog-BGN"));

                for (String key : printRequestMap.keySet()) {
                    String text = printRequestMap.get(key);
                    if (FastStringUtils.isEmpty(text)) {
                        continue;
                    }
                    String tipChar = buildChar(IN_CHAR, key);
                    print.append(tipChar).append(" ").append(formatString(key, maxKeyLength)).append(text).append("\n");
                }
                print.append(buildSplit(SPLIT_CHAR, "FastCharLog-RSP"));
                for (String key : printResponseMap.keySet()) {
                    String text = printResponseMap.get(key);
                    if (FastStringUtils.isEmpty(text)) {
                        continue;
                    }
                    String tipChar = buildChar(OUT_CHAR, key);
                    print.append(tipChar).append(" ").append(formatString(key, maxKeyLength)).append(text).append("\n");
                }
                print.append(buildSplit(BLOCK_CHAR, "FastCharLog-END"));

                if (userTotal > FastChar.getConstant().getMaxResponseTime()) {
                    System.out.println(FastChar.getLog().warnStyle(print.toString()));
                } else {
                    if (isErrorStatus(fastOut.getStatus())) {
                        System.out.println(FastChar.getLog().errorStyle(print.toString()));
                    } else if (isWarnStatus(fastOut.getStatus())) {
                        System.out.println(FastChar.getLog().warnStyle(print.toString()));
                    } else if (FastOutRedirect.class.isAssignableFrom(fastOut.getClass())
                            || FastOutForward.class.isAssignableFrom(fastOut.getClass())) {
                        System.out.println(FastChar.getLog().softStyle(print.toString()));
                    } else {
                        System.out.println(FastChar.getLog().lightStyle(print.toString()));
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static boolean isErrorStatus(int status) {
        return status == 404 ||
                status == 500 ||
                status == 502;
    }

    private static boolean isWarnStatus(int status) {
        return status == 400;
    }

    private static String buildChar(String normalChar, String key) {
        String tipChar = normalChar;
        if (key.contains("@")) {
            tipChar = key.split("@")[0];
        }
        return tipChar;
    }


    private static StringBuilder buildSplit(String charSplit, String centerText) {
        StringBuilder lineSplit = new StringBuilder();
        StringBuilder split = new StringBuilder();
        for (int i = 0; i < 45; i++) {
            split.append(charSplit);
        }
        return lineSplit.append(split).append(centerText).append(split).append("\n");
    }


    private static String formatString(String target, int targetLength) {
        if (target.contains("@")) {
            target = target.split("@")[1];
        }
        StringBuilder targetBuilder = new StringBuilder(target);
        while (targetBuilder.length() < targetLength + 1) {
            targetBuilder.append(" ");
        }
        target = targetBuilder.toString();
        return target + "： ";
    }


    private static String mapToString(Map<String, Object> map) {
        List<String> strings = new ArrayList<>();
        for (String s : map.keySet()) {
            Object value = map.get(s);
            if (value instanceof String[]) {
                strings.add(s + "=" + Arrays.toString((String[]) value));
            } else {
                strings.add(s + "=" + value);
            }
        }
        return "{" + FastStringUtils.join(strings, ",") + "}";
    }

    private static class RequestStackTrace {
        private FastAction action;
        private List<StackTraceElement> rootInterceptorStacks;
        private List<StackTraceElement> beforeInterceptorStacks;
        private List<StackTraceElement> afterInterceptorStacks;
        private List<StackTraceElement> actionStacks;
        private List<StackTraceElement> outStacks;

        public RequestStackTrace(FastAction action) {
            this.action = action;
        }

        public List<StackTraceElement> getBeforeInterceptorStacks() {
            return beforeInterceptorStacks;
        }

        public List<StackTraceElement> getAfterInterceptorStacks() {
            return afterInterceptorStacks;
        }

        public List<StackTraceElement> getRootInterceptorStacks() {
            return rootInterceptorStacks;
        }

        public List<StackTraceElement> getActionStacks() {
            return actionStacks;
        }

        public List<StackTraceElement> getOutStacks() {
            return outStacks;
        }

        public RequestStackTrace invoke() {
            rootInterceptorStacks = new ArrayList<>();
            beforeInterceptorStacks = new ArrayList<>();
            afterInterceptorStacks = new ArrayList<>();
            actionStacks = new ArrayList<>();
            outStacks = new ArrayList<>();
            boolean hasActionOut = false;
            if (action.getFastRoute().getMethod() != null) {
                action.getFastRoute().stackTraceElements.addAll(Arrays.asList(Thread.currentThread().getStackTrace()));
                for (StackTraceElement stackTraceElement : action.getFastRoute().stackTraceElements) {
                    Class targetClass = FastClassUtils.getClass(stackTraceElement.getClassName(), false);
                    if (targetClass == null) {
                        continue;
                    }
                    if (IFastInterceptor.class.isAssignableFrom(targetClass)) {
                        if (action.getFastRoute().isRootInterceptor(stackTraceElement.getClassName())
                                && !rootInterceptorStacks.contains(stackTraceElement)) {
                            rootInterceptorStacks.add(stackTraceElement);
                        }
                        if (action.getFastRoute().isBeforeInterceptor(stackTraceElement.getClassName())
                                && !beforeInterceptorStacks.contains(stackTraceElement)) {
                            beforeInterceptorStacks.add(stackTraceElement);
                        }
                        if (action.getFastRoute().isAfterInterceptor(stackTraceElement.getClassName())
                                && !afterInterceptorStacks.contains(stackTraceElement)) {
                            afterInterceptorStacks.add(stackTraceElement);
                        }
                    } else if (FastAction.class.isAssignableFrom(targetClass) && targetClass != FastAction.class) {
                        if (!actionStacks.contains(stackTraceElement)) {
                            actionStacks.add(stackTraceElement);
                        }
                    } else if (FastOut.class.isAssignableFrom(targetClass) && targetClass != FastOut.class) {
                        if (!outStacks.contains(stackTraceElement)) {
                            outStacks.add(stackTraceElement);
                        }
                        if (action.getFastOut().getClass() == targetClass) {
                            hasActionOut = true;
                        }
                    }
                }
                if (actionStacks.size() == 0) {
                    StackTraceElement stackTraceElement = new StackTraceElement(action.getClass().getName(),
                            action.getFastRoute().getMethod().getName(),
                            action.getClass().getSimpleName() + ".java",
                            action.getFastRoute().firstMethodLineNumber);
                    actionStacks.add(stackTraceElement);
                }
            }
            if (action.getFastOut() != null && !hasActionOut) {
                try {
                    IFastMethodRead methodRead = FastChar.getOverrides().newInstance(IFastMethodRead.class);
                    List<IFastMethodRead.MethodLine> responses = methodRead.getMethodLineNumber(action.getFastOut().getClass(), "response");
                    outStacks.add(new StackTraceElement(action.getFastOut().getClass().getName(),
                            "response",
                            action.getFastOut().getClass().getSimpleName() + ".java",
                            responses.get(0).getFirstLine() - 1));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return this;
        }
    }
}
