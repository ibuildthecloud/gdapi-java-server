package io.github.ibuildthecloud.gdapi.request;

import io.github.ibuildthecloud.gdapi.condition.Condition;
import io.github.ibuildthecloud.gdapi.model.Include;
import io.github.ibuildthecloud.gdapi.model.Sort;
import io.github.ibuildthecloud.gdapi.server.model.RequestServletContext;
import io.github.ibuildthecloud.model.Pagination;
import io.github.ibuildthecloud.url.UrlBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

public class ApiRequest {

    Locale locale;
    String type;
    String id;
    String link;
    String action;
    String method;
    String clientIp;
    String queryString;
    String requestPath;
    boolean commited = false;
    int responseCode = HttpServletResponse.SC_OK;
    RequestServletContext requestServletContext;
    Object responseObject;
    Object requestObject;
    Object requestBodyObject;
    Map<String,Object> requestParams;
    String requestUrl;
    String requestVersion;
    String apiVersion;
    String responseUrlBase;
    String responseFormat;
    String responseContentType;
    long startTime = System.currentTimeMillis();
    UrlBuilder urlBuilder;
    Map<String,List<Condition>> conditions = new LinkedHashMap<String, List<Condition>>();
    Sort sort;
    Pagination pagination;
    Include include;
    Map<Object,Object> attributes = new HashMap<Object, Object>();

    public ApiRequest(String apiVersion, RequestServletContext requestServletContext) {
        super();
        this.apiVersion = apiVersion;
        this.requestServletContext = requestServletContext;
        this.locale = requestServletContext.getRequest().getLocale();
    }

    public InputStream getInputStream() throws IOException {
        if ( requestServletContext == null ) {
            return null;
        }
        return requestServletContext.getRequest().getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        if ( commited ) {
            throw new IllegalStateException("Response is commited");
        }
        commit();
        commited = true;
        return requestServletContext.getResponse().getOutputStream();
    }

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

    public Map<String, Object> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(Map<String, Object> requestParams) {
        this.requestParams = requestParams;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getResponseUrlBase() {
        return responseUrlBase;
    }

    public void setResponseUrlBase(String responseUrlBase) {
        this.responseUrlBase = responseUrlBase;
    }

    public String getRequestVersion() {
        return requestVersion;
    }

    public void setRequestVersion(String requestVersion) {
        this.requestVersion = requestVersion;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public void setResponseFormat(String responseType) {
        this.responseFormat = responseType;
    }

    public void setResponseContentType(String contentType) {
        this.responseContentType = contentType;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public RequestServletContext getRequestServletContext() {
        return requestServletContext;
    }

    public void setRequestServletContext(RequestServletContext requestServletContext) {
        this.requestServletContext = requestServletContext;
    }

    public long getStartTime() {
        return startTime;
    }

    public Object getRequestBodyObject() {
        return requestBodyObject;
    }

    public void setRequestBodyObject(Object requestBodyObject) {
        this.requestBodyObject = requestBodyObject;
    }

    public void commit() {
        if ( ! commited ) {
            if ( responseContentType != null ) {
                requestServletContext.getResponse().setHeader("Content-Type", responseContentType);
            }
            requestServletContext.getResponse().setStatus(responseCode);
        }
    }

    public UrlBuilder getUrlBuilder() {
        return urlBuilder;
    }

    public void setUrlBuilder(UrlBuilder urlWriter) {
        this.urlBuilder = urlWriter;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Map<String, List<Condition>> getConditions() {
        return conditions;
    }

    public void setConditions(Map<String, List<Condition>> conditions) {
        this.conditions = conditions;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Include getInclude() {
        return include;
    }

    public void setInclude(Include include) {
        this.include = include;
    }

    public Object getAttribute(Object object) {
        return this.attributes.get(object);
    }

    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }
}