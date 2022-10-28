package com.fastchar.core;

import com.fastchar.extend.freemarker.FastFreemarkerEngine;
import com.fastchar.extend.thymeleaf.FastThymeleafEngine;
import com.fastchar.extend.velocity.FastVelocityEngine;

import java.util.HashMap;
import java.util.Map;

public final class FastTemplates {

    private final Map<String, Object> finalContext = new HashMap<>(16);

    FastTemplates() {
    }

    public FastFreemarkerEngine getFreemarker() {
        return  FastChar.getOverrides().singleInstance(FastFreemarkerEngine.class);
    }

    public FastThymeleafEngine getThymeleaf() {
       return  FastChar.getOverrides().singleInstance(FastThymeleafEngine.class);
    }

    public FastVelocityEngine getVelocity() {
        return FastChar.getOverrides().singleInstance(FastVelocityEngine.class);
    }

    public FastTemplates put(String key, Object value) {
        finalContext.put(key, value);
        return this;
    }

    public Map<String, Object> getFinalContext() {
        return finalContext;
    }


}
