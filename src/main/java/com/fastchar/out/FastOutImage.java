package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.utils.FastMD5Utils;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.zip.GZIPOutputStream;

/**
 * 响应图片
 */
public class FastOutImage extends FastOut<FastOutImage> {
    private String formatName = "jpg";

    public String getFormatName() {
        return formatName;
    }

    public FastOutImage setFormatName(String formatName) {
        this.formatName = formatName;
        return this;
    }

    @Override
    public void response(FastAction action) throws Exception {
        this.contentType = "image/" + formatName;

        HttpServletResponse response = action.getResponse();
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setStatus(getStatus());
        response.setContentType(toContentType(action));

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            if (data instanceof RenderedImage) {
                RenderedImage renderedImage = (RenderedImage) getData();
                ImageIO.write(renderedImage, formatName, outputStream);
            } else if (data instanceof File) {
                File file = (File) getData();
                ImageIO.write(ImageIO.read(file), formatName, outputStream);
            }
            outputStream.flush();
        }
    }
}
