package io.github.verils.gotemplate.internal.ast;

import io.github.verils.gotemplate.internal.lang.Complex;

public class NumberNode implements Node {

    private final String text;

    private boolean isInt;
    private long intValue;

    private boolean isFloat;
    private double floatValue;

    private boolean isComplex;
    private Complex complexValue;

    public NumberNode(String text) {
        this.text = text;
    }

    public NumberNode(String text, Number number) {
        this.text = text;
        this.complexValue = null;
    }

    public NumberNode(String text, Complex complex) {
        this.text = text;
        this.complexValue = complex;
    }

    public boolean isComplex() {
        return isComplex;
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean isInt() {
        return isInt;
    }

    public void setIsInt(boolean isInt) {
        this.isInt = isInt;
    }

    public void setIntValue(long intValue) {
        this.intValue = intValue;
    }

    public boolean isFloat() {
        return isFloat;
    }

    public void setIsFloat(boolean isFloat) {
        this.isFloat = isFloat;
    }

    public void setFloatValue(double floatValue) {
        this.floatValue = floatValue;
    }

    public void setIsComplex(boolean isComplex) {
        this.isComplex = isComplex;
    }

    public void setNumber(Number number) {
    }

    public void setComplex(Complex complex) {
        this.complexValue = complex;
    }
}
