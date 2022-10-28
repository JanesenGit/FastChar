package com.fastchar.utils;

import com.fastchar.servlet.http.FastHttpServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author 沈建（Janesen）
 * @date 2020/8/6 17:46
 */
public class FastRequestUtils {

    private static final Map<String, String> HTTP_CHAR_MAP = new HashMap<>(16);
    static {
        HTTP_CHAR_MAP.put("/", "_@A_");
        HTTP_CHAR_MAP.put(":", "_@B_");
        HTTP_CHAR_MAP.put(".", "_@C_");
        HTTP_CHAR_MAP.put("?", "_@D_");
        HTTP_CHAR_MAP.put("&", "_@E_");
        HTTP_CHAR_MAP.put("=", "_@F_");
        HTTP_CHAR_MAP.put("+", "_@G_");

    }


    public FastRequestUtils() {
    }

    public static Hashtable<String, String[]> parseQueryString(String s) {
        String[] valArray = null;
        if (s == null) {
            throw new IllegalArgumentException();
        } else {
            Hashtable<String, String[]> ht = new Hashtable<>();
            StringBuilder sb = new StringBuilder();

            String key;
            for(StringTokenizer st = new StringTokenizer(s, "&"); st.hasMoreTokens(); ht.put(key, valArray)) {
                String pair = st.nextToken();
                int pos = pair.indexOf(61);
                if (pos == -1) {
                    throw new IllegalArgumentException();
                }

                key = parseName(pair.substring(0, pos), sb);
                String val = parseName(pair.substring(pos + 1, pair.length()), sb);
                if (!ht.containsKey(key)) {
                    valArray = new String[]{val};
                } else {
                    String[] oldVals = (String[])ht.get(key);
                    valArray = new String[oldVals.length + 1];

                    System.arraycopy(oldVals, 0, valArray, 0, oldVals.length);

                    valArray[oldVals.length] = val;
                }
            }

            return ht;
        }
    }

    public static Hashtable<String, String[]> parsePostData(int len, InputStream in) {
        if (len <= 0) {
            return new Hashtable<>();
        } else if (in == null) {
            throw new IllegalArgumentException();
        } else {
            byte[] postedBytes = new byte[len];

            try {
                int offset = 0;

                do {
                    int inputLen = in.read(postedBytes, offset, len - offset);
                    if (inputLen <= 0) {
                        throw new IllegalArgumentException("Short read");
                    }

                    offset += inputLen;
                } while(len - offset > 0);
            } catch (IOException var7) {
                throw new IllegalArgumentException(var7.getMessage());
            }

            try {
                String postedBody = new String(postedBytes, 0, len, "8859_1");
                return parseQueryString(postedBody);
            } catch (UnsupportedEncodingException var6) {
                throw new IllegalArgumentException(var6.getMessage());
            }
        }
    }

    private static String parseName(String s, StringBuilder sb) {
        sb.setLength(0);

        for(int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch(c) {
                case '%':
                    try {
                        sb.append((char)Integer.parseInt(s.substring(i + 1, i + 3), 16));
                        i += 2;
                    } catch (NumberFormatException var6) {
                        throw new IllegalArgumentException();
                    } catch (StringIndexOutOfBoundsException var7) {
                        String rest = s.substring(i);
                        sb.append(rest);
                        if (rest.length() == 2) {
                            ++i;
                        }
                    }
                    break;
                case '+':
                    sb.append(' ');
                    break;
                default:
                    sb.append(c);
            }
        }

        return sb.toString();
    }

    public static StringBuffer getRequestURL(FastHttpServletRequest req) {
        StringBuffer url = new StringBuffer();
        String scheme = req.getScheme();
        int port = req.getServerPort();
        String urlPath = req.getRequestURI();
        url.append(scheme);
        url.append("://");
        url.append(req.getServerName());
        if ("http".equals(scheme) && port != 80 || "https".equals(scheme) && port != 443) {
            url.append(':');
            url.append(req.getServerPort());
        }

        url.append(urlPath);
        return url;
    }


    public static String getRequestParamString(FastHttpServletRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        List<String> params = new ArrayList<>(16);
        for (Map.Entry<String, String[]> stringEntry : parameterMap.entrySet()) {
            params.add(stringEntry.getKey() + "=" + FastStringUtils.join(stringEntry.getValue(), ","));
        }
        return FastStringUtils.join(params, "&");
    }


    public static String encodeUrl(String url) {
        for (Map.Entry<String, String> stringStringEntry : HTTP_CHAR_MAP.entrySet()) {
            url = url.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
        }
        return url;
    }

    public static String decodeUrl(String url) {
        for (Map.Entry<String, String> stringStringEntry : HTTP_CHAR_MAP.entrySet()) {
            url = url.replace(stringStringEntry.getValue(), stringStringEntry.getKey());
        }
        return url;
    }


    public static String encodeFileName(FastHttpServletRequest request, String fileName) {
        String userAgent = request.getHeader("User-Agent");
        try {
            String encodedFileName = URLEncoder.encode(fileName, "UTF8");
            if (userAgent == null) {
                return "filename=\"" + encodedFileName + "\"";
            }
            userAgent = userAgent.toLowerCase();
            if (userAgent.contains("msie")) {
                return "filename=\"" + encodedFileName + "\"";
            }
            if (userAgent.contains("opera")) {
                return "filename*=UTF-8''" + encodedFileName;
            }
            if (userAgent.contains("safari") || userAgent.contains("applewebkit") || userAgent.contains("chrome")) {
                return "filename=\"" + new String(fileName.getBytes(StandardCharsets.UTF_8), "ISO8859-1") + "\"";
            }
            if (userAgent.contains("mozilla")) {
                return "filename*=UTF-8''" + encodedFileName;
            }
            return "filename=\"" + encodedFileName + "\"";
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
