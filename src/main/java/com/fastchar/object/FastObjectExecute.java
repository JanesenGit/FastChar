package com.fastchar.object;

import com.fastchar.utils.FastNumberUtils;
import com.fastchar.utils.FastStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表达式读取对象的值
 *
 * @author 沈建（Janesen）
 * @date 2021/8/20 10:02
 */
public class FastObjectExecute {
    private static final Pattern ATTR_PATTERN = Pattern.compile("\\$\\{(.*)}");
    private static final Pattern EXTRACT_PATTERN = Pattern.compile("(.*)\\[(\\d+)]");
    private final Object value;

    public FastObjectExecute(Object value) {
        this.value = value;
    }

    public Object execute(String expression) {
        return executeProperty(expression).getValue();
    }

    public FastObjectProperty executeProperty(String expression) {
        if (FastStringUtils.isEmpty(expression)) {
            return new FastObjectProperty(value, expression);
        }
        Matcher matcher = ATTR_PATTERN.matcher(expression);
        if (matcher.find()) {
            List<Object> extractAttr = extractAttr(matcher.group(1));
            if (extractAttr.isEmpty()) {
                return null;
            }
            Object firstAttr = extractAttr.get(0);
            if (extractAttr.size() > 1) {
                Object nextValue = new FastObjectProperty(value, firstAttr).getValue();
                if (nextValue != null) {
                    List<Object> nextAttrs = extractAttr.subList(1, extractAttr.size());
                    return new FastObjectExecute(nextValue).executeProperty("${" + FastStringUtils.join(nextAttrs, ".") + "}");
                }
            }
            return new FastObjectProperty(value, firstAttr);
        }
        return null;
    }

    public List<Object> extractAttr(String attr) {
        List<Object> attrList = new ArrayList<>(16);
        String[] inAttrs = FastStringUtils.splitByWholeSeparator(attr,".");
        for (String inAttr : inAttrs) {
            if (FastStringUtils.isEmpty(inAttr)) {
                continue;
            }
            Matcher matcher = EXTRACT_PATTERN.matcher(inAttr);
            if (matcher.find()) {
                String realAttr = matcher.group(1);
                int index = FastNumberUtils.formatToInt(matcher.group(2));
                attrList.addAll(extractAttr(realAttr));
                attrList.add(index);
            }else{
                attrList.add(inAttr);
            }
        }
        return attrList;
    }

}
