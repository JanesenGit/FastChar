package com.fastchar.servlet.http;

import com.fastchar.core.FastChar;
import com.fastchar.interfaces.IFastCache;

public class FastHttpShareSessionFactory {
    private static final String SESSION_STORE_TAG = "FASTCHAR_SESSION";

    public static FastHttpShareSession getSession(String sessionId) {
        try {
            IFastCache fastCache = FastChar.getCache();
            if (fastCache.exists(SESSION_STORE_TAG, sessionId)) {
                FastHttpShareSession httpShareSession = fastCache.get(SESSION_STORE_TAG, sessionId);
                if (httpShareSession.isTimeout()) {
                    deleteSession(sessionId);
                }else{
                    httpShareSession.setLastAccessedTime(System.currentTimeMillis());
                    return httpShareSession.store();
                }
            }
        } catch (Exception e) {
            FastChar.getLogger().error(FastHttpShareSessionFactory.class, e);
        }
        return new FastHttpShareSession()
                .setId(sessionId)
                .setCreationTime(System.currentTimeMillis())
                .setLastAccessedTime(System.currentTimeMillis())
                .setMaxInactiveInterval(FastChar.getConstant().getSessionMaxInterval())
                .store();
    }

    public static void deleteSession(String sessionId) {
        try {
            FastChar.getCache().delete(SESSION_STORE_TAG, sessionId);
        } catch (Exception e) {
            FastChar.getLogger().error(FastHttpShareSessionFactory.class, e);
        }
    }

    public static void saveSession(FastHttpShareSession session) {
        try {
            if (session.isTimeout()) {
                deleteSession(session.getId());
                return;
            }
            FastChar.getCache().set(SESSION_STORE_TAG, session.getId(), session);
        } catch (Exception e) {
            FastChar.getLogger().error(FastHttpShareSessionFactory.class, e);
        }
    }


}
