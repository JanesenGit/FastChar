package com.fastchar.core;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.interfaces.IFastWeb;
import com.fastchar.interfaces.IFastWebRun;
import com.fastchar.utils.FastClassUtils;

import java.lang.reflect.Modifier;
import java.util.*;

final class FastWebs {
    private List<Class<? extends IFastWeb>> webs = new ArrayList<>();
    private Set<String> inited = new HashSet<>();

    FastWebs addFastWeb(Class<? extends IFastWeb> webClass) {
        if (!FastClassUtils.checkNewInstance(webClass)) {
            return this;
        }
        if (webs.contains(webClass)) {
            return this;
        }
        webs.add(webClass);
        return this;
    }


    void sortWeb() {
        Collections.sort(webs, new Comparator<Class<? extends IFastWeb>>() {
            @Override
            public int compare(Class<? extends IFastWeb> o1, Class<? extends IFastWeb> o2) {
                int priority1 = 0, priority2 = 0;

                if (o1.isAnnotationPresent(AFastPriority.class)) {
                    AFastPriority priority = o1.getAnnotation(AFastPriority.class);
                    priority1 = priority.value();
                }

                if (o2.isAnnotationPresent(AFastPriority.class)) {
                    AFastPriority priority = o2.getAnnotation(AFastPriority.class);
                    priority2 = priority.value();
                }

                if (priority1 > priority2) {
                    return -1;
                }
                if (priority1 < priority2) {
                    return 1;
                }
                return 0;
            }
        });
    }

    void initWeb(FastEngine engine) throws Exception {
        sortWeb();
        for (Class<? extends IFastWeb> web : webs) {
            if (inited.contains(web.getName())) {
                continue;
            }
            inited.add(web.getName());
            IFastWeb iFastWeb = FastChar.getOverrides().singleInstance(web);
            if (iFastWeb != null) {
                iFastWeb.onInit(engine);
            }
        }
    }

    void runWeb(FastEngine engine) throws Exception {
        sortWeb();
        for (Class<? extends IFastWeb> web : webs) {
            IFastWeb iFastWeb = FastChar.getOverrides().singleInstance(web);
            if ((iFastWeb instanceof IFastWebRun)) {
                ((IFastWebRun) iFastWeb).onRun(engine);
            }
        }
    }

    void destroyWeb(FastEngine engine) throws Exception {
        for (Class<? extends IFastWeb> web : webs) {
            IFastWeb iFastWeb = FastChar.getOverrides().singleInstance(web);
            if (iFastWeb != null) {
                iFastWeb.onDestroy(engine);
            }
        }
    }


}
