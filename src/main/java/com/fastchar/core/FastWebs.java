package com.fastchar.core;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.interfaces.IFastWeb;
import com.fastchar.interfaces.IFastWebRun;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class FastWebs {
    private final List<Class<? extends IFastWeb>> webs = new ArrayList<>(5);
    private final List<Class<? extends IFastWeb>> initialed =new ArrayList<>(5);
    private final List<Class<? extends IFastWeb>> ran = new ArrayList<>(5);

    FastWebs putFastWeb(Class<? extends IFastWeb> webClass) {
        if (!FastClassUtils.checkNewInstance(webClass)) {
            return this;
        }
        if (webs.contains(webClass)) {
            return this;
        }
        webs.add(webClass);
        return this;
    }

    public void flush() {
        List<Class<? extends IFastWeb>> waitRemove = new ArrayList<>(16);
        for (Class<? extends IFastWeb> aClass : webs) {
            if (FastClassUtils.isRelease(aClass)) {
                waitRemove.add(aClass);
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().warn(FastWebs.class,
                            FastChar.getLocal().getInfo(FastCharLocal.WEB_ERROR1, aClass));
                }
            }
        }
        try {
            for (Class<? extends IFastWeb> aClass : waitRemove) {
                IFastWeb iFastWeb = FastChar.getOverrides().singleInstance(aClass);
                iFastWeb.onDestroy(FastEngine.instance());
            }
        } catch (Exception ignored) {}

        webs.removeAll(waitRemove);
        initialed.removeAll(waitRemove);
        ran.removeAll(waitRemove);
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
        ArrayList<Class<? extends IFastWeb>> doingArray = new ArrayList<>(webs);
        for (Class<? extends IFastWeb> web : doingArray) {
            if (initialed.contains(web) ) {
                continue;
            }
            initialed.add(web);
            IFastWeb iFastWeb = FastChar.getOverrides().singleInstance(web);
            if (iFastWeb != null) {
                iFastWeb.onInit(engine);
            }
        }
    }

    void runWeb(FastEngine engine) throws Exception {
        sortWeb();
        ArrayList<Class<? extends IFastWeb>> doingArray = new ArrayList<>(webs);
        for (Class<? extends IFastWeb> web : doingArray) {
            if (ran.contains(web)) {
                continue;
            }
            ran.add(web);
            IFastWeb iFastWeb = FastChar.getOverrides().singleInstance(web);
            if ((iFastWeb instanceof IFastWebRun)) {
                ((IFastWebRun) iFastWeb).onRun(engine);
            }
        }
    }

    void destroyWeb(FastEngine engine) throws Exception {
        ArrayList<Class<? extends IFastWeb>> doingArray = new ArrayList<>(webs);
        for (Class<? extends IFastWeb> web : doingArray) {
            IFastWeb iFastWeb = FastChar.getOverrides().singleInstance(web);
            if (iFastWeb != null) {
                iFastWeb.onDestroy(engine);
            }
        }
    }


}
