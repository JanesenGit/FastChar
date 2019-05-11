package com.fastchar.core;

import com.fastchar.utils.FastClassUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.Vector;

@SuppressWarnings("UnusedReturnValue")
public final class FastObservable{

    public FastEngine engine() {
        return FastEngine.instance();
    }
    private Vector<Object> obs;

    public FastObservable() {
        obs = new Vector<>();
    }

    public synchronized FastObservable addObserver(Object o) {
        if (o == null)
            return this;
        if (!obs.contains(o)) {
            obs.addElement(o);
        }
        return this;
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


    public List<Object> notifyObservers(String methodName, Object... params) {
        Object[] arrLocal;
        synchronized (this) {
            arrLocal = obs.toArray();
        }
        List<Object> results = new ArrayList<>();
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


}
