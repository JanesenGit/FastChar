package com.fastchar.servlet;

import com.fastchar.core.FastChar;
import com.fastchar.enums.FastServletType;

public class FastServletHelper {

    public static boolean isJavaxServlet() {
        return FastChar.getConstant().getServletType() == FastServletType.Javax;
    }

    public static boolean isJakartaServlet() {
        return FastChar.getConstant().getServletType() == FastServletType.Jakarta;
    }



}
