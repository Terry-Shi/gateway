package gateway.filters.pre;

import gateway.error.Err;
import gateway.error.ErrResponse;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.netflix.zuul.context.RequestContext;

/**
 *
 */
public class FilterHelper {
    public static void setErrorResponse(Err err) {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setSendZuulResponse(false);
        ctx.getResponse().setHeader("Content-Type", "application/json");
        ctx.setResponseBody(new Gson().toJson(ErrResponse.build(err)));
        ctx.setResponseStatusCode(err.getHttpStatus());
    }

    public static void startStopwatch(String name) {
        RequestContext.getCurrentContext().set(name, Stopwatch.createStarted());
    }

    public static void setResponseCharacterEncoding() {
        RequestContext.getCurrentContext().getResponse().setCharacterEncoding("UTF-8");
    }

    public static void setRequestHeader(String key, String value) {
        // If the request headers previously contained a header for the key,
        // the old value is replaced by the specified one.
        RequestContext.getCurrentContext().getZuulRequestHeaders().put(key, value);
    }

    public static String getRequestHeader(String key) {
        return RequestContext.getCurrentContext().getZuulRequestHeaders().get(key);
    }

}
