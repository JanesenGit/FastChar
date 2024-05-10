package com.fastchar.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FastWatcher {

    private final Map<String, List<Object>> watcher_list = new LinkedHashMap<>();

    public void register(Object obj) {
        if (obj == null) {
            return;
        }
        String key = obj.getClass().getSimpleName();
        if (!watcher_list.containsKey(key)) {
            watcher_list.put(key, new ArrayList<>());
        }
        watcher_list.get(key).add(obj);
    }


    public void unregister(Object obj) {
        if (obj == null) {
            return;
        }
        String key = obj.getClass().getSimpleName();
        if (!watcher_list.containsKey(key)) {
            watcher_list.put(key, new ArrayList<>());
        }
        watcher_list.get(key).remove(obj);
    }

}
