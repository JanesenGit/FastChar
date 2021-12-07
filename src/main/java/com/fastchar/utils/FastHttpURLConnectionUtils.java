package com.fastchar.utils;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

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
        try {
            trustAllHttpsCertificates();
            URL url=new URL(urlStr);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(60 * 1000);
            httpURLConnection.setReadTimeout(3 * 60 * 1000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(true);
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");// 维持长连接
            if (httpURLConnection.getResponseCode() == 302) {
                String newLocation = httpURLConnection.getHeaderField("Location");
                return getInputStream(newLocation);
            }
            return httpURLConnection.getInputStream();
        } catch (Exception e) {
            e.printStackTrace();
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
