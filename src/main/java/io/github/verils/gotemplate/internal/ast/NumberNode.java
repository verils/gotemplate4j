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

    public long getIntValue() {
        return intValue;
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

    public void setComplex(Complex complex) {
        this.complexValue = complex;
    }
}
