package com.fastchar.object;

/**
 * 对象操作类
 * @author 沈建（Janesen）
 * @date 2021/8/31 11:22
 */
public class FastObjectProperty {
    private transient final Object property;
    private transient final Object target;

    public FastObjectProperty(Object target, Object property) {
        this.property = property;
        this.target = target;
    }

    public Object getValue() {
        return new FastObjectGetHandler(target, property).get();
    }

    public void setValue(Object value) {
        new FastObjectSetHandler(target, property).set(value);
    }

    public void addValue(Object value) {
        new FastObjectAddHandler(target, property).add(value);
    }

    public void addValue(int index,Object value) {
        new FastObjectAddHandler(target, property).add(index, value);
    }

    public void addKeyValue(String key, Object value) {
        new FastObjectAddHandler(target, property).addKeyValue(key, value);

    }



}
