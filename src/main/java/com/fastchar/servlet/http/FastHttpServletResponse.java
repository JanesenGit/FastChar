package com.fastchar.servlet.http;

import com.fastchar.core.FastChar;
import com.fastchar.servlet.FastServletHelper;
import com.fastchar.utils.FastClassUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

public class FastHttpServletResponse {

    public static FastHttpServletResponse newInstance(Object target) {
        return FastChar.getOverrides().newInstance(FastHttpServletResponse.class, target);
    }

    private final Object target;

    public FastHttpServletResponse(Object target) {
        this.target = target;
    }

    public Object getTarget() {
        return target;
    }

    public void addCookie(FastCookie cookie) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).addCookie((javax.servlet.http.Cookie) cookie.getTarget());
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).addCookie((jakarta.servlet.http.Cookie) cookie.getTarget());
        }
    }

    public void setBufferSize(int size) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).setBufferSize(size);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).setBufferSize(size);
        }
    }

    public int getBufferSize() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).getBufferSize();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).getBufferSize();
        }
        return 0;
    }

    public void flushBuffer() throws IOException {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).flushBuffer();
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).flushBuffer();
        }
    }

    public void resetBuffer() {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).resetBuffer();
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).resetBuffer();
        }
    }

    public boolean isCommitted() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).isCommitted();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).isCommitted();
        }
        return false;
    }

    public void reset() {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).reset();
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).reset();
        }
    }

    public void setLocale(Locale loc) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).setLocale(loc);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).setLocale(loc);
        }
    }

    public Locale getLocale() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).getLocale();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).getLocale();
        }
        return null;
    }

    public void setHeader(String name, String value) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).setHeader(name, value);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).setHeader(name, value);
        }
    }

    public void setIntHeader(String name, int value) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).setIntHeader(name, value);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).setIntHeader(name, value);
        }
    }

    public void setDateHeader(String name, long date) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).setDateHeader(name, date);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).setDateHeader(name, date);
        }
    }


    public void addHeader(String name, String value) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).addHeader(name, value);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).addHeader(name, value);
        }
    }

    public void setStatus(int sc) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).setStatus(sc);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).setStatus(sc);
        }
    }


    public int getStatus() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).getStatus();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).getStatus();
        }
        return 0;
    }

    public String getHeader(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).getHeader(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).getHeader(name);
        }
        return null;
    }

    public Collection<String> getHeaders(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).getHeaders(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).getHeaders(name);
        }
        return null;
    }

    public Collection<String> getHeaderNames() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).getHeaderNames();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).getHeaderNames();
        }
        return null;
    }

    public String getCharacterEncoding() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).getCharacterEncoding();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).getCharacterEncoding();
        }
        return null;
    }

    public String getContentType() {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).getContentType();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).getContentType();
        }
        return null;
    }

    public OutputStream getOutputStream() throws IOException {
        //此处需要动态调用并执行方法，因为classloader会提前编译检测调用的类  javax.servlet.ServletOutputStream 和 jakarta.servlet.ServletOutputStream
        Object getOutputStream = FastClassUtils.safeInvokeMethod(target, "getOutputStream");
        if (getOutputStream != null) {
            return (OutputStream) getOutputStream;
        }
        return null;
    }


    public PrintWriter getWriter() throws IOException {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).getWriter();
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).getWriter();
        }
        return null;
    }

    public void setCharacterEncoding(String charset) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).setCharacterEncoding(charset);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).setCharacterEncoding(charset);
        }
    }


    public void setContentLengthLong(long len) {
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).setContentLengthLong(len);
        }
    }

    public void setContentType(String type) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).setContentType(type);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).setContentType(type);
        }
    }

    public void addDateHeader(String name, long date) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).addDateHeader(name, date);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).addDateHeader(name, date);
        }
    }

    public void addIntHeader(String name, int value) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).addIntHeader(name, value);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).addIntHeader(name, value);
        }
    }

    public void setContentLength(int len) {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).setContentLength(len);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).setContentLength(len);
        }
    }


    public boolean containsHeader(String name) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).containsHeader(name);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).containsHeader(name);
        }
        return false;
    }

    public String encodeURL(String url) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).encodeURL(url);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).encodeURL(url);
        }
        return null;
    }

    public String encodeRedirectURL(String url) {
        if (FastServletHelper.isJavaxServlet()) {
            return ((javax.servlet.http.HttpServletResponse) target).encodeRedirectURL(url);
        }
        if (FastServletHelper.isJakartaServlet()) {
            return ((jakarta.servlet.http.HttpServletResponse) target).encodeRedirectURL(url);
        }
        return null;
    }

    public void sendError(int sc, String msg) throws IOException {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).sendError(sc, msg);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).sendError(sc, msg);
        }
    }

    public void sendError(int sc) throws IOException {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).sendError(sc);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).sendError(sc);
        }
    }

    public void sendRedirect(String location) throws IOException {
        if (FastServletHelper.isJavaxServlet()) {
            ((javax.servlet.http.HttpServletResponse) target).sendRedirect(location);
        }
        if (FastServletHelper.isJakartaServlet()) {
            ((jakarta.servlet.http.HttpServletResponse) target).sendRedirect(location);
        }
    }


    /**
     * Status code (100) indicating the client can continue.
     */
    public static final int SC_CONTINUE = 100;


    /**
     * Status code (101) indicating the server is switching protocols
     * according to Upgrade header.
     */

    public static final int SC_SWITCHING_PROTOCOLS = 101;

    /**
     * Status code (200) indicating the request succeeded normally.
     */

    public static final int SC_OK = 200;

    /**
     * Status code (201) indicating the request succeeded and created
     * a new resource on the server.
     */

    public static final int SC_CREATED = 201;

    /**
     * Status code (202) indicating that a request was accepted for
     * processing, but was not completed.
     */

    public static final int SC_ACCEPTED = 202;

    /**
     * Status code (203) indicating that the meta information presented
     * by the client did not originate from the server.
     */

    public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;

    /**
     * Status code (204) indicating that the request succeeded but that
     * there was no new information to return.
     */

    public static final int SC_NO_CONTENT = 204;

    /**
     * Status code (205) indicating that the agent <em>SHOULD</em> reset
     * the document view which caused the request to be sent.
     */

    public static final int SC_RESET_CONTENT = 205;

    /**
     * Status code (206) indicating that the server has fulfilled
     * the partial GET request for the resource.
     */

    public static final int SC_PARTIAL_CONTENT = 206;

    /**
     * Status code (300) indicating that the requested resource
     * corresponds to any one of a set of representations, each with
     * its own specific location.
     */

    public static final int SC_MULTIPLE_CHOICES = 300;

    /**
     * Status code (301) indicating that the resource has permanently
     * moved to a new location, and that future references should use a
     * new URI with their requests.
     */

    public static final int SC_MOVED_PERMANENTLY = 301;

    /**
     * Status code (302) indicating that the resource has temporarily
     * moved to another location, but that future references should
     * still use the original URI to access the resource.
     * <p>
     * This definition is being retained for backwards compatibility.
     * SC_FOUND is now the preferred definition.
     */

    public static final int SC_MOVED_TEMPORARILY = 302;

    /**
     * Status code (302) indicating that the resource reside
     * temporarily under a different URI. Since the redirection might
     * be altered on occasion, the client should continue to use the
     * Request-URI for future requests.(HTTP/1.1) To represent the
     * status code (302), it is recommended to use this variable.
     */

    public static final int SC_FOUND = 302;

    /**
     * Status code (303) indicating that the response to the request
     * can be found under a different URI.
     */

    public static final int SC_SEE_OTHER = 303;

    /**
     * Status code (304) indicating that a conditional GET operation
     * found that the resource was available and not modified.
     */

    public static final int SC_NOT_MODIFIED = 304;

    /**
     * Status code (305) indicating that the requested resource
     * <em>MUST</em> be accessed through the proxy given by the
     * <code><em>Location</em></code> field.
     */

    public static final int SC_USE_PROXY = 305;

    /**
     * Status code (307) indicating that the requested resource
     * resides temporarily under a different URI. The temporary URI
     * <em>SHOULD</em> be given by the <code><em>Location</em></code>
     * field in the response.
     */

    public static final int SC_TEMPORARY_REDIRECT = 307;

    /**
     * Status code (400) indicating the request sent by the client was
     * syntactically incorrect.
     */

    public static final int SC_BAD_REQUEST = 400;

    /**
     * Status code (401) indicating that the request requires HTTP
     * authentication.
     */

    public static final int SC_UNAUTHORIZED = 401;

    /**
     * Status code (402) reserved for future use.
     */

    public static final int SC_PAYMENT_REQUIRED = 402;

    /**
     * Status code (403) indicating the server understood the request
     * but refused to fulfill it.
     */

    public static final int SC_FORBIDDEN = 403;

    /**
     * Status code (404) indicating that the requested resource is not
     * available.
     */

    public static final int SC_NOT_FOUND = 404;

    /**
     * Status code (405) indicating that the method specified in the
     * <code><em>Request-Line</em></code> is not allowed for the resource
     * identified by the <code><em>Request-URI</em></code>.
     */

    public static final int SC_METHOD_NOT_ALLOWED = 405;

    /**
     * Status code (406) indicating that the resource identified by the
     * request is only capable of generating response entities which have
     * content characteristics not acceptable according to the accept
     * headers sent in the request.
     */

    public static final int SC_NOT_ACCEPTABLE = 406;

    /**
     * Status code (407) indicating that the client <em>MUST</em> first
     * authenticate itself with the proxy.
     */

    public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;

    /**
     * Status code (408) indicating that the client did not produce a
     * request within the time that the server was prepared to wait.
     */

    public static final int SC_REQUEST_TIMEOUT = 408;

    /**
     * Status code (409) indicating that the request could not be
     * completed due to a conflict with the current state of the
     * resource.
     */

    public static final int SC_CONFLICT = 409;

    /**
     * Status code (410) indicating that the resource is no longer
     * available at the server and no forwarding address is known.
     * This condition <em>SHOULD</em> be considered permanent.
     */

    public static final int SC_GONE = 410;

    /**
     * Status code (411) indicating that the request cannot be handled
     * without a defined <code><em>Content-Length</em></code>.
     */

    public static final int SC_LENGTH_REQUIRED = 411;

    /**
     * Status code (412) indicating that the precondition given in one
     * or more of the request-header fields evaluated to false when it
     * was tested on the server.
     */

    public static final int SC_PRECONDITION_FAILED = 412;

    /**
     * Status code (413) indicating that the server is refusing to process
     * the request because the request entity is larger than the server is
     * willing or able to process.
     */

    public static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;

    /**
     * Status code (414) indicating that the server is refusing to service
     * the request because the <code><em>Request-URI</em></code> is longer
     * than the server is willing to interpret.
     */

    public static final int SC_REQUEST_URI_TOO_LONG = 414;

    /**
     * Status code (415) indicating that the server is refusing to service
     * the request because the entity of the request is in a format not
     * supported by the requested resource for the requested method.
     */

    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;

    /**
     * Status code (416) indicating that the server cannot serve the
     * requested byte range.
     */

    public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    /**
     * Status code (417) indicating that the server could not meet the
     * expectation given in the Expect request header.
     */

    public static final int SC_EXPECTATION_FAILED = 417;

    /**
     * Status code (500) indicating an error inside the HTTP server
     * which prevented it from fulfilling the request.
     */

    public static final int SC_INTERNAL_SERVER_ERROR = 500;

    /**
     * Status code (501) indicating the HTTP server does not support
     * the functionality needed to fulfill the request.
     */

    public static final int SC_NOT_IMPLEMENTED = 501;

    /**
     * Status code (502) indicating that the HTTP server received an
     * invalid response from a server it consulted when acting as a
     * proxy or gateway.
     */

    public static final int SC_BAD_GATEWAY = 502;

    /**
     * Status code (503) indicating that the HTTP server is
     * temporarily overloaded, and unable to handle the request.
     */

    public static final int SC_SERVICE_UNAVAILABLE = 503;

    /**
     * Status code (504) indicating that the server did not receive
     * a timely response from the upstream server while acting as
     * a gateway or proxy.
     */

    public static final int SC_GATEWAY_TIMEOUT = 504;

    /**
     * Status code (505) indicating that the server does not support
     * or refuses to support the HTTP protocol version that was used
     * in the request message.
     */

    public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;


}

