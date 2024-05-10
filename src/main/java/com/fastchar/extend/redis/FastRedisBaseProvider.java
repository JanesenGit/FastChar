package com.fastchar.extend.redis;

public abstract class FastRedisBaseProvider {

    protected String wrapKey(String tag, String key) {
        return this.safeString(tag) + "#" + this.safeString(key);
    }

    //redis避免属性 . 匹配符
    protected String safeString(String tag) {
        return tag.replaceAll("\\.", "_");
    }

    protected String wrapPattern( String pattern) {
        return "*" + pattern + "*";
    }

}
