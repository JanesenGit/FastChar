package com.fastchar.utils;

import com.fastchar.core.FastChar;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * Java原生HttpURLConnection辅助类
 * @author 沈建（Janesen）
 * @date 2021/10/27 10:30
 */
public class FastHttpURLConnectionUtils {

    /**
     * 打开地址输入流，支持http重定向https问题
     * @param urlStr 网络地址
     * @return InputStream
     */
    public static InputStream getInputStream(String urlStr) {
        HashMap<String, String> headers = new HashMap<>(16);
        headers.put("User-Agent", "");
        return getInputStream(urlStr, headers);
    }

    /**
     * 打开地址输入流，支持http重定向https问题
     *
     * @param urlStr 网络地址
     * @return InputStream
     */
    public static InputStream getInputStream(String urlStr, Map<String, String> headers) {
        try {
            trustAllHttpsCertificates();
            URL url = new URL(urlStr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(60 * 1000);
            httpURLConnection.setReadTimeout(3 * 60 * 1000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(true);
            for (Map.Entry<String, String> stringStringEntry : headers.entrySet()) {
                httpURLConnection.setRequestProperty(stringStringEntry.getKey(), stringStringEntry.getValue());
            }
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            if (httpURLConnection.getResponseCode() == 302 || httpURLConnection.getResponseCode() == 301) {
                String newLocation = httpURLConnection.getHeaderField("Location");
                newLocation = new String(newLocation.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                return getInputStream(newLocation);
            }
            return httpURLConnection.getInputStream();
        } catch (Exception e) {
            FastChar.getLogger().error(FastHttpURLConnectionUtils.class, e);
        }
        return null;
    }


    private static void trustAllHttpsCertificates()
            throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[1];
        trustAllCerts[0] = new TrustAllManager();
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        HttpURLConnection.setFollowRedirects(true);
    }

    private static class TrustAllManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }
    }



}
