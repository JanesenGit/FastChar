package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.exception.FastOutException;
import com.fastchar.local.FastCharLocal;
import com.fastchar.utils.FastFileUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 * 响应stream
 *
 * @author 沈建（Janesen）
 * @date 2021/10/22 10:54
 */
public class FastOutStream extends FastOut<FastOutStream> {
    private int inputSize = 1024 * 4;

    public int getInputSize() {
        return inputSize;
    }

    public FastOutStream setInputSize(int inputSize) {
        this.inputSize = inputSize;
        return this;
    }

    @Override
    public void response(FastAction action) throws Exception {
        HttpServletResponse response = action.getResponse();
        response.setStatus(getStatus());
        response.setContentType(toContentType(action));
        response.setCharacterEncoding(getCharset());
        if (data instanceof InputStream) {
            InputStream inputStream = (InputStream) data;
            try (ServletOutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[inputSize];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
                outputStream.flush();
            } finally {
                FastFileUtils.closeQuietly(inputStream);
            }
            return;
        }
        throw new FastOutException(FastChar.getLocal().getInfo(FastCharLocal.OUT_ERROR1, InputStream.class.getName()));
    }

}
