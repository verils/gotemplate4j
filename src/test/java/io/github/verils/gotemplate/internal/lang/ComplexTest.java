package io.github.verils.gotemplate.internal.lang;

import org.junit.jupiter.api.Test;

class ComplexTest {


    @Test
    void testParseNumber() {

        class NumberTest {
            private final String text;

            private final boolean isInt;
            private final boolean isFloat;
            private final boolean isComplex;

            NumberTest(String text, boolean isInt, boolean isFloat, boolean isComplex) {
                this.text = text;
                this.isInt = isInt;
                this.isFloat = isFloat;
                this.isComplex = isComplex;
            }
        }


        NumberTest[] tests = new NumberTest[]{
                new NumberTest("0", true, false, false),
                new NumberTest("-0", true, false, false),
                new NumberTest("73", true, false, false),
                new NumberTest("7_3", true, false, false),
                new NumberTest("0b10_010_01", true, false, false),
                new NumberTest("0B10_010_01", true, false, false),
                new NumberTest("073", true, false, false),
                new NumberTest("0o73", true, false, false),
                new NumberTest("0O73", true, false, false),
                new NumberTest("0x73", true, false, false),
                new NumberTest("0X73", true, false, false),
                new NumberTest("0x7_3", true, false, false),
                new NumberTest("-73", true, false, false),
                new NumberTest("+73", true, false, false),
                new NumberTest("100", true, false, false),
                new NumberTest("1e9", true, false, false),
                new NumberTest("-1e9", true, false, false),
                new NumberTest("-1.2", true, false, false),
                new NumberTest("1e19", true, false, false),
                new NumberTest("1e1_9", true, false, false),
                new NumberTest("1E19", true, false, false),
                new NumberTest("-1e19", true, false, false),
                new NumberTest("0x_1p4", true, false, false),
                new NumberTest("0X_1P4", true, false, false),
                new NumberTest("0x_1p-4", true, false, false),
                new NumberTest("4i", true, false, false),
                new NumberTest("-1.2+4.2i", true, false, false),
                new NumberTest("073i", true, false, false),
                new NumberTest("0i", true, false, false),
                new NumberTest("-1.2+0i", true, false, false),
                new NumberTest("-12+0i", true, false, false),
                new NumberTest("13+0i", true, false, false),
                new NumberTest("0123", true, false, false),
                new NumberTest("-0x0", true, false, false),
                new NumberTest("0xdeadbeef", true, false, false),
                new NumberTest("'a'", true, false, false),
                new NumberTest("'\\n'", true, false, false),
                new NumberTest("'\\\\'", true, false, false),
                new NumberTest("'\\''", true, false, false),
                new NumberTest("'\\xFF'", true, false, false),
                new NumberTest("'パ'", true, false, false),
                new NumberTest("'\\u30d1'", true, false, false),
                new NumberTest("'\\U000030d1'", true, false, false),
                new NumberTest("+-2", true, false, false),
                new NumberTest("0x123.", true, false, false),
                new NumberTest("1e.", true, false, false),
                new NumberTest("0xi.", true, false, false),
                new NumberTest("1+2.", true, false, false),
                new NumberTest("'x", true, false, false),
                new NumberTest("'xx'", true, false, false),
                new NumberTest("'433937734937734969526500969526500'", true, false, false),
                new NumberTest("0xef3", true, false, false),
        };

        for (NumberTest test : tests) {
            Complex
        }
    }

}