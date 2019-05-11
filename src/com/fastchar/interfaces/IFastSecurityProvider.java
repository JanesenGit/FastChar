package com.fastchar.interfaces;

import com.fastchar.annotation.AFastPriority;
import com.fastchar.core.FastConstant;

@AFastPriority
public interface IFastSecurityProvider {

    String MD5_Encrypt(String value);

    String AES_Encrypt(String value);

    String AES_Decrypt(String value);

    String RSA_Encrypt(String value);

    String RSA_Decrypt(String value);
}
