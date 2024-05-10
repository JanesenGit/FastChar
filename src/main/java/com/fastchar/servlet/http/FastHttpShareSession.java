package com.fastchar.servlet.http;

import com.fastchar.servlet.FastServletContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 共享session
 */
public class FastHttpShareSession implements Serializable {

    private transient FastServletContext servletContext;

    private String id;
    private long creationTime;

    private long lastAccessedTime;

    private int maxInactiveInterval;

    private volatile Map<String, Object> attribute;

    public FastHttpShareSession store() {
        FastHttpShareSessionFactory.saveSession(this);
        return this;
    }


    public boolean isTimeout() {
        return System.currentTimeMillis() >= lastAccessedTime + maxInactiveInterval * 1000L;
    }

    public String getId() {
        return id;
    }

    public FastHttpShareSession setId(String id) {
        this.id = id;
        return this;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public FastHttpShareSession setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public FastHttpShareSession setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
        return this;
    }

    public FastServletContext getServletContext() {
        return servletContext;
    }

    public FastHttpShareSession setServletContext(FastServletContext servletContext) {
        this.servletContext = servletContext;
        return this;
    }


    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public FastHttpShareSession setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
        return this;
    }

    public Map<String, Object> getAttribute() {
        if (attribute == null) {
            synchronized (FastHttpShareSession.class) {
                if (attribute == null) {
                    attribute = new HashMap<>();
                }
            }
        }
        return attribute;
    }

    public FastHttpShareSession setAttribute(Map<String, Object> attribute) {
        this.attribute = attribute;
        return this;
    }


}
