package com.fastchar.core;

import com.fastchar.asm.FastMethodRead;
import com.fastchar.interfaces.IFastInterceptor;
import com.fastchar.out.FastOut;
import com.fastchar.out.FastOutForward;
import com.fastchar.out.FastOutRedirect;
import com.fastchar.utils.FastClassUtils;
import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public final class FastRequestLog {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private static final String BlockChar = "*";
    private static final String SplitChar = "-";
    private static final String InChar = "->";
    private static final String OutChar = "<-";
    private static final String RootInterceptorChar = ">!";
    private static final String BeforeInterceptorChar = ">:";
    private static final String AfterInterceptorChar = ":<";


    static void log(FastAction action) {
        try {
            if (FastChar.getConstant().isDebug() && action.isLog()) {
                if (action.getFastRoute() == null || action.getFastOut() == null) {
                    return;
                }

                if (action.getFastOut().isLogged()) {
                    return;
                }
                action.getFastOut().setLogged(true);
                if (action.getForwarder() != null) {
                    log(action.getForwarder());
                }

                ActionStackTrace actionStackTraces = new ActionStackTrace(action).invoke();
                List<StackTraceElement> rootInterceptorStacks = actionStackTraces.getRootInterceptorStacks();
                List<StackTraceElement> beforeInterceptorStacks = actionStackTraces.getBeforeInterceptorStacks();
                List<StackTraceElement> afterInterceptorStacks = actionStackTraces.getAfterInterceptorStacks();
                List<StackTraceElement> actionStacks = actionStackTraces.getActionStacks();

                Date inTime = action.getFastRoute().inTime;

                LinkedHashMap<String, String> printRequestMap = new LinkedHashMap<>();
                int rootIndex = 0;
                for (int i = rootInterceptorStacks.size() - 1; i >= 0; i--) {
                    printRequestMap.put(RootInterceptorChar + "@RootInterceptor-" + rootIndex, rootInterceptorStacks.get(i).toString());
                    rootIndex++;
                }

                int beforeIndex = 0;
                for (int i = beforeInterceptorStacks.size() - 1; i >= 0; i--) {
                    printRequestMap.put(BeforeInterceptorChar + "@BeforeInterceptor-" + beforeIndex, beforeInterceptorStacks.get(i).toString());
                    beforeIndex++;
                }

                if (action.fastRoute.responseInvoked && action.fastRoute.actionLog) {
                    int actionIndex = 0;
                    for (int i = actionStacks.size() - 1; i >= 0; i--) {
                        printRequestMap.put("Action-" + actionIndex, actionStacks.get(i).toString());
                        actionIndex++;
                    }
                }

                printRequestMap.put("HttpMethod", action.getRequestMethod());
                printRequestMap.put("ContentType", action.getContentType());
                printRequestMap.put("Url", action.getFastRoute().getRoute());
                if (action.getUrlParams().size() > 0) {
                    printRequestMap.put("UrlParams", Arrays.toString(action.getUrlParams().toArray()));
                }
                Map<String, Object> paramToMap = action.getParamToMap();
                if (paramToMap.size() > 0) {
                    printRequestMap.put("Params", mapToString(paramToMap));
                }

                printRequestMap.put("InTime", simpleDateFormat.format(inTime));


                FastOut fastOut = action.getFastOut();
                LinkedHashMap<String, String> printResponseMap = new LinkedHashMap<>();

                int afterIndex = 0;
                for (int i = afterInterceptorStacks.size() - 1; i >= 0; i--) {
                    printResponseMap.put(AfterInterceptorChar + "@AfterInterceptor-" + afterIndex, afterInterceptorStacks.get(i).toString());
                    afterIndex++;
                }

                for (int i = 0; i < actionStackTraces.getOutStacks().size(); i++) {
                    printResponseMap.put("Out-" + i, actionStackTraces.getOutStacks().get(i).toString());
                }
                printResponseMap.put("ContentType", fastOut.toContentType());
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

                Date outTime = fastOut.getOutTime();
                printResponseMap.put("OutTime", simpleDateFormat.format(outTime));
                float userTotal = FastNumberUtils.formatToFloat((outTime.getTime() - inTime.getTime()) / 1000.0, 4);
                printResponseMap.put("UseTotal", userTotal + " seconds");
                int maxKeyLength = 10;
                for (String key : printRequestMap.keySet()) {
                    if (FastStringUtils.isEmpty(printRequestMap.get(key))) {
                        continue;
                    }
                    maxKeyLength = Math.max(maxKeyLength, key.length() + 1);
                }
                for (String key : printResponseMap.keySet()) {
                    if (FastStringUtils.isEmpty(printResponseMap.get(key))) {
                        continue;
                    }
                    maxKeyLength = Math.max(maxKeyLength, key.length() + 1);
                }


                StringBuilder print = new StringBuilder();
                print.append(buildSplit(BlockChar, "FastCharLog-BGN"));

                for (String key : printRequestMap.keySet()) {
                    String text = printRequestMap.get(key);
                    if (FastStringUtils.isEmpty(text)) {
                        continue;
                    }
                    String tipChar = buildChar(InChar, key);
                    print.append(tipChar).append(" ").append(formatString(key, maxKeyLength)).append(text).append("\n");
                }
                print.append(buildSplit(SplitChar, "FastCharLog-RSP"));
                for (String key : printResponseMap.keySet()) {
                    String text = printResponseMap.get(key);
                    if (FastStringUtils.isEmpty(text)) {
                        continue;
                    }
                    String tipChar = buildChar(OutChar, key);
                    print.append(tipChar).append(" ").append(formatString(key, maxKeyLength)).append(text).append("\n");
                }
                print.append(buildSplit(BlockChar, "FastCharLog-END"));

                if (userTotal > FastChar.getConstant().getMaxUseTotalLog()) {
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
        for (int i = 0; i < targetLength; i++) {
            if (i >= targetBuilder.length()) {
                targetBuilder.append(" ");
            }
        }
        target = targetBuilder.toString();
        return target + "：";
    }


    private static String mapToString(Map<String, Object> map) {
        List<String> strings = new ArrayList<>();
        for (String s : map.keySet()) {
            Object value = map.get(s);
            if (value instanceof String[]) {
                strings.add(s + "=" + Arrays.toString((String[]) value));
            }else{
                strings.add(s + "=" + value);
            }
        }
        return "{" + FastStringUtils.join(strings, ",") + "}";
    }

    private static class ActionStackTrace {
        private FastAction action;
        private List<StackTraceElement> rootInterceptorStacks;
        private List<StackTraceElement> beforeInterceptorStacks;
        private List<StackTraceElement> afterInterceptorStacks;
        private List<StackTraceElement> actionStacks;
        private List<StackTraceElement> outStacks;
        public ActionStackTrace(FastAction action) {
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

        public ActionStackTrace invoke() {
            rootInterceptorStacks = new ArrayList<>();
            beforeInterceptorStacks = new ArrayList<>();
            afterInterceptorStacks = new ArrayList<>();
            actionStacks = new ArrayList<>();
            outStacks = new ArrayList<>();
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
                    } else if (FastOut.class.isAssignableFrom(targetClass)&& targetClass != FastOut.class) {
                        if (!outStacks.contains(stackTraceElement)) {
                            outStacks.add(stackTraceElement);
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
            if (action.getFastOut() != null) {
                try {
                    FastMethodRead methodRead = new FastMethodRead();
                    List<FastMethodRead.MethodLine> responses = methodRead.getMethodLineNumber(action.getFastOut().getClass(), "response");
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
