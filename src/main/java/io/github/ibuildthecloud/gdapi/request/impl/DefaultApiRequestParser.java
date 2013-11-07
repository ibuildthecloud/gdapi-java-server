package io.github.ibuildthecloud.gdapi.request.impl;

import io.github.ibuildthecloud.gdapi.request.ApiRequestParser;
import io.github.ibuildthecloud.gdapi.server.model.ApiRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class DefaultApiRequestParser implements ApiRequestParser {

    ServletFileUpload servletFileUpload;
    int maxUploadSize = 100 * 1024;

    @Override
    public boolean parse(ApiRequest apiRequest) {
        HttpServletRequest request = apiRequest.getServletContext().getRequest();
        apiRequest.setMethod(parseMethod(apiRequest, request));
        apiRequest.setAction(parseAction(apiRequest, request));

        return true;
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
    
    protected void parsePath(ApiRequest apiRequest, HttpServletRequest request) {
        
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String,Object> getParams(final ApiRequest apiRequest, final HttpServletRequest request) throws IOException {
//        try {
            Map<String,Object> multiPart = parseMultipart(request);

            return multiPart == null ? request.getParameterMap() : multiPart; 
//        } catch ( IOException e ) {
//            if ( e.getCause() instanceof FileUploadBase.SizeLimitExceededException )
//                throw new RequestEntityTooLargeException();
//            return new HashMap<String, Object>();
//        }
    }

    protected Map<String, Object> parseMultipart(HttpServletRequest request) throws IOException {
        if ( ! ServletFileUpload.isMultipartContent(request) )
            return null;

        Map<String,List<String>> params = new HashMap<String, List<String>>();

        try {
            @SuppressWarnings("unchecked")
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

    @PostConstruct
    public void init() {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        factory.setSizeThreshold(maxUploadSize * 2);

        servletFileUpload = new ServletFileUpload(factory);
        servletFileUpload.setFileSizeMax(maxUploadSize);
        servletFileUpload.setSizeMax(maxUploadSize);
    }

}
