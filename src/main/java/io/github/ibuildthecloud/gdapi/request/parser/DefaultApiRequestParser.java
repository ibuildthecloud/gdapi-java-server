package io.github.ibuildthecloud.gdapi.request.parser;

import io.github.ibuildthecloud.gdapi.exception.RequestEntityTooLargeException;
import io.github.ibuildthecloud.gdapi.exception.ResourceNotFoundException;
import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;

public class DefaultApiRequestParser implements ApiRequestParser {

    public static final String DEFAULT_OVERRIDE_URL_HEADER = "X-API-request-url";
    public static final String DEFAULT_OVERRIDE_CLIENT_IP_HEADER = "X-API-request-url";
    public static final String FORWARDED_HEADER = "X-Forwarded-For";

    public static final String HTML = "html";
    public static final String JSON = "json";

    ServletFileUpload servletFileUpload;
    int maxUploadSize = 100 * 1024;
    boolean allowClientOverrideHeaders = false;
    String overrideUrlHeader = DEFAULT_OVERRIDE_URL_HEADER;
    String overrideClientIpHeader = DEFAULT_OVERRIDE_CLIENT_IP_HEADER;
    Set<String> allowedFormats;
    SchemaFactory schemaFactory;

    @Override
    public boolean parse(ApiRequest apiRequest) throws IOException {
        HttpServletRequest request = apiRequest.getRequestServletContext().getRequest();

        apiRequest.setMethod(parseMethod(apiRequest, request));
        apiRequest.setAction(parseAction(apiRequest, request));
        apiRequest.setRequestParams(parseParams(apiRequest, request));
        apiRequest.setRequestUrl(parseRequestUrl(apiRequest, request));
        apiRequest.setClientIp(parseClientIp(apiRequest, request));
        apiRequest.setResponseUrlBase(parseResponseUrlBase(apiRequest, request));
        apiRequest.setRequestVersion(parseRequestVersion(apiRequest, request));
        apiRequest.setResponseFormat(parseResponseType(apiRequest, request));
        apiRequest.setQueryString(parseQueryString(apiRequest, request));

        parsePath(apiRequest, request);

        return true;
    }

    protected String parseQueryString(ApiRequest apiRequest, HttpServletRequest request) {
        return request.getQueryString();
    }

    protected String parseMethod(ApiRequest apiRequest, HttpServletRequest request) {
        String method = request.getParameter("_method");

        if ( method == null )
            method = request.getMethod();

        return method;
    }

