package com.fastchar.extend.lock;

import com.fastchar.interfaces.IFastLocker;
import com.fastchar.utils.FastLockUtils;

import java.util.concurrent.locks.Lock;

public class FastLockProvider implements IFastLocker {
    @Override
    public Lock getLock(String key) {
        return FastLockUtils.getLock(key);
    }

    @Override
    public void removeLock(String key) {
        FastLockUtils.removeLock(key);
    }
}
