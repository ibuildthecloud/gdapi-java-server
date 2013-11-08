package io.github.ibuildthecloud.gdapi.json;

import java.io.IOException;
import java.io.OutputStream;

public interface JsonMapper {

    Object readValue(byte[] content) throws IOException;

    void writeValue(OutputStream os, Object object) throws IOException;

}
