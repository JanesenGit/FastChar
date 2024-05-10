package com.fastchar.core;

import com.fastchar.enums.FastServletType;
import com.fastchar.utils.FastFileUtils;
import com.fastchar.utils.FastSerializeUtils;
import com.fastchar.utils.FastStringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FastTicket {

    private final String ticketFileName;
    private final Map<String, String> ticketMap = new HashMap<>(16);
    private boolean restoredTicket = false;

    public FastTicket(String ticketFileName) {
        this.ticketFileName = ticketFileName;
    }


    private File getTicketFile() {
        File file = new File(FastChar.getPath().getClassRootPath(), ".fastchar" + File.separator + ticketFileName);
        if (FastChar.getConstant().isWebServer()) {
            file = new File(FastChar.getPath().getWebRootPath(), ".fastchar" + File.separator + ticketFileName);
        }
        return file;
    }

    public void saveTicket() {
        try {
            if (FastChar.getConstant().getServletType() == FastServletType.None) {
                return;
            }
            FastFileUtils.writeByteArrayToFile(getTicketFile(), FastSerializeUtils.serialize(ticketMap));
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }finally {
            ticketMap.clear();
        }
    }

    public void restoreTicket() {
        try {
            if (FastChar.getConstant().getServletType() == FastServletType.None) {
                return;
            }
            File file = getTicketFile();

            if (file.exists()) {
                //noinspection unchecked
                Map<String, String> restoreTicketMap = (Map<String, String>) FastSerializeUtils.deserialize(FastFileUtils.readFileToByteArray(file));
                if (restoreTicketMap != null) {
                    ticketMap.putAll(restoreTicketMap);
                }
            }
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        } finally {
            restoredTicket = true;
        }
    }


    public String getTicket(String key) {
        return FastStringUtils.defaultValue(ticketMap.get(key), "NONE");
    }

    public void putTicket(String key, String value) {
        ticketMap.put(key, value);
    }


    /**
     * 设置ticket值
     *
     * @param key   key
     * @param value 值
     * @return 是否修改了历史值
     */
    public boolean pushTicket(String key, String value) {
        return pushTicket(key, value, false);
    }
    /**
     * 设置ticket值
     *
     * @param key   key
     * @param value 值
     * @param strict 严格模式，如果true则在首次加入key时便认为修改了
     * @return 是否修改了历史值
     */
    public boolean pushTicket(String key, String value,boolean strict) {
        try {
            if (!restoredTicket) {
                restoreTicket();
            }

            if (ticketMap.containsKey(key)) {
                String oldValue = getTicket(key);
                return !oldValue.equalsIgnoreCase(value);
            }
            return strict;
        } finally {
            putTicket(key, value);
        }
    }

    /**
     * 移除包含前缀的key
     *
     * @param keyPrefix 前缀
     */
    public void removeTicketWithPrefix(String keyPrefix) {
        List<String> waitRemovedKey = new ArrayList<>(16);
        for (Map.Entry<String, String> stringStringEntry : ticketMap.entrySet()) {
            if (stringStringEntry.getKey().startsWith(keyPrefix)) {
                waitRemovedKey.add(stringStringEntry.getKey());
            }
        }
        for (String key : waitRemovedKey) {
            ticketMap.remove(key);
        }
    }


}
