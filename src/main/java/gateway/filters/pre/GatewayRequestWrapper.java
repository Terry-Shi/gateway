package gateway.filters.pre;

import com.netflix.zuul.http.ServletInputStreamWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

public class GatewayRequestWrapper extends HttpServletRequestWrapper {

    public GatewayRequestWrapper(HttpServletRequest request, byte[] data) {
        super(request);
        dataBytes = data;
    }

    public GatewayRequestWrapper(HttpServletRequest request, String body) {
        super(request);
        try {
            dataBytes = body.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private byte[] dataBytes;

    // @Override
    public byte[] getContentData() {
        return dataBytes;
    }

    @Override
    public int getContentLength() {
        return dataBytes.length;
    }

    @Override
    public long getContentLengthLong() {
        return dataBytes.length;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(dataBytes)));
    }

    @Override
    public ServletInputStream getInputStream() throws UnsupportedEncodingException {
        return new ServletInputStreamWrapper(dataBytes);
    }
}
