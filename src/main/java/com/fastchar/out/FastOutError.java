package com.fastchar.out;

import com.fastchar.core.FastAction;
import com.fastchar.core.FastChar;
import com.fastchar.core.FastConstant;
import com.fastchar.servlet.http.FastHttpServletResponse;
import com.fastchar.utils.FastStringUtils;

/**
 * 响应错误
 */
public class FastOutError extends FastOut<FastOutError> {

    public FastOutError() {
        this.contentType = "text/html";
    }

    private String powered_by = "<a href=\"https://www.fastchar.com\" target=\"_blank\">FastChar</a>";

    public String toHtml(FastAction action) {
        if (data == null) {
            data = getDescription();
        }
        if (!FastChar.getConstant().isMarkers()) {
            powered_by = "";
        }
        String description = data.toString()
                .replace("\n", "<br/>")
                .replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
        String html = "";
        if (getStatus() == 404) {
            html = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>404</title><style>* {margin: 0;padding: 0;}table {width: 100%;font-weight: bold;font-size: 20px;}tr {background-color: lightgray;}td {padding-left: 10px;padding-top: 5px;padding-bottom: 5px;word-wrap: break-word; word-break:break-all;}</style></head><body><table><tr style=\"background-color: orangered; color: #ffffff;\"><td width=\"200px;\" align=\"right\">HTTP Status：</td><td>404</td></tr><tr><td style=\"background-color: lightgray\" align=\"right\">HTTP Method：</td><td>"+action.getRequestMethod()+"</td></tr><tr><td style=\"background-color: lightgray\" align=\"right\">Message：</td><td>Http url does not exist!</td></tr><tr><td  align=\"right\" valign=\"top\">Description：</td><td>" + description + "</td></tr><tr><td align=\"right\">Powered By：</td><td>" + powered_by + "</td></tr><tr><td  align=\"right\">Version：</td><td>" + FastConstant.FAST_CHAR_VERSION + "</td></tr></table></body></html>";
        } else if (getStatus() == 502) {
            html = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>502</title><style>* {margin: 0;padding: 0;}table {width: 100%;font-weight: bold;font-size: 20px;}tr {background-color: lightgray;}td {padding-left: 10px;padding-top: 5px;padding-bottom: 5px;word-wrap: break-word; word-break:break-all;}</style></head><body><table><tr style=\"background-color: orangered; color: #ffffff;\"><td width=\"200px;\" align=\"right\">HTTP Status：</td><td>502</td></tr><tr><td style=\"background-color: lightgray\" align=\"right\">HTTP Method：</td><td>"+action.getRequestMethod()+"</td></tr><tr><td style=\"background-color: lightgray\" align=\"right\">Message：</td><td>The Http response is not received!</td></tr><tr><td  align=\"right\" valign=\"top\">Description：</td><td>" + description + "</td></tr><tr><td align=\"right\">Powered By：</td><td>" + powered_by + "</td></tr><tr><td  align=\"right\">Version：</td><td>" + FastConstant.FAST_CHAR_VERSION + "</td></tr></table></body></html>";
        } else {
            html = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"><title>" + getStatus() + "</title><style>* {margin: 0;padding: 0;}table {width: 100%;font-weight: bold;font-size: 20px;}tr {background-color: lightgray;}td {padding-left: 10px;padding-top: 5px;padding-bottom: 5px; word-wrap: break-word; word-break:break-all;}</style></head><body><table><tr style=\"background-color: orangered; color: #ffffff;\"><td width=\"200px;\" align=\"right\">HTTP Status：</td><td>500</td></tr><tr><td style=\"background-color: lightgray\" align=\"right\">HTTP Method：</td><td>"+action.getRequestMethod()+"</td></tr><tr><td style=\"background-color: lightgray\" align=\"right\">Message：</td><td>An error occurred on the server!</td></tr><tr><td  align=\"right\" valign=\"top\">Description：</td><td>" + description + "</td></tr><tr><td align=\"right\">Powered By：</td><td>" + powered_by + "</td></tr><tr><td  align=\"right\">Version：</td><td>" + FastConstant.FAST_CHAR_VERSION + "</td></tr></table></body></html>";
        }
        return html;
    }

    public String getErrorPage() {
        if (getStatus() == 500) {
            return FastChar.getConstant().getErrorPage500();
        } else if (getStatus() == 502) {
            return FastChar.getConstant().getErrorPage502();
        } else if (getStatus() == 404) {
            return FastChar.getConstant().getErrorPage404();
        }
        return null;
    }

    @Override
    public void response(FastAction action) {
        try {
            FastHttpServletResponse response = action.getResponse();
            if (FastStringUtils.isEmpty(getErrorPage())) {
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Content-Disposition", null);
                response.setHeader("Accept-Ranges", null);
                response.setDateHeader("Expires", 0);
                response.setStatus(getStatus());
                response.setContentType(toContentType(action));
                response.setCharacterEncoding(getCharset());

                write(response,toHtml(action));
            } else {
                response.sendRedirect(FastChar.wrapperUrl(getErrorPage()));
            }
        } catch (Exception e) {
            FastChar.getLogger().error(this.getClass(), e);
        }
    }
}
