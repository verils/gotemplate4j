package io.github.verils.gotemplate.internal.ast;

import io.github.verils.gotemplate.internal.lang.Complex;

public class NumberNode implements Node {

    private final String text;
    private Number number;
    private Complex complex;

    public NumberNode(String text) {
        this.text = text;
    }

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
