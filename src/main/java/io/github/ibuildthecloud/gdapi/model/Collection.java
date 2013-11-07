package io.github.ibuildthecloud.gdapi.model;

import java.net.URL;
import java.util.List;
import java.util.Map;

public interface Collection {

    String getType();
    
    String getResourceType();
    
    Map<String,URL> getLinks();
    
    List<Resource> getData();
    
    Map<String,URL> getCreateTypes();
    
    Map<String,URL> getActions();
    
    Pagination getPagination();
    
    Sort getSort();

}
