package io.github.ibuildthecloud.gdapi.condition;

public enum ConditionType {
    EQ,
    NE,
    LT,
    LTE,
    GT,
    GTE,
    PREFIX,
    LIKE,
    NOTLIKE,
    NULL,
    NOTNULL,
    IN(true),
    OR(true);

    private String externalForm;
    private boolean internal = false;

    private ConditionType() {
        this.externalForm = toString().toLowerCase();
    }

    private ConditionType(boolean internal) {
        this();
        this.internal = internal;
    }

    public String getExternalForm() {
        return externalForm;
    }

    public boolean isInternal() {
        return internal;
    }

}
