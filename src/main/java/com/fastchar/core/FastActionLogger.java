package com.fastchar.core;

import com.fastchar.interfaces.IFastInterceptor;
import com.fastchar.interfaces.IFastMethodRead;
import com.fastchar.interfaces.IFastRootInterceptor;
import com.fastchar.out.FastOut;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public final class FastActionLogger {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private static final String BLOCK_CHAR = "#";
    private static final String SPLIT_CHAR = "=";

    private static final String APPEND_CHAR = "+";

    private static final String IN_CHAR = "->";
    private static final String OUT_CHAR = "<-";
    private static final String ROOT_INTERCEPTOR_CHAR = ">!";
    private static final String BEFORE_INTERCEPTOR_CHAR = ">:";
    private static final String AFTER_INTERCEPTOR_CHAR = ":<";


    public static void log(FastAction action) {
        if ((FastChar.getConstant().isDebug() && action.isLog())) {
            if (action.getFastRoute() == null || action.getFastOut() == null) {
                return;
            }

            if (action.getFastOut().isLogged()) {
                return;
            }

            Date inTime = action.getFastRoute().getInTime();
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
            requestStackTrace.sortAfterInterceptorStacks();
            requestStackTrace.sortBeforeInterceptorStacks();
            requestStackTrace.sortRootInterceptorStacks();

            List<StackTraceElement> rootInterceptorStacks = requestStackTrace.getRootInterceptorStacks();
            List<StackTraceElement> beforeInterceptorStacks = requestStackTrace.getBeforeInterceptorStacks();
            List<StackTraceElement> afterInterceptorStacks = requestStackTrace.getAfterInterceptorStacks();
            List<StackTraceElement> actionStacks = requestStackTrace.getActionStacks();


            LinkedHashMap<String, String> printRequestMap = new LinkedHashMap<>();
            printRequestMap.put("Thread-Info", Thread.currentThread().getName() + " - " + Thread.currentThread().getId());

            for (int i = 0; i < rootInterceptorStacks.size(); i++) {
                printRequestMap.put(ROOT_INTERCEPTOR_CHAR + "@RootInterceptor-" + i, rootInterceptorStacks.get(i).toString());
            }

            for (int i = 0; i < beforeInterceptorStacks.size(); i++) {
                printRequestMap.put(BEFORE_INTERCEPTOR_CHAR + "@BeforeInterceptor-" + i, beforeInterceptorStacks.get(i).toString());
            }

            if (FastChar.getConstant().isLogInterceptorUseTotal()) {
                float useTotal = FastNumberUtils.formatToFloat(action.getFastRoute().getBeforeInterceptorUseTotal() / 1000.0, 4);
                printRequestMap.put(BEFORE_INTERCEPTOR_CHAR + "@BeforeInterceptor-UseTotal", useTotal + " seconds");
            }

            if (action.getFastRoute().isResponseInvoked() && action.getFastRoute().isActionLog()) {
                if (actionStacks.size() > 1) {
                    for (int i = 0; i < actionStacks.size(); i++) {
                        printRequestMap.put("Action-" + i, actionStacks.get(i).toString());
                    }
                } else if (!actionStacks.isEmpty()) {
                    printRequestMap.put("Action", actionStacks.get(0).toString());
                }
            }

            printRequestMap.put("HttpMethod", action.getRequestMethod());
            if (!action.getFastRoute().getCrossAllowDomains().isEmpty()) {
                printRequestMap.put("CrossDomain-Allows", FastStringUtils.join(action.getFastRoute().getCrossAllowDomains(), ";"));
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
            if (!action.getUrlParams().isEmpty()) {
                printRequestMap.put("UrlParams", Arrays.toString(action.getUrlParams().toArray()));
            }
            Map<String, Object> paramToMap = action.getParamToMap();
            if (!paramToMap.isEmpty()) {
                printRequestMap.put("Params", mapToString(paramToMap));
            }

            if (FastChar.getConstant().isLogRemoteAddress()) {
                printRequestMap.put("RemoteAddress", action.getRemoteIp());
            }
            printRequestMap.put("InTime", SIMPLE_DATE_FORMAT.format(inTime));

            FastOut<?> fastOut = action.getFastOut();
            LinkedHashMap<String, String> printResponseMap = new LinkedHashMap<>();

            for (int i = 0; i < afterInterceptorStacks.size(); i++) {
                printResponseMap.put(AFTER_INTERCEPTOR_CHAR + "@AfterInterceptor-" + i, afterInterceptorStacks.get(i).toString());
            }

            if (FastChar.getConstant().isLogInterceptorUseTotal()) {
                float useTotal = FastNumberUtils.formatToFloat(action.getFastRoute().getAfterInterceptorUseTotal() / 1000.0, 6);
                printResponseMap.put(AFTER_INTERCEPTOR_CHAR + "@AfterInterceptor-UseTotal", useTotal + " seconds");
            }

            if (requestStackTrace.getOutStacks().size() > 1) {
                for (int i = 0; i < requestStackTrace.getOutStacks().size(); i++) {
                    printResponseMap.put("Out-" + i, requestStackTrace.getOutStacks().get(i).toString());
                }
            } else if (!requestStackTrace.getOutStacks().isEmpty()) {
                printResponseMap.put("Out", requestStackTrace.getOutStacks().get(0).toString());
            }

            if (isErrorStatus(fastOut.getStatus()) || isWarnStatus(fastOut.getStatus())) {
                printResponseMap.put("Out-Content", FastStringUtils.defaultValue(fastOut.getData(), ""));
            } else if (action.isLogResponse()) {
                printResponseMap.put("Out-Content", FastStringUtils.defaultValue(fastOut.getData(), ""));
            }

            printResponseMap.put("ContentType", fastOut.toContentType(action));
            printResponseMap.put("ContentEncoding", action.getResponse().getHeader("Content-Encoding"));
            printResponseMap.put("Status", String.valueOf(fastOut.getStatus()));

            if (FastStringUtils.isNotEmpty(fastOut.getDescription())) {
                printResponseMap.put("Description", fastOut.getDescription());
            }
            if (isErrorStatus(fastOut.getStatus()) &&
                    !actionStacks.isEmpty()) {
                int actionIndex = 0;
                for (int i = actionStacks.size() - 1; i >= 0; i--) {
                    printRequestMap.put("Throwable-" + actionIndex, actionStacks.get(i).toString());
                    actionIndex++;
                }
            }


            printResponseMap.put("OutTime", SIMPLE_DATE_FORMAT.format(outTime));
            printResponseMap.put("UseTotal", userTotal + " seconds");
            int maxKeyLength = 10;
            int maxLineLength = 90;
            for (Map.Entry<String, String> stringStringEntry : printRequestMap.entrySet()) {
                if (FastStringUtils.isEmpty(stringStringEntry.getValue())) {
                    continue;
                }
                String key = stringStringEntry.getKey();
                if (stringStringEntry.getKey().contains("@")) {
                    key = FastStringUtils.splitByWholeSeparator(key, "@")[1];
                }
                maxKeyLength = Math.max(maxKeyLength, key.length());
            }
            for (Map.Entry<String, String> stringStringEntry : printResponseMap.entrySet()) {
                if (FastStringUtils.isEmpty(stringStringEntry.getValue())) {
                    continue;
                }
                String key = stringStringEntry.getKey();
                if (key.contains("@")) {
                    key = FastStringUtils.splitByWholeSeparator(key, "@")[1];
                }
                maxKeyLength = Math.max(maxKeyLength, key.length());
            }

            StringBuilder print = new StringBuilder();
            print.append("\n");
            print.append("____A");

            for (Map.Entry<String, String> stringStringEntry : printRequestMap.entrySet()) {
                String text = stringStringEntry.getValue();
                if (FastStringUtils.isEmpty(text)) {
                    continue;
                }
                String tipChar = buildChar(IN_CHAR, stringStringEntry.getKey());
                String line = tipChar + " " + formatString(stringStringEntry.getKey(), maxKeyLength) + text + "\n";
                print.append(line);
                maxLineLength = Math.max(line.length(), maxLineLength);

            }

            print.append("____B");
            for (Map.Entry<String, String> stringStringEntry : printResponseMap.entrySet()) {
                String text = stringStringEntry.getValue();
                if (FastStringUtils.isEmpty(text)) {
                    continue;
                }
                String tipChar = buildChar(OUT_CHAR, stringStringEntry.getKey());
                String line = tipChar + " " + formatString(stringStringEntry.getKey(), maxKeyLength) + text + "\n";
                print.append(line);
                maxLineLength = Math.max(line.length(), maxLineLength);
            }

            if (action.getAttribute().containsAttr("sqlLogList")) {
                print.append("____D");
                ArrayList<Map<String, String>> sqlLogList = action.getAttribute().getObject("sqlLogList");
                for (int i = 0; i < sqlLogList.size(); i++) {
                    Map<String, String> map = sqlLogList.get(i);
                    for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
                        String text = stringStringEntry.getValue();
                        if (FastStringUtils.isEmpty(text)) {
                            continue;
                        }
                        String line = "~~ " + formatString(stringStringEntry.getKey(), maxKeyLength) + text + "\n";
                        print.append(line);
                        maxLineLength = Math.max(line.length(), maxLineLength);
                    }
                    if (i != sqlLogList.size() - 1) {
                        print.append("____E");
                    }
                }
            }

            maxLineLength = 158;

            print.append("____C");
            print.append("\n\n\n");

            String printContent = print.toString();

            printContent = printContent
                    .replace("____A", buildSplit(maxLineLength, BLOCK_CHAR, " FastCharLog-Request-Begin "))
                    .replace("____B", buildSplit(maxLineLength, SPLIT_CHAR, " FastCharLog-Response-Out "))
                    .replace("____D", buildSplit(maxLineLength, SPLIT_CHAR, " FastCharLog-Run-Sql "))
                    .replace("____E", buildSplit(maxLineLength, APPEND_CHAR, ""))
                    .replace("____C", buildSplit(maxLineLength, BLOCK_CHAR, " FastCharLog-Request-End "));

            if (userTotal > FastChar.getConstant().getMaxResponseTime()) {
                FastChar.getLogger().warn(FastActionLogger.class, printContent);
            } else {
                if (isErrorStatus(fastOut.getStatus())) {
                    FastChar.getLogger().error(FastActionLogger.class, printContent);
                } else if (isWarnStatus(fastOut.getStatus())) {
                    FastChar.getLogger().warn(FastActionLogger.class, printContent);
                } else {
                    FastChar.getLogger().debug(FastActionLogger.class, printContent);
                }
            }
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
            tipChar = FastStringUtils.splitByWholeSeparator(key, "@")[0];
        }
        return tipChar;
    }


    private static StringBuilder buildSplit(int maxLength, String charSplit, String centerText) {
        if (FastStringUtils.isNotEmpty(centerText)) {
            centerText = "【" + centerText + "】";
        }
        StringBuilder split = new StringBuilder();
        int insertIndex = (maxLength - centerText.length()) / 2;

        while (split.length() < maxLength) {
            split.append(charSplit);
            if (split.length() >= insertIndex && split.length() < insertIndex + centerText.length()) {
                split.append(centerText);
            }
        }
        split.append("\n");
        return split;
    }


    private static String formatString(String target, int targetLength) {
        if (target.contains("@")) {
            target = FastStringUtils.splitByWholeSeparator(target, "@")[1];
        }
        StringBuilder targetBuilder = new StringBuilder(target);
        while (targetBuilder.length() < targetLength + 1) {
            targetBuilder.append(" ");
        }
        target = targetBuilder.toString();
        return target + "： ";
    }


    private static String mapToString(Map<String, Object> map) {
        List<String> strings = new ArrayList<>(16);
        for (Map.Entry<String, Object> stringObjectEntry : map.entrySet()) {
            Object value = stringObjectEntry.getValue();
            if (value instanceof String[]) {
                strings.add(stringObjectEntry.getKey() + "=" + Arrays.toString((String[]) value));
            } else {
                strings.add(stringObjectEntry.getKey() + "=" + value);
            }
        }
        return "{" + FastStringUtils.join(strings, ",") + "}";
    }

    private static class RequestStackTrace {
        private final FastAction action;
        private List<StackTraceElement> rootInterceptorStacks;
        private List<StackTraceElement> beforeInterceptorStacks;
        private List<StackTraceElement> afterInterceptorStacks;
        private List<StackTraceElement> actionStacks;
        private List<StackTraceElement> outStacks;

        private final Comparator<FastInterceptorInfo<?>> comparator = (o1, o2) -> {
            if (o1.getPriority() > o2.getPriority()) {
                return -1;
            }
            if (o1.getPriority() < o2.getPriority()) {
                return 1;
            }
            if (o1.getIndex() > o2.getIndex()) {
                return 1;
            }
            if (o1.getIndex() < o2.getIndex()) {
                return -1;
            }
            return 0;
        };

        public RequestStackTrace(FastAction action) {
            this.action = action;
        }

        public List<StackTraceElement> getBeforeInterceptorStacks() {
            return beforeInterceptorStacks;
        }

        public void sortBeforeInterceptorStacks() {
            Map<String, FastInterceptorInfo<IFastInterceptor>> levelMap = new HashMap<>(action.getFastRoute().doBeforeInterceptor.size());

            for (FastInterceptorInfo<IFastInterceptor> routeInterceptor : action.getFastRoute().doBeforeInterceptor) {
                levelMap.put(routeInterceptor.interceptor.getName(), routeInterceptor);
            }

            beforeInterceptorStacks.sort((o1, o2) -> comparator.compare(levelMap.get(o1.getClassName()), levelMap.get(o2.getClassName())));

        }

        public List<StackTraceElement> getAfterInterceptorStacks() {
            return afterInterceptorStacks;
        }

        public void sortAfterInterceptorStacks() {
            Map<String, FastInterceptorInfo<IFastInterceptor>> levelMap = new HashMap<>(action.getFastRoute().doAfterInterceptor.size());

            for (FastInterceptorInfo<IFastInterceptor> routeInterceptor : action.getFastRoute().doAfterInterceptor) {
                levelMap.put(routeInterceptor.interceptor.getName(), routeInterceptor);
            }

            afterInterceptorStacks.sort((o1, o2) -> comparator.compare(levelMap.get(o1.getClassName()), levelMap.get(o2.getClassName())));
        }

        public List<StackTraceElement> getRootInterceptorStacks() {
            return rootInterceptorStacks;
        }

        public void sortRootInterceptorStacks() {
            Map<String, FastInterceptorInfo<IFastRootInterceptor>> levelMap = new HashMap<>(action.getFastRoute().rootInterceptor.size());

            for (FastInterceptorInfo<IFastRootInterceptor> routeInterceptor : action.getFastRoute().rootInterceptor) {
                levelMap.put(routeInterceptor.interceptor.getName(), routeInterceptor);
            }

            rootInterceptorStacks.sort((o1, o2) -> comparator.compare(levelMap.get(o1.getClassName()), levelMap.get(o2.getClassName())));
        }

        public List<StackTraceElement> getActionStacks() {
            return actionStacks;
        }

        public List<StackTraceElement> getOutStacks() {
            return outStacks;
        }

        public RequestStackTrace invoke() {
            rootInterceptorStacks = new ArrayList<>(16);
            beforeInterceptorStacks = new ArrayList<>(16);
            afterInterceptorStacks = new ArrayList<>(16);
            actionStacks = new ArrayList<>(16);
            outStacks = new ArrayList<>(16);
            boolean hasActionOut = false;
            if (action.getFastRoute().getMethod() != null) {
                action.getFastRoute().stackTraceElements.addAll(Arrays.asList(Thread.currentThread().getStackTrace()));

                for (StackTraceElement stackTraceElement : action.getFastRoute().stackTraceElements) {
                    Class<?> targetClass = FastClassUtils.getClass(stackTraceElement.getClassName(), false);
                    if (targetClass == null) {
                        continue;
                    }
                    if (IFastInterceptor.class.isAssignableFrom(targetClass)) {
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
                    } else if (IFastRootInterceptor.class.isAssignableFrom(targetClass)) {
                        if (action.getFastRoute().isRootInterceptor(stackTraceElement.getClassName())
                                && !rootInterceptorStacks.contains(stackTraceElement)) {
                            rootInterceptorStacks.add(stackTraceElement);
                        }
                    }
                }
                if (actionStacks.isEmpty()) {
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
                    FastChar.getLogger().error(this.getClass(), e);
                }
            }
            return this;
        }
    }


}