    protected String parseAction(ApiRequest apiRequest, HttpServletRequest request) {
        if ( "POST".equals(apiRequest.getMethod()) ) {
            return request.getParameter("action");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected Map<String,Object> parseParams(ApiRequest apiRequest, HttpServletRequest request) throws IOException {
        try {
            Map<String,Object> multiPart = parseMultipart(request);

            return multiPart == null ? request.getParameterMap() : multiPart;
        } catch ( IOException e ) {
            if ( e.getCause() instanceof FileUploadBase.SizeLimitExceededException )
                throw new RequestEntityTooLargeException();
            throw e;
        }
    }

    protected Map<String, Object> parseMultipart(HttpServletRequest request) throws IOException {
        if ( ! ServletFileUpload.isMultipartContent(request) )
            return null;

        Map<String,List<String>> params = new HashMap<String, List<String>>();

        try {
            List<FileItem> items = servletFileUpload.parseRequest(request);

            for ( FileItem item : items ) {
                if ( item.isFormField() ) {
                    List<String> values = params.get(item.getFieldName());
                    if ( values == null ) {
                        values = new ArrayList<String>();
                        params.put(item.getFieldName(), values);
                    }
                    values.add(item.getString());
                }
            }

            Map<String,Object> result = new HashMap<String, Object>();

            for ( Map.Entry<String, List<String>> entry : params.entrySet() ) {
                List<String> values = entry.getValue();
                result.put(entry.getKey(), values.toArray(new String[values.size()]));
            }

            return result;
        } catch (FileUploadException e) {
            throw new IOException(e);
        }
    }

    protected String getOverrideHeader(HttpServletRequest request, String header, String defaultValue) {
        if ( ! allowClientOverrideHeaders ) {
            return defaultValue;
        }
        String value = request.getHeader(header);
        return value == null ? defaultValue : value;
    }

    protected String parseClientIp(ApiRequest apiRequest, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        clientIp = getOverrideHeader(request, overrideClientIpHeader, clientIp);
        clientIp = getOverrideHeader(request, FORWARDED_HEADER, clientIp);

        return clientIp;
    }

    protected String parseRequestUrl(ApiRequest apiRequest, HttpServletRequest request) {
        String requestUrl = null;
        try {
            requestUrl = URLDecoder.decode(request.getRequestURL().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }

        requestUrl = getOverrideHeader(request, overrideUrlHeader, requestUrl);

        return requestUrl;
    }

    protected String parseResponseUrlBase(ApiRequest apiRequest, HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String requestUrl = apiRequest.getRequestUrl();

        int index = requestUrl.lastIndexOf(servletPath);
        if ( index == -1 ) {
            try {
                /* Fallback, if we can't find servletPath in requestUrl, then
                 * we just assume the base is the root of the web request
                 */
                URL url = new URL(requestUrl);
                StringBuilder buffer = new StringBuilder(url.getProtocol())
                    .append("://")
                    .append(url.getHost());

                if ( url.getPort() != -1 ) {
                    buffer.append(":").append(url.getPort());
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                throw new ResourceNotFoundException();
            }
        } else {
            return requestUrl.substring(0, index);
        }
    }

    protected String parseRequestVersion(ApiRequest apiRequest, HttpServletRequest request) {
        String servletPath = request.getServletPath();
        servletPath = servletPath.replaceAll("//+", "/");

        if ( ! servletPath.startsWith("/") || servletPath.length() < 2 )
            return null;

        return servletPath.split("/")[1];
    }

    protected String parseResponseType(ApiRequest apiRequest, HttpServletRequest request) {
        String format = request.getParameter("_format");

        if ( format != null ) {
            format = format.toLowerCase().trim();
        }

        /* Format specified */
        if ( format != null && allowedFormats.contains(format)) {
            return format;
        }

        String accepts = request.getHeader("Accept");
        String userAgent = request.getHeader("User-Agent");

        if ( accepts == null ) {
            accepts = "*/*";
        }

        accepts = accepts.toLowerCase();

        // User agent has Mozilla and browser accepts */*
        if ( userAgent != null && userAgent.toLowerCase().indexOf("mozilla") != -1 &&
                accepts.indexOf("*/*") != -1 ) {
            return HTML;
        }

        return JSON;
    }

    protected void parsePath(ApiRequest apiRequest, HttpServletRequest request) {
        if ( apiRequest.getRequestVersion() == null )
            return;

        String servletPath = request.getServletPath();
        servletPath = servletPath.replaceAll("//+", "/");

        String versionPrefix = "/" + apiRequest.getRequestVersion();
        if ( ! servletPath.startsWith(versionPrefix) ) {
            return;
        }

        String[] parts = servletPath.substring(versionPrefix.length()).split("/");

        if ( parts.length > 4 )
            return;

        String typeName = indexValue(parts, 1);
        String id = indexValue(parts, 2);
        String link = indexValue(parts, 3);

        if ( StringUtils.isBlank(typeName) ) {
            return;
        } else {
            apiRequest.setType(schemaFactory.getSingularName(typeName));
        }

        if ( StringUtils.isBlank(id) ) {
            return;
        } else {
            apiRequest.setId(id);
        }

        if ( StringUtils.isBlank(link) ) {
            return;
        } else {
            apiRequest.setLink(link);
        }
    }

    protected String indexValue(String[] array, int index) {
        if ( array.length <= index ) {
            return null;
        }
        String value = array[index];
        return value == null ? value : value.trim();
    }

    @PostConstruct
    public void init() {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(maxUploadSize * 2);

        servletFileUpload = new ServletFileUpload(factory);
        servletFileUpload.setFileSizeMax(maxUploadSize);
        servletFileUpload.setSizeMax(maxUploadSize);

        if ( allowedFormats == null ) {
            allowedFormats = new HashSet<String>();
            allowedFormats.add(HTML);
            allowedFormats.add(JSON);
        }
    }

    public Set<String> getAllowedFormats() {
        return allowedFormats;
    }

    public void setAllowedFormats(Set<String> allowedFormats) {
        this.allowedFormats = allowedFormats;
    }

    public SchemaFactory getSchemaFactory() {
        return schemaFactory;
    }

    @Inject
    public void setSchemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
    }

}
