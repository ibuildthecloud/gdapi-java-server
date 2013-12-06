package io.github.ibuildthecloud.gdapi.model;

import java.util.List;

public class Filter {

    List<String> modifiers;

    public Filter(List<String> modifiers) {
        super();
        this.modifiers = modifiers;
    }

    public List<String> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<String> modifiers) {
        this.modifiers = modifiers;
    }
}
