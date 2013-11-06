package io.github.ibuildthecloud.gdapi.model;

import java.net.URL;
import java.util.Map;

public interface Resource {

	String getId();
	
	String getType();
	
	Map<String,URL> getLinks();
	
	Map<String,URL> getActions();
	
	Map<String,Object> getAttributes();
	
}
