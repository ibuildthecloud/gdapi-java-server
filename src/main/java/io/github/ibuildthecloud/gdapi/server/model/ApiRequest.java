package io.github.ibuildthecloud.gdapi.server.model;

import io.github.ibuildthecloud.gdapi.util.ExceptionUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;


public class ApiRequest {

    String type;
    String id;
    String link;
    String action;
    String method;
    boolean commited = false;
    int responseCode;
    ServletContext servletContext;
    Object responseObject;
    Object requestObject;
    Callable<Map<String,Object>> paramsCallback;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isCommited() {
        return commited;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public Object getResponseObject() {
        return responseObject;
    }

    public void setResponseObject(Object responseObject) {
        this.responseObject = responseObject;
    }

    public Object getRequestObject() {
        return requestObject;
    }

    public void setRequestObject(Object requestObject) {
        this.requestObject = requestObject;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String,Object> getParameters() throws IOException {
        try {
            return paramsCallback.call();
        } catch (Exception e) {
            ExceptionUtils.rethrowRuntime(e);
            ExceptionUtils.rethrow(e, IOException.class);
            throw new IllegalStateException("Failed to get parameters", e);
        }
    }

    public void setParameterCallback(Callable<Map<String,Object>> callback) {
        paramsCallback = callback;
    }

}
