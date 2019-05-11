package com.fastchar.core;

import java.awt.*;

public final class FastCaptcha {

    private Font font;
    private int codeLength;
    private int imgWidth;
    private int imgHeight;

    public Font getFont() {
        return font;
    }

    public FastCaptcha setFont(Font font) {
        this.font = font;
        return this;
    }

    public int getCodeLength() {
        return codeLength;
    }

    public FastCaptcha setCodeLength(int codeLength) {
        this.codeLength = codeLength;
        return this;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public FastCaptcha setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
        return this;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public FastCaptcha setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
        return this;
    }
}
