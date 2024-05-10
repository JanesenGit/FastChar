package com.fastchar.core;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.interfaces.IFastWeb;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;

import java.util.ArrayList;
import java.util.List;

final class FastWebs {
    private final List<Class<? extends IFastWeb>> webs = new ArrayList<>(5);
    private final List<Class<? extends IFastWeb>> initialed =new ArrayList<>(5);
    private final List<Class<? extends IFastWeb>> ran = new ArrayList<>(5);
    private final List<Class<? extends IFastWeb>> finished = new ArrayList<>(5);
    private final List<Class<? extends IFastWeb>> registered = new ArrayList<>(5);

    void putFastWeb(Class<? extends IFastWeb> webClass) {
        if (!FastClassUtils.checkNewInstance(webClass)) {
            return;
        }
        if (webs.contains(webClass)) {
            return;
        }
        webs.add(webClass);
    }

    public void flush() {
        List<Class<? extends IFastWeb>> waitRemove = new ArrayList<>(16);
        for (Class<? extends IFastWeb> aClass : webs) {
            if (FastClassUtils.isRelease(aClass)) {
                waitRemove.add(aClass);
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLogger().warn(FastWebs.class,
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
        finished.removeAll(waitRemove);
        registered.removeAll(waitRemove);
    }


    void sortWeb() {
        webs.sort((o1, o2) -> {
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
        });
    }

    void onRegisterWeb(FastEngine engine) throws Exception {
        sortWeb();
        ArrayList<Class<? extends IFastWeb>> doingArray = new ArrayList<>(webs);
        for (Class<? extends IFastWeb> web : doingArray) {
            if (registered.contains(web)) {
                continue;
            }
            registered.add(web);
            IFastWeb iFastWeb = FastChar.getOverrides().singleInstance(web);
            iFastWeb.onRegister(engine);
        }
    }

    void onInitWeb(FastEngine engine) throws Exception {
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

    void onRunWeb(FastEngine engine) throws Exception {
        sortWeb();
        ArrayList<Class<? extends IFastWeb>> doingArray = new ArrayList<>(webs);
        for (Class<? extends IFastWeb> web : doingArray) {
            if (ran.contains(web)) {
                continue;
            }
            ran.add(web);
            IFastWeb iFastWeb = FastChar.getOverrides().singleInstance(web);
            iFastWeb.onRun(engine);
        }
    }

    void onFinishWeb(FastEngine engine) throws Exception {
        sortWeb();
        ArrayList<Class<? extends IFastWeb>> doingArray = new ArrayList<>(webs);
        for (Class<? extends IFastWeb> web : doingArray) {
            if (finished.contains(web)) {
                continue;
            }
            finished.add(web);
            IFastWeb iFastWeb = FastChar.getOverrides().singleInstance(web);
            iFastWeb.onFinish(engine);
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
