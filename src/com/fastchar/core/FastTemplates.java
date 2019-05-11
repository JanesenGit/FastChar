package com.fastchar.core;

import com.fastchar.extend.freemarker.FastFreemarkerEngine;
import com.fastchar.extend.thymeleaf.FastThymeleafEngine;
import com.fastchar.extend.velocity.FastVelocityEngine;

import java.util.HashMap;
import java.util.Map;

public final class FastTemplates {

    private Map<String, Object> finalContext = new HashMap<>();

    //for thymeleaf
    private FastThymeleafEngine thymeleaf;

    //for freemarker
    private FastFreemarkerEngine freemarker;

    //for velocity
    private FastVelocityEngine velocity;


    public FastFreemarkerEngine getFreemarker() {
        if (freemarker == null) {
            freemarker = FastChar.getOverrides().newInstance(FastFreemarkerEngine.class);
        }
        return freemarker;
    }

    public FastThymeleafEngine getThymeleaf() {
        if (thymeleaf == null) {
            thymeleaf = FastChar.getOverrides().newInstance(FastThymeleafEngine.class);
        }
        return thymeleaf;
    }

    public FastVelocityEngine getVelocity() {
        if (velocity == null) {
            velocity = FastChar.getOverrides().newInstance(FastVelocityEngine.class);
        }
        return velocity;
    }

    public FastTemplates put(String key, Object value) {
        finalContext.put(key, value);
        return this;
    }

    public Map<String, Object> getFinalContext() {
        return finalContext;
    }
}
