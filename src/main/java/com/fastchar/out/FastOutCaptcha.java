package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.utils.FastMD5Utils;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

/**
 * 响应验证码图片
 * @author Janesen
 */
public class FastOutCaptcha extends FastOut<FastOutCaptcha> {

    private static final String CAPTCHA_STR = "3456789ABCDEFGHJKMNPQRSTUVWXY";
    private static final String[] FONT_NAMES = new String[]{"Verdana"};
    private static final int[][] COLOR = {{0, 135, 255}, {51, 153, 51}, {255, 102, 102}, {255, 153, 0}, {153, 102, 0}, {153, 102, 153}, {51, 153, 153}, {102, 102, 255}, {0, 102, 204}, {204, 51, 51}, {0, 153, 204}, {0, 51, 102}};
    private int width = 120;
    private int height = 40;

    public FastOutCaptcha() {
        this.contentType = "image/jpeg";
    }

    @Override
    public void response(FastAction action) throws Exception {
        HttpServletResponse response = action.getResponse();
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setStatus(getStatus());
        response.setContentType(toContentType(action));

        char[] chars = randomChar();
        action.setSession(FastMD5Utils.MD5(FastChar.getConstant().getProjectName()), new String(chars));
        outCaptcha(action, chars);
    }


    private Color randomColor(int alpha) {
        Random random = new Random();
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return new Color(r, g, b, alpha);
    }


    private char[] randomChar() {
        Random random = new Random();
        return new char[]{CAPTCHA_STR.charAt(random.nextInt(CAPTCHA_STR.length())),
                CAPTCHA_STR.charAt(random.nextInt(CAPTCHA_STR.length())),
                CAPTCHA_STR.charAt(random.nextInt(CAPTCHA_STR.length())),
                CAPTCHA_STR.charAt(random.nextInt(CAPTCHA_STR.length()))};
    }


    private void outCaptcha(FastAction action,char[] codes) throws Exception {
        Random random = new Random();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        g2d.setColor(randomColor(20));
        g2d.fillRect(0, 0, width, height);


        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        for (int i = 0; i < 15; i++) {
            g2d.setColor(randomColor(150));
            int oWidth = Math.max(random.nextInt(28), 10);
            int oHeight = Math.max(random.nextInt(28), 10);
            g2d.drawOval(random.nextInt(width - oWidth), random.nextInt(height),
                    oWidth,
                    oHeight);
        }


        int[] xArray = new int[codes.length];
        int[] yArray = new int[codes.length];
        Font[] fontArray = new Font[codes.length];

        for (int i = 0; i < codes.length; i++) {
            Font font = new Font(FONT_NAMES[random.nextInt(FONT_NAMES.length)], Font.ITALIC | Font.BOLD, 28);
            fontArray[i] = font;
            FontMetrics fm = FontDesignMetrics.getMetrics(font);
            int charWidth = fm.charWidth(codes[i]);
            int y = Math.max(random.nextInt(height), fm.getHeight());
            int x = charWidth * i + (width - charWidth * codes.length) / 2;
            yArray[i] = y;
            xArray[i] = x;
        }


        Color lastColor = Color.CYAN;
        for (int i = 0; i < codes.length; i++) {
            int degree = random.nextInt(32);
            if (i % 2 == 0) {
                degree = degree * (-1);
            }
            g2d.setFont(fontArray[i]);
            int x = xArray[i];
            int y = yArray[i];
            g2d.rotate(Math.toRadians(degree), x, y);

            int index = random.nextInt(COLOR.length);
            lastColor = new Color(COLOR[index][0], COLOR[index][1], COLOR[index][2]);
            g2d.setColor(lastColor);
            g2d.drawString(String.valueOf(codes[i]), x, y);
            g2d.rotate(-Math.toRadians(degree), x, y);
        }

        int area = (int) (0.1f * width * height);
        for (int i = 0; i < area; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            image.setRGB(x, y, lastColor.getRGB());
        }
        g2d.dispose();

        try (OutputStream outputStream = action.getResponse().getOutputStream()) {
            ImageIO.write(image, "jpg", outputStream);
            outputStream.flush();
        }
    }

}
