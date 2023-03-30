package io.github.verils.gotemplate.internal.lang;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComplexTest {

    @Test
    void parsePureImaginary() {
        Complex complex = Complex.parseComplex("4i");
        assertEquals(0, complex.getReal());
        assertEquals(4, complex.getImaginary());
    }

    @Test
    void parsePureZeroImaginary() {
        Complex complex = Complex.parseComplex("0i");
        assertEquals(0, complex.getReal());
        assertEquals(0, complex.getImaginary());
    }

    @Test
    void parseNormal() {
        Complex complex = Complex.parseComplex("-1.2+4.2i");
        assertEquals(-1.2, complex.getReal());
        assertEquals(4.2, complex.getImaginary());
    }

}