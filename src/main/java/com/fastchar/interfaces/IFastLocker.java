package com.fastchar.interfaces;

import java.util.concurrent.locks.Lock;

public interface IFastLocker  {


    Lock getLock(String key);


    void removeLock(String key);


}
