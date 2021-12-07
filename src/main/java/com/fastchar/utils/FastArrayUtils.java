package com.fastchar.utils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * from org.apache.commons.lang3
 */
public class FastArrayUtils {
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class[0];
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
    public static final short[] EMPTY_SHORT_ARRAY = new short[0];
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
    public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];
    public static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];
    public static final char[] EMPTY_CHAR_ARRAY = new char[0];
    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];


    public static Object[] nullToEmpty(Object[] array) {
        return isEmpty(array) ? EMPTY_OBJECT_ARRAY : array;
    }

    public static Class<?>[] nullToEmpty(Class<?>[] array) {
        return isEmpty((Object[]) array) ? EMPTY_CLASS_ARRAY : array;
    }

    public static String[] nullToEmpty(String[] array) {
        return isEmpty((Object[]) array) ? EMPTY_STRING_ARRAY : array;
    }

    public static long[] nullToEmpty(long[] array) {
        return isEmpty(array) ? EMPTY_LONG_ARRAY : array;
    }

    public static int[] nullToEmpty(int[] array) {
        return isEmpty(array) ? EMPTY_INT_ARRAY : array;
    }

    public static short[] nullToEmpty(short[] array) {
        return isEmpty(array) ? EMPTY_SHORT_ARRAY : array;
    }

    public static char[] nullToEmpty(char[] array) {
        return isEmpty(array) ? EMPTY_CHAR_ARRAY : array;
    }

    public static byte[] nullToEmpty(byte[] array) {
        return isEmpty(array) ? EMPTY_BYTE_ARRAY : array;
    }

    public static double[] nullToEmpty(double[] array) {
        return isEmpty(array) ? EMPTY_DOUBLE_ARRAY : array;
    }

    public static float[] nullToEmpty(float[] array) {
        return isEmpty(array) ? EMPTY_FLOAT_ARRAY : array;
    }

    public static boolean[] nullToEmpty(boolean[] array) {
        return isEmpty(array) ? EMPTY_BOOLEAN_ARRAY : array;
    }

    public static Long[] nullToEmpty(Long[] array) {
        return isEmpty((Object[]) array) ? EMPTY_LONG_OBJECT_ARRAY : array;
    }

    public static Integer[] nullToEmpty(Integer[] array) {
        return isEmpty((Object[]) array) ? EMPTY_INTEGER_OBJECT_ARRAY : array;
    }

    public static Short[] nullToEmpty(Short[] array) {
        return isEmpty((Object[]) array) ? EMPTY_SHORT_OBJECT_ARRAY : array;
    }

    public static Character[] nullToEmpty(Character[] array) {
        return isEmpty((Object[]) array) ? EMPTY_CHARACTER_OBJECT_ARRAY : array;
    }

    public static Byte[] nullToEmpty(Byte[] array) {
        return isEmpty((Object[]) array) ? EMPTY_BYTE_OBJECT_ARRAY : array;
    }

    public static Double[] nullToEmpty(Double[] array) {
        return isEmpty((Object[]) array) ? EMPTY_DOUBLE_OBJECT_ARRAY : array;
    }

    public static Float[] nullToEmpty(Float[] array) {
        return isEmpty((Object[]) array) ? EMPTY_FLOAT_OBJECT_ARRAY : array;
    }

    public static Boolean[] nullToEmpty(Boolean[] array) {
        return isEmpty((Object[]) array) ? EMPTY_BOOLEAN_OBJECT_ARRAY : array;
    }


    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(long[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(short[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(char[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(byte[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(double[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(float[] array) {
        return array == null || array.length == 0;
    }

    public static boolean isEmpty(boolean[] array) {
        return array == null || array.length == 0;
    }

    public static <T> boolean isNotEmpty(T[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isNotEmpty(long[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isNotEmpty(int[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isNotEmpty(short[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isNotEmpty(char[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isNotEmpty(byte[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isNotEmpty(double[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isNotEmpty(float[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isNotEmpty(boolean[] array) {
        return array != null && array.length != 0;
    }

    public static boolean isArray(Object value) {
        return value != null && ((value instanceof Array) || value.getClass().isArray());
    }



    public static <T> T[] clone(T[] array) {
        return array == null ? null : (T[]) (Object[]) array.clone();
    }

    public static long[] clone(long[] array) {
        return array == null ? null : (long[]) array.clone();
    }

    public static int[] clone(int[] array) {
        return array == null ? null : (int[]) array.clone();
    }

    public static short[] clone(short[] array) {
        return array == null ? null : (short[]) array.clone();
    }

    public static char[] clone(char[] array) {
        return array == null ? null : (char[]) array.clone();
    }

    public static byte[] clone(byte[] array) {
        return array == null ? null : (byte[]) array.clone();
    }

    public static double[] clone(double[] array) {
        return array == null ? null : (double[]) array.clone();
    }

    public static float[] clone(float[] array) {
        return array == null ? null : (float[]) array.clone();
    }

    public static boolean[] clone(boolean[] array) {
        return array == null ? null : (boolean[]) array.clone();
    }


    public static <T> T[] remove(T[] array, int index) {
        return (T[]) remove((Object) array, index);
    }

    public static <T> T[] removeElement(T[] array, Object element) {
        int index = indexOf(array, element);
        return index == -1 ? clone(array) : remove(array, index);
    }

    public static boolean[] remove(boolean[] array, int index) {
        return (boolean[]) ((boolean[]) remove((Object) array, index));
    }

    public static boolean[] removeElement(boolean[] array, boolean element) {
        int index = indexOf(array, element);
        return index == -1 ? clone(array) : remove(array, index);
    }

    public static byte[] remove(byte[] array, int index) {
        return (byte[]) ((byte[]) remove((Object) array, index));
    }

    public static byte[] removeElement(byte[] array, byte element) {
        int index = indexOf(array, element);
        return index == -1 ? clone(array) : remove(array, index);
    }

    public static char[] remove(char[] array, int index) {
        return (char[]) ((char[]) remove((Object) array, index));
    }

    public static char[] removeElement(char[] array, char element) {
        int index = indexOf(array, element);
        return index == -1 ? clone(array) : remove(array, index);
    }

    public static double[] remove(double[] array, int index) {
        return (double[]) ((double[]) remove((Object) array, index));
    }

    public static double[] removeElement(double[] array, double element) {
        int index = indexOf(array, element);
        return index == -1 ? clone(array) : remove(array, index);
    }

    public static float[] remove(float[] array, int index) {
        return (float[]) ((float[]) remove((Object) array, index));
    }

    public static float[] removeElement(float[] array, float element) {
        int index = indexOf(array, element);
        return index == -1 ? clone(array) : remove(array, index);
    }

    public static int[] remove(int[] array, int index) {
        return (int[]) ((int[]) remove((Object) array, index));
    }

    public static int[] removeElement(int[] array, int element) {
        int index = indexOf(array, element);
        return index == -1 ? clone(array) : remove(array, index);
    }

    public static long[] remove(long[] array, int index) {
        return (long[]) ((long[]) remove((Object) array, index));
    }

    public static long[] removeElement(long[] array, long element) {
        int index = indexOf(array, element);
        return index == -1 ? clone(array) : remove(array, index);
    }

    public static short[] remove(short[] array, int index) {
        return (short[]) ((short[]) remove((Object) array, index));
    }

    public static short[] removeElement(short[] array, short element) {
        int index = indexOf(array, element);
        return index == -1 ? clone(array) : remove(array, index);
    }

    private static Object remove(Object array, int index) {
        int length = getLength(array);
        if (index >= 0 && index < length) {
            Object result = Array.newInstance(array.getClass().getComponentType(), length - 1);
            System.arraycopy(array, 0, result, 0, index);
            if (index < length - 1) {
                System.arraycopy(array, index + 1, result, index, length - index - 1);
            }

            return result;
        } else {
            throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
        }
    }

    public static <T> T[] removeAll(T[] array, int... indices) {
        return (T[]) removeAll((Object) array, (int[]) clone(indices));
    }

    public static <T> T[] removeElements(T[] array, T... values) {
        if (!isEmpty(array) && !isEmpty(values)) {
            HashMap<T, MutableInt> occurrences = new HashMap(values.length);
            Object[] arr$ = values;
            int len$ = values.length;

            Object v;
            for (int i$ = 0; i$ < len$; ++i$) {
                v = arr$[i$];
                MutableInt count = (MutableInt) occurrences.get(v);
                if (count == null) {
                    occurrences.put((T) v, new MutableInt(1));
                } else {
                    count.increment();
                }
            }

            BitSet toRemove = new BitSet();
            Iterator i$ = occurrences.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry<T, MutableInt> e = (Map.Entry) i$.next();
                v = e.getKey();
                int found = 0;
                int i = 0;

                for (int ct = ((MutableInt) e.getValue()).intValue(); i < ct; ++i) {
                    found = indexOf(array, v, found);
                    if (found < 0) {
                        break;
                    }

                    toRemove.set(found++);
                }
            }

            T[] result = (T[]) removeAll((Object) array, (BitSet) toRemove);
            return result;
        } else {
            return clone(array);
        }
    }

    public static byte[] removeAll(byte[] array, int... indices) {
        return (byte[]) ((byte[]) removeAll((Object) array, (int[]) clone(indices)));
    }

    public static byte[] removeElements(byte[] array, byte... values) {
        if (!isEmpty(array) && !isEmpty(values)) {
            Map<Byte, MutableInt> occurrences = new HashMap(values.length);
            byte[] arr$ = values;
            int len$ = values.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                byte v = arr$[i$];
                Byte boxed = v;
                MutableInt count = (MutableInt) occurrences.get(boxed);
                if (count == null) {
                    occurrences.put(boxed, new MutableInt(1));
                } else {
                    count.increment();
                }
            }

            BitSet toRemove = new BitSet();
            Iterator i$ = occurrences.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry<Byte, MutableInt> e = (Map.Entry) i$.next();
                Byte v = (Byte) e.getKey();
                int found = 0;
                int i = 0;

                for (int ct = ((MutableInt) e.getValue()).intValue(); i < ct; ++i) {
                    found = indexOf(array, v, found);
                    if (found < 0) {
                        break;
                    }

                    toRemove.set(found++);
                }
            }

            return (byte[]) ((byte[]) removeAll((Object) array, (BitSet) toRemove));
        } else {
            return clone(array);
        }
    }

    public static short[] removeAll(short[] array, int... indices) {
        return (short[]) ((short[]) removeAll((Object) array, (int[]) clone(indices)));
    }

    public static short[] removeElements(short[] array, short... values) {
        if (!isEmpty(array) && !isEmpty(values)) {
            HashMap<Short, MutableInt> occurrences = new HashMap(values.length);
            short[] arr$ = values;
            int len$ = values.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                short v = arr$[i$];
                Short boxed = v;
                MutableInt count = (MutableInt) occurrences.get(boxed);
                if (count == null) {
                    occurrences.put(boxed, new MutableInt(1));
                } else {
                    count.increment();
                }
            }

            BitSet toRemove = new BitSet();
            Iterator i$ = occurrences.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry<Short, MutableInt> e = (Map.Entry) i$.next();
                Short v = (Short) e.getKey();
                int found = 0;
                int i = 0;

                for (int ct = ((MutableInt) e.getValue()).intValue(); i < ct; ++i) {
                    found = indexOf(array, v, found);
                    if (found < 0) {
                        break;
                    }

                    toRemove.set(found++);
                }
            }

            return (short[]) ((short[]) removeAll((Object) array, (BitSet) toRemove));
        } else {
            return clone(array);
        }
    }

    public static int[] removeAll(int[] array, int... indices) {
        return (int[]) ((int[]) removeAll((Object) array, (int[]) clone(indices)));
    }

    public static int[] removeElements(int[] array, int... values) {
        if (!isEmpty(array) && !isEmpty(values)) {
            HashMap<Integer, MutableInt> occurrences = new HashMap(values.length);
            int[] arr$ = values;
            int len$ = values.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                int v = arr$[i$];
                Integer boxed = v;
                MutableInt count = (MutableInt) occurrences.get(boxed);
                if (count == null) {
                    occurrences.put(boxed, new MutableInt(1));
                } else {
                    count.increment();
                }
            }

            BitSet toRemove = new BitSet();
            Iterator i$ = occurrences.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry<Integer, MutableInt> e = (Map.Entry) i$.next();
                Integer v = (Integer) e.getKey();
                int found = 0;
                int i = 0;

                for (int ct = ((MutableInt) e.getValue()).intValue(); i < ct; ++i) {
                    found = indexOf(array, v, found);
                    if (found < 0) {
                        break;
                    }

                    toRemove.set(found++);
                }
            }

            return (int[]) ((int[]) removeAll((Object) array, (BitSet) toRemove));
        } else {
            return clone(array);
        }
    }

    public static char[] removeAll(char[] array, int... indices) {
        return (char[]) ((char[]) removeAll((Object) array, (int[]) clone(indices)));
    }

    public static char[] removeElements(char[] array, char... values) {
        if (!isEmpty(array) && !isEmpty(values)) {
            HashMap<Character, MutableInt> occurrences = new HashMap(values.length);
            char[] arr$ = values;
            int len$ = values.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                char v = arr$[i$];
                Character boxed = v;
                MutableInt count = (MutableInt) occurrences.get(boxed);
                if (count == null) {
                    occurrences.put(boxed, new MutableInt(1));
                } else {
                    count.increment();
                }
            }

            BitSet toRemove = new BitSet();
            Iterator i$ = occurrences.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry<Character, MutableInt> e = (Map.Entry) i$.next();
                Character v = (Character) e.getKey();
                int found = 0;
                int i = 0;

                for (int ct = ((MutableInt) e.getValue()).intValue(); i < ct; ++i) {
                    found = indexOf(array, v, found);
                    if (found < 0) {
                        break;
                    }

                    toRemove.set(found++);
                }
            }

            return (char[]) ((char[]) removeAll((Object) array, (BitSet) toRemove));
        } else {
            return clone(array);
        }
    }

    public static long[] removeAll(long[] array, int... indices) {
        return (long[]) ((long[]) removeAll((Object) array, (int[]) clone(indices)));
    }

    public static long[] removeElements(long[] array, long... values) {
        if (!isEmpty(array) && !isEmpty(values)) {
            HashMap<Long, MutableInt> occurrences = new HashMap(values.length);
            long[] arr$ = values;
            int len$ = values.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                long v = arr$[i$];
                Long boxed = v;
                MutableInt count = (MutableInt) occurrences.get(boxed);
                if (count == null) {
                    occurrences.put(boxed, new MutableInt(1));
                } else {
                    count.increment();
                }
            }

            BitSet toRemove = new BitSet();
            Iterator i$ = occurrences.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry<Long, MutableInt> e = (Map.Entry) i$.next();
                Long v = (Long) e.getKey();
                int found = 0;
                int i = 0;

                for (int ct = ((MutableInt) e.getValue()).intValue(); i < ct; ++i) {
                    found = indexOf(array, v, found);
                    if (found < 0) {
                        break;
                    }

                    toRemove.set(found++);
                }
            }

            return (long[]) ((long[]) removeAll((Object) array, (BitSet) toRemove));
        } else {
            return clone(array);
        }
    }

    public static float[] removeAll(float[] array, int... indices) {
        return (float[]) ((float[]) removeAll((Object) array, (int[]) clone(indices)));
    }

    public static float[] removeElements(float[] array, float... values) {
        if (!isEmpty(array) && !isEmpty(values)) {
            HashMap<Float, MutableInt> occurrences = new HashMap(values.length);
            float[] arr$ = values;
            int len$ = values.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                float v = arr$[i$];
                Float boxed = v;
                MutableInt count = (MutableInt) occurrences.get(boxed);
                if (count == null) {
                    occurrences.put(boxed, new MutableInt(1));
                } else {
                    count.increment();
                }
            }

            BitSet toRemove = new BitSet();
            Iterator i$ = occurrences.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry<Float, MutableInt> e = (Map.Entry) i$.next();
                Float v = (Float) e.getKey();
                int found = 0;
                int i = 0;

                for (int ct = ((MutableInt) e.getValue()).intValue(); i < ct; ++i) {
                    found = indexOf(array, v, found);
                    if (found < 0) {
                        break;
                    }

                    toRemove.set(found++);
                }
            }

            return (float[]) ((float[]) removeAll((Object) array, (BitSet) toRemove));
        } else {
            return clone(array);
        }
    }

    public static double[] removeAll(double[] array, int... indices) {
        return (double[]) ((double[]) removeAll((Object) array, (int[]) clone(indices)));
    }

    public static double[] removeElements(double[] array, double... values) {
        if (!isEmpty(array) && !isEmpty(values)) {
            HashMap<Double, MutableInt> occurrences = new HashMap(values.length);
            double[] arr$ = values;
            int len$ = values.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                double v = arr$[i$];
                Double boxed = v;
                MutableInt count = (MutableInt) occurrences.get(boxed);
                if (count == null) {
                    occurrences.put(boxed, new MutableInt(1));
                } else {
                    count.increment();
                }
            }

            BitSet toRemove = new BitSet();
            Iterator i$ = occurrences.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry<Double, MutableInt> e = (Map.Entry) i$.next();
                Double v = (Double) e.getKey();
                int found = 0;
                int i = 0;

                for (int ct = ((MutableInt) e.getValue()).intValue(); i < ct; ++i) {
                    found = indexOf(array, v, found);
                    if (found < 0) {
                        break;
                    }

                    toRemove.set(found++);
                }
            }

            return (double[]) ((double[]) removeAll((Object) array, (BitSet) toRemove));
        } else {
            return clone(array);
        }
    }

    public static boolean[] removeAll(boolean[] array, int... indices) {
        return (boolean[]) ((boolean[]) removeAll((Object) array, (int[]) clone(indices)));
    }

    public static boolean[] removeElements(boolean[] array, boolean... values) {
        if (!isEmpty(array) && !isEmpty(values)) {
            HashMap<Boolean, MutableInt> occurrences = new HashMap(2);
            boolean[] arr$ = values;
            int len$ = values.length;

            for (int i$ = 0; i$ < len$; ++i$) {
                boolean v = arr$[i$];
                Boolean boxed = v;
                MutableInt count = (MutableInt) occurrences.get(boxed);
                if (count == null) {
                    occurrences.put(boxed, new MutableInt(1));
                } else {
                    count.increment();
                }
            }

            BitSet toRemove = new BitSet();
            Iterator i$ = occurrences.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry<Boolean, MutableInt> e = (Map.Entry) i$.next();
                Boolean v = (Boolean) e.getKey();
                int found = 0;
                int i = 0;

                for (int ct = ((MutableInt) e.getValue()).intValue(); i < ct; ++i) {
                    found = indexOf(array, v, found);
                    if (found < 0) {
                        break;
                    }

                    toRemove.set(found++);
                }
            }

            return (boolean[]) ((boolean[]) removeAll((Object) array, (BitSet) toRemove));
        } else {
            return clone(array);
        }
    }

    static Object removeAll(Object array, int... indices) {
        int length = getLength(array);
        int diff = 0;
        int end;
        int dest;
        if (isNotEmpty(indices)) {
            Arrays.sort(indices);
            int i = indices.length;
            end = length;

            while (true) {
                --i;
                if (i < 0) {
                    break;
                }

                dest = indices[i];
                if (dest < 0 || dest >= length) {
                    throw new IndexOutOfBoundsException("Index: " + dest + ", Length: " + length);
                }

                if (dest < end) {
                    ++diff;
                    end = dest;
                }
            }
        }

        Object result = Array.newInstance(array.getClass().getComponentType(), length - diff);
        if (diff < length) {
            end = length;
            dest = length - diff;

            for (int i = indices.length - 1; i >= 0; --i) {
                int index = indices[i];
                if (end - index > 1) {
                    int cp = end - index - 1;
                    dest -= cp;
                    System.arraycopy(array, index + 1, result, dest, cp);
                }

                end = index;
            }

            if (end > 0) {
                System.arraycopy(array, 0, result, 0, end);
            }
        }

        return result;
    }

    static Object removeAll(Object array, BitSet indices) {
        int srcLength = getLength(array);
        int removals = indices.cardinality();
        Object result = Array.newInstance(array.getClass().getComponentType(), srcLength - removals);
        int srcIndex = 0;

        int destIndex;
        int count;
        int set;
        for (destIndex = 0; (set = indices.nextSetBit(srcIndex)) != -1; srcIndex = indices.nextClearBit(set)) {
            count = set - srcIndex;
            if (count > 0) {
                System.arraycopy(array, srcIndex, result, destIndex, count);
                destIndex += count;
            }
        }

        count = srcLength - srcIndex;
        if (count > 0) {
            System.arraycopy(array, srcIndex, result, destIndex, count);
        }

        return result;
    }


    public static String[] trimEmpty(String[] array) {
        List<String> newArray = new ArrayList<String>();
        for (String s : array) {
            if (FastStringUtils.isNotEmpty(s)) {
                newArray.add(s);
            }
        }
        return newArray.toArray(new String[]{});
    }

    public static int indexOf(short[] array, short valueToFind) {
        return indexOf((short[]) array, (short) valueToFind, 0);
    }

    public static int indexOf(short[] array, short valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            for (int i = startIndex; i < array.length; ++i) {
                if (valueToFind == array[i]) {
                    return i;
                }
            }

            return -1;
        }
    }

    public static int indexOf(float[] array, float valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    public static int indexOf(float[] array, float valueToFind, int startIndex) {
        if (isEmpty(array)) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            for (int i = startIndex; i < array.length; ++i) {
                if (valueToFind == array[i]) {
                    return i;
                }
            }

            return -1;
        }
    }


    public static int indexOf(byte[] array, byte valueToFind) {
        return indexOf((byte[]) array, (byte) valueToFind, 0);
    }

    public static int indexOf(byte[] array, byte valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            for (int i = startIndex; i < array.length; ++i) {
                if (valueToFind == array[i]) {
                    return i;
                }
            }

            return -1;
        }
    }


    public static int indexOf(int[] array, int valueToFind) {
        return indexOf((int[]) array, (int) valueToFind, 0);
    }

    public static int indexOf(int[] array, int valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            for (int i = startIndex; i < array.length; ++i) {
                if (valueToFind == array[i]) {
                    return i;
                }
            }

            return -1;
        }
    }

    public static int indexOf(boolean[] array, boolean valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    public static int indexOf(boolean[] array, boolean valueToFind, int startIndex) {
        if (isEmpty(array)) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            for (int i = startIndex; i < array.length; ++i) {
                if (valueToFind == array[i]) {
                    return i;
                }
            }

            return -1;
        }
    }


    public static int indexOf(char[] array, char valueToFind) {
        return indexOf((char[]) array, (char) valueToFind, 0);
    }


    public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
        if (array == null) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            int i;
            if (objectToFind == null) {
                for (i = startIndex; i < array.length; ++i) {
                    if (array[i] == null) {
                        return i;
                    }
                }
            } else if (array.getClass().getComponentType().isInstance(objectToFind)) {
                for (i = startIndex; i < array.length; ++i) {
                    if (objectToFind.equals(array[i])) {
                        return i;
                    }
                }
            }

            return -1;
        }
    }

    public static boolean contains(Object[] array, Object objectToFind) {
        return indexOf(array, objectToFind) != -1;
    }

    public static boolean contains(long[] array, long valueToFind) {
        return indexOf(array, valueToFind) != -1;
    }

    public static int indexOf(long[] array, long valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    public static boolean contains(double[] array, double valueToFind) {
        return indexOf(array, valueToFind) != -1;
    }

    public static boolean contains(double[] array, double valueToFind, double tolerance) {
        return indexOf(array, valueToFind, 0, tolerance) != -1;
    }

    public static int indexOf(double[] array, double valueToFind) {
        return indexOf(array, valueToFind, 0);
    }

    public static int indexOf(double[] array, double valueToFind, double tolerance) {
        return indexOf(array, valueToFind, 0, tolerance);
    }

    public static int indexOf(long[] array, long valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            for (int i = startIndex; i < array.length; ++i) {
                if (valueToFind == array[i]) {
                    return i;
                }
            }

            return -1;
        }
    }


    public static int indexOf(char[] array, char valueToFind, int startIndex) {
        if (array == null) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            for (int i = startIndex; i < array.length; ++i) {
                if (valueToFind == array[i]) {
                    return i;
                }
            }

            return -1;
        }
    }

    public static int indexOf(double[] array, double valueToFind, int startIndex, double tolerance) {
        if (isEmpty(array)) {
            return -1;
        } else {
            if (startIndex < 0) {
                startIndex = 0;
            }

            double min = valueToFind - tolerance;
            double max = valueToFind + tolerance;

            for (int i = startIndex; i < array.length; ++i) {
                if (array[i] >= min && array[i] <= max) {
                    return i;
                }
            }

            return -1;
        }
    }


    public static int indexOf(Object[] array, Object objectToFind) {
        return indexOf(array, objectToFind, 0);
    }


    public static boolean[] toPrimitive(Boolean[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BOOLEAN_ARRAY;
        } else {
            boolean[] result = new boolean[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static boolean[] toPrimitive(Boolean[] array, boolean valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BOOLEAN_ARRAY;
        } else {
            boolean[] result = new boolean[array.length];

            for (int i = 0; i < array.length; ++i) {
                Boolean b = array[i];
                result[i] = b == null ? valueForNull : b;
            }

            return result;
        }
    }

    public static Boolean[] toObject(boolean[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BOOLEAN_OBJECT_ARRAY;
        } else {
            Boolean[] result = new Boolean[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i] ? Boolean.TRUE : Boolean.FALSE;
            }

            return result;
        }
    }


    public static char[] toPrimitive(Character[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_CHAR_ARRAY;
        } else {
            char[] result = new char[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static char[] toPrimitive(Character[] array, char valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_CHAR_ARRAY;
        } else {
            char[] result = new char[array.length];

            for (int i = 0; i < array.length; ++i) {
                Character b = array[i];
                result[i] = b == null ? valueForNull : b;
            }

            return result;
        }
    }

    public static Character[] toObject(char[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_CHARACTER_OBJECT_ARRAY;
        } else {
            Character[] result = new Character[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static long[] toPrimitive(Long[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_LONG_ARRAY;
        } else {
            long[] result = new long[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static long[] toPrimitive(Long[] array, long valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_LONG_ARRAY;
        } else {
            long[] result = new long[array.length];

            for (int i = 0; i < array.length; ++i) {
                Long b = array[i];
                result[i] = b == null ? valueForNull : b;
            }

            return result;
        }
    }

    public static Long[] toObject(long[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_LONG_OBJECT_ARRAY;
        } else {
            Long[] result = new Long[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static int[] toPrimitive(Integer[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_INT_ARRAY;
        } else {
            int[] result = new int[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static int[] toPrimitive(Integer[] array, int valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_INT_ARRAY;
        } else {
            int[] result = new int[array.length];

            for (int i = 0; i < array.length; ++i) {
                Integer b = array[i];
                result[i] = b == null ? valueForNull : b;
            }

            return result;
        }
    }

    public static Integer[] toObject(int[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_INTEGER_OBJECT_ARRAY;
        } else {
            Integer[] result = new Integer[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static short[] toPrimitive(Short[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_SHORT_ARRAY;
        } else {
            short[] result = new short[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static short[] toPrimitive(Short[] array, short valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_SHORT_ARRAY;
        } else {
            short[] result = new short[array.length];

            for (int i = 0; i < array.length; ++i) {
                Short b = array[i];
                result[i] = b == null ? valueForNull : b;
            }

            return result;
        }
    }

    public static Short[] toObject(short[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_SHORT_OBJECT_ARRAY;
        } else {
            Short[] result = new Short[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static byte[] toPrimitive(Byte[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BYTE_ARRAY;
        } else {
            byte[] result = new byte[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static byte[] toPrimitive(Byte[] array, byte valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BYTE_ARRAY;
        } else {
            byte[] result = new byte[array.length];

            for (int i = 0; i < array.length; ++i) {
                Byte b = array[i];
                result[i] = b == null ? valueForNull : b;
            }

            return result;
        }
    }

    public static Byte[] toObject(byte[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_BYTE_OBJECT_ARRAY;
        } else {
            Byte[] result = new Byte[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static double[] toPrimitive(Double[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_DOUBLE_ARRAY;
        } else {
            double[] result = new double[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static double[] toPrimitive(Double[] array, double valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_DOUBLE_ARRAY;
        } else {
            double[] result = new double[array.length];

            for (int i = 0; i < array.length; ++i) {
                Double b = array[i];
                result[i] = b == null ? valueForNull : b;
            }

            return result;
        }
    }

    public static Double[] toObject(double[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_DOUBLE_OBJECT_ARRAY;
        } else {
            Double[] result = new Double[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static float[] toPrimitive(Float[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_FLOAT_ARRAY;
        } else {
            float[] result = new float[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }

    public static float[] toPrimitive(Float[] array, float valueForNull) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_FLOAT_ARRAY;
        } else {
            float[] result = new float[array.length];

            for (int i = 0; i < array.length; ++i) {
                Float b = array[i];
                result[i] = b == null ? valueForNull : b;
            }

            return result;
        }
    }

    public static Float[] toObject(float[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return EMPTY_FLOAT_OBJECT_ARRAY;
        } else {
            Float[] result = new Float[array.length];

            for (int i = 0; i < array.length; ++i) {
                result[i] = array[i];
            }

            return result;
        }
    }


    public static <T> T[] subarray(T[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }

            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }

            int newSize = endIndexExclusive - startIndexInclusive;
            Class<?> type = array.getClass().getComponentType();
            Object[] subarray;
            if (newSize <= 0) {
                subarray = (Object[]) Array.newInstance(type, 0);
                return (T[]) subarray;
            } else {
                subarray = (Object[]) Array.newInstance(type, newSize);
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return (T[]) subarray;
            }
        }
    }

    public static long[] subarray(long[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }

            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }

            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return EMPTY_LONG_ARRAY;
            } else {
                long[] subarray = new long[newSize];
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return subarray;
            }
        }
    }

    public static int[] subarray(int[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }

            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }

            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return EMPTY_INT_ARRAY;
            } else {
                int[] subarray = new int[newSize];
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return subarray;
            }
        }
    }

    public static short[] subarray(short[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }

            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }

            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return EMPTY_SHORT_ARRAY;
            } else {
                short[] subarray = new short[newSize];
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return subarray;
            }
        }
    }

    public static char[] subarray(char[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }

            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }

            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return EMPTY_CHAR_ARRAY;
            } else {
                char[] subarray = new char[newSize];
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return subarray;
            }
        }
    }

    public static byte[] subarray(byte[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }

            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }

            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return EMPTY_BYTE_ARRAY;
            } else {
                byte[] subarray = new byte[newSize];
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return subarray;
            }
        }
    }

    public static double[] subarray(double[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }

            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }

            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return EMPTY_DOUBLE_ARRAY;
            } else {
                double[] subarray = new double[newSize];
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return subarray;
            }
        }
    }

    public static float[] subarray(float[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }

            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }

            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return EMPTY_FLOAT_ARRAY;
            } else {
                float[] subarray = new float[newSize];
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return subarray;
            }
        }
    }

    public static boolean[] subarray(boolean[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        } else {
            if (startIndexInclusive < 0) {
                startIndexInclusive = 0;
            }

            if (endIndexExclusive > array.length) {
                endIndexExclusive = array.length;
            }

            int newSize = endIndexExclusive - startIndexInclusive;
            if (newSize <= 0) {
                return EMPTY_BOOLEAN_ARRAY;
            } else {
                boolean[] subarray = new boolean[newSize];
                System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
                return subarray;
            }
        }
    }


    private static Object copyArrayGrow1(Object array, Class<?> newArrayComponentType) {
        if (array != null) {
            int arrayLength = Array.getLength(array);
            Object newArray = Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
            System.arraycopy(array, 0, newArray, 0, arrayLength);
            return newArray;
        } else {
            return Array.newInstance(newArrayComponentType, 1);
        }
    }


    public static <T> T[] addAll(T[] array1, T... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            Class<?> type1 = array1.getClass().getComponentType();
            T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);

            try {
                System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
                return joinedArray;
            } catch (ArrayStoreException var6) {
                Class<?> type2 = array2.getClass().getComponentType();
                if (!type1.isAssignableFrom(type2)) {
                    throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of " + type1.getName(), var6);
                } else {
                    throw var6;
                }
            }
        }
    }

    public static boolean[] addAll(boolean[] array1, boolean... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            boolean[] joinedArray = new boolean[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static char[] addAll(char[] array1, char... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            char[] joinedArray = new char[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static byte[] addAll(byte[] array1, byte... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            byte[] joinedArray = new byte[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static short[] addAll(short[] array1, short... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            short[] joinedArray = new short[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static int[] addAll(int[] array1, int... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            int[] joinedArray = new int[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static long[] addAll(long[] array1, long... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            long[] joinedArray = new long[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static float[] addAll(float[] array1, float... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            float[] joinedArray = new float[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static double[] addAll(double[] array1, double... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        } else {
            double[] joinedArray = new double[array1.length + array2.length];
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
            return joinedArray;
        }
    }

    public static <T> T[] add(T[] array, int index, T element) {
        Class<?> clss = null;
        if (array != null) {
            clss = array.getClass().getComponentType();
        } else {
            if (element == null) {
                throw new IllegalArgumentException("Array and element cannot both be null");
            }

            clss = element.getClass();
        }

        T[] newArray = (T[]) add(array, index, element, clss);
        return newArray;
    }


    public static boolean[] add(boolean[] array, int index, boolean element) {
        return (boolean[]) ((boolean[]) add(array, index, element, Boolean.TYPE));
    }

    public static char[] add(char[] array, int index, char element) {
        return (char[]) ((char[]) add(array, index, element, Character.TYPE));
    }

    public static byte[] add(byte[] array, int index, byte element) {
        return (byte[]) ((byte[]) add(array, index, element, Byte.TYPE));
    }

    public static short[] add(short[] array, int index, short element) {
        return (short[]) ((short[]) add(array, index, element, Short.TYPE));
    }

    public static int[] add(int[] array, int index, int element) {
        return (int[]) ((int[]) add(array, index, element, Integer.TYPE));
    }

    public static long[] add(long[] array, int index, long element) {
        return (long[]) ((long[]) add(array, index, element, Long.TYPE));
    }

    public static float[] add(float[] array, int index, float element) {
        return (float[]) ((float[]) add(array, index, element, Float.TYPE));
    }

    public static double[] add(double[] array, int index, double element) {
        return (double[]) ((double[]) add(array, index, element, Double.TYPE));
    }

    private static Object add(Object array, int index, Object element, Class<?> clss) {
        if (array == null) {
            if (index != 0) {
                throw new IndexOutOfBoundsException("Index: " + index + ", Length: 0");
            } else {
                Object joinedArray = Array.newInstance(clss, 1);
                Array.set(joinedArray, 0, element);
                return joinedArray;
            }
        } else {
            int length = Array.getLength(array);
            if (index <= length && index >= 0) {
                Object result = Array.newInstance(clss, length + 1);
                System.arraycopy(array, 0, result, 0, index);
                Array.set(result, index, element);
                if (index < length) {
                    System.arraycopy(array, index, result, index + 1, length - index);
                }

                return result;
            } else {
                throw new IndexOutOfBoundsException("Index: " + index + ", Length: " + length);
            }
        }
    }

    public static int getLength(Object array) {
        return array == null ? 0 : Array.getLength(array);
    }



    public static class MutableInt extends Number implements Comparable<MutableInt>{

        private static final long serialVersionUID = 512176391864L;

        private int value;

        public MutableInt() {
            super();
        }

        public MutableInt(final int value) {
            super();
            this.value = value;
        }

        public MutableInt(final Number value) {
            super();
            this.value = value.intValue();
        }

        public MutableInt(final String value) {
            super();
            this.value = Integer.parseInt(value);
        }

        public Integer getValue() {
            return this.value;
        }

        public void setValue(final int value) {
            this.value = value;
        }

        public void setValue(final Number value) {
            this.value = value.intValue();
        }

        public void increment() {
            value++;
        }

        public int getAndIncrement() {
            final int last = value;
            value++;
            return last;
        }

        public int incrementAndGet() {
            value++;
            return value;
        }

        public void decrement() {
            value--;
        }

        public int getAndDecrement() {
            final int last = value;
            value--;
            return last;
        }

        public int decrementAndGet() {
            value--;
            return value;
        }

        public void add(final int operand) {
            this.value += operand;
        }

        public void add(final Number operand) {
            this.value += operand.intValue();
        }
        public void subtract(final int operand) {
            this.value -= operand;
        }

        public void subtract(final Number operand) {
            this.value -= operand.intValue();
        }

        public int addAndGet(final int operand) {
            this.value += operand;
            return value;
        }

        public int addAndGet(final Number operand) {
            this.value += operand.intValue();
            return value;
        }

        public int getAndAdd(final int operand) {
            final int last = value;
            this.value += operand;
            return last;
        }

        public int getAndAdd(final Number operand) {
            final int last = value;
            this.value += operand.intValue();
            return last;
        }

        @Override
        public int intValue() {
            return value;
        }

        @Override
        public long longValue() {
            return value;
        }

        /**
         * Returns the value of this MutableInt as a float.
         *
         * @return the numeric value represented by this object after conversion to type float.
         */
        @Override
        public float floatValue() {
            return value;
        }

        @Override
        public double doubleValue() {
            return value;
        }

        public Integer toInteger() {
            return intValue();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof MutableInt) {
                return value == ((MutableInt) obj).intValue();
            }
            return false;
        }

        @Override
        public int hashCode() {
            return value;
        }

        @Override
        public int compareTo(final MutableInt other) {
            return Integer.compare(this.value, other.value);
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

    }


}
