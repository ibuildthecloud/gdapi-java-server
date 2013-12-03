package io.github.ibuildthecloud.gdapi.condition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Condition {

    List<Object> values;
    ConditionType conditionType;
    Condition left, right;

    public Condition(Condition left, Condition right) {
        this(ConditionType.OR);
        this.left = left;
        this.right = right;
    }

    public Condition(ConditionType conditionType, List<Object> values) {
        super();
        this.values = values;
        this.conditionType = conditionType;
    }

    public Condition(ConditionType conditionType, Object value) {
        super();
        this.values = Arrays.asList(value);
        this.conditionType = conditionType;
    }

    public Condition(ConditionType conditionType) {
        super();
        this.values = Collections.emptyList();
        this.conditionType = conditionType;
    }

    public List<Object> getValues() {
        return values;
    }

    public Object getValue() {
        return values.size() == 0 ? null : values.get(0);
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public Condition getLeft() {
        return left;
    }

    public Condition getRight() {
        return right;
    }

}
