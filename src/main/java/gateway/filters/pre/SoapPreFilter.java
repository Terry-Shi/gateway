package gateway.filters.pre;

import static com.netflix.zuul.context.RequestContext.getCurrentContext;
import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

@Component
public class SoapPreFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER + 15;
    }

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = getCurrentContext().getRequest();
        // TODO: how to deal with wsdl ?
        boolean wsdlRequest = request.getRequestURI().toLowerCase().endsWith(".wsdl");
        String contentType = request.getContentType() == null ? "" : request.getContentType().toLowerCase();
        boolean postSoap = request.getMethod().equalsIgnoreCase("post") && (contentType.contains("text/xml") || contentType.contains("application/soap+xml"));
        return wsdlRequest || postSoap;
    }

    @Override
    public Object run() {

        try {
            RequestContext context = getCurrentContext();
            InputStream in = (InputStream) context.get("requestEntity");
            if (in == null) {
                in = context.getRequest().getInputStream();
            }

            String body = StreamUtils.copyToString(in, Charset.forName("UTF-8"));
            // TODO do something about soap data
            System.out.println(body);
            context.setRequest(new GatewayRequestWrapper(context.getRequest(), body));
        }
        catch (IOException e) {
            rethrowRuntimeException(e);
        }
        return null;
    }
}