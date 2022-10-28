package com.fastchar.core;

import com.fastchar.annotation.AFastObserver;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastClassUtils;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public final class FastObservable {

    public FastEngine engine() {
        return FastEngine.instance();
    }

    private final Vector<Object> obs;

    public FastObservable() {
        obs = new Vector<>();
    }

    private synchronized void sortObserver() {
        Collections.sort(obs, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int priority1 = 0, priority2 = 0;
                if (o1.getClass().isAnnotationPresent(AFastObserver.class)) {
                    AFastObserver observer = o1.getClass().getAnnotation(AFastObserver.class);
                    priority1 = observer.priority();
                }
                if (o2.getClass().isAnnotationPresent(AFastObserver.class)) {
                    AFastObserver observer = o2.getClass().getAnnotation(AFastObserver.class);
                    priority2 = observer.priority();
                }
                return Integer.compare(priority2, priority1);
            }
        });
    }

    public synchronized FastObservable addObserver(Object o) {
        try {
            if (o == null) {
                return this;
            }
            if (!obs.contains(o)) {
                obs.addElement(o);
            }
            return this;
        } finally {
            sortObserver();
        }
    }

    public synchronized FastObservable addObserver(Class<?> targetClass) {
        try {
            Object o = FastChar.getOverrides().singleInstance(targetClass);
            if (o == null) {
                return this;
            }
            if (!obs.contains(o)) {
                obs.addElement(o);
            }
            return this;
        } finally {
            sortObserver();
        }
    }

    public boolean containerObserver(Class<?> targetClass) {
        for (Object ob : obs) {
            if (ob.getClass() == targetClass) {
                return true;
            }
        }
        return false;
    }

    public synchronized void deleteObserver(Observer o) {
        obs.removeElement(o);
    }


    public List<Object> notifyObservers(String methodName, Object... params) throws Exception {
        Object[] arrLocal;
        synchronized (this) {
            Collections.sort(obs, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {

                    return 0;
                }
            });
            arrLocal = obs.toArray();
        }
        List<Object> results = new ArrayList<>(16);
        for (Object o : arrLocal) {
            List<Method> declaredMethod = FastClassUtils.getDeclaredMethod(o.getClass(), methodName);
            for (Method method : declaredMethod) {
                results.add(FastClassUtils.invokeMethod(o, method, params));
            }
        }
        return results;
    }

    public synchronized void deleteObservers() {
        obs.removeAllElements();
    }


    public synchronized int countObservers() {
        return obs.size();
    }


    public synchronized void flush() {
        List<Object> waitRemove = new ArrayList<>(16);
        for (Object ob : obs) {
            if (ob == null) {
                continue;
            }
            if (FastClassUtils.isRelease(ob)) {
                waitRemove.add(ob);
                if (FastChar.getConstant().isDebug()) {
                    FastChar.getLog().warn(FastObservable.class,
                            FastChar.getLocal().getInfo(FastCharLocal.OBSERVABLE_ERROR1, ob.getClass()));
                }
            }
        }
        obs.removeAll(waitRemove);
    }

}
