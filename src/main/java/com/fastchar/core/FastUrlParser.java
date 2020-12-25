package com.fastchar.core;

import com.fastchar.utils.FastArrayUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

final class FastUrlParser {

    public static String getContentPath(String url) {
        try {
            URL netUrl = new URL( URLDecoder.decode(url,"utf-8"));
            String replace = netUrl.getPath().replace(FastChar.getConstant().getProjectName(), "");
            return "/" + FastStringUtils.strip(replace, "/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
    }


    public static List<FastUrl> parse(String url) {
        List<FastRequestParam> params = parseParams(url);

        url = url.split("[?]")[0];
        List<FastUrl> urls = new ArrayList<>();
        String[] split = FastArrayUtils.trimEmpty(url.split("/"));
        StringBuilder stringBuilder = new StringBuilder("/");

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
        Collections.sort(urls, new Comparator<FastUrl>() {
            @Override
            public int compare(FastUrl o1, FastUrl o2) {
                return Integer.compare(o2.getLevel(), o1.getLevel());
            }
        });
        return urls;
    }


    /**
     * 解析url参数
     *
     * @param url 例如：down?path=name.txt&id=1&key=a
     */
    public static List<FastRequestParam> parseParams(String url) {
        try {
            url=URLDecoder.decode(url,"utf-8");
        } catch (Exception ignored) { }
        String[] splitUrl = url.split("[?]");
        if (splitUrl.length != 2) {
            return null;
        }
        List<FastRequestParam> params = new ArrayList<>();
        String[] split = splitUrl[1].split("[&]");
        for (String keyValue : split) {
            String[] values = keyValue.split("[=]");
            if (values.length == 2) {
                params.add(new FastRequestParam().setName(values[0]).setValue(values[1]));
            }
        }
        return params;
    }
}
