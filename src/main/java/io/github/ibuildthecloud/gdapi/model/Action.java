package io.github.ibuildthecloud.gdapi.model;

public class Action {

    String input, output;

    public Action() {
    }

    public Action(Action other) {
        this(other.getInput(), other.getOutput());
    }

    public Action(String input, String output) {
        super();
        this.input = input;
        this.output = output;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

}
