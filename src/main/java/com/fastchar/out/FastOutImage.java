package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.utils.FastMD5Utils;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.RenderedImage;

public class FastOutImage  extends FastOut<FastOutImage>  {
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
        response.setContentType(getContentType());
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            RenderedImage data = (RenderedImage) getData();
            ImageIO.write(data, formatName, outputStream);
            outputStream.flush();
        }
    }
}
