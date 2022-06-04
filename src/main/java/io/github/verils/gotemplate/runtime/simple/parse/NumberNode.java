package io.github.verils.gotemplate.runtime.simple.parse;

import io.github.verils.gotemplate.java.Complex;

public class NumberNode implements Node {

    private final String text;
    private final Number number;
    private final Complex complex;

    public NumberNode(String text, Number number) {
        this.text = text;
        this.number = number;
        this.complex = null;
    }

    public NumberNode(String text, Complex complex) {
        this.text = text;
        this.number = null;
        this.complex = complex;
    }

    public boolean isComplex() {
        return complex != null;
    }

    @Override
    public String toString() {
        return text;
    }
}
