package com.fastchar.core;

import com.fastchar.utils.FastArrayUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FastUrlParser {

    public static String getPureUrl(String url) {
        int paramSplitChar = url.indexOf("?");
        if (paramSplitChar > 0) {
            return url.substring(0, paramSplitChar);
        }
        return url;
    }

    public static String getParamsUrl(String url) {
        int paramSplitChar = url.indexOf("?");
        if (paramSplitChar > 0) {
            return url.substring(paramSplitChar + 1);
        }
        return "";
    }


    public static String getContentPath(String url) {
        URL netUrl = getURL(url);
        if (netUrl != null) {
            String netUrlPath = netUrl.getPath();
            int projectNameIndex = netUrlPath.indexOf(FastChar.getConstant().getProjectName());
            if (projectNameIndex >= 0) {
                int index = projectNameIndex + FastChar.getConstant().getProjectName().length();
                return "/" + FastStringUtils.strip(netUrlPath.substring(index), "/");
            }
        }
        return url;
    }


    private static URL getURL(String urlString) {
        try {
            return new URL(URLDecoder.decode(urlString, "utf-8"));
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            return null;
        }
    }

    public static boolean isFileUrl(String url) {
        return getPureUrl(url).lastIndexOf(".") > 0;
    }


    public static List<FastUrl> parse(String url) {
        String requestUrl = url;
        List<FastRequestParam> params = parseParams(requestUrl);

        url = getPureUrl(url);
        //url参数分割符，/
        String[] split = FastArrayUtils.trimEmpty(FastStringUtils.splitByWholeSeparator(url, "/"));
        StringBuilder stringBuilder = new StringBuilder("/");

        List<FastUrl> urls = new ArrayList<>(split.length + 1);

        FastUrl first = new FastUrl();
        first.setLevel(1);
        first.setMethodRoute(stringBuilder.toString());
        List<String> splitList = Arrays.asList(split);
        first.getUrlParams().addAll(splitList);
        first.setParams(params);
        urls.add(first);

        int level = first.getLevel() + 1;
        for (int i = 0; i < split.length; i++) {
            String address = split[i];
            stringBuilder.append(address).append("/");
            FastUrl fastUrl = new FastUrl();
            fastUrl.setLevel(level);
            fastUrl.setParams(params);
            fastUrl.setMethodRoute(FastStringUtils.stripEnd(stringBuilder.toString(), "/"));
            fastUrl.getUrlParams().addAll(splitList.subList(i + 1, split.length));
            urls.add(fastUrl);
            level++;
        }
        urls.sort((o1, o2) -> Integer.compare(o2.getLevel(), o1.getLevel()));
        return urls;
    }


    /**
     * 解析url参数
     *
     * @param url 例如：down?path=name.txt&id=1&key=a
     */
    public static List<FastRequestParam> parseParams(String url) {
        try {
            url = URLDecoder.decode(url, "utf-8");
        } catch (Exception ignored) {
        }

        String paramsUrl = getParamsUrl(url);
        if (FastStringUtils.isEmpty(paramsUrl)) {
            return new ArrayList<>();
        }

        String[] split = FastStringUtils.splitByWholeSeparator(paramsUrl, "&");
        List<FastRequestParam> params = new ArrayList<>(split.length);
        for (String keyValue : split) {
            if (FastStringUtils.isEmpty(keyValue)) {
                continue;
            }
            int valueSplit = keyValue.indexOf("=");
            if (valueSplit >= 0) {
                params.add(new FastRequestParam().setQuery(true).setName(keyValue.substring(0, valueSplit)).setValue(keyValue.substring(valueSplit + 1)));
            }
        }
        return params;
    }

}
