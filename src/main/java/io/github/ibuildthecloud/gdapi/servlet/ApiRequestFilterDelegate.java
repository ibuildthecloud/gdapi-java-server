package io.github.ibuildthecloud.gdapi.servlet;

import io.github.ibuildthecloud.gdapi.auth.ApiAuthenticator;
import io.github.ibuildthecloud.gdapi.request.ApiRequestHandler;
import io.github.ibuildthecloud.gdapi.request.ApiRequestParser;
import io.github.ibuildthecloud.gdapi.server.model.ApiRequest;
import io.github.ibuildthecloud.gdapi.server.model.ServletContext;
import io.github.ibuildthecloud.gdapi.util.ExceptionUtils;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiRequestFilterDelegate  {

    private static final Logger log = LoggerFactory.getLogger(ApiRequestFilterDelegate.class);

    ApiRequestParser parser;
    ApiAuthenticator authenticator;
    List<ApiRequestHandler> handlers;
    boolean throwErrors = false;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        if ( ! (request instanceof HttpServletRequest) || ! (response instanceof HttpServletResponse) ) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpServletResponse httpResponse = (HttpServletResponse)response;

        ApiRequest apiRequest = new ApiRequest();
        apiRequest.setServletContext(new ServletContext(httpRequest, httpResponse, chain));
        try {
            if ( ! parser.parse(apiRequest) ) {
                chain.doFilter(httpRequest, httpResponse);
                return;
            }

            for ( ApiRequestHandler handler : handlers ) {
                handler.handle(apiRequest);
            }
        } catch ( Throwable t ) {
            boolean handled = false;
            for ( ApiRequestHandler handler : handlers ) {
                handled |= handler.handleException(apiRequest, t);
            }
            if ( ! handled ) {
                log.error("Unhandled exception in API for request [{}]", apiRequest, t);
                if ( throwErrors ) {
                    ExceptionUtils.rethrowRuntime(t);
                    ExceptionUtils.rethrow(t, IOException.class);
                    ExceptionUtils.rethrow(t, ServletException.class);
                    throw new ServletException(t);
                } else {
                    if ( ! apiRequest.isCommited() ) {
                        apiRequest.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                }
            }
        }
    }

    public ApiRequestParser getParser() {
        return parser;
    }

    @Inject
    public void setParser(ApiRequestParser parser) {
        this.parser = parser;
    }

    public ApiAuthenticator getAuthenticator() {
        return authenticator;
    }

    @Inject
    public void setAuthenticator(ApiAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public boolean isThrowErrors() {
        return throwErrors;
    }

    public void setThrowErrors(boolean throwErrors) {
        this.throwErrors = throwErrors;
    }

    public List<ApiRequestHandler> getHandlers() {
        return handlers;
    }

    @Inject
    public void setHandlers(List<ApiRequestHandler> handlers) {
        this.handlers = handlers;
    }

}
